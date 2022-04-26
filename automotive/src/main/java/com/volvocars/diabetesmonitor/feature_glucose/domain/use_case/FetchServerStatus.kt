package com.volvocars.diabetesmonitor.feature_glucose.domain.use_case

import com.volvocars.diabetesmonitor.core.util.Resource
import com.volvocars.diabetesmonitor.feature_glucose.domain.model.ServerStatus
import com.volvocars.diabetesmonitor.feature_glucose.domain.repository.DiabetesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Login to the server by receive the server status information
 */
class FetchServerStatus(private val diabetesRepository: DiabetesRepository) {
    /**
     * Call the getServerStatus function from [DiabetesRepository]
     *
     * @param url URL address for device
     */
    operator fun invoke(url: String): Flow<Resource<ServerStatus>> {
        // Don't proceed if url is empty or the url doesn't contains https
        if (url.isEmpty() || url.isBlank() || !url.contains("https")) {
            return flow {}
        }

        return diabetesRepository.fetchServerStatus(url)
    }

    companion object {
        val TAG = FetchServerStatus::class.simpleName
    }
}