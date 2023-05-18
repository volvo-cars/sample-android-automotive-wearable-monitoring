package com.volvocars.wearablemonitor.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.PeriodicWorkRequest
import com.volvocars.wearablemonitor.core.service.WearableMonitorService
import com.volvocars.wearablemonitor.core.util.NotificationConstants.ACTION_REQUIRE_CONFIGURATION
import com.volvocars.wearablemonitor.core.util.NotificationConstants.ACTION_SHOW_GLUCOSE_VALUES
import com.volvocars.wearablemonitor.core.worker.GlucoseFetchWorker
import com.volvocars.wearablemonitor.domain.usecase.IsUserSignedIn
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootCompleteReceiver : BroadcastReceiver() {

    @Inject
    lateinit var glucoseFetchRequest: PeriodicWorkRequest

    @Inject
    lateinit var isUserSignedIn: IsUserSignedIn

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            GlucoseFetchWorker.create(context, glucoseFetchRequest)
            if (isUserSignedIn()) {
                WearableMonitorService.create(context, ACTION_SHOW_GLUCOSE_VALUES)
            } else {
                WearableMonitorService.create(context, ACTION_REQUIRE_CONFIGURATION)
            }.also {
                context.startForegroundService(it)
            }
        }
    }

    companion object {
        val TAG = BootCompleteReceiver::class.simpleName
    }
}