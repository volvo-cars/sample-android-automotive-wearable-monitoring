package com.volvocars.wearablemonitor.domain.repository

import com.volvocars.wearablemonitor.domain.model.ServerStatus
import com.volvocars.wearablemonitor.domain.util.ThresholdUnit

interface PreferenceRepository {
    fun setPreferenceFromServerStatus(serverStatus: ServerStatus)
    fun getThreshold(unit: ThresholdUnit): Long
    fun setThresholdValue(thresholdName: String, thresholdValue: Long)
    fun getGlucoseFetchInterval(): Int
    fun setGlucoseFetchInterval(interval: Int)
    fun getBaseUrl(): String
    fun setBaseUrl(url: String)
    fun getTimeFormat(): Long
    fun setTimeFormat(timeFormat: Long)
    fun getUnit(): String
    fun setUnit(unit: String)
    fun getCriticalNotificationInterval(): Long
    fun setCriticalNotificationInterval(interval: Long)
    fun isGlucoseNotificationEnabled(): Boolean
    fun isGlucoseAlarmLowEnabled(): Boolean
    fun isUserSignedIn(): Boolean
    fun setUserSignedIn(isUserSignedIn: Boolean)
    fun clearData()
}