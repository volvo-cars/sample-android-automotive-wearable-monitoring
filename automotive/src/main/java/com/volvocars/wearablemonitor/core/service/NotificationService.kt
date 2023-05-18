package com.volvocars.wearablemonitor.core.service

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
import com.volvocars.wearablemonitor.R
import com.volvocars.wearablemonitor.core.util.Constants.ACTION_REQUIRE_CONFIGURATION
import com.volvocars.wearablemonitor.core.util.Constants.ACTION_REQUIRE_NETWORK
import com.volvocars.wearablemonitor.core.util.Constants.ACTION_SHOW_GLUCOSE_VALUES
import com.volvocars.wearablemonitor.core.util.Constants.KEY_CRITICAL_NOTIFICATION
import com.volvocars.wearablemonitor.core.util.Constants.KEY_IMPORTANCE_DEFAULT
import com.volvocars.wearablemonitor.core.util.Constants.KEY_IMPORTANCE_HIGH
import com.volvocars.wearablemonitor.core.util.Constants.KEY_INFORMATION_NOTIFICATION
import com.volvocars.wearablemonitor.core.util.Constants.SHOW_CRITICAL_NOTIFICATION_ID
import com.volvocars.wearablemonitor.core.util.Constants.SHOW_INFO_NOTIFICATION_ID
import com.volvocars.wearablemonitor.domain.usecase.GetCriticalNotificationInterval
import com.volvocars.wearablemonitor.domain.usecase.GetThresholds
import com.volvocars.wearablemonitor.domain.usecase.IsGlucoseAlarmLowEnabled
import com.volvocars.wearablemonitor.domain.usecase.IsGlucoseNotificationEnabled
import com.volvocars.wearablemonitor.domain.usecase.IsUnitMmol
import com.volvocars.wearablemonitor.domain.usecase.ObserveCachedGlucoseValues
import com.volvocars.wearablemonitor.presentation.mapper.toPresentationModels
import com.volvocars.wearablemonitor.presentation.model.GlucoseSummary
import com.volvocars.wearablemonitor.presentation.util.calculateDifference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class NotificationService : LifecycleService() {

    private lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var observeGlucoseValues: ObserveCachedGlucoseValues

    @Inject
    lateinit var isGlucoseNotificationEnabled: IsGlucoseNotificationEnabled

    @Inject
    lateinit var isGlucoseAlarmLowEnabled: IsGlucoseAlarmLowEnabled

    @Inject
    lateinit var getCriticalNotificationInterval: GetCriticalNotificationInterval

    @Inject
    lateinit var getThresholds: GetThresholds

    @Inject
    lateinit var isUnitMmol: IsUnitMmol

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

    private val _glucoseValues = MutableSharedFlow<List<GlucoseSummary>>()
    private val glucoseValues = _glucoseValues.asSharedFlow()

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        notificationManager.createNotificationChannels(
            listOf(
                infoNotificationChannel,
                criticalNotificationChannel
            )
        )

        observeGlucoseValues()
        checkConnectivity()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        handleIntentAction(intent)

        return START_STICKY
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
            _glucoseValues.emit(glucoseList.toPresentationModels())
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
        latestGlucoseValue: GlucoseSummary,
        currentGlucoseValue: GlucoseSummary,
    ) {
        val diff = calculateDifference(
            currentGlucoseValue.sgv,
            latestGlucoseValue.sgv,
            isUnitMmol()
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
        currentGlucoseValue: GlucoseSummary
    ) {
        val threshold = getThresholds()

        if (currentGlucoseValue.isOutOfRange(threshold)) {
            val timeSince = lastCriticalUpdate.timeSince()
            val criticalNotificationInterval = getCriticalNotificationInterval.invoke()
            val interval = TimeUnit.MINUTES.toMillis(criticalNotificationInterval)

            Log.d(TAG, "buildCriticalNotification: $timeSince $interval")
            if (timeSince > interval) {
                val glucoseValueText = getGlucoseLevelText(currentGlucoseValue)
                val unit = getUnit()

                val (criticalContentTitle, criticalContentText) = when {
                    currentGlucoseValue.sgv <= threshold.bgLow -> {
                        Pair(
                            getString(R.string.alarm_low_notification_title),
                            getString(
                                R.string.alarm_low_notification_text,
                                glucoseValueText,
                                unit
                            ),
                        )
                    }

                    currentGlucoseValue.sgv >= threshold.bgHigh -> {
                        Pair(
                            getString(R.string.alarm_high_notification_title),
                            getString(
                                R.string.alarm_high_notification_text,
                                glucoseValueText, unit
                            )
                        )
                    }

                    else -> {
                        Pair("", "")
                    }
                }


                lastCriticalUpdate = System.currentTimeMillis()
                showCriticalNotification(criticalContentTitle, criticalContentText)
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

    private fun getGlucoseLevelText(currentGlucoseValue: GlucoseSummary): String {
        return if (isUnitMmol()) {
            currentGlucoseValue.sgvMmol.toString()
        } else {
            currentGlucoseValue.sgvUnit.toString()
        }
    }

    private fun getUnit(): String {
        val unitEntries = resources.getStringArray(R.array.unit_entries)
        return if (isUnitMmol()) unitEntries[0] else unitEntries[1]
    }

    private fun GlucoseSummary.isOutOfRange(threshold: com.volvocars.wearablemonitor.domain.model.Thresholds): Boolean {
        return sgv >= threshold.bgHigh || sgv <= threshold.bgLow
    }

    companion object {
        val TAG = NotificationService::class.simpleName
    }
}