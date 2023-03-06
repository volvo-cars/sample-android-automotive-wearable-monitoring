package com.volvocars.wearable_monitor.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.volvocars.wearable_monitor.feature_glucose.domain.storage.Storage
import com.volvocars.wearable_monitor.feature_glucose.domain.use_case.FetchGlucoseValues
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@HiltWorker
class GlucoseFetchWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val sharedPreferences: Storage,
    private val fetchGlucoseValues: FetchGlucoseValues,
) : Worker(appContext, workerParameters) {
    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    override fun doWork(): Result {
        scope.launch {
            if (sharedPreferences.userSignedIn()) {
                fetchGlucoseValues.invoke(sharedPreferences.getBaseUrl(), 50).collect()
            }
        }
        return Result.success()
    }

    companion object {
        val TAG = GlucoseFetchWorker::class.simpleName
    }
}