package com.diabdata.core.database

import androidx.room.withTransaction
import com.diabdata.core.backup.ExportData
import com.diabdata.core.model.Appointment
import com.diabdata.core.model.Hba1c
import com.diabdata.core.model.ImportantDate
import com.diabdata.core.model.MedicalDevice
import com.diabdata.core.model.MedicalDeviceInfoEntity
import com.diabdata.core.model.Medication
import com.diabdata.core.model.Treatment
import com.diabdata.core.model.UserDetails
import com.diabdata.core.model.UserPreferences
import com.diabdata.core.model.Weight
import com.diabdata.core.model.newEntryUuid
import com.diabdata.core.model.nowMillisInLong
import com.diabdata.core.utils.data.GsonFactory
import com.diabdata.feature.appointments.data.AppointmentDao
import com.diabdata.feature.dataMatrixScanner.data.MedicationDao
import com.diabdata.feature.devices.classes.FaultyBatchCount
import com.diabdata.feature.devices.data.MedicalDeviceDao
import com.diabdata.feature.devices.data.MedicalDevicesInfoDao
import com.diabdata.feature.graphs.classes.PlotPoint
import com.diabdata.feature.hba1c.data.HBA1CDao
import com.diabdata.feature.importantDates.data.ImportantDateDao
import com.diabdata.feature.settings.data.UserPreferencesDao
import com.diabdata.feature.treatments.data.TreatmentDao
import com.diabdata.feature.userProfile.data.UserDetailsDao
import com.diabdata.feature.weight.data.WeightDao
import com.diabdata.shared.utils.dataTypes.MedicalDeviceInfoType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime

