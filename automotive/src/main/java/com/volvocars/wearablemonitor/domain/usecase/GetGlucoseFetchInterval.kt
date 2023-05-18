package com.volvocars.wearablemonitor.domain.usecase

import com.volvocars.wearablemonitor.domain.repository.PreferenceRepository
import javax.inject.Inject

class GetGlucoseFetchInterval @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) {
    operator fun invoke(): Int {
        return preferenceRepository.getGlucoseFetchInterval()
    }
}