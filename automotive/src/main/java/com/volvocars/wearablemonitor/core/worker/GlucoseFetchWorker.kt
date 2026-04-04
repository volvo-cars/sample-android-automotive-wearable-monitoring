package com.volvocars.wearablemonitor.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.volvocars.wearablemonitor.core.util.Constants
import com.volvocars.wearablemonitor.domain.storage.Storage
import com.volvocars.wearablemonitor.domain.usecase.FetchGlucoseValues
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.collect

@HiltWorker
class GlucoseFetchWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val storage: Storage,
    private val fetchGlucoseValues: FetchGlucoseValues,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        if (!storage.userSignedIn()) return Result.success()
        return try {
            fetchGlucoseValues.invoke(storage.getBaseUrl(), 50).collect()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    companion object {
        val TAG = GlucoseFetchWorker::class.simpleName
        private const val MAX_RETRIES = 3

        fun create(context: Context, workRequest: PeriodicWorkRequest) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                Constants.GLUCOSE_FETCH_WORK_ID,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest,
            )
        }
    }
}
