package com.volvocars.wearable_monitor.feature_glucose.data.repository

import com.volvocars.wearable_monitor.feature_glucose.domain.model.Glucose
import com.volvocars.wearable_monitor.feature_glucose.domain.model.ServerStatus
import com.volvocars.wearable_monitor.feature_glucose.domain.repository.DiabetesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FakeDiabetesRepository @Inject constructor() : DiabetesRepository {
    private var shouldReturnError = false
    private val serverStatus: MutableSet<ServerStatus> = mutableSetOf()
    private val glucoseValues: MutableList<Glucose> = mutableListOf()

    override fun fetchGlucoseValues(url: String, counts: Int): Flow<Result<List<Glucose>>> =
        flow {
            val response = when (shouldReturnError) {
                true -> Result.failure(Exception("ERROR"))
                false -> Result.success(glucoseValues.toList().take(counts))
            }
            emit(response)
        }


    override fun fetchServerStatus(url: String): Flow<Result<ServerStatus>> = flow {
        val response = when (shouldReturnError) {
            true -> Result.failure(Exception("ERROR"))
            false -> Result.success(serverStatus.first())
        }
        emit(response)
    }

    override fun fetchCachedGlucoseValues(counts: Int): Flow<List<Glucose>> = flow {
        emit(glucoseValues)
    }

    override suspend fun deleteCachedGlucoseValues() {
        glucoseValues.clear()
    }

    fun shouldReturnError(shouldFail: Boolean) {
        shouldReturnError = shouldFail
    }

    fun insertGlucoseValues(glucoseList: List<Glucose>) {
        glucoseValues.addAll(glucoseList)
    }

    fun insertServerStatus(status: ServerStatus) {
        serverStatus.add(status)
    }
}