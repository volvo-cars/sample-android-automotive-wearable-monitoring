package com.volvocars.wearablemonitor.domain.usecase

import com.volvocars.wearablemonitor.domain.repository.PreferenceRepository
import com.volvocars.wearablemonitor.domain.util.ThresholdUnit
import javax.inject.Inject

class GetThreshold @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) {
    operator fun invoke(thresholdUnit: ThresholdUnit): Long {
        return preferenceRepository.getThreshold(thresholdUnit)
    }
}