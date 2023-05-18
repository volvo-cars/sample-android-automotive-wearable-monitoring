package com.volvocars.wearablemonitor.core.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.volvocars.wearablemonitor.R
import com.volvocars.wearablemonitor.core.util.NotificationActionButton
import com.volvocars.wearablemonitor.core.util.NotificationConstants
import com.volvocars.wearablemonitor.core.util.NotificationUtil
import com.volvocars.wearablemonitor.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ServiceNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationUtil: NotificationUtil,
) {
    fun showForegroundServiceNotification() = showInfoNotification(
        context.getString(R.string.configurationsRequired_title),
        context.getString(R.string.configurationsRequired_text)
    )

    fun showGlucoseInfoNotification(
        glucoseValue: String,
        diff: String,
        unit: String,
        direction: String
    ) {
        val title = context.getString(R.string.notification_info_title, glucoseValue, direction)
        val text = context.getString(R.string.notification_info_text, diff, unit)

        showInfoNotification(title, text)
    }

    fun showMissingNetworkNotification() {
        showInfoNotification(
            context.getString(R.string.missing_network_title),
            context.getString(R.string.missing_network_text)
        )
    }

    fun showRequireConfigurationNotification() {
        showInfoNotification(
            context.getString(R.string.configurationsRequired_title),
            context.getString(R.string.configurationsRequired_text)
        )
    }

    fun showGlucoseCriticalHighNotification(
        glucoseValue: String,
        unit: String
    ) {
        val title = context.getString(R.string.alarm_high_notification_title)
        val text = context.getString(R.string.alarm_high_notification_text, glucoseValue, unit)

        notificationUtil.displayHeadsUpNotification(
            title,
            text,
            NotificationConstants.GLUCOSE_CRITICAL_NOTIFICATION_ID,
            mainActivityPendingIntent,
            glucoseCriticalNotificationActionButtons()
        )
    }

    fun showGlucoseCriticalLowNotification(
        glucoseValue: String,
        unit: String,
    ) {
        val title = context.getString(R.string.alarm_low_notification_title)
        val text = context.getString(R.string.alarm_low_notification_text, glucoseValue, unit)

        notificationUtil.displayHeadsUpNotification(
            title,
            text,
            NotificationConstants.GLUCOSE_CRITICAL_NOTIFICATION_ID,
            mainActivityPendingIntent,
            glucoseCriticalNotificationActionButtons()
        )
    }

    fun hideGlucoseCriticalNotification() {
        notificationUtil.run {
            cancelNotification(NotificationConstants.GLUCOSE_CRITICAL_NOTIFICATION_ID.toInt())
            deleteNotificationChannel(NotificationConstants.GLUCOSE_CRITICAL_NOTIFICATION_ID)
        }
    }

    private fun glucoseCriticalNotificationActionButtons() = listOf(
        NotificationActionButton(
            0, R.string.alarm_critical_notification_buttonText_snack, findSnackIntent
        ),
        NotificationActionButton(
            0,
            R.string.alarm_critical_notification_buttonText_parking,
            findParkingIntent
        )
    )

    private fun showInfoNotification(
        title: String,
        text: String,
    ) = notificationUtil.displayInfoNotification(
        title,
        text,
        NotificationConstants.GLUCOSE_INFO_NOTIFICATION_CHANNEL_ID,
        mainActivityPendingIntent
    )

    private val findSnackIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse(NotificationConstants.GOOGLE_MAPS_FIND_NEAREST_CONVENIENCE_STORE_URI)
        ).setPackage(NotificationConstants.GOOGLE_MAPS_PACKAGE_NAME), PendingIntent.FLAG_IMMUTABLE
    )

    private val findParkingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse(NotificationConstants.GOOGLE_MAPS_FIND_NEAREST_PARKING_URI)
        ).setPackage(NotificationConstants.GOOGLE_MAPS_PACKAGE_NAME), PendingIntent.FLAG_IMMUTABLE
    )

    private val mainActivityPendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE
    )
}