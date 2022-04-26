package com.volvocars.diabetesmonitor.feature_glucose.domain.use_case

import com.volvocars.diabetesmonitor.core.util.Resource
import com.volvocars.diabetesmonitor.feature_glucose.domain.model.Glucose
import com.volvocars.diabetesmonitor.feature_glucose.domain.repository.DiabetesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Fetch glucose values from the server.
 */
class FetchGlucoseValues(private val diabetesRepository: DiabetesRepository) {
    /**
     * Call the getGlucose function from [DiabetesRepository]
     *
     * @param url URL address for the device.
     * @param count Number of entries to fetch.
     */
    operator fun invoke(url: String, count: Int): Flow<Resource<List<Glucose>>> {

        // Don't proceed if url is empty or the url doesn't contains https
        if (url.isEmpty() || url.isBlank() || !url.contains("https") || count <= 0) {
            return flow {}
        }

        return diabetesRepository.fetchGlucoseValues(url, count)
    }
}