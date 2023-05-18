package com.volvocars.wearablemonitor.data.repository

import android.util.Log
import com.volvocars.wearablemonitor.data.local.dao.GlucoseDao
import com.volvocars.wearablemonitor.data.remote.NightScoutApi
import com.volvocars.wearablemonitor.data.util.fetchRemoteData
import com.volvocars.wearablemonitor.data.util.networkBoundResource
import com.volvocars.wearablemonitor.domain.model.Glucose
import com.volvocars.wearablemonitor.domain.model.ServerStatus
import com.volvocars.wearablemonitor.domain.repository.DiabetesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class DiabetesRepositoryImpl @Inject constructor(
    private val api: NightScoutApi,
    private val dao: GlucoseDao,
) : DiabetesRepository {

    /**
     *  Will first fetch cached values from database if any.
     *
     *  Then make a network request and update with the new values if the call was successful
     *
     *  @param url Url to endpoint
     *  @param counts Number of entries to fetch
     *  @return a [Flow] with a [List] of [Glucose] values wrapped in a [Result] object
     *
     *  @note Since we using dynamically urls we're using a url annotator,
     *   this seems to be the easiest solution for now.
     *   The url annotator can maybe be replaced with an interceptor later.
     */
    override fun fetchGlucoseValues(url: String, counts: Int): Flow<Result<List<com.volvocars.wearablemonitor.domain.model.Glucose>>> =
        networkBoundResource(
            query = {
                dao.fetchGlucoseEntities(counts).map { glucoseList ->
                    Result.success(glucoseList.map { it.toGlucose() }.sorted())
                }
            },
            fetch = {
                delay(1000)
                api.getGlucose(url, counts).mapCatching { list ->
                    list.map {
                        it.toGlucoseEntity()
                    }
                }
            },
            saveFetchResult = { glucoseValues ->
                dao.deleteAllGlucoseEntities()
                dao.insertGlucoseEntityList(glucoseValues.getOrThrow())
            }
        )

    /**
     **
     *  @param url Url to the endpoint
     *  @return A [Flow] with a [List] of [ServerStatus] wrapped in a [Result] object
     *
     *  @note Since we using dynamically urls we're using a url annotator,
     *   this seems to be the easiest solution for now.
     *   The url annotator can maybe be replaced with an interceptor later.
     */
    override fun fetchServerStatus(url: String): Flow<Result<com.volvocars.wearablemonitor.domain.model.ServerStatus>> =
        fetchRemoteData { api.getStatus(url) }.map { response ->
            Log.d(TAG, "fetchServerStatus: $response")
            response.mapCatching {
                it.toServerStatus()
            }
        }

    /**
     * Fetch cached glucose values stored in the local room database
     *
     * @param counts How many entries to fetch
     * @return a list of [Glucose] in a [Flow]
     */
    override fun fetchCachedGlucoseValues(counts: Int): Flow<List<com.volvocars.wearablemonitor.domain.model.Glucose>> =
        dao.fetchGlucoseEntities(counts).map { entityList ->
            entityList.map { it.toGlucose() }.sorted()
        }

    /**
     * Delete all cached glucose values
     */
    override suspend fun deleteCachedGlucoseValues() = dao.deleteAllGlucoseEntities()

    companion object {
        val TAG = DiabetesRepositoryImpl::class.simpleName
    }
}

