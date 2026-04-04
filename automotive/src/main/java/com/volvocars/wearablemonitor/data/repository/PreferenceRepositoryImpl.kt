package com.volvocars.wearablemonitor.data.repository

import android.util.Log
import com.volvocars.wearablemonitor.data.storage.SharedPreferenceStorage
import com.volvocars.wearablemonitor.domain.model.ServerStatus
import com.volvocars.wearablemonitor.domain.repository.PreferenceRepository
import com.volvocars.wearablemonitor.domain.util.ThresholdUnit
import javax.inject.Inject

class PreferenceRepositoryImpl @Inject constructor(
    private val sharedPreferenceStorage: SharedPreferenceStorage,
) : PreferenceRepository {
    override fun setPreferenceFromServerStatus(serverStatus: ServerStatus) {
        sharedPreferenceStorage.setPreferenceFromServerStatus(serverStatus)
    }

    override fun getThreshold(unit: ThresholdUnit): Long = when (unit) {
        ThresholdUnit.LOW -> sharedPreferenceStorage.getThresholdLow()
        ThresholdUnit.HIGH -> sharedPreferenceStorage.getThresholdHigh()
        ThresholdUnit.TARGET_LOW -> sharedPreferenceStorage.getThresholdTargetLow()
        ThresholdUnit.TARGET_HIGH -> sharedPreferenceStorage.getThresholdTargetHigh()
    }

    override fun setThresholdValue(thresholdName: String, thresholdValue: Long) {
        Log.d(TAG, "setThresholdValue: $thresholdName $thresholdValue")
        sharedPreferenceStorage.setThresholdValue(thresholdName, thresholdValue)
    }

    override fun getGlucoseFetchInterval(): Int = sharedPreferenceStorage.getGlucoseFetchInterval()

    override fun setGlucoseFetchInterval(interval: Int) {
        sharedPreferenceStorage.setGlucoseFetchInterval(interval)
    }

    override fun getBaseUrl(): String = sharedPreferenceStorage.getBaseUrl()

    override fun setBaseUrl(url: String) {
        sharedPreferenceStorage.setBaseUrl(url)
    }

    override fun getTimeFormat(): Long = sharedPreferenceStorage.getTimeFormat()

    override fun setTimeFormat(timeFormat: Long) {
        sharedPreferenceStorage.setTimeFormat(timeFormat)
    }

    override fun getUnit(): String = sharedPreferenceStorage.getUnit()

    override fun getCriticalNotificationInterval(): Long = sharedPreferenceStorage.getCriticalNotificationInterval()

    override fun setCriticalNotificationInterval(interval: Long) {
        sharedPreferenceStorage.setCriticalNotificationInterval(interval)
    }

    override fun isGlucoseNotificationEnabled(): Boolean = sharedPreferenceStorage.getGlucoseNotificationEnabled()

    override fun isGlucoseAlarmLowEnabled(): Boolean = sharedPreferenceStorage.getGlucoseAlarmLowEnabled()

    override fun isUserSignedIn(): Boolean = sharedPreferenceStorage.userSignedIn()

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
