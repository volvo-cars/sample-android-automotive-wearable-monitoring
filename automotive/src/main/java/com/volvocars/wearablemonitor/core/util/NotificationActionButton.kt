package com.volvocars.wearablemonitor.core.util

import android.app.PendingIntent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class NotificationActionButton(
    @DrawableRes val icon: Int,
    @StringRes val text: Int,
    val pendingIntent: PendingIntent
)