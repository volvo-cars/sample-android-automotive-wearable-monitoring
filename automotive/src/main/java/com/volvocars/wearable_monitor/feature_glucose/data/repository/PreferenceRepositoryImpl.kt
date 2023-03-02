package com.volvocars.wearable_monitor.feature_glucose.data.repository

import com.volvocars.wearable_monitor.feature_glucose.data.storage.SharedPreferenceStorage
import com.volvocars.wearable_monitor.feature_glucose.domain.repository.PreferenceRepository
import com.volvocars.wearable_monitor.feature_glucose.domain.util.ThresholdUnit
import javax.inject.Inject

class PreferenceRepositoryImpl @Inject constructor(
    private val sharedPreferenceStorage: SharedPreferenceStorage
) : PreferenceRepository {
    override fun getThreshold(unit: ThresholdUnit): Long {
        return when (unit) {
            ThresholdUnit.LOW -> sharedPreferenceStorage.getThresholdLow()
            ThresholdUnit.HIGH -> sharedPreferenceStorage.getThresholdHigh()
            ThresholdUnit.TARGET_LOW -> sharedPreferenceStorage.getThresholdTargetLow()
            ThresholdUnit.TARGET_HIGH -> sharedPreferenceStorage.getThresholdTargetHigh()
        }
    }

    override fun getGlucoseFetchInterval(): Int {
        return sharedPreferenceStorage.getGlucoseFetchInterval()
    }

    override fun getBaseUrl(): String {
        return sharedPreferenceStorage.getBaseUrl()
    }

    override fun getTimeFormat(): Long {
        return sharedPreferenceStorage.getTimeFormat()
    }
}