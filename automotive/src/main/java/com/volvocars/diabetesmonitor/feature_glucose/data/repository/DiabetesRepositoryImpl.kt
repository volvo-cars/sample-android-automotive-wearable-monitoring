package com.volvocars.diabetesmonitor.feature_glucose.data.repository

import com.volvocars.diabetesmonitor.core.util.Resource
import com.volvocars.diabetesmonitor.feature_glucose.data.local.GlucoseDao
import com.volvocars.diabetesmonitor.feature_glucose.data.remote.NightScoutApi
import com.volvocars.diabetesmonitor.feature_glucose.data.storage.SharedPreferenceStorage
import com.volvocars.diabetesmonitor.feature_glucose.data.util.fetchRemoteData
import com.volvocars.diabetesmonitor.feature_glucose.data.util.networkBoundResource
import com.volvocars.diabetesmonitor.feature_glucose.domain.model.Glucose
import com.volvocars.diabetesmonitor.feature_glucose.domain.model.ServerStatus
import com.volvocars.diabetesmonitor.feature_glucose.domain.repository.DiabetesRepository
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
    private val sharedPreferenceStorage: SharedPreferenceStorage,
) : DiabetesRepository {

    /**
     *  Will first fetch cached values from database if any.
     *
     *  Then make a network request and update with the new values if the call was successful
     *
     *  @param url Url to endpoint
     *  @param counts Number of entries to fetch
     *  @return a [Flow] with a [List] of [Glucose] values wrapped in a [Resource] object
     *
     *  @note Since we using dynamically urls we're using a url annotator,
     *   this seems to be the easiest solution for now.
     *   The url annotator can maybe be replaced with an interceptor later.
     */
    override fun fetchGlucoseValues(url: String, counts: Int): Flow<Resource<List<Glucose>>> =
        networkBoundResource(
            query = {
                dao.fetchGlucoseEntities(counts).map { glucoseList ->
                    glucoseList.map { it.toGlucose() }.sorted()
                }
            },
            fetch = {
                val urlWithHeader = "$url/api/v1/entries.json"
                delay(1000)
                api.getGlucose(urlWithHeader, counts).body()
                    ?.map { it.toGlucoseEntity() }
            },
            saveFetchResult = { glucoseValues ->
                dao.deleteAllGlucoseEntities()
                dao.insertGlucoseEntityList(glucoseValues!!)
            }
        )

    /**
     **
     *  @param url Url to the endpoint
     *  @return A [Flow] with a [List] of [ServerStatus] wrapped in a [Resource] object
     *
     *  @note Since we using dynamically urls we're using a url annotator,
     *   this seems to be the easiest solution for now.
     *   The url annotator can maybe be replaced with an interceptor later.
     */
    override fun fetchServerStatus(url: String): Flow<Resource<ServerStatus>> {
        val urlWithHeader = "$url/api/v1/status"
        return fetchRemoteData { api.getStatus(urlWithHeader) }.map { response ->
            when (response) {
                is Resource.Success -> {
                    Resource.Success(response.data?.toServerStatus())
                }
                is Resource.Error -> {
                    Resource.Error(response.message!!)
                }
                is Resource.Loading -> {
                    Resource.Loading()
                }
            }
        }
    }

    /**
     * Fetch cached glucose values stored in the local room database
     *
     * @param counts How many entries to fetch
     * @return a list of [Glucose] in a [Flow]
     */
    override fun fetchCachedGlucoseValues(counts: Int): Flow<List<Glucose>> =
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

