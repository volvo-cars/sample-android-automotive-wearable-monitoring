package com.volvocars.diabetesmonitor.feature_glucose.data.repository

import com.volvocars.diabetesmonitor.core.util.Resource
import com.volvocars.diabetesmonitor.feature_glucose.domain.model.Glucose
import com.volvocars.diabetesmonitor.feature_glucose.domain.model.ServerStatus
import com.volvocars.diabetesmonitor.feature_glucose.domain.repository.DiabetesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FakeDiabetesRepository @Inject constructor() : DiabetesRepository {
    private var shouldReturnError = false
    private val serverStatus: MutableSet<ServerStatus> = mutableSetOf()
    private val glucoseValues: MutableList<Glucose> = mutableListOf()

    override fun fetchGlucoseValues(url: String, counts: Int): Flow<Resource<List<Glucose>>> =
        flow {
            emit(Resource.Loading())
            val response = when (shouldReturnError) {
                true -> Resource.Error("ERROR")
                false -> Resource.Success(glucoseValues.toList().take(counts))
            }
            emit(response)
        }


    override fun fetchServerStatus(url: String): Flow<Resource<ServerStatus>> = flow {
        emit(Resource.Loading())
        val response = when (shouldReturnError) {
            true -> Resource.Error("ERROR")
            false -> Resource.Success(serverStatus.first())
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

    companion object {
        @JvmStatic
        val glucoseList = ('a'..'z').mapIndexed { index, c ->
            Glucose(
                id = c.toString(),
                sgv = index,
                sgvMmol = index.toFloat(),
                sgvUnit = index.toFloat(),
                date = index.toLong(),
                dateString = index.toString(),
                trend = c.toString(),
                direction = c.toString(),
                type = c.toString() + index.toString()
            )
        }
    }
}