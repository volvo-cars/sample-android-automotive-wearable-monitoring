package com.volvocars.wearable_monitor.feature_glucose.domain.repository

import com.volvocars.wearable_monitor.feature_glucose.domain.util.ThresholdUnit

interface PreferenceRepository {
    fun getThreshold(unit: ThresholdUnit): Long
    fun getGlucoseFetchInterval(): Int
    fun getBaseUrl(): String
    fun getTimeFormat(): Long
    fun getUnit(): String
    fun getCriticalNotificationInterval(): Long
    fun isGlucoseNotificationEnabled(): Boolean
    fun isGlucoseAlarmLowEnabled(): Boolean
}