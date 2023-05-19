package com.volvocars.wearablemonitor.domain.usecase

import com.volvocars.wearablemonitor.domain.repository.PreferenceRepository
import javax.inject.Inject

class SetBaseUrl @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) {
    operator fun invoke(url: String) {
        preferenceRepository.setBaseUrl(url)
    }
}