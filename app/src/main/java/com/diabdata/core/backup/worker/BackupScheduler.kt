package com.diabdata.core.backup.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.Operation
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.diabdata.shared.utils.dataTypes.BackupFrequency
import java.util.concurrent.TimeUnit

/**
 * Thin wrapper around [WorkManager] for scheduling and cancelling the automatic backup
 * [BackupWorker], as a single named periodic work request.
 */
object BackupScheduler {
    /** Unique work name used to enqueue/cancel the automatic backup, ensuring only one instance runs. */
    const val AUTO_BACKUP_UNIQUE_WORK_NAME = "auto_backup_periodic"

    /**
     * Builds a [PeriodicWorkRequest] for [BackupWorker], running every [BackupFrequency.days] days,
     * with an initial delay of the same duration (no backup runs immediately upon scheduling).
     * Requires the device to not be low on battery or storage.
     */
    private fun buildWorkRequest(frequency: BackupFrequency): PeriodicWorkRequest {
        Log.d("BackupDebug", "Building Periodic work request")
        return PeriodicWorkRequestBuilder<BackupWorker>(
            frequency.days,
            TimeUnit.DAYS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .setRequiresStorageNotLow(true)
                    .build()
            )
            .setInitialDelay(frequency.days, TimeUnit.DAYS)
            .addTag("auto_backup")
            .build()
    }

    /**
     * Schedules the automatic backup at [frequency], replacing any previously scheduled instance.
     *
     * @param context Used to access [WorkManager].
     * @param frequency How often the backup should run.
     * @return The [Operation] representing this enqueue request; its result can be awaited to know
     * when scheduling has taken effect.
     */
    fun scheduleFromUser(context: Context, frequency: BackupFrequency): Operation {
        Log.d("BackupDebug", "Queuing unique work with name=$AUTO_BACKUP_UNIQUE_WORK_NAME")
        return WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            AUTO_BACKUP_UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            buildWorkRequest(frequency)
        )
    }

    /**
     * Cancels the automatic backup, if currently scheduled.
     *
     * @param context Used to access [WorkManager].
     * @return The [Operation] representing this cancellation request; its result can be awaited to
     * know when cancellation has taken effect.
     */

    fun cancel(context: Context): Operation {
        Log.d("BackupDebug", "Cancelling unique work with name=$AUTO_BACKUP_UNIQUE_WORK_NAME")
        return WorkManager.getInstance(context).cancelUniqueWork(AUTO_BACKUP_UNIQUE_WORK_NAME)
    }
}
