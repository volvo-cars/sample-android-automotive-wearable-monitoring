package com.volvocars.wearable_monitor.feature_glucose.data.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retry

/**
 * Do a network call and return the response inside a [Resource]
 *
 * @param call The coroutine function from webservice.
 * @return flow of the model wrapped in [Result]
 */
fun <T> fetchRemoteData(
    call: suspend () -> T,
) = flow {
    runCatching {
        val result = call()
        Log.d("Fetcher", "fetchRemoteData: $result")
        emit(result)
    }
}.flowOn(Dispatchers.IO).retry(2) {
    delay(2000)
    true
}