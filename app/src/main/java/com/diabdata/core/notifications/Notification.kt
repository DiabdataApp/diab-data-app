package com.diabdata.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.diabdata.shared.R as shared

fun Context.showNotification(
    title: String,
    channelId: String = "diabdata_channel",
    channelName: String? = null,
    content: String,
    channelDescription: String? = null,
    iconId: Int? = null,
    importance: NotificationImportance = NotificationImportance.DEFAULT
) {
    val notification = createNotification(
        title = title,
        channelId = channelId,
        channelName = channelName,
        content = content,
        channelDescription = channelDescription,
        iconId = iconId,
        importance = importance
    )

    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    // ID unique basé sur le temps
    manager.notify(System.currentTimeMillis().toInt(), notification)
}

fun Context.createNotification(
    title: String,
    channelId: String = "diabdata_channel",
    channelName: String? = null,
    content: String,
    channelDescription: String? = null,
    iconId: Int? = null,
    importance: NotificationImportance = NotificationImportance.DEFAULT
): android.app.Notification {
    val safeChannelName = channelName ?: "DiabData Notifications"
    val safeChannelDescription = channelDescription ?: ""

    val (notifImportance, notifPriority) = when (importance) {
        NotificationImportance.HIGH ->
            NotificationManager.IMPORTANCE_HIGH to
                NotificationCompat.PRIORITY_HIGH
        NotificationImportance.LOW ->
            NotificationManager.IMPORTANCE_LOW to
                NotificationCompat.PRIORITY_LOW
        NotificationImportance.MIN ->
            NotificationManager.IMPORTANCE_MIN to
                NotificationCompat.PRIORITY_MIN
        else -> NotificationManager.IMPORTANCE_DEFAULT to NotificationCompat.PRIORITY_DEFAULT
    }

    val channel = NotificationChannel(channelId, safeChannelName, notifImportance).apply {
        description = safeChannelDescription
    }

    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.createNotificationChannel(channel)

    val iconResId = iconId ?: shared.drawable.ic_logo_outlined

    return NotificationCompat.Builder(this, channelId)
        .setSmallIcon(iconResId)
        .setContentTitle(title)
        .setContentText(content)
        .setStyle(NotificationCompat.BigTextStyle().bigText(content))
        .setPriority(notifPriority)
        .build()
}

enum class NotificationImportance {
    MIN,
    LOW,
    DEFAULT,
    HIGH
}
