package com.volvocars.wearablemonitor.domain.usecase

import com.volvocars.wearablemonitor.domain.repository.PreferenceRepository
import javax.inject.Inject

class SetUserSignedIn @Inject constructor(
    private val repository: PreferenceRepository
) {
    operator fun invoke(isUserSignedIn: Boolean) {
        repository.setUserSignedIn(isUserSignedIn)
    }
}