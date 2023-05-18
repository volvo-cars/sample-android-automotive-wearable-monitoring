package com.volvocars.wearablemonitor.domain.usecase

import com.volvocars.wearablemonitor.domain.repository.PreferenceRepository
import javax.inject.Inject

class SetThresholdValue @Inject constructor(
    private val repository: PreferenceRepository
) {
    operator fun invoke(thresholdName: String, thresholdValue: Long) {
        repository.setThresholdValue(thresholdName, thresholdValue)
    }
}