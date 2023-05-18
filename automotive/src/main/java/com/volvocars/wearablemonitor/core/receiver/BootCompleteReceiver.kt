package com.volvocars.wearablemonitor.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.PeriodicWorkRequest
import com.volvocars.wearablemonitor.core.service.WearableMonitorService
import com.volvocars.wearablemonitor.core.util.NotificationConstants.ACTION_REQUIRE_CONFIGURATION
import com.volvocars.wearablemonitor.core.util.NotificationConstants.ACTION_SHOW_GLUCOSE_VALUES
import com.volvocars.wearablemonitor.core.worker.GlucoseFetchWorker
import com.volvocars.wearablemonitor.data.storage.SharedPreferenceStorage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootCompleteReceiver : BroadcastReceiver() {

    @Inject
    lateinit var glucoseFetchRequest: PeriodicWorkRequest

    @Inject
    lateinit var sharedPreferenceStorage: SharedPreferenceStorage

    override fun onReceive(
        context: Context?,
        intent: Intent
    ) {
        if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED) || equals(Intent.ACTION_LOCKED_BOOT_COMPLETED)) {
            context?.let {
                GlucoseFetchWorker.create(it, glucoseFetchRequest)
                if (sharedPreferenceStorage.userSignedIn()) {
                    if (sharedPreferenceStorage.getGlucoseNotificationEnabled()) {
                        sendCommandToService(it, ACTION_SHOW_GLUCOSE_VALUES)
                    }
                } else {
                    sendCommandToService(it, ACTION_REQUIRE_CONFIGURATION)
                }
            }
        }
    }

    private fun sendCommandToService(context: Context, action: String) {
        Intent("com.volvocars.wearable_monitor.service.NotificationService").also {
            it.setClass(context, WearableMonitorService::class.java)
            it.action = action
            context.startForegroundService(it)
        }
    }

    companion object {
        val TAG = BootCompleteReceiver::class.simpleName
    }
}