package com.diabdata.core.backup.worker

import android.content.Context
import android.provider.DocumentsContract
import android.util.Log
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.diabdata.core.backup.BackupArchiveManager
import com.diabdata.core.backup.BackupScheduleCoordinator
import com.diabdata.core.database.DataRepository
import com.diabdata.core.notifications.NotificationImportance
import com.diabdata.core.notifications.showNotification
import com.diabdata.shared.R
import com.diabdata.shared.utils.utils.uriStringToReadablePath
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.first

/**
 * [CoroutineWorker] that performs a scheduled automatic backup, writing it to the user's configured
 * backup folder and notifying them of the outcome. Scheduled via [BackupScheduleCoordinator].
 */
@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dataRepository: DataRepository,
    private val backupArchiveManager: BackupArchiveManager
) : CoroutineWorker(appContext, workerParams) {
    private val tag = "BackupWorker"

    /**
     * Logs [technicalMessage] and notifies the user with the localized string identified by
     * [messageResId], then reports this work run as failed.
     */
    private fun failWithNotification(technicalMessage: String, localisedString: Int): Result {
        Log.w(tag, technicalMessage)
        applicationContext.showNotification(
            title = applicationContext.getString(
                R.string.settings_notifications_scheduled_backup_error_title
            ),
            content = applicationContext.getString(localisedString),
            channelName = applicationContext.getString(
                R.string.settings_notifications_scheduled_data_backup_channel_name
            ),
            importance = NotificationImportance.DEFAULT
        )

        return Result.failure()
    }

    /**
     * Writes a new encrypted or plain backup archive to the user's configured backup folder, using
     * [backupArchiveManager], and notifies the user of the outcome.
     *
     * Fails without retrying if user preferences or the backup path are missing/invalid, or if the
     * app no longer holds permission to write to the configured folder ([SecurityException]).
     * Retries up to 3 times for any other unexpected failure.
     */
    override suspend fun doWork(): Result {
        return try {
            val prefs = dataRepository.getUserPreferences().first() ?: run {
                return failWithNotification(
                    "No preferences found, skipping",
                    R.string.settings_notifications_scheduled_backup_no_preferences_error
                )
            }

            val backupPath = prefs.backupPath ?: run {
                return failWithNotification(
                    "No backup path configured, skipping",
                    R.string.settings_notifications_scheduled_backup_undefined_backup_directory_error
                )
            }

            val isEncrypted = dataRepository.isBackupEncryptionEnabled()

            val treeUri = backupPath.toUri()
            val dateFormat = SimpleDateFormat("dd-MM-yyyy_HH-mm", Locale.getDefault())
            val readableDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            val date = dateFormat.format(Date())
            val readableDate = readableDateFormat.format(Date())

            val fileName = "diabdata_backup_$date.zip"

            val docUri = DocumentsContract.createDocument(
                applicationContext.contentResolver,
                DocumentsContract.buildDocumentUriUsingTree(
                    treeUri,
                    DocumentsContract.getTreeDocumentId(treeUri)
                ),
                "application/zip",
                fileName
            ) ?: run {
                return failWithNotification(
                    "createDocument returned null",
                    R.string.settings_notifications_scheduled_backup_file_creation_error
                )
            }

            val outputStream = applicationContext.contentResolver.openOutputStream(docUri)
                ?: run {
                    return failWithNotification(
                        "openOutputStream returned null",
                        R.string.settings_notifications_scheduled_backup_file_access_error
                    )
                }

            outputStream.use {
                backupArchiveManager.writeBackup(
                    it,
                    isScheduledBackup = true,
                    isEncrypted = isEncrypted
                ).getOrThrow()
            }

            dataRepository.setLastBackupUpdate(
                LocalDateTime.now().toString()
            )

            applicationContext.showNotification(
                title = applicationContext.getString(
                    R.string.settings_notifications_scheduled_backup_success_title
                ),
                content = applicationContext.getString(
                    R.string.settings_notifications_scheduled_backup_success,
                    backupPath.uriStringToReadablePath(applicationContext),
                    readableDate
                ),
                channelName = applicationContext.getString(
                    R.string.settings_notifications_scheduled_data_backup_channel_name
                ),
                importance = NotificationImportance.LOW
            )

            Result.success()
        } catch (e: CancellationException) {
            throw e
            // Permission issues won't resolve by retrying, unlike transient I/O failures below.
        } catch (e: SecurityException) {
            Log.e(tag, "Permission denied", e)
            applicationContext.showNotification(
                title = applicationContext.getString(
                    R.string.settings_notifications_scheduled_backup_error_title
                ),
                content = applicationContext.getString(
                    R.string.settings_notifications_scheduled_backup_error_permission_denied,
                    e.message.toString()
                ),
                channelName = applicationContext.getString(
                    R.string.settings_notifications_scheduled_data_backup_channel_name
                ),
                importance = NotificationImportance.DEFAULT
            )
            Result.failure()
        } catch (e: Exception) {
            Log.e(tag, "Backup failed", e)
            val errorDetail =
                "${e.javaClass.simpleName}: ${e.message}\nat ${e.stackTrace.firstOrNull()}"
            applicationContext.showNotification(
                title = applicationContext.getString(
                    R.string.settings_notifications_scheduled_backup_error_title
                ),
                content = errorDetail,
                channelName = applicationContext.getString(
                    R.string.settings_notifications_scheduled_data_backup_channel_name
                ),
                importance = NotificationImportance.DEFAULT
            )

            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
