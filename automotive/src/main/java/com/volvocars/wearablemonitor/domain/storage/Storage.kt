package com.volvocars.wearablemonitor.domain.storage

import com.volvocars.wearablemonitor.domain.model.ServerStatus


/**
 * Simple API for add preferences to SharedPreferences
 */
interface Storage {
    fun setPreferenceFromServerStatus(serverStatus: ServerStatus)
    fun setBaseUrl(value: String)
    fun getBaseUrl(): String
    fun getThresholdLow(): Long
    fun setThresholdLow(value: Long)
    fun getThresholdHigh(): Long
    fun getThresholdTargetLow(): Long
    fun getThresholdTargetHigh(): Long
    fun userSignedIn(): Boolean
    fun setUserSignedIn(value: Boolean)
    fun getGlucoseNotificationEnabled(): Boolean
    fun getGlucoseAlarmLowEnabled(): Boolean
    fun getCriticalNotificationIntervalMillis(): Long
    fun setThresholdTargetLow(value: Long)
    fun setThresholdHigh(value: Long)
    fun setThresholdTargetHigh(value: Long)
    fun setGlucoseNotificationEnabled(value: Boolean)
    fun setGlucoseAlarmLowEnabled(value: Boolean)
    fun setUnit(value: String)
    fun getUnit(): String
    fun setTimeFormat(value: Long)
    fun getTimeFormat(): Long
    fun setGlucoseFetchInterval(value: Int)
    fun getGlucoseFetchInterval(): Int
    fun setThresholdValue(threshold: String, value: Long)
    fun setCriticalNotificationInterval(value: Long)
    fun getCriticalNotificationInterval(): Long
    fun clear()
}