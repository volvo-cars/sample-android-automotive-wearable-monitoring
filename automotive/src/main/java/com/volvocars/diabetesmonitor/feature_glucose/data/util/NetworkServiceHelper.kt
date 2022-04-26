package com.volvocars.diabetesmonitor.feature_glucose.data.util

import com.volvocars.diabetesmonitor.core.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retry
import retrofit2.Response

/**
 * Do a network call and return the response inside a [Resource]
 *
 * @param call The coroutine function from webservice.
 * @return flow of the model wrapped in [Resource]
 */
@InternalCoroutinesApi
@ExperimentalCoroutinesApi
fun <T> fetchRemoteData(
    call: suspend () -> Response<T>,
) = flow {
    emit(Resource.Loading())
    val callResponse = call()
    val state = when (callResponse.isSuccessful && callResponse.body() != null) {
        true -> Resource.Success(callResponse.body()!!)
        else -> Resource.Error(callResponse.message())
    }
    emit(state)
}.flowOn(Dispatchers.IO).retry(2) {
    delay(2000)
    true
}.catch { e ->
    emit(Resource.Error((e.message!!)))
}