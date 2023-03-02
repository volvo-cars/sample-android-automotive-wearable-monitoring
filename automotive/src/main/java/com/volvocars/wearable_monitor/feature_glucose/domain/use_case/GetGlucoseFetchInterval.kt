package com.volvocars.wearable_monitor.feature_glucose.domain.use_case

import com.volvocars.wearable_monitor.feature_glucose.domain.repository.PreferenceRepository
import javax.inject.Inject

class GetGlucoseFetchInterval @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) {
    operator fun invoke(): Int {
        return preferenceRepository.getGlucoseFetchInterval()
    }
}