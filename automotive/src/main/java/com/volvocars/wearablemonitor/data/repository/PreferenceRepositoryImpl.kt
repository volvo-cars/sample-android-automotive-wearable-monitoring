package com.volvocars.wearablemonitor.data.repository

import android.util.Log
import com.volvocars.wearablemonitor.data.storage.SharedPreferenceStorage
import com.volvocars.wearablemonitor.domain.model.ServerStatus
import com.volvocars.wearablemonitor.domain.repository.PreferenceRepository
import com.volvocars.wearablemonitor.domain.util.ThresholdUnit
import javax.inject.Inject

class PreferenceRepositoryImpl @Inject constructor(
    private val sharedPreferenceStorage: SharedPreferenceStorage
) : PreferenceRepository {
    override fun setPreferenceFromServerStatus(serverStatus: ServerStatus) {
        sharedPreferenceStorage.setPreferenceFromServerStatus(serverStatus)
    }

    override fun getThreshold(unit: ThresholdUnit): Long {
        return when (unit) {
            ThresholdUnit.LOW -> sharedPreferenceStorage.getThresholdLow()
            ThresholdUnit.HIGH -> sharedPreferenceStorage.getThresholdHigh()
            ThresholdUnit.TARGET_LOW -> sharedPreferenceStorage.getThresholdTargetLow()
            ThresholdUnit.TARGET_HIGH -> sharedPreferenceStorage.getThresholdTargetHigh()
        }
    }

    override fun setThresholdValue(thresholdName: String, thresholdValue: Long) {
        Log.d(TAG, "setThresholdValue: $thresholdName $thresholdValue")
        sharedPreferenceStorage.setThresholdValue(thresholdName, thresholdValue)
    }

    override fun getGlucoseFetchInterval(): Int {
        return sharedPreferenceStorage.getGlucoseFetchInterval()
    }

    override fun setGlucoseFetchInterval(interval: Int) {
        sharedPreferenceStorage.setGlucoseFetchInterval(interval)
    }

    override fun getBaseUrl(): String {
        return sharedPreferenceStorage.getBaseUrl()
    }

    override fun setBaseUrl(url: String) {
        sharedPreferenceStorage.setBaseUrl(url)
    }

    override fun getTimeFormat(): Long {
        return sharedPreferenceStorage.getTimeFormat()
    }

    override fun setTimeFormat(timeFormat: Long) {
        sharedPreferenceStorage.setTimeFormat(timeFormat)
    }

    override fun getUnit(): String {
        return sharedPreferenceStorage.getUnit()
    }

    override fun getCriticalNotificationInterval(): Long {
        return sharedPreferenceStorage.getCriticalNotificationInterval()
    }

    override fun setCriticalNotificationInterval(interval: Long) {
        sharedPreferenceStorage.setCriticalNotificationInterval(interval)
    }

    override fun isGlucoseNotificationEnabled(): Boolean {
        return sharedPreferenceStorage.getGlucoseNotificationEnabled()
    }

    override fun isGlucoseAlarmLowEnabled(): Boolean {
        return sharedPreferenceStorage.getGlucoseAlarmLowEnabled()
    }

    override fun isUserSignedIn(): Boolean {
        return sharedPreferenceStorage.userSignedIn()
    }

    override fun setUserSignedIn(isUserSignedIn: Boolean) {
        sharedPreferenceStorage.setUserSignedIn(isUserSignedIn)
    }

    override fun clearData() {
        sharedPreferenceStorage.clear()
    }

    override fun setUnit(unit: String) {
        sharedPreferenceStorage.setUnit(unit)
    }

    companion object {
        private val TAG = PreferenceRepositoryImpl::class.simpleName
    }
}