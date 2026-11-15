package com.diabdata.workers.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.diabdata.core.notifications.NotificationImportance
import com.diabdata.core.notifications.showNotification
import com.diabdata.shared.R as shared

class SingleNotificationWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString("title") ?: return Result.failure()
        val content = inputData.getString("content") ?: return Result.failure()
        val tag = inputData.getString("tag") ?: "default"

        val channelName = when (tag) {
            "treatments" -> applicationContext.getString(
                shared.string.medications_notification_channel_name
            )
            "appointments" -> applicationContext.getString(
                shared.string.appointments_notification_channel_name
            )
            else -> "DiabData Notifications"
        }

        applicationContext.showNotification(
            title = title,
            content = content,
            channelName = channelName,
            importance = NotificationImportance.HIGH
        )

        return Result.success()
    }
}
