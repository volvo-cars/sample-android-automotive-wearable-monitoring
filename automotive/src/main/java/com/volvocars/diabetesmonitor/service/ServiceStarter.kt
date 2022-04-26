package com.volvocars.diabetesmonitor.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.volvocars.diabetesmonitor.core.util.Constants
import com.volvocars.diabetesmonitor.feature_glucose.data.storage.SharedPreferenceStorage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ServiceStarter : BroadcastReceiver() {

    @Inject
    lateinit var glucoseFetchWorker: PeriodicWorkRequest

    @Inject
    lateinit var sharedPreferenceStorage: SharedPreferenceStorage

    override fun onReceive(
        context: Context?,
        intent: Intent
    ) {
        if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED) || equals(Intent.ACTION_LOCKED_BOOT_COMPLETED)) {
            context?.let {
                WorkManager.getInstance(it).enqueueUniquePeriodicWork(
                    Constants.GLUCOSE_FETCH_WORK_ID,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    glucoseFetchWorker
                )
                if (sharedPreferenceStorage.userSignedIn()) {
                    if (sharedPreferenceStorage.getGlucoseNotificationEnabled()) {
                        sendCommandToService(it, Constants.ACTION_SHOW_GLUCOSE_VALUES)
                    }
                } else {
                    sendCommandToService(it, Constants.ACTION_REQUIRE_CONFIGURATION)
                }

            }
        }
    }

    private fun sendCommandToService(context: Context, action: String) {
        Intent("com.volvocars.diabetesmonitor.service.NotificationService").also {
            it.setClass(context, NotificationService::class.java)
            it.action = action
            context.startForegroundService(it)
        }
    }

    companion object {
        val TAG = ServiceStarter::class.simpleName
    }
}