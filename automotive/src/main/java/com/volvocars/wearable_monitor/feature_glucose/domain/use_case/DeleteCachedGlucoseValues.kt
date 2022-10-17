package com.volvocars.wearable_monitor.feature_glucose.domain.use_case

import com.volvocars.wearable_monitor.feature_glucose.domain.repository.DiabetesRepository
import kotlinx.coroutines.runBlocking

/**
 * Delete all cached values.
 */
class DeleteCachedGlucoseValues(private val diabetesRepository: DiabetesRepository) {
    operator fun invoke() = runBlocking {
        diabetesRepository.deleteCachedGlucoseValues()
    }
}