package com.volvocars.wearablemonitor.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.volvocars.wearablemonitor.core.util.Constants
import com.volvocars.wearablemonitor.domain.storage.Storage
import com.volvocars.wearablemonitor.domain.usecase.FetchGlucoseValues
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@HiltWorker
class GlucoseFetchWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val sharedPreferences: Storage,
    private val fetchGlucoseValues: FetchGlucoseValues,
) : Worker(appContext, workerParameters) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

        fun create(context: Context, workRequest: PeriodicWorkRequest) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                Constants.GLUCOSE_FETCH_WORK_ID,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }
}