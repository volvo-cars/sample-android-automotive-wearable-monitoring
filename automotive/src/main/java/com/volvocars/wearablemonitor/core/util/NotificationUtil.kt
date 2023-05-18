package com.volvocars.wearablemonitor.core.util

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.volvocars.wearablemonitor.R
import com.volvocars.wearablemonitor.core.service.NotificationChannelHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationUtil @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManager,
    private val notificationChannelHelper: NotificationChannelHelper
) {

    fun displayInfoNotification(
        title: String,
        text: String,
        notificationId: String,
        contentIntent: PendingIntent? = null,
        actionButtons: List<NotificationActionButton>? = null,
    ): Notification = buildNotification(
        title,
        text,
        R.drawable.ic_baseline_notifications_active_24,
        notificationId,
        NotificationManager.IMPORTANCE_DEFAULT,
        contentIntent,
        actionButtons
    ).also { displayNotification(it) }

    fun displayHeadsUpNotification(
        title: String,
        text: String,
        notificationId: String,
        contentIntent: PendingIntent? = null,
        actionButton: List<NotificationActionButton>? = null,
    ) = buildNotification(
        title,
        text,
        R.drawable.ic_baseline_notification_important_24,
        notificationId,
        NotificationManager.IMPORTANCE_HIGH,
        contentIntent,
        actionButton
    ).also { displayNotification(it) }

    fun deleteNotificationChannel(id: String) {
        notificationManager.deleteNotificationChannel(id)
    }

    fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
    }

    private fun displayNotification(notification: Notification) {
        val channelId = notification.channelId
        notificationChannelHelper.getChannelById(channelId)?.let { notificationChannel ->
            notificationManager.run {
                createNotificationChannel(notificationChannel)
                notify(channelId.toInt(), notification)
            }
        }
    }

    private fun buildNotification(
        title: String,
        text: String,
        @DrawableRes icon: Int,
        notificationId: String,
        priority: Int,
        contentIntent: PendingIntent? = null,
        actionButton: List<NotificationActionButton>? = null
    ): Notification = NotificationCompat.Builder(context, notificationId)
        .setSmallIcon(icon)
        .setContentIntent(contentIntent)
        .setPriority(priority)
        .setContentTitle(title)
        .setShowWhen(true)
        .setWhen(System.currentTimeMillis())
        .setContentText(text).also { notificationBuilder ->
            actionButton?.forEach {
                notificationBuilder.addAction(
                    it.icon,
                    context.getString(it.text),
                    it.pendingIntent
                )
            }
        }.build()
}