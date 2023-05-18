package com.volvocars.wearablemonitor.core.service

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.volvocars.wearablemonitor.R
import com.volvocars.wearablemonitor.core.util.NotificationConstants.ACTION_REQUIRE_CONFIGURATION
import com.volvocars.wearablemonitor.core.util.NotificationConstants.ACTION_REQUIRE_NETWORK
import com.volvocars.wearablemonitor.core.util.NotificationConstants.ACTION_SHOW_GLUCOSE_VALUES
import com.volvocars.wearablemonitor.domain.usecase.GetCriticalNotificationInterval
import com.volvocars.wearablemonitor.domain.usecase.GetThresholds
import com.volvocars.wearablemonitor.domain.usecase.IsGlucoseAlarmLowEnabled
import com.volvocars.wearablemonitor.domain.usecase.IsGlucoseNotificationEnabled
import com.volvocars.wearablemonitor.domain.usecase.IsUnitMmol
import com.volvocars.wearablemonitor.domain.usecase.ObserveCachedGlucoseValues
import com.volvocars.wearablemonitor.presentation.mapper.toPresentationModels
import com.volvocars.wearablemonitor.presentation.model.GlucoseSummary
import com.volvocars.wearablemonitor.presentation.util.calculateDifference
import com.volvocars.wearablemonitor.presentation.util.isOutOfRange
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class WearableMonitorService : LifecycleService() {

    @Inject
    lateinit var notificationHelper: ServiceNotificationHelper

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

    private var lastCriticalUpdate = 0L
    private var criticalGlucoseTimer: TimerTask? = null

    private val _glucoseValues = MutableSharedFlow<List<GlucoseSummary>>()
    private val glucoseValues = _glucoseValues.asSharedFlow()

    override fun onCreate() {
        super.onCreate()
        notificationHelper.showForegroundServiceNotification().also {
            startForeground(it.channelId.toInt(), it)
        }
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
                ACTION_REQUIRE_CONFIGURATION -> notificationHelper.showRequireConfigurationNotification()
                ACTION_REQUIRE_NETWORK -> notificationHelper.showMissingNetworkNotification()
                ACTION_SHOW_GLUCOSE_VALUES -> observeGlucose()
                else -> {
                    /** no-op **/
                }
            }
        }
    }

    private fun observeGlucoseValues() = lifecycleScope.launch {
        observeGlucoseValues.invoke(2).collectLatest { glucoseList ->
            _glucoseValues.emit(glucoseList.toPresentationModels())
        }
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
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
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

        notificationHelper.showGlucoseInfoNotification(
            glucoseValueText,
            diff,
            unit,
            currentGlucoseValue.direction
        )
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

                when {
                    currentGlucoseValue.sgv <= threshold.bgLow -> {
                        notificationHelper.showGlucoseCriticalLowNotification(
                            glucoseValueText,
                            unit
                        )
                    }

                    currentGlucoseValue.sgv >= threshold.bgHigh -> {
                        notificationHelper.showGlucoseCriticalHighNotification(
                            glucoseValueText,
                            unit
                        )
                    }

                    else -> {
                        notificationHelper.hideGlucoseCriticalNotification()
                    }
                }

                lastCriticalUpdate = System.currentTimeMillis()
            }
        } else {
            // If the glucose values isn't outside the thresholds,
            // cancel the potentially current notification
            criticalGlucoseTimer?.cancel()
            criticalGlucoseTimer = null
            notificationHelper.hideGlucoseCriticalNotification()
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

    companion object {
        val TAG = WearableMonitorService::class.simpleName

        fun create(context: Context, action: String? = null): Intent {
            return Intent(context, WearableMonitorService::class.java).apply {
                setAction(action)
            }
        }
    }
}