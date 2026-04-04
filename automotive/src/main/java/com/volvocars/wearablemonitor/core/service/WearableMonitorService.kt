package com.volvocars.wearablemonitor.core.service

import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.volvocars.wearablemonitor.R
import com.volvocars.wearablemonitor.core.util.NotificationConstants.ACTION_REQUIRE_CONFIGURATION
import com.volvocars.wearablemonitor.core.util.NotificationConstants.ACTION_REQUIRE_NETWORK
import com.volvocars.wearablemonitor.core.util.NotificationConstants.ACTION_SHOW_GLUCOSE_VALUES
import com.volvocars.wearablemonitor.domain.usecase.GetCriticalNotificationInterval
import com.volvocars.wearablemonitor.domain.usecase.GetThresholds
import com.volvocars.wearablemonitor.domain.usecase.IsUnitMmol
import com.volvocars.wearablemonitor.domain.usecase.ObserveCachedGlucoseValues
import com.volvocars.wearablemonitor.presentation.mapper.toPresentationModels
import com.volvocars.wearablemonitor.presentation.model.GlucoseSummary
import com.volvocars.wearablemonitor.presentation.util.calculateDifference
import com.volvocars.wearablemonitor.presentation.util.isOutOfRange
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WearableMonitorService : LifecycleService() {

    @Inject
    lateinit var notificationHelper: ServiceNotificationHelper

    @Inject
    lateinit var observeGlucoseValues: ObserveCachedGlucoseValues

    @Inject
    lateinit var getCriticalNotificationInterval: GetCriticalNotificationInterval

    @Inject
    lateinit var getThresholds: GetThresholds

    @Inject
    lateinit var isUnitMmol: IsUnitMmol

    private var lastCriticalUpdate = 0L

    /**
     * Holds the two most-recent readings so both the info and critical notification
     * logic share the same snapshot without an extra coroutine hop.
     */
    private val _latestReadings = MutableStateFlow<List<GlucoseSummary>>(emptyList())
    private val latestReadings = _latestReadings.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        val notification = notificationHelper.showForegroundServiceNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                notification.channelId.toInt(),
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(notification.channelId.toInt(), notification)
        }
        collectGlucoseReadings()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        handleIntentAction(intent)
        return START_STICKY
    }

    private fun handleIntentAction(intent: Intent?) {
        when (intent?.action) {
            ACTION_REQUIRE_CONFIGURATION -> notificationHelper.showRequireConfigurationNotification()
            ACTION_REQUIRE_NETWORK -> notificationHelper.showMissingNetworkNotification()
            ACTION_SHOW_GLUCOSE_VALUES -> startNotificationUpdates()
            else -> { /* no-op */ }
        }
    }

    /**
     * Observes the DB flow and keeps [latestReadings] up to date.
     * Started once in [onCreate] so readings are always fresh regardless of intent action.
     */
    private fun collectGlucoseReadings() = lifecycleScope.launch {
        observeGlucoseValues.invoke(2).collectLatest { glucoseList ->
            _latestReadings.value = glucoseList.toPresentationModels()
        }
    }

    /**
     * Subscribes to [latestReadings] and rebuilds notifications on every new emission.
     * Called when [ACTION_SHOW_GLUCOSE_VALUES] is received.
     */
    private fun startNotificationUpdates() = lifecycleScope.launch {
        latestReadings.collectLatest { readings ->
            if (readings.size < 2) return@collectLatest
            val (previousReading, currentReading) = readings
            val unitMmol = isUnitMmol()
            buildInfoNotification(previousReading, currentReading, unitMmol)
            buildCriticalNotification(currentReading)
        }
    }

    private fun buildInfoNotification(
        previousReading: GlucoseSummary,
        currentReading: GlucoseSummary,
        isUnitMmol: Boolean,
    ) {
        val diff = calculateDifference(currentReading.sgv, previousReading.sgv, isUnitMmol)
        val unit = getUnitLabel(isUnitMmol)
        val glucoseText = formatGlucoseValue(currentReading, isUnitMmol)

        notificationHelper.showGlucoseInfoNotification(glucoseText, diff, unit, currentReading.direction)
    }

    private fun buildCriticalNotification(currentReading: GlucoseSummary) {
        val threshold = getThresholds()
        if (!currentReading.isOutOfRange(threshold)) {
            notificationHelper.hideGlucoseCriticalNotification()
            return
        }

        val elapsed = System.currentTimeMillis() - lastCriticalUpdate
        val intervalMillis = TimeUnit.MINUTES.toMillis(getCriticalNotificationInterval.invoke())
        Log.d(TAG, "buildCriticalNotification: elapsed=$elapsed intervalMillis=$intervalMillis")

        if (elapsed <= intervalMillis) return

        val isUnitMmol = isUnitMmol()
        val glucoseText = formatGlucoseValue(currentReading, isUnitMmol)
        val unit = getUnitLabel(isUnitMmol)

        when {
            currentReading.sgv <= threshold.bgLow ->
                notificationHelper.showGlucoseCriticalLowNotification(glucoseText, unit)

            currentReading.sgv >= threshold.bgHigh ->
                notificationHelper.showGlucoseCriticalHighNotification(glucoseText, unit)

            else -> notificationHelper.hideGlucoseCriticalNotification()
        }

        lastCriticalUpdate = System.currentTimeMillis()
    }

    private fun formatGlucoseValue(reading: GlucoseSummary, isUnitMmol: Boolean): String = if (isUnitMmol) reading.sgvMmol.toString() else reading.sgvUnit.toString()

    private fun getUnitLabel(isUnitMmol: Boolean): String {
        val entries = resources.getStringArray(R.array.unit_entries)
        return if (isUnitMmol) entries[0] else entries[1]
    }

    companion object {
        val TAG = WearableMonitorService::class.simpleName

        fun startAsForeground(context: Context, action: String? = null) {
            Intent(context, WearableMonitorService::class.java)
                .apply { setAction(action) }
                .also { context.startForegroundService(it) }
        }
    }
}
