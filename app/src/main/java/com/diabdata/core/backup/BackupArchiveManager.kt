package com.diabdata.core.backup

import android.app.Application
import android.os.Build
import android.util.Log
import com.diabdata.BuildConfig
import com.diabdata.core.backup.encryption.BackupEncryptionKeyManager
import com.diabdata.core.backup.worker.BackupWorker
import com.diabdata.core.database.DataRepository
import com.diabdata.core.model.BackupMetadata
import com.diabdata.core.model.DataSummary
import com.diabdata.core.model.UserDetails
import com.diabdata.core.model.UserPreferences
import com.diabdata.core.utils.data.GsonFactory
import com.diabdata.shared.utils.dataTypes.BackupFrequency
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import net.lingala.zip4j.exception.ZipException
import net.lingala.zip4j.io.inputstream.ZipInputStream
import net.lingala.zip4j.io.outputstream.ZipOutputStream
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock

private const val PROFILE_PHOTO_ENTRY = "profile_photo.jpg"

/**
 * Handles reading and writing backup archives for the app's user data.
 *
 * Supports three archive formats, resolved transparently on read via [detectBackupFormat]:
 * - The current format: a ZIP containing a plain-text `metadata.json` entry alongside one entry
 *   per data type, optionally AES-encrypted (see [zipImport]).
 * - A legacy ZIP format containing a single `data.json` entry and an optional profile photo
 *   (see [legacyZipImport]).
 * - A legacy raw JSON format, predating any ZIP-based backup (see [legacyJsonImport]).
 */
class BackupArchiveManager @Inject constructor(
    private val repository: DataRepository,
    private val application: Application,
    private val backupEncryptionKeyManager: BackupEncryptionKeyManager,
    private val backupScheduleCoordinator: BackupScheduleCoordinator,
) {
    private val backupManagerTag = "BackupArchiveManager"

    private val gson = GsonFactory.create(prettyPrint = true)

    /** Serializes [list] to a JSON array string. */
    inline fun <reified T> Gson.toJsonList(list: List<T>): String = this.toJson(list)

    /** Deserializes [json], a JSON array string, into a `List<T>`. */
    inline fun <reified T> Gson.fromJsonList(json: String): List<T> =
        this.fromJson(json, TypeToken.getParameterized(List::class.java, T::class.java).type)

    /** Result of [detectBackupFormat], identifying which archive format a backup byte array uses. */
    sealed class BackupFormat {
        /** The current backup format, identified by the presence of a parsed [metadata]. */
        data class NewFormat(val metadata: BackupMetadata) : BackupFormat()

        /** A legacy ZIP archive containing a single `data.json` entry, never encrypted. */
        object LegacyZip : BackupFormat()

        /** A legacy backup consisting of raw JSON content, with no ZIP wrapping. */
        object LegacyRawJson : BackupFormat()

        /** The byte array could not be recognized as any supported backup format. */
        object Invalid : BackupFormat()
    }

    /**
     * Identifies which [BackupFormat] [byteArray] represents, without decrypting or extracting any
     * content besides the unencrypted `metadata.json` entry when present.
     *
     * This is a read-only inspection pass: for ZIP archives, only entry names are collected and only
     * `metadata.json` (never encrypted) is actually read, so this function never needs a password.
     *
     * @param byteArray The full backup content, buffered in memory.
     * @return The detected [BackupFormat].
     */
    private fun detectBackupFormat(byteArray: ByteArray): BackupFormat {
        val isZip = byteArray.size >= 2 && byteArray[0] == 0x50.toByte() && byteArray[1] == 0x4B.toByte()
        if (!isZip) {
            return if (byteArray.isNotEmpty()) BackupFormat.LegacyRawJson else BackupFormat.Invalid
        }

        var metadata: BackupMetadata?
        val entryNames = mutableListOf<String>()

        ZipInputStream(ByteArrayInputStream(byteArray)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                entryNames += entry.fileName

                if (entry.fileName == "metadata.json") {
                    metadata = gson.fromJson(zip.readBytes().toString(Charsets.UTF_8), BackupMetadata::class.java)
                    return BackupFormat.NewFormat(metadata)
                }

                entry = zip.nextEntry
            }
        }

        return when {
            "data.json" in entryNames -> BackupFormat.LegacyZip
            else -> BackupFormat.Invalid
        }
    }

    /**
     * Attempts to silently resolve the password for an encrypted [BackupFormat.NewFormat] archive,
     * using the key stored in [backupEncryptionKeyManager]. Validates the candidate password by
     * actually decrypting a single non-`metadata.json` entry from [input].
     *
     * Only covers the silent Keystore attempt; resolving a password entered manually by the user is
     * handled separately by [zipImport] itself.
     *
     * @param input The full backup archive content, buffered in memory.
     * @return The validated password on success, or `null` if no key is stored, or if it fails to
     * decrypt this archive.
     */
    private suspend fun resolvePassword(input: ByteArray): CharArray? {
        val storedPassword = backupEncryptionKeyManager.getPassword().getOrNull()?.toCharArray()
            ?: return null

        return try {
            ZipInputStream(ByteArrayInputStream(input), storedPassword).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (entry.fileName != "metadata.json") {
                        zis.readBytes()
                        return storedPassword
                    }
                    entry = zis.nextEntry
                }
                null
            }
        } catch (e: ZipException) {
            if (e.type == ZipException.Type.WRONG_PASSWORD) null else throw e
        }
    }

    /**
     * Builds a backup archive of all user data and writes it to [output] in the current ZIP format
     * (see [BackupFormat.NewFormat]).
     *
     * Also used by [BackupWorker] to produce scheduled backups.
     *
     * @param output The [OutputStream] the archive is written to.
     * @param isScheduledBackup Whether this backup was triggered by the automatic backup schedule,
     * as opposed to a manual export by the user. Recorded in the archive's metadata only.
     * @param isEncrypted Whether every entry except `metadata.json` should be AES-encrypted using the
     * key managed by [backupEncryptionKeyManager].
     * @return [Result.success] on success, [Result.failure] with the underlying exception otherwise.
     */
    suspend fun writeBackup(
        output: OutputStream, isScheduledBackup: Boolean = false, isEncrypted: Boolean = false
    ): Result<Unit> {
        var encryptionKey: CharArray? = null

        return try {
            withContext(Dispatchers.IO) {

                if (isEncrypted) {
                    encryptionKey = backupEncryptionKeyManager.getPassword().let {
                        if (it.isSuccess) {
                            it.getOrNull()?.toCharArray()
                                ?: throw BackupExportException.EncryptionKeyUnavailable()
                        } else {
                            throw BackupExportException.EncryptionKeyUnavailable(cause = it.exceptionOrNull())
                        }
                    }
                }

                val createdAt = Clock.System.now().toString()

                val weights = repository.getAllWeights().first()
                val weightCount = weights.size
                val hba1cs = repository.getAllHba1c().first()
                val hba1cCount = hba1cs.size
                val appointments = repository.getAllAppointments().first()
                val appointmentsCount = appointments.size
                val treatments = repository.getAllTreatments().first()
                val treatmentsCount = treatments.size
                val importantDates = repository.getAllImportantDates().first()
                val importantDatesCount = importantDates.size
                val medicalDevices = repository.getAllDevices().first()
                val medicalDevicesCount = medicalDevices.size
                val userDetails = repository.getUserDetails().first()
                val userPreferences = repository.getUserPreferences().first()

                val totalEntriesCount =
                    weightCount + hba1cCount + appointmentsCount + treatmentsCount + importantDatesCount + medicalDevicesCount

                val backupMetadata = BackupMetadata(
                    app = BuildConfig.APPLICATION_ID,
                    appVersion = BuildConfig.VERSION_NAME,
                    formatVersion = 2,
                    createdAt = createdAt,
                    isScheduledBackup = isScheduledBackup,
                    isEncrypted = isEncrypted,
                    deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
                    dataSummary = DataSummary(
                        totalEntriesCount = totalEntriesCount,
                        weightEntriesCount = weightCount,
                        hba1cEntriesCount = hba1cCount,
                        appointmentEntriesCount = appointmentsCount,
                        treatmentEntriesCount = treatmentsCount,
                        importantDateEntriesCount = importantDatesCount,
                        medicalDeviceEntriesCount = medicalDevicesCount,
                        hasUserProfile = userDetails != null,
                        hasUserPreferences = userPreferences != null,
                        hasProfilePhoto = userDetails?.profilePhotoPath != null,
                    )
                )

                val plainFiles: List<Triple<String, ByteArray, Boolean>> = buildList {
                    add(Triple("metadata.json", gson.toJson(backupMetadata).toByteArray(), false))
                }

                val photoToEncrypt: Triple<String, ByteArray, Boolean>? = userDetails?.profilePhotoPath?.let { path ->
                    val photoFile = File(path)
                    if (photoFile.exists()) {
                        Triple(PROFILE_PHOTO_ENTRY, photoFile.readBytes(), isEncrypted)
                    } else {
                        null
                    }
                }

                val filesToEncrypt: List<Triple<String, ByteArray, Boolean>> = buildList {
                    add(Triple("weights.json", gson.toJsonList(weights).toByteArray(), isEncrypted))
                    add(Triple("hba1c.json", gson.toJsonList(hba1cs).toByteArray(), isEncrypted))
                    add(Triple("appointments.json", gson.toJsonList(appointments).toByteArray(), isEncrypted))
                    add(Triple("treatments.json", gson.toJsonList(treatments).toByteArray(), isEncrypted))
                    add(Triple("important_dates.json", gson.toJsonList(importantDates).toByteArray(), isEncrypted))
                    add(Triple("medical_devices.json", gson.toJsonList(medicalDevices).toByteArray(), isEncrypted))
                    userDetails?.let {
                        add(Triple("user_profile.json", gson.toJson(it).toByteArray(), isEncrypted))
                    }
                    userPreferences?.let {
                        add(Triple("user_preferences.json", gson.toJson(it).toByteArray(), isEncrypted))
                    }
                }

                val fileList: List<Triple<String, ByteArray, Boolean>> = buildList {
                    addAll(plainFiles)
                    photoToEncrypt?.let { add(it) }
                    addAll(filesToEncrypt)
                }

                ZipOutputStream(output, encryptionKey).use { zip ->
                    fileList.forEach {
                        zip.putNextEntry(ZipParameters().apply {
                            fileNameInZip = it.first
                            isEncryptFiles = it.third
                            if (it.third) {
                                encryptionMethod = EncryptionMethod.AES
                                aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256
                            }
                        })
                        zip.write(it.second)
                        zip.closeEntry()
                    }
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(
                backupManagerTag, "writeBackup Exception: ${e.message}", e
            )
            Result.failure(exception = e)
        }
    }

    /**
     * Reads a backup archive from [input] and imports its content into the database, transparently
     * handling all supported [BackupFormat]s (see [detectBackupFormat]).
     *
     * @param input The [InputStream] to read the backup from, fully buffered before processing.
     * @param password The password to decrypt the archive with, if it was encrypted. Ignored for
     * unencrypted or legacy backups. Defaults to `null`.
     * @return [Result.success] on success, [Result.failure] with the underlying exception
     * otherwise. See [BackupImportException] for the specific business errors that can occur.
     */
    suspend fun readBackup(input: InputStream, password: CharArray? = null): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val bytes = input.readBytes()

            when (val backupType = detectBackupFormat(bytes)) {
                is BackupFormat.LegacyRawJson -> legacyJsonImport(String(bytes, Charsets.UTF_8))
                is BackupFormat.LegacyZip -> legacyZipImport(bytes)
                is BackupFormat.NewFormat -> {
                    if (!backupType.metadata.isEncrypted || password != null) {
                        zipImport(bytes, password)
                    } else {
                        val resolvedPassword = resolvePassword(bytes)
                        if (resolvedPassword != null) {
                            zipImport(bytes, resolvedPassword)
                        } else {
                            Result.failure(BackupImportException.PasswordRequired())
                        }
                    }
                }
                is BackupFormat.Invalid -> Result.failure(BackupImportException.InvalidFormat())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(backupManagerTag, "readBackup Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Imports a [BackupFormat.LegacyRawJson] backup: [jsonContent] is the entire backup content,
     * predating any ZIP wrapping.
     *
     * @param jsonContent The raw JSON backup content.
     * @return [Result.success] on success, [Result.failure] with the underlying exception otherwise.
     */
    private suspend fun legacyJsonImport(jsonContent: String): Result<Unit> {
        return try {
            if (jsonContent.isEmpty()) return Result.failure(BackupImportException.EmptyBackup())

            repository.importDataFromJsonString(jsonContent)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(backupManagerTag, "legacyJsonImport Exception: ${e.message}", e)
            Result.failure(exception = e)
        }
    }

    /**
     * Imports a [BackupFormat.LegacyZip] backup: a ZIP archive containing a single `data.json` entry
     * and an optional `profile_photo.jpg` entry, never encrypted.
     *
     * @param input The full ZIP archive content, buffered in memory.
     * @return [Result.success] on success, [Result.failure] with the underlying exception otherwise.
     */
    private suspend fun legacyZipImport(input: ByteArray): Result<Unit> {
        return try {
            val entries = mutableMapOf<String, ByteArray>()
            ZipInputStream(ByteArrayInputStream(input)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    entries[entry.fileName] = zis.readBytes()
                    entry = zis.nextEntry
                }
            }

            val jsonContent = entries["data.json"]!!.toString(Charsets.UTF_8)
            val photoBytes: ByteArray? = entries[PROFILE_PHOTO_ENTRY]

            repository.importDataFromJsonString(jsonContent)

            photoBytes?.let { repository.saveProfilePhotoBytes(it, application.filesDir) }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(backupManagerTag, "legacyZipImport Exception: ${e.message}", e)
            Result.failure(exception = e)
        }
    }

    /**
     * Maps each entry name of a [BackupFormat.NewFormat] archive to the handler responsible for
     * deserializing its content and importing it via [repository]. Consumed by [zipImport].
     *
     * Adding a new data type to the backup format only requires adding an entry here, no change to
     * [zipImport] itself.
     */
    private val importHandlers: Map<String, suspend (ByteArray) -> Unit> = mapOf(
        "weights.json" to { bytes -> repository.importWeights(gson.fromJsonList(bytes.toString(Charsets.UTF_8))) },
        "hba1c.json" to { bytes -> repository.importHba1c(gson.fromJsonList(bytes.toString(Charsets.UTF_8))) },
        "appointments.json" to { bytes -> repository.importAppointments(gson.fromJsonList(bytes.toString(Charsets.UTF_8))) },
        "treatments.json" to { bytes -> repository.importTreatments(gson.fromJsonList(bytes.toString(Charsets.UTF_8))) },
        "important_dates.json" to { bytes -> repository.importImportantDates(gson.fromJsonList(bytes.toString(Charsets.UTF_8))) },
        "medical_devices.json" to { bytes -> repository.importMedicalDevices(gson.fromJsonList(bytes.toString(Charsets.UTF_8))) },
        "user_preferences.json" to { bytes -> repository.importUserPreferences(gson.fromJson(bytes.toString(Charsets.UTF_8), UserPreferences::class.java)) },
        "user_profile.json" to { bytes -> repository.importUserDetails(gson.fromJson(bytes.toString(Charsets.UTF_8), UserDetails::class.java)) },
        PROFILE_PHOTO_ENTRY to { bytes -> repository.saveProfilePhotoBytes(bytes, application.filesDir) }
    )

    /**
     * Imports a [BackupFormat.NewFormat] archive: extracts every entry (decrypting them with
     * [password] if needed, per [importHandlers]), then resynchronizes the automatic backup
     * schedule against the imported preferences via [backupScheduleCoordinator].
     *
     * @param input The full ZIP archive content, buffered in memory.
     * @param password The password to decrypt entries with, if the archive is encrypted. Defaults to
     * `null` for unencrypted archives.
     * @return [Result.success] on success, [Result.failure] with the underlying exception otherwise.
     * See [BackupImportException] for the specific business errors that can occur.
     */
    private suspend fun zipImport(input: ByteArray, password: CharArray? = null): Result<Unit> {
        return try {
            val entries = mutableMapOf<String, ByteArray>()
            ZipInputStream(ByteArrayInputStream(input), password).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    entries[entry.fileName] = zis.readBytes()
                    entry = zis.nextEntry
                }
            }

            repository.inTransaction {
                importHandlers.forEach { (fileName, handler) ->
                    if (fileName != PROFILE_PHOTO_ENTRY) {
                        entries[fileName]?.let { handler(it) }
                    }
                }
            }

            entries[PROFILE_PHOTO_ENTRY]?.let { importHandlers[PROFILE_PHOTO_ENTRY]?.invoke(it) }

            val importedPrefs = repository.getUserPreferences().first()

            importedPrefs?.let {
                val scheduleResult = backupScheduleCoordinator.applyBackupSchedule(
                    it.automaticBackupEnabled, BackupFrequency.fromKey(it.frequency), it.backupPath
                )
                scheduleResult.onSuccess { didSchedule ->
                    if (it.automaticBackupEnabled && !didSchedule) {
                        repository.setAutoBackupEnabled(false)
                    }
                }
            }

            Result.success(Unit)
        } catch (e: ZipException) {
            if (e.type == ZipException.Type.WRONG_PASSWORD) return Result.failure(
                BackupImportException.WrongPassword()
            )
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(backupManagerTag, "zipImport Exception: ${e.message}", e)
            Result.failure(exception = e)
        }
    }
}