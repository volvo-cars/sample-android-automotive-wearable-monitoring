package com.volvocars.diabetesmonitor.feature_glucose.presentation.diabetes_monitor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.PeriodicWorkRequest
import com.volvocars.diabetesmonitor.feature_glucose.data.storage.SharedPreferenceStorage
import com.volvocars.diabetesmonitor.feature_glucose.domain.use_case.ObserveCachedGlucoseValues
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiabetesMonitorViewModel @Inject constructor(
    private val observeCachedGlucoseValues: ObserveCachedGlucoseValues,
    val sharedPreferenceStorage: SharedPreferenceStorage,
    val glucoseFetchWorker: PeriodicWorkRequest
) : ViewModel() {
    private val _glucoseValues = MutableStateFlow(DiabetesMonitorState())
    val glucoseValues = _glucoseValues.asStateFlow()

    fun fetchCachedValues() {
        Log.d(TAG, "fetchCachedValues: fetch new glucose values")
        viewModelScope.launch {
            observeCachedGlucoseValues.invoke(24).collect { result ->
                _glucoseValues.value = glucoseValues.value.copy(
                    glucoseValues = result,
                )
            }
        }
    }

    companion object {
        val TAG = DiabetesMonitorViewModel::class.simpleName
    }
}