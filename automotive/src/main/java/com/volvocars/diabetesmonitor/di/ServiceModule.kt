package com.volvocars.diabetesmonitor.di

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.volvocars.diabetesmonitor.R
import com.volvocars.diabetesmonitor.core.util.Constants.KEY_CONTENT_INTENT
import com.volvocars.diabetesmonitor.core.util.Constants.KEY_CRITICAL_NOTIFICATION
import com.volvocars.diabetesmonitor.core.util.Constants.KEY_CRITICAL_NOTIFICATION_CHANNEL_ID
import com.volvocars.diabetesmonitor.core.util.Constants.KEY_CRITICAL_NOTIFICATION_CHANNEL_NAME
import com.volvocars.diabetesmonitor.core.util.Constants.KEY_FIND_PARK_INTENT
import com.volvocars.diabetesmonitor.core.util.Constants.KEY_FIND_SNACK_INTENT
import com.volvocars.diabetesmonitor.core.util.Constants.KEY_IMPORTANCE_DEFAULT
import com.volvocars.diabetesmonitor.core.util.Constants.KEY_IMPORTANCE_HIGH
import com.volvocars.diabetesmonitor.core.util.Constants.KEY_INFORMATION_NOTIFICATION
import com.volvocars.diabetesmonitor.core.util.Constants.KEY_NOTIFICATION_CHANNEL_ID
import com.volvocars.diabetesmonitor.core.util.Constants.KEY_NOTIFICATION_CHANNEL_NAME
import com.volvocars.diabetesmonitor.feature_glucose.presentation.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Named

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    @Named(KEY_IMPORTANCE_HIGH)
    fun provideCriticalNotificationChannel() = NotificationChannel(
        KEY_CRITICAL_NOTIFICATION_CHANNEL_ID,
        KEY_CRITICAL_NOTIFICATION_CHANNEL_NAME,
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        setShowBadge(true)
        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
    }

    @ServiceScoped
    @Provides
    @Named(KEY_IMPORTANCE_DEFAULT)
    fun provideInfoNotificationChannel() = NotificationChannel(
        KEY_NOTIFICATION_CHANNEL_ID,
        KEY_NOTIFICATION_CHANNEL_NAME,
        NotificationManager.IMPORTANCE_DEFAULT
    )

    @ServiceScoped
    @Provides
    fun provideMainActivityIntent(@ApplicationContext context: Context) =
        Intent(context, MainActivity::class.java)

    @ServiceScoped
    @Provides
    @Named(KEY_CONTENT_INTENT)
    fun provideContentIntent(
        @ApplicationContext context: Context,
        mainActivityIntent: Intent,
    ): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        mainActivityIntent,
        0
    )

    @ServiceScoped
    @Provides
    @Named(KEY_FIND_PARK_INTENT)
    fun provideFindParkingIntent(
        @ApplicationContext context: Context,
    ): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(
            Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.uri_find_parking))
        ).setPackage("com.google.android.apps.maps"), PendingIntent.FLAG_IMMUTABLE
    )

    @ServiceScoped
    @Provides
    @Named(KEY_FIND_SNACK_INTENT)
    fun provideFindSnackIntent(
        @ApplicationContext context: Context,
    ): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(
            Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.uri_find_store))
        ).setPackage("com.google.android.apps.maps"), PendingIntent.FLAG_IMMUTABLE
    )

    @ServiceScoped
    @Provides
    @Named(KEY_CRITICAL_NOTIFICATION)
    fun provideCriticalNotification(
        @ApplicationContext context: Context,
        @Named(KEY_CONTENT_INTENT) contentIntent: PendingIntent,
        @Named(KEY_IMPORTANCE_HIGH) criticalNotificationChannel: NotificationChannel,
        @Named(KEY_FIND_SNACK_INTENT) findSnackIntent: PendingIntent,
        @Named(KEY_FIND_PARK_INTENT) findParkingIntent: PendingIntent,
        mainActivityIntent: Intent,
    ): NotificationCompat.Builder =
        NotificationCompat.Builder(context, criticalNotificationChannel.id)
            .setSmallIcon(R.drawable.ic_baseline_notification_important_24)
            .setContentIntent(contentIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(criticalNotificationChannel.importance)
            .setAutoCancel(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    mainActivityIntent,
                    0
                )
            )
            .setContentTitle(context.getString(R.string.alarmLow_notification_title))
            .addAction(
                0,
                context.getString(R.string.alarmLow_notification_buttonText_snack),
                findSnackIntent
            )
            .addAction(
                0,
                context.getString(R.string.alarmLow_notification_buttonText_parking),
                findParkingIntent
            )

    @ServiceScoped
    @Provides
    @Named(KEY_INFORMATION_NOTIFICATION)
    fun provideDefaultNotification(
        @ApplicationContext context: Context,
        @Named(KEY_CONTENT_INTENT) contentIntent: PendingIntent,
        @Named(KEY_IMPORTANCE_DEFAULT) infoNotificationChannel: NotificationChannel,
    ): NotificationCompat.Builder =
        NotificationCompat.Builder(context, infoNotificationChannel.id)
            .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
            .setContentIntent(contentIntent)
            .setPriority(infoNotificationChannel.importance)
}