class DataRepository(
    private val weightDao: WeightDao,
    private val hba1cDao: HBA1CDao,
    private val appointmentDao: AppointmentDao,
    private val treatmentDao: TreatmentDao,
    private val importantDateDao: ImportantDateDao,
    private val medicationDao: MedicationDao,
    private val medicalDevicesDao: MedicalDeviceDao,
    private val medicalDeviceInfo: MedicalDevicesInfoDao,
    private val userDetailsDao: UserDetailsDao,
    private val userPreferencesDao: UserPreferencesDao,
    val database: DiabDataDatabase,
) {
    // ----------------
    // Weight
    // ----------------
    /** Insert a new weight entry */
    suspend fun insertWeight(weight: Weight) = weightDao.insert(weight)

    /** Update weight entry */
    suspend fun updateWeight(weight: Weight) = weightDao.update(weight)

    /** Flow of all weight entries, sorted by date */
    fun getAllWeights(): Flow<List<Weight>> = weightDao.getAllWeightsFlow()

    /** Flow of weight points to plot in graph between min and max date */
    fun getWeightPlotData(minDate: LocalDate, maxDate: LocalDate): Flow<List<PlotPoint>> =
        weightDao.getWeightPlotData(minDate, maxDate)

    /** Flow of weight entries from the last year */
    fun getRecentWeights(): Flow<List<Weight>> {
        val oneYearAgo = LocalDate.now().minusYears(1)
        return weightDao.getWeightsSince(oneYearAgo)
    }

    /** Delete a weight record by Id**/
    suspend fun deleteWeight(id: Int) = weightDao.deleteById(id)

    // ----------------
    // HBA1C
    // ----------------
    /** Insert a new HBA1C entry */
    suspend fun insertHba1c(hba1C: Hba1c) = hba1cDao.insert(hba1C)

    /** Update HBA1C */
    suspend fun updateHBA1C(hba1C: Hba1c) = hba1cDao.update(hba1C)

    /** Flow of all HBA1C entries */
    fun getAllHba1c(): Flow<List<Hba1c>> = hba1cDao.getAllHBA1CFlow()

    /** Flow of weight points to plot in graph between min and max date */
    fun getHba1cPlotData(minDate: LocalDate, maxDate: LocalDate): Flow<List<PlotPoint>> =
        hba1cDao.getHBA1CPlotData(minDate, maxDate)

    /** Flow of HBA1C entries from the last year */
    fun getRecentHba1c(): Flow<List<Hba1c>> {
        val oneYearAgo = LocalDate.now().minusYears(1)
        return hba1cDao.getHBA1CEntriesSince(oneYearAgo)
    }

    /** Delete an HBA1C record by Id**/
    suspend fun deleteHba1c(id: Int) = hba1cDao.deleteById(id)

    // ----------------
    // Appointment
    // ----------------
    /** Insert a new appointment */
    suspend fun insertAppointment(appointment: Appointment) = appointmentDao.insert(appointment)

    /** Update appointment */
    suspend fun updateAppointment(appointment: Appointment) = appointmentDao.update(appointment)

    /** Flow of all appointments */
    fun getAllAppointments(): Flow<List<Appointment>> = appointmentDao.getAllAppointmentsFlow()

    /** Flow of upcoming appointments starting today */
    fun getUpcomingAppointments(): Flow<List<Appointment>> {
        return appointmentDao.getUpcomingAppointmentsFlow(LocalDateTime.now())
    }

    /** Delete an Appointment record by Id**/
    suspend fun deleteAppointment(id: Int) = appointmentDao.deleteById(id)

    // ----------------
    // Treatment
    // ----------------
    /** Insert a new treatments */
    suspend fun insertTreatment(treatment: Treatment) = treatmentDao.insert(treatment)

    /** Update treatments */
    suspend fun updateTreatment(treatment: Treatment) = treatmentDao.update(treatment)

    /** Flow of all treatments */
    fun getAllTreatments(): Flow<List<Treatment>> = treatmentDao.getAllTreatmentsFlow()

    /** Flow of upcoming treatments expiration dates */
    fun getUpcomingExpDates(): Flow<List<Treatment>> =
        treatmentDao.getUpcomingExpirationDatesFlow(LocalDate.now())

    /** Delete a treatments record by Id**/
    suspend fun deleteTreatment(id: Int) = treatmentDao.deleteById(id)

    // ----------------
    // Medication
    // ----------------
    /** Find a medication by its code (GTIN or CIP) */
    suspend fun findMedicationByCode(code: String): Medication? =
        medicationDao.findByCode(code)

    // ----------------
    // Important Dates
    // ----------------
    /** Insert a new important date */
    suspend fun insertImportantDate(importantDate: ImportantDate) =
        importantDateDao.insert(importantDate)

    /** Update diagnosis date */
    suspend fun updateImportantDate(importantDate: ImportantDate) =
        importantDateDao.update(importantDate)

    /** Flow of all diagnosis dates */
    fun getAllImportantDates(): Flow<List<ImportantDate>> =
        importantDateDao.getAllImportantDates()

    /** Delete a diagnosis date record by Id**/
    suspend fun deleteImportantDate(id: Int) = importantDateDao.deleteById(id)

    // ----------------
    // Medical Devices
    // ----------------
    /** Insert a new devices */
    suspend fun insertDevice(device: MedicalDevice) = medicalDevicesDao.insert(device)

    /** Update devices */
    suspend fun updateDevice(device: MedicalDevice) = medicalDevicesDao.update(device)

    /** Flow of all devices */
    fun getAllDevices(): Flow<List<MedicalDevice>> = medicalDevicesDao.getAllMedicalDevices()

    /** Flow of current medical devices */
    fun getAllCurrentConsumableDevices(): Flow<List<MedicalDevice>> =
        medicalDevicesDao.getAllCurrentConsumableMedicalDevices(today = LocalDate.now())

    /** Flow of all non-consumable devices */
    fun getAllNonConsumableDevices(): Flow<List<MedicalDevice>> =
        medicalDevicesDao.getAllNonConsumableDevices()

    /** Flow of all consumable devices */
    fun getAllConsumableDevices(): Flow<List<MedicalDevice>> =
        medicalDevicesDao.getAllConsumableDevices()

    /** Flow of all unreported faulty devices **/
    fun getAllUnreportedFaultyDevices(): Flow<List<MedicalDevice>> =
        medicalDevicesDao.getAllFaultyUnreportedMedicalDevices()

    /** Flow of all reported faulty devices **/
    fun getAllReportedFaultyDevices(): Flow<List<MedicalDevice>> =
        medicalDevicesDao.getAllFaultyReportedMedicalDevices()

    /** Flow of all faulty devices that have or are expiring today **/
    fun getAllFaultyDevicesExpiredToday(): List<MedicalDevice> =
        medicalDevicesDao.getSimilarExpiringConsumableDevices(
            today = LocalDate.now(),
            deviceType = MedicalDeviceInfoType.UNKNOWN
        )

    /** Flow of all faulty batch numbers **/
    fun getAllFaultyBatchNumbers(): Flow<List<FaultyBatchCount>> =
        medicalDevicesDao.getFaultyBatchNumbersCounts()

    // ----------------
    // Medical devices
    // ----------------
    /** Find a medical devices by its code (GTIN or CIP) */
    suspend fun findMedicalDeviceByCode(code: String): MedicalDeviceInfoEntity? =
        medicalDeviceInfo.findByCode(code)

    // ----------------
    // User details
    // ----------------
    /** Flow of user details */
    fun getUserDetails(): Flow<UserDetails?> = userDetailsDao.getUserDetails()

    /** Update user details */
    suspend fun updateUserDetails(userDetails: UserDetails) =
        userDetailsDao.upsertUserDetails(userDetails)

    /** Delete user details */
    suspend fun deleteUserDetails() = userDetailsDao.deleteUserDetails()

    /** Add profile photo path */
    suspend fun addProfilePhotoPath(path: String?) = userDetailsDao.updateProfilePhotoPath(
        path,
        nowMillisInLong()
    )

    // ----------------
    // User preferences
    // ----------------
    suspend fun insertOrUpdate(preferences: UserPreferences) =
        userPreferencesDao.insertOrUpdate(preferences)

    fun getUserPreferences(): Flow<UserPreferences?> =
        userPreferencesDao.getUserPreferences()

    suspend fun setAutoBackupEnabled(enabled: Boolean) =
        userPreferencesDao.setAutoBackupEnabled(enabled)

    suspend fun enableExpirationReminder(enabled: Boolean) =
        userPreferencesDao.enableExpirationReminder(enabled)

    suspend fun enableAppointmentReminder(enabled: Boolean) =
        userPreferencesDao.enableAppointmentReminder(enabled)

    suspend fun setBackupFrequency(frequency: String) =
        userPreferencesDao.setFrequency(frequency)

    suspend fun setBackupPath(path: String) =
        userPreferencesDao.setBackupPath(path)

    suspend fun setLastBackupUpdate(date: String) =
        userPreferencesDao.setLastBackupDate(date)

    suspend fun isExpirationReminderEnabled(): Boolean =
        userPreferencesDao.isExpirationReminderEnabled()

    suspend fun isAppointmentReminderEnabled(): Boolean =
        userPreferencesDao.isAppointmentReminderEnabled()

    suspend fun restorePreferences(preferences: UserPreferences) =
        userPreferencesDao.insertOrUpdate(preferences)

    suspend fun resetBackupPreferences() =
        userPreferencesDao.resetBackupPreferences()

    /** Delete a devices record by Id**/
    suspend fun deleteDevice(id: Int) = medicalDevicesDao.deleteById(id)

    //-----------------
    // ImEx utils
    //-----------------
    /** Export all data as a JSON string */
    suspend fun exportDataAsJsonString(): String {
        val gson = GsonFactory.create(prettyPrint = true)

        val weights = getAllWeights().first()
        val hba1cEntries = getAllHba1c().first()
        val appointments = getAllAppointments().first()
        val treatments = getAllTreatments().first()
        val importantDates = getAllImportantDates().first()
        val medicalDevices = getAllDevices().first()
        val userDetails = getUserDetails().first()
        val userPreferences = getUserPreferences().first()


        val exportData = ExportData(
            weights = weights,
            hba1c = hba1cEntries,
            appointments = appointments,
            treatments = treatments,
            importantDates = importantDates,
            devices = medicalDevices,
            userDetails = userDetails,
            userPreferences = userPreferences
        )

        return gson.toJson(exportData)
    }

    /**
     * Transaction util to run database operations as transactions
     */
    suspend fun inTransaction(block: suspend () -> Unit) {
        database.withTransaction {
            block()
        }
    }

    // Domains specific data import functions
    /**
     * Import weights from a list of [Weight]
     * Compares uuid and updatedAt to insert new entry if none exist or
     * the most recent one in cases of conflicts
     *
     * @param weights List of weights to import
     */
    suspend fun importWeights(weights: List<Weight>) = withContext(Dispatchers.IO) {
        weights.forEach { backupEntry ->
            val backupUuid: String? = backupEntry.entryUuid   // Gson may leave it null for old backups
            val hasUuid = !backupUuid.isNullOrBlank()

            val localEntry = when {
                hasUuid -> weightDao.getByUuid(backupEntry.entryUuid)
                else -> weightDao.getByContent(backupEntry.createdAt, backupEntry.date, backupEntry.value)
            }

            val uuidForInsert = if (hasUuid) backupUuid else newEntryUuid()

            when {
                localEntry == null -> insertWeight(backupEntry.copy(id = 0, entryUuid = uuidForInsert))
                backupEntry.updatedAt > localEntry.updatedAt -> updateWeight(backupEntry.copy(id = localEntry.id, entryUuid = localEntry.entryUuid))
                else -> Unit // local entry is same or newer: keep it
            }
        }
    }

    /**
     * Import HBA1C from a list of [Hba1c]
     * Compares uuid and updatedAt to insert new entry if none exist or
     * the most recent one in cases of conflicts
     *
     * @param hba1c List of HBA1C to import
     */
    suspend fun importHba1c(hba1c: List<Hba1c>) = withContext(Dispatchers.IO) {
        hba1c.forEach { backupEntry ->
            val backupUuid: String? = backupEntry.entryUuid   // Gson may leave it null for old backups
            val hasUuid = !backupUuid.isNullOrBlank()

            val localEntry = when {
                hasUuid -> hba1cDao.getByUuid(backupEntry.entryUuid)
                else -> hba1cDao.getByContent(backupEntry.createdAt, backupEntry.date, backupEntry.value)
            }

            val uuidForInsert = if (hasUuid) backupUuid else newEntryUuid()

            when {
                localEntry == null -> insertHba1c(backupEntry.copy(id = 0, entryUuid = uuidForInsert))
                backupEntry.updatedAt > localEntry.updatedAt -> updateHBA1C(backupEntry.copy(id = localEntry.id, entryUuid = localEntry.entryUuid))
                else -> Unit // local entry is same or newer: keep it
            }
        }
    }

    /**
     * Import appointments from a list of [Appointment]
     * Compares uuid and updatedAt to insert new entry if none exist or
     * the most recent one in cases of conflicts
     *
     * @param appointments List of appointments to import
     */
    suspend fun importAppointments(appointments: List<Appointment>) = withContext(Dispatchers.IO) {
        appointments.forEach { backupEntry ->
            val backupUuid: String? = backupEntry.entryUuid   // Gson may leave it null for old backups
            val hasUuid = !backupUuid.isNullOrBlank()

            val localEntry = when {
                hasUuid -> appointmentDao.getByUuid(backupEntry.entryUuid)
                else -> appointmentDao.getByContent(backupEntry.createdAt, backupEntry.date, backupEntry.type)
            }

            val uuidForInsert = if (hasUuid) backupUuid else newEntryUuid()

            when {
                localEntry == null -> insertAppointment(backupEntry.copy(id = 0, entryUuid = uuidForInsert))
                backupEntry.updatedAt > localEntry.updatedAt -> updateAppointment(backupEntry.copy(id = localEntry.id, entryUuid = localEntry.entryUuid))
                else -> Unit // local entry is same or newer: keep it
            }
        }
    }

    /**
     * Import treatments from a list of [Treatment]
     * Compares uuid and updatedAt to insert new entry if none exist or
     * the most recent one in cases of conflicts
     *
     * @param treatments List of treatments to import
     */
    suspend fun importTreatments(treatments: List<Treatment>) = withContext(Dispatchers.IO) {
        treatments.forEach { backupEntry ->
            val backupUuid: String? = backupEntry.entryUuid   // Gson may leave it null for old backups
            val hasUuid = !backupUuid.isNullOrBlank()

            val localEntry = when {
                hasUuid -> treatmentDao.getByUuid(backupEntry.entryUuid)
                else -> treatmentDao.getByContent(backupEntry.createdAt, backupEntry.name, backupEntry.expirationDate, backupEntry.type)
            }

            val uuidForInsert = if (hasUuid) backupUuid else newEntryUuid()

            when {
                localEntry == null -> insertTreatment(backupEntry.copy(id = 0, entryUuid = uuidForInsert))
                backupEntry.updatedAt > localEntry.updatedAt -> updateTreatment(backupEntry.copy(id = localEntry.id, entryUuid = localEntry.entryUuid))
                else -> Unit // local entry is same or newer: keep it
            }
        }
    }

    /**
     * Import important dates from a list of [ImportantDate]
     * Compares uuid and updatedAt to insert new entry if none exist or
     * the most recent one in cases of conflicts
     *
     * @param importantDates List of important dates to import
     */
    suspend fun importImportantDates(importantDates: List<ImportantDate>) =
        withContext(Dispatchers.IO) {
            importantDates.forEach { backupEntry ->
                val backupUuid: String? = backupEntry.entryUuid   // Gson may leave it null for old backups
                val hasUuid = !backupUuid.isNullOrBlank()

                val localEntry = when {
                    hasUuid -> importantDateDao.getByUuid(backupEntry.entryUuid)
                    else -> importantDateDao.getByContent(backupEntry.createdAt, backupEntry.date, backupEntry.importantDate)
                }

                val uuidForInsert = if (hasUuid) backupUuid else newEntryUuid()

                when {
                    localEntry == null -> insertImportantDate(backupEntry.copy(id = 0, entryUuid = uuidForInsert))
                    backupEntry.updatedAt > localEntry.updatedAt -> updateImportantDate(backupEntry.copy(id = localEntry.id, entryUuid = localEntry.entryUuid))
                    else -> Unit // local entry is same or newer: keep it
                }
            }
        }

    /**
     * Import medical devices from a list of [MedicalDevice]
     * Compares uuid and updatedAt to insert new entry if none exist or
     * the most recent one in cases of conflicts
     *
     * @param medicalDevices List of medical devices to import
     */
    suspend fun importMedicalDevices(medicalDevices: List<MedicalDevice>) =
        withContext(Dispatchers.IO) {
            medicalDevices.forEach { backupEntry ->
                val backupUuid: String? = backupEntry.entryUuid   // Gson may leave it null for old backups
                val hasUuid = !backupUuid.isNullOrBlank()

                val localEntry = when {
                    hasUuid -> medicalDevicesDao.getByUuid(backupEntry.entryUuid)
                    else -> medicalDevicesDao.getByContent(backupEntry.createdAt, backupEntry.date, backupEntry.deviceType, backupEntry.name, backupEntry.batchNumber)
                }

                val uuidForInsert = if (hasUuid) backupUuid else newEntryUuid()

                when {
                    localEntry == null -> insertDevice(backupEntry.copy(id = 0, entryUuid = uuidForInsert))
                    backupEntry.updatedAt > localEntry.updatedAt -> updateDevice(backupEntry.copy(id = localEntry.id, entryUuid = localEntry.entryUuid))
                    else -> Unit // local entry is same or newer: keep it
                }
            }
        }

    /**
     * Import user details [UserDetails]
     *
     * User details are stored as a single row (id 0): the imported details
     * always replace the local ones, without comparing `updatedAt` (unlike [importWeights]).
     * The id is forced to 0 because the app always reads the row with id 0,
     * whatever id the backup contains.
     *
     * @param userDetails User details to import
     */
    suspend fun importUserDetails(userDetails: UserDetails) = withContext(Dispatchers.IO) {
        updateUserDetails(userDetails.copy(id = 0))
    }

    /**
     * Import user preferences [UserPreferences]
     *
     * User preferences are stored as a single row (id 0): the imported preferences
     * always replace the local ones, without comparing `updatedAt` (unlike [importWeights]).
     * The id is forced to 0 because backups made with v4.9.76 may contain id 1,
     * a row the app would never read (see #121).
     *
     * @param userPreferences User preferences to import
     */
    suspend fun importUserPreferences(userPreferences: UserPreferences) =
        withContext(Dispatchers.IO) {
            insertOrUpdate(userPreferences.copy(id = 0))
        }

    /**
     * Toggle backup encryption
     *
     * @param enabled Whether to enable backup encryption
     */
    suspend fun toggleBackupEncryption(enabled: Boolean) = withContext(Dispatchers.IO) {
        userPreferencesDao.toggleBackupEncryptionEnabled(enabled)
    }

    /**
     * Check if backup encryption is enabled
     *
     * @return Whether backup encryption is enabled
     */
    suspend fun isBackupEncryptionEnabled(): Boolean = withContext(Dispatchers.IO) {
        userPreferencesDao.isBackupEncryptionEnabled()
    }

    /**
     * Legacy import from JSON method
     *
     * @param json JSON string to import
     * @param profilePhotoPath Path to profile photo file
     */
    suspend fun importDataFromJsonString(json: String) {
        val gson = GsonFactory.create()

        val importedData: ExportData = gson.fromJson(json, ExportData::class.java)

        inTransaction {
            importWeights(importedData.weights)
            importHba1c(importedData.hba1c)
            importAppointments(importedData.appointments)
            importTreatments(importedData.treatments)
            importImportantDates(importedData.importantDates)
            importMedicalDevices(importedData.devices)
            importUserDetails(importedData.userDetails ?: UserDetails())
            importUserPreferences(importedData.userPreferences ?: UserPreferences())
        }
    }

    /**
     * Save profile photo to files directory and udpate photo path
     *
     * @param bytes Bytes of the profile photo
     * @param filesDir Files directory
     *
     * @return Path to the saved profile photo
     */
    suspend fun saveProfilePhotoBytes(bytes: ByteArray, filesDir: File): String {
        val fileName = "profile_photo_${System.currentTimeMillis()}.jpg"
        val file = File(filesDir, fileName)

        filesDir.listFiles()
            ?.filter { it.name.startsWith("profile_photo_") && it.name != fileName }
            ?.forEach { it.delete() }

        file.writeBytes(bytes)
        addProfilePhotoPath(file.absolutePath)
        return file.absolutePath
    }

    // ----------------
    // Generic / Database Utilities
    // ----------------
    /** Clear the entire database and reset autoincrement IDs */
    fun clearAllDataAndReset() = database.clearAllDataAndReset()
}