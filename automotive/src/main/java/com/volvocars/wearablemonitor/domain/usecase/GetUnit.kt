package com.volvocars.wearablemonitor.domain.usecase

import com.volvocars.wearablemonitor.domain.repository.PreferenceRepository
import javax.inject.Inject

class GetUnit @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) {
    operator fun invoke(): String {
        return preferenceRepository.getUnit()
    }
}