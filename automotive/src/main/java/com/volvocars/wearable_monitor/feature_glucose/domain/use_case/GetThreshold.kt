package com.volvocars.wearable_monitor.feature_glucose.domain.use_case

import com.volvocars.wearable_monitor.feature_glucose.domain.repository.PreferenceRepository
import com.volvocars.wearable_monitor.feature_glucose.domain.util.ThresholdUnit
import javax.inject.Inject

class GetThreshold @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) {
    operator fun invoke(thresholdUnit: ThresholdUnit): Long {
        return preferenceRepository.getThreshold(thresholdUnit)
    }
}