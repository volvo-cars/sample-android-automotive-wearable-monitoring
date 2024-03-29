package com.volvocars.wearablemonitor.domain.usecase

import com.volvocars.wearablemonitor.domain.repository.DiabetesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * Login to the server by receive the server status information
 */
class FetchServerStatus @Inject constructor(
    private val diabetesRepository: DiabetesRepository
) {
    /**
     * Call the getServerStatus function from [DiabetesRepository]
     *
     * @param url URL address for device
     */
    operator fun invoke(url: String): Flow<Result<com.volvocars.wearablemonitor.domain.model.ServerStatus>> {
        // Don't proceed if url is empty or the url doesn't contains https
        if (url.isEmpty() || url.isBlank() || !url.contains("https")) {
            return flow {}
        }

        return diabetesRepository.fetchServerStatus(url).flowOn(Dispatchers.IO)
    }

    companion object {
        val TAG = FetchServerStatus::class.simpleName
    }
}