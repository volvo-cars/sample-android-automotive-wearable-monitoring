package com.volvocars.wearablemonitor.domain.usecase

import com.volvocars.wearablemonitor.domain.repository.DiabetesRepository
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Fetch glucose values from the server.
 */
class FetchGlucoseValues @Inject constructor(
    private val diabetesRepository: DiabetesRepository,
) {
    /**
     * Call the getGlucose function from [DiabetesRepository]
     *
     * @param url URL address for the device.
     * @param count Number of entries to fetch.
     */
    operator fun invoke(url: String, count: Int): Flow<Result<List<com.volvocars.wearablemonitor.domain.model.Glucose>>> {
        // Don't proceed if url is empty or count is invalid
        if (url.isEmpty() || url.isBlank() || count <= 0) {
            return flow {}
        }

        return diabetesRepository.fetchGlucoseValues(url, count).flowOn(Dispatchers.IO)
    }
}
