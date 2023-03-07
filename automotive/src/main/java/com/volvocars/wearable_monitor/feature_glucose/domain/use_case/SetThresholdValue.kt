package com.volvocars.wearable_monitor.feature_glucose.domain.use_case

import com.volvocars.wearable_monitor.feature_glucose.domain.repository.PreferenceRepository
import javax.inject.Inject

class SetThresholdValue @Inject constructor(
    private val repository: PreferenceRepository
) {
    operator fun invoke(thresholdName: String, thresholdValue: Long) {
        repository.setThresholdValue(thresholdName, thresholdValue)
    }
}