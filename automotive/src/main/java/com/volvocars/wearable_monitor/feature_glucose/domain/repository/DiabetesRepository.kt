package com.volvocars.wearable_monitor.feature_glucose.domain.repository

import com.volvocars.wearable_monitor.core.util.Resource
import com.volvocars.wearable_monitor.feature_glucose.domain.model.Glucose
import com.volvocars.wearable_monitor.feature_glucose.domain.model.ServerStatus
import kotlinx.coroutines.flow.Flow

/**
 * Simple repository to handle glucose values and server status
 */
interface DiabetesRepository {
    fun fetchGlucoseValues(url: String, counts: Int): Flow<Resource<List<Glucose>>>
    fun fetchServerStatus(url: String): Flow<Resource<ServerStatus>>
    fun fetchCachedGlucoseValues(counts: Int): Flow<List<Glucose>>
    suspend fun deleteCachedGlucoseValues()
}
