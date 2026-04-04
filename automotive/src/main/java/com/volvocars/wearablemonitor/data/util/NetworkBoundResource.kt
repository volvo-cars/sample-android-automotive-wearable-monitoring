package com.volvocars.wearablemonitor.data.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * Offline-first network-bound resource helper.
 *
 * Emits cached data immediately, then kicks off a remote fetch and persists the
 * result so the DB flow re-emits the fresh data automatically.  Any network error
 * is surfaced via [Result.failure] so callers can decide how to handle it.
 *
 * @param query     Returns a DB [Flow] that is kept alive for downstream observation.
 * @param fetch     Executes the remote API call and returns a [Result].
 * @param saveFetchResult  Persists a successful fetch result; skipped on failure.
 */
inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<Result<ResultType>>,
    crossinline fetch: suspend () -> Result<RequestType>,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
) = flow {
    // 1. Emit whatever is in cache right away (may be empty).
    emit(query().first())

    // 2. Fetch from the network and persist on success.
    val fetchResult = fetch()
    if (fetchResult.isSuccess) {
        saveFetchResult(fetchResult.getOrThrow())
    } else {
        emit(Result.failure(fetchResult.exceptionOrNull() ?: Exception("Unknown network error")))
    }

    // 3. Re-emit the DB flow so observers always see the freshest data.
    emitAll(query())
}
