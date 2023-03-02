package com.volvocars.wearable_monitor.feature_glucose.presentation.wearable_monitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.PeriodicWorkRequest
import com.volvocars.wearable_monitor.feature_glucose.domain.use_case.FetchGlucoseValues
import com.volvocars.wearable_monitor.feature_glucose.domain.use_case.GetBaseUrl
import com.volvocars.wearable_monitor.feature_glucose.domain.use_case.GetGlucoseFetchInterval
import com.volvocars.wearable_monitor.feature_glucose.domain.use_case.GetThresholds
import com.volvocars.wearable_monitor.feature_glucose.domain.use_case.GetTimeFormat
import com.volvocars.wearable_monitor.feature_glucose.domain.use_case.ObserveCachedGlucoseValues
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WearableMonitorViewModel @Inject constructor(
    private val observeCachedGlucoseValues: ObserveCachedGlucoseValues,
    private val getBaseUrl: GetBaseUrl,
    private val getTimeFormat: GetTimeFormat,
    private val getThresholds: GetThresholds,
    private val getGlucoseFetchInterval: GetGlucoseFetchInterval,
    private val fetchGlucoseValues: FetchGlucoseValues,
    private val glucoseFetchWorker: PeriodicWorkRequest,
) : ViewModel() {
    private val _glucoseValues = MutableStateFlow(WearableMonitorState())
    val glucoseValues = _glucoseValues.asStateFlow()

    init {
        fetchCachedValues()
    }

    private fun fetchCachedValues() = viewModelScope.launch {
        observeCachedGlucoseValues.invoke(24).collect { result ->
            if (result.isNotEmpty()) {
                _glucoseValues.value = glucoseValues.value.copy(
                    glucoseValues = result,
                )
            }
        }
    }

    fun getThresholds() = getThresholds.invoke()

    fun getGlucoseFetchInterval() = getGlucoseFetchInterval.invoke()

    fun getGlucoseFetchWorker() = glucoseFetchWorker

    fun getTimeFormat(): Long {
        return getTimeFormat.invoke()
    }

    fun fetchGlucoseValues() = viewModelScope.launch {
        fetchGlucoseValues.invoke(getBaseUrl(), 24).collect()
    }

    companion object {
        val TAG = WearableMonitorViewModel::class.simpleName
    }
}