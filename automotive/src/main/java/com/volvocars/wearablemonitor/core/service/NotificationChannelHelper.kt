package com.volvocars.wearablemonitor.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import com.volvocars.wearablemonitor.core.util.NotificationConstants
import javax.inject.Inject

class NotificationChannelHelper @Inject constructor() {

    fun getChannelById(id: String) = when (id) {
        NotificationConstants.GLUCOSE_CRITICAL_NOTIFICATION_ID -> createGlucoseCriticalNotificationChannel()
        NotificationConstants.GLUCOSE_INFO_NOTIFICATION_CHANNEL_ID -> createGlucoseInfoNotificationChannel()
        else -> null
    }

    private fun createGlucoseCriticalNotificationChannel() = NotificationChannel(
        NotificationConstants.GLUCOSE_CRITICAL_NOTIFICATION_ID,
        NotificationConstants.GLUCOSE_CRITICAL_NOTIFICATION_NAME,
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        setShowBadge(true)
        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
    }

    private fun createGlucoseInfoNotificationChannel() = NotificationChannel(
        NotificationConstants.GLUCOSE_INFO_NOTIFICATION_CHANNEL_ID,
        NotificationConstants.GLUCOSE_INFO_NOTIFICATION_CHANNEL_NAME,
        NotificationManager.IMPORTANCE_DEFAULT
    )
}