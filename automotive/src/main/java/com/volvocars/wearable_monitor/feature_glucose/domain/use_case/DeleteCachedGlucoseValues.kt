package com.volvocars.wearable_monitor.feature_glucose.domain.use_case

import com.volvocars.wearable_monitor.feature_glucose.domain.repository.DiabetesRepository
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