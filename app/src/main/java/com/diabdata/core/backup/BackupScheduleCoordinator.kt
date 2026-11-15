package com.diabdata.core.backup

import android.app.Application
import android.content.ContentResolver
import android.util.Log
import androidx.core.net.toUri
import com.diabdata.core.backup.worker.BackupScheduler
import com.diabdata.core.backup.worker.BackupWorker
import com.diabdata.feature.settings.sections.dataSettings.BackupViewModel
import com.diabdata.shared.utils.dataTypes.BackupFrequency
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Centralizes scheduling of the automatic backup [BackupWorker], ensuring only one scheduling
 * operation runs at a time via [backupScheduleMutex].
 *
 * Used both by [BackupViewModel] (when the user actively changes backup preferences) and by
 * [BackupArchiveManager.zipImport] (to resynchronize the schedule against imported preferences).
 */
@Singleton
class BackupScheduleCoordinator @Inject constructor(private val application: Application) {
    private val backupScheduleMutex: Mutex = Mutex()

    /**
     * Checks whether [path] still points to a location this app holds a persisted read/write
     * permission for, via [ContentResolver.getPersistedUriPermissions]. A previously valid backup
     * path can become inaccessible if the user revokes access (e.g. from Android's file picker or
     * settings) without going through this app.
     *
     * @param path The backup folder URI to check, as a string. `null` or blank is never accessible.
     */
    private fun isBackupPathAccessible(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        val uri = path.toUri()
        return application.contentResolver.persistedUriPermissions.any {
            it.uri == uri && it.isReadPermission && it.isWritePermission
        }
    }

    /**
     * Cancels any previously scheduled [BackupWorker], then reschedules it with [frequency] if
     * [enabled] is true and [backupPath] is still accessible (see [isBackupPathAccessible]).
     *
     * @param enabled Whether automatic backup should be scheduled at all.
     * @param frequency How often the automatic backup should run, if scheduled.
     * @param backupPath The destination folder URI, as a string, backups should be written to.
     * @return [Result.success] with `true` if the schedule was actually (re)scheduled, `false` if
     * automatic backup was disabled or [backupPath] is inaccessible; [Result.failure] with the
     * underlying exception if cancelling or scheduling itself failed.
     */
    suspend fun applyBackupSchedule(
        enabled: Boolean,
        frequency: BackupFrequency,
        backupPath: String?
    ): Result<Boolean> {
        val canSchedule = enabled && isBackupPathAccessible(backupPath)
        Log.d(
            "BackupScheduleCoordinator",
            "applyBackupSchedule called with enabled=$enabled, frequency=$frequency"
        )
        return backupScheduleMutex.withLock {
            try {
                Log.d(
                    "BackupScheduleCoordinator",
                    "applyBackupSchedule cancelling previous backup worker"
                )
                BackupScheduler.cancel(application).result.await()
                if (canSchedule) {
                    Log.d(
                        "BackupScheduleCoordinator",
                        "applyBackupSchedule scheduling new backup worker"
                    )
                    BackupScheduler.scheduleFromUser(application, frequency).result.await()
                }
                Result.success(canSchedule)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("BackupScheduleCoordinator", "Failed to apply backup schedule", e)
                Result.failure(e)
            }
        }
    }
}
