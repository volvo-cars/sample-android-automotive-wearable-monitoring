package com.volvocars.wearable_monitor.feature_glucose.data.util

import com.volvocars.wearable_monitor.core.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retry

/**
 * Do a network call and return the response inside a [Resource]
 *
 * @param call The coroutine function from webservice.
 * @return flow of the model wrapped in [Resource]
 */
@InternalCoroutinesApi
@ExperimentalCoroutinesApi
fun <T> fetchRemoteData(
    call: suspend () -> T,
) = flow {
    emit(Resource.Loading())
    val state = try {
        val callResponse = call()
        Resource.Success(callResponse)
    } catch (e: Exception) {
        Resource.Error(e.message.toString())
    }
    emit(state)
}.flowOn(Dispatchers.IO).retry(2) {
    delay(2000)
    true
}.catch { e ->
    emit(Resource.Error((e.message!!)))
}