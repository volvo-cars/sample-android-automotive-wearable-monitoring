package com.volvocars.wearablemonitor.domain.usecase

import com.volvocars.wearablemonitor.domain.repository.PreferenceRepository
import javax.inject.Inject

class ClearPreferenceStorage @Inject constructor(
    private val repository: PreferenceRepository
) {
    operator fun invoke() {
        repository.clearData()
    }
}