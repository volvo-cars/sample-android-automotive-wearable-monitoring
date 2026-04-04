package com.volvocars.wearablemonitor.domain.usecase

import com.volvocars.wearablemonitor.domain.repository.DiabetesRepository
import javax.inject.Inject
import kotlinx.coroutines.runBlocking

/**
 * Delete all cached values.
 */
class DeleteCachedGlucoseValues @Inject constructor(
    private val diabetesRepository: DiabetesRepository,
) {
    operator fun invoke() = runBlocking {
        diabetesRepository.deleteCachedGlucoseValues()
    }
}
