package com.volvocars.wearable_monitor.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.volvocars.wearable_monitor.R
import com.volvocars.wearable_monitor.core.util.Constants.ACTION_REQUIRE_CONFIGURATION
import com.volvocars.wearable_monitor.core.util.Constants.ACTION_REQUIRE_NETWORK
import com.volvocars.wearable_monitor.core.util.Constants.ACTION_SHOW_GLUCOSE_VALUES
import com.volvocars.wearable_monitor.core.util.Constants.KEY_CRITICAL_NOTIFICATION
import com.volvocars.wearable_monitor.core.util.Constants.KEY_IMPORTANCE_DEFAULT
import com.volvocars.wearable_monitor.core.util.Constants.KEY_IMPORTANCE_HIGH
import com.volvocars.wearable_monitor.core.util.Constants.KEY_INFORMATION_NOTIFICATION
import com.volvocars.wearable_monitor.core.util.Constants.SHOW_CRITICAL_NOTIFICATION_ID
import com.volvocars.wearable_monitor.core.util.Constants.SHOW_INFO_NOTIFICATION_ID
import com.volvocars.wearable_monitor.core.util.GlucoseUtils
import com.volvocars.wearable_monitor.feature_glucose.domain.model.Glucose
import com.volvocars.wearable_monitor.feature_glucose.domain.model.Thresholds
import com.volvocars.wearable_monitor.feature_glucose.domain.use_case.GetCriticalNotificationInterval
import com.volvocars.wearable_monitor.feature_glucose.domain.use_case.GetThresholds
import com.volvocars.wearable_monitor.feature_glucose.domain.use_case.IsGlucoseAlarmLowEnabled
import com.volvocars.wearable_monitor.feature_glucose.domain.use_case.IsGlucoseNotificationEnabled
import com.volvocars.wearable_monitor.feature_glucose.domain.use_case.ObserveCachedGlucoseValues
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class NotificationService : LifecycleService() {

    private lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var observeGlucoseValues: ObserveCachedGlucoseValues

    @Inject
    lateinit var glucoseUtils: GlucoseUtils

    @Inject
    lateinit var isGlucoseNotificationEnabled: IsGlucoseNotificationEnabled

    @Inject
    lateinit var isGlucoseAlarmLowEnabled: IsGlucoseAlarmLowEnabled

    @Inject
    lateinit var getCriticalNotificationInterval: GetCriticalNotificationInterval

    @Inject
    lateinit var getThresholds: GetThresholds

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

    private var lastCriticalUpdate = 0L
    private var criticalGlucoseTimer: TimerTask? = null

    private val _glucoseValues = MutableSharedFlow<List<Glucose>>()
    private val glucoseValues = _glucoseValues.asSharedFlow()

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        notificationManager.createNotificationChannel(infoNotificationChannel)
        notificationManager.createNotificationChannel(criticalNotificationChannel)

        observeGlucoseValues()
        checkConnectivity()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: intent - $intent, flags - $flags, startId - $startId ")
        handleIntentAction(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun handleIntentAction(intent: Intent?) {
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

                else -> {}
            }
        }
    }

    private fun observeGlucoseValues() = lifecycleScope.launch {
        observeGlucoseValues.invoke(2).collectLatest { glucoseList ->
            _glucoseValues.emit(glucoseList)
        }
    }

    /**
     * Show info notification
     *
     * Could be configuration required, missing network or glucose values
     *
     * @param contentTitle Notification title
     * @param contentText Notification text
     */
    private fun showInfoNotification(contentTitle: String, contentText: String) {
        if (isGlucoseNotificationEnabled()) {
            defaultNotification.setContentTitle(contentTitle)
            defaultNotification.setContentText(contentText)
            startForeground(SHOW_INFO_NOTIFICATION_ID, defaultNotification.build())
        } else {
            stopForeground(STOP_FOREGROUND_REMOVE)
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

    /**
     * Show critical notification when values becomes out of threshold range
     */
    private fun showCriticalNotification(contentTitle: String, contentText: String) {
        // If not critical notification is enabled, cancel current notification and return
        if (!isGlucoseAlarmLowEnabled()) {
            notificationManager.cancel(SHOW_CRITICAL_NOTIFICATION_ID)
            return
        }

        criticalNotification.setContentTitle(contentTitle)
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
        val connectivityManager = getSystemService(
            CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        connectivityManager.registerDefaultNetworkCallback(
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    notificationManager.cancel(SHOW_INFO_NOTIFICATION_ID)
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    showMissingNetworkNotification()
                }
            }
        )
    }

    private fun observeGlucose() = lifecycleScope.launch {
        glucoseValues.collectLatest { glucoseList ->
            if (glucoseList.isNotEmpty()) {
                val (latestGlucoseValue, currentGlucoseValue) = glucoseList.take(2)
                buildInfoNotification(latestGlucoseValue, currentGlucoseValue)
                buildCriticalNotification(currentGlucoseValue)
            }
        }
    }

    private fun buildInfoNotification(
        latestGlucoseValue: Glucose,
        currentGlucoseValue: Glucose
    ) {
        val diff = glucoseUtils.calculateDifference(
            currentGlucoseValue.sgv,
            latestGlucoseValue.sgv
        )

        val unit = getUnit()
        val glucoseValueText = getGlucoseLevelText(currentGlucoseValue)

        val contentTitle = getString(
            R.string.notification_info_title,
            glucoseValueText,
            currentGlucoseValue.direction
        )
        val contentText = getString(
            R.string.notification_info_text, diff, unit
        )

        showInfoNotification(contentTitle, contentText)
    }

    private fun buildCriticalNotification(
        currentGlucoseValue: Glucose
    ) {
        val threshold = getThresholds()

        if (currentGlucoseValue.isOutOfRange(threshold)) {
            if (lastCriticalUpdate.timeSince() > getCriticalNotificationInterval()) {

                val glucoseValueText = getGlucoseLevelText(currentGlucoseValue)
                val unit = getUnit()

                val criticalContentTitle = when {
                    currentGlucoseValue.sgv <= threshold.bgLow -> {
                        getString(R.string.alarm_low_notification_title)
                    }

                    currentGlucoseValue.sgv >= threshold.bgHigh -> {
                        getString(R.string.alarm_high_notification_title)
                    }

                    else -> {
                        ""
                    }
                }

                val criticalContentText = when {
                    currentGlucoseValue.sgv <= threshold.bgTargetTop -> {
                        getString(
                            R.string.alarm_low_notification_text,
                            glucoseValueText,
                            unit
                        )
                    }

                    currentGlucoseValue.sgv >= threshold.bgTargetBottom -> {
                        getString(
                            R.string.alarm_high_notification_text,
                            glucoseValueText, unit
                        )
                    }

                    else -> {
                        ""
                    }
                }

                showCriticalNotification(criticalContentTitle, criticalContentText)
                lastCriticalUpdate = System.currentTimeMillis()

            }
        } else {
            // If the glucose values isn't outside the thresholds,
            // cancel the potentially current notification
            criticalGlucoseTimer?.cancel()
            criticalGlucoseTimer = null
            notificationManager.cancel(SHOW_CRITICAL_NOTIFICATION_ID)
        }
    }

    private fun Long.timeSince(): Long {
        return System.currentTimeMillis() - this
    }

    private fun getGlucoseLevelText(currentGlucoseValue: Glucose): String {
        return if (glucoseUtils.checkIsMmol()) {
            currentGlucoseValue.sgvMmol.toString()
        } else {
            currentGlucoseValue.sgvUnit.toString()
        }
    }

    private fun getUnit(): String {
        val unitEntries = resources.getStringArray(R.array.unit_entries)
        return if (glucoseUtils.checkIsMmol()) unitEntries[0] else unitEntries[1]
    }

    private fun Glucose.isOutOfRange(threshold: Thresholds): Boolean {
        return sgv >= threshold.bgHigh || sgv <= threshold.bgLow
    }

    companion object {
        val TAG = NotificationService::class.simpleName
    }
}