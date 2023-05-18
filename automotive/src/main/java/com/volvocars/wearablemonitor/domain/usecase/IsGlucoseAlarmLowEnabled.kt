package com.volvocars.wearablemonitor.domain.usecase

import com.volvocars.wearablemonitor.domain.repository.PreferenceRepository
import javax.inject.Inject

class IsGlucoseAlarmLowEnabled @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) {
    operator fun invoke(): Boolean {
        return preferenceRepository.isGlucoseAlarmLowEnabled()
    }
}