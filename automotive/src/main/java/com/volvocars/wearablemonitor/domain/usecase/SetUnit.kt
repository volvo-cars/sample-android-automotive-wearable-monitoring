package com.volvocars.wearablemonitor.domain.usecase

import com.volvocars.wearablemonitor.domain.repository.PreferenceRepository
import javax.inject.Inject

class SetUnit @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) {
    operator fun invoke(unit: String) {
        preferenceRepository.setUnit(unit)
    }
}