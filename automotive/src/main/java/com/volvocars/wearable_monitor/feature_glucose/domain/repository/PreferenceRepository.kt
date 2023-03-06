package com.volvocars.wearable_monitor.feature_glucose.domain.repository

import com.volvocars.wearable_monitor.feature_glucose.domain.use_case.SetThresholdValue
import com.volvocars.wearable_monitor.feature_glucose.domain.util.ThresholdUnit

interface PreferenceRepository {
    fun getThreshold(unit: ThresholdUnit): Long
    fun setThresholdValue(thresholdName: String, thresholdValue: Long)

    fun getGlucoseFetchInterval(): Int
    fun setGlucoseFetchInterval(interval: Int)
    fun getBaseUrl(): String
    fun getTimeFormat(): Long
    fun setTimeFormat(timeFormat: Long)
    fun getUnit(): String
    fun setUnit(unit: String)
    fun getCriticalNotificationInterval(): Long
    fun isGlucoseNotificationEnabled(): Boolean
    fun isGlucoseAlarmLowEnabled(): Boolean
    fun clearData()
}