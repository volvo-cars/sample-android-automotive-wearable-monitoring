package com.volvocars.wearable_monitor.feature_glucose.domain.use_case

import com.volvocars.wearable_monitor.feature_glucose.domain.repository.PreferenceRepository
import javax.inject.Inject

class SetGlucoseFetchInterval @Inject constructor(
    private val repository: PreferenceRepository
) {
    operator fun invoke(interval: Int) {
        repository.setGlucoseFetchInterval(interval)
    }
}