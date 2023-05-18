package com.volvocars.wearablemonitor.presentation.wearablemonitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.PeriodicWorkRequest
import com.volvocars.wearablemonitor.domain.usecase.FetchGlucoseValues
import com.volvocars.wearablemonitor.domain.usecase.GetBaseUrl
import com.volvocars.wearablemonitor.domain.usecase.GetGlucoseFetchInterval
import com.volvocars.wearablemonitor.domain.usecase.GetThresholds
import com.volvocars.wearablemonitor.domain.usecase.GetTimeFormat
import com.volvocars.wearablemonitor.domain.usecase.IsUnitMmol
import com.volvocars.wearablemonitor.domain.usecase.ObserveCachedGlucoseValues
import com.volvocars.wearablemonitor.presentation.mapper.toPresentationModels
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
    private val isUnitMmol: IsUnitMmol,
    private val glucoseFetchWorker: PeriodicWorkRequest,
) : ViewModel() {
    private val _glucoseValues = MutableStateFlow(WearableMonitorState())
    val glucoseValues = _glucoseValues.asStateFlow()

    init {
        fetchCachedValues()
    }

    private fun fetchCachedValues() = viewModelScope.launch {
        observeCachedGlucoseValues.invoke(24).collect { result ->
            _glucoseValues.value = glucoseValues.value.copy(
                glucoseValues = result.toPresentationModels(),
            )
        }
    }

    fun isUnitMmol() = isUnitMmol.invoke()

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