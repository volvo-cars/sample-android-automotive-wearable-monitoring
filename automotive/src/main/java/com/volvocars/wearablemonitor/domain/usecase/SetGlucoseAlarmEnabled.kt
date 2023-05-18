package com.volvocars.wearablemonitor.domain.usecase

import com.volvocars.wearablemonitor.domain.repository.PreferenceRepository
import javax.inject.Inject

class SetGlucoseAlarmEnabled @Inject constructor(
    private val repository: PreferenceRepository
) {

    operator fun invoke(enabled: Boolean) {
        repository
    }
}