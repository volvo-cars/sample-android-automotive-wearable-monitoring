package com.volvocars.diabetesmonitor.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.volvocars.diabetesmonitor.R
import com.volvocars.diabetesmonitor.core.util.Constants.ACTION_REQUIRE_CONFIGURATION
import com.volvocars.diabetesmonitor.core.util.Constants.ACTION_REQUIRE_NETWORK
import com.volvocars.diabetesmonitor.core.util.Constants.ACTION_SHOW_GLUCOSE_VALUES
import com.volvocars.diabetesmonitor.core.util.Constants.KEY_CRITICAL_NOTIFICATION
import com.volvocars.diabetesmonitor.core.util.Constants.KEY_IMPORTANCE_DEFAULT
import com.volvocars.diabetesmonitor.core.util.Constants.KEY_IMPORTANCE_HIGH
import com.volvocars.diabetesmonitor.core.util.Constants.KEY_INFORMATION_NOTIFICATION
import com.volvocars.diabetesmonitor.core.util.Constants.SHOW_CRITICAL_NOTIFICATION_ID
import com.volvocars.diabetesmonitor.core.util.Constants.SHOW_INFO_NOTIFICATION_ID
import com.volvocars.diabetesmonitor.core.util.GlucoseUtils
import com.volvocars.diabetesmonitor.feature_glucose.data.storage.SharedPreferenceStorage
import com.volvocars.diabetesmonitor.feature_glucose.domain.model.Glucose
import com.volvocars.diabetesmonitor.feature_glucose.domain.use_case.ObserveCachedGlucoseValues
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class NotificationService : LifecycleService() {

    private lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var preferenceStorage: SharedPreferenceStorage

    @Inject
    lateinit var observeGlucoseValues: ObserveCachedGlucoseValues

    @Inject
    lateinit var glucoseUtils: GlucoseUtils

    @Inject
    @Named(KEY_CRITICAL_NOTIFICATION)
    lateinit var criticalNotification: NotificationCompat.Builder

    @Inject
    @Named(KEY_INFORMATION_NOTIFICATION)
    lateinit var defaultNotification: NotificationCompat.Builder

    @Inject
    @Named(KEY_IMPORTANCE_HIGH)
    lateinit var criticalNotificationChannel: NotificationChannel

    @Inject
    @Named(KEY_IMPORTANCE_DEFAULT)
    lateinit var infoNotificationChannel: NotificationChannel

    private val _glucoseValues = MutableSharedFlow<List<Glucose>>()
    private val glucoseValues = _glucoseValues.asSharedFlow()

    override fun onCreate() {
        super.onCreate()
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(infoNotificationChannel)
        notificationManager.createNotificationChannel(criticalNotificationChannel)

        lifecycleScope.launch {
            observeGlucoseValues.invoke(2).collect { glucoseList ->
                _glucoseValues.emit(glucoseList)
            }
        }

        checkConnectivity()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: intent - $intent, flags - $flags, startId - $startId ")

        intent?.let {
            when (it.action) {
                ACTION_REQUIRE_CONFIGURATION -> {
                    val contentTitle = getString(R.string.configurationsRequired_title)
                    val contentText = getString(R.string.configurationsRequired_text)
                    showInfoNotification(contentTitle, contentText)
                }
                ACTION_REQUIRE_NETWORK -> {
                    showMissingNetworkNotification()
                }
                ACTION_SHOW_GLUCOSE_VALUES -> {
                    observeGlucose()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    /**
     * Show info notification
     *
     * Could be configuration required, missing network or glucose values
     *
     * @param contentTitle Notification title
     * @param contentText Notification text
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showInfoNotification(contentTitle: String, contentText: String) {
        if (preferenceStorage.getGlucoseNotificationEnabled()) {
            defaultNotification.setContentTitle(contentTitle)
            defaultNotification.setContentText(contentText)
            startForeground(SHOW_INFO_NOTIFICATION_ID, defaultNotification.build())
        } else {
            stopForeground(true)
            notificationManager.cancel(SHOW_INFO_NOTIFICATION_ID)
        }
    }

    /**
     * Show info notification about missing network connection
     */
    fun showMissingNetworkNotification() {
        val contentTitle = getString(R.string.missing_network_title)
        val contentText = getString(R.string.missing_network_text)
        showInfoNotification(contentTitle, contentText)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun showCriticalNotification(contentText: String) {
        // If not critical notification is enabled, cancel current notification and return
        if (!preferenceStorage.getGlucoseAlarmLowEnabled()) {
            notificationManager.cancel(SHOW_CRITICAL_NOTIFICATION_ID)
            return
        }

        criticalNotification.setContentText(contentText)
        notificationManager.notify(
            SHOW_CRITICAL_NOTIFICATION_ID,
            criticalNotification.build()
        )
    }

    /**
     *  Check if the device has an internet connection or not
     */
    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    private fun checkConnectivity() {
        val connectivityManager =
            this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                notificationManager.cancel(SHOW_INFO_NOTIFICATION_ID)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                showMissingNetworkNotification()
            }
        })
    }

    private fun observeGlucose() {
        lifecycleScope.launch {
            glucoseValues.collectLatest { glucoseList ->

                if (glucoseList.isEmpty()) {
                    return@collectLatest
                }

                val (latestGlucoseValue, currentGlucoseValue) = glucoseList.take(2)

                val diff = glucoseUtils.calculateDifference(
                    currentGlucoseValue.sgv,
                    latestGlucoseValue.sgv
                )

                val unitEntries = resources.getStringArray(R.array.unit_entries)
                val unit = if (glucoseUtils.checkIsMmol()) unitEntries[0] else unitEntries[1]
                val glucoseValueText =
                    if (glucoseUtils.checkIsMmol()) currentGlucoseValue.sgvMmol.toString() else currentGlucoseValue.sgvUnit.toString()

                val contentTitle = getString(
                    R.string.notification_info_title,
                    glucoseValueText,
                    currentGlucoseValue.direction
                )
                val contentText = getString(
                    R.string.notification_info_text, diff, unit
                )

                showInfoNotification(contentTitle, contentText)

                if (glucoseValueBecomingOutOfRange(currentGlucoseValue)) {
                    val criticalContentText =
                        getString(R.string.alarmLow_notification_text, glucoseValueText, unit)

                    showCriticalNotification(criticalContentText)
                } else {
                    // If the glucose values isn't outside the thresholds,
                    // cancel the potentially current notification
                    notificationManager.cancel(SHOW_CRITICAL_NOTIFICATION_ID)
                }
            }
        }
    }

    /**
     * Check if the provided glucose is out of range
     * @param glucose [Glucose] value
     * @return true if the glucose value is out of range otherwise false
     */
    private fun glucoseValueBecomingOutOfRange(glucose: Glucose): Boolean {
        return glucose.sgv >= preferenceStorage.getThresholdHigh() || glucose.sgv <= preferenceStorage.getThresholdLow()
    }

    companion object {
        val TAG = NotificationService::class.simpleName
    }
}