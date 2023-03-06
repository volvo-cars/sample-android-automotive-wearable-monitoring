package com.volvocars.wearable_monitor.feature_glucose.data.util

import kotlinx.coroutines.flow.*

/**
 * An inline function that will make an api call and if the call is successful,
 * it will emit and also store the data in a local database.
 *
 * Otherwise it will emit the error of call
 *
 * @param query The database query from the dao that should be called
 * @param fetch The web service api call that should be called
 * @param saveFetchResult A lambda that saves the data from the api call to the database
 * @param shouldFetch Weather this request should fetch or not, default set to true
 * @return A flow of the [ResultType] wrapped in [Result]
 */
inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline shouldFetch: (ResultType) -> Boolean = { true },
) = flow {
    runCatching {
        val data = query().first()
        val flow = if (shouldFetch(data)) {
            saveFetchResult(fetch())
            query().map { it }
        } else {
            query().map { it }
        }

        emitAll(flow)
    }
}

