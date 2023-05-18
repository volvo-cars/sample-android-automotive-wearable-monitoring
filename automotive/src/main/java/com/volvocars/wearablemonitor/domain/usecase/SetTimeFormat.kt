package com.volvocars.wearablemonitor.domain.usecase

import com.volvocars.wearablemonitor.domain.repository.PreferenceRepository
import javax.inject.Inject

class SetTimeFormat @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) {

    operator fun invoke(timeFormat: Long) {
        preferenceRepository.setTimeFormat(timeFormat)
    }
}