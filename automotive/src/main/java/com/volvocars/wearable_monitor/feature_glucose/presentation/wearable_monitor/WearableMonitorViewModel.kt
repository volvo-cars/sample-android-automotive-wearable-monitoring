package com.volvocars.wearable_monitor.feature_glucose.presentation.wearable_monitor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.PeriodicWorkRequest
import com.volvocars.wearable_monitor.feature_glucose.data.storage.SharedPreferenceStorage
import com.volvocars.wearable_monitor.feature_glucose.domain.use_case.FetchGlucoseValues
import com.volvocars.wearable_monitor.feature_glucose.domain.use_case.ObserveCachedGlucoseValues
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@HiltViewModel
class WearableMonitorViewModel @Inject constructor(
    private val observeCachedGlucoseValues: ObserveCachedGlucoseValues,
    val fetchGlucoseValues: FetchGlucoseValues,
    val sharedPreferenceStorage: SharedPreferenceStorage,
    val glucoseFetchWorker: PeriodicWorkRequest,
) : ViewModel() {
    private val _glucoseValues = MutableStateFlow(WearableMonitorState())
    val glucoseValues = _glucoseValues.asStateFlow()

    fun fetchCachedValues() {
        Log.d(TAG, "fetchCachedValues: fetch new glucose values")
        viewModelScope.launch {
            observeCachedGlucoseValues.invoke(24).collect { result ->
                if (result.isNotEmpty()) {
                    _glucoseValues.value = glucoseValues.value.copy(
                        glucoseValues = result,
                    )
                }
            }
        }
    }

    companion object {
        val TAG = WearableMonitorViewModel::class.simpleName
    }
}