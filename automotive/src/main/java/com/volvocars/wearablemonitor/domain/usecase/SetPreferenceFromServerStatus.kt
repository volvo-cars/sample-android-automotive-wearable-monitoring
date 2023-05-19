package com.volvocars.wearablemonitor.domain.usecase

import com.volvocars.wearablemonitor.domain.model.ServerStatus
import com.volvocars.wearablemonitor.domain.repository.PreferenceRepository
import javax.inject.Inject

class SetPreferenceFromServerStatus @Inject constructor(
    private val repository: PreferenceRepository
) {
    operator fun invoke(serverStatus: ServerStatus) {
        repository.setPreferenceFromServerStatus(serverStatus)
    }
}