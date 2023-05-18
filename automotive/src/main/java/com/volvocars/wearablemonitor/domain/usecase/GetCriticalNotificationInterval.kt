package com.volvocars.wearablemonitor.domain.usecase

import com.volvocars.wearablemonitor.domain.repository.PreferenceRepository
import javax.inject.Inject

class GetCriticalNotificationInterval @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) {
    operator fun invoke(): Long {
        return preferenceRepository.getCriticalNotificationInterval()
    }
}