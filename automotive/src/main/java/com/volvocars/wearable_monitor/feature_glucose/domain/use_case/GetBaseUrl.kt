package com.volvocars.wearable_monitor.feature_glucose.domain.use_case

import com.volvocars.wearable_monitor.feature_glucose.domain.repository.PreferenceRepository
import javax.inject.Inject

class GetBaseUrl @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) {
    operator fun invoke(): String {
        return preferenceRepository.getBaseUrl()
    }
}