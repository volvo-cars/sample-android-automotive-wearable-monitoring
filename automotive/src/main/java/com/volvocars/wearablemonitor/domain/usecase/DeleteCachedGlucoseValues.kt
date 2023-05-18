package com.volvocars.wearablemonitor.domain.usecase

import com.volvocars.wearablemonitor.domain.repository.DiabetesRepository
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Delete all cached values.
 */
class DeleteCachedGlucoseValues @Inject constructor(
    private val diabetesRepository: DiabetesRepository
) {
    operator fun invoke() = runBlocking {
        diabetesRepository.deleteCachedGlucoseValues()
    }
}