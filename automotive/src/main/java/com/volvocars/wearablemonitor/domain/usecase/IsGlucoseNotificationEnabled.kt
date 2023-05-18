package com.volvocars.wearablemonitor.domain.usecase

import com.volvocars.wearablemonitor.domain.repository.PreferenceRepository
import javax.inject.Inject

class IsGlucoseNotificationEnabled @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) {
    operator fun invoke(): Boolean {
        return preferenceRepository.isGlucoseNotificationEnabled()
    }
}