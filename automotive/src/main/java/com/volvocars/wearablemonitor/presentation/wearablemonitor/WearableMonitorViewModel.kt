package com.volvocars.wearablemonitor.presentation.wearablemonitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.volvocars.wearablemonitor.domain.usecase.FetchGlucoseValues
import com.volvocars.wearablemonitor.domain.usecase.GetBaseUrl
import com.volvocars.wearablemonitor.domain.usecase.GetThresholds
import com.volvocars.wearablemonitor.domain.usecase.GetTimeFormat
import com.volvocars.wearablemonitor.domain.usecase.IsUnitMmol
import com.volvocars.wearablemonitor.domain.usecase.ObserveCachedGlucoseValues
import com.volvocars.wearablemonitor.presentation.mapper.toPresentationModels
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WearableMonitorViewModel @Inject constructor(
    private val observeCachedGlucoseValues: ObserveCachedGlucoseValues,
    private val getBaseUrl: GetBaseUrl,
    private val getTimeFormat: GetTimeFormat,
    private val getThresholds: GetThresholds,
    private val fetchGlucoseValues: FetchGlucoseValues,
    private val isUnitMmol: IsUnitMmol,
) : ViewModel() {

    private val _glucoseValues = MutableStateFlow(WearableMonitorState())
    val glucoseValues = _glucoseValues.asStateFlow()

    init {
        observeCache()
    }

    /** Observe cached DB readings and keep UI state updated. */
    private fun observeCache() = viewModelScope.launch {
        observeCachedGlucoseValues.invoke(24).collect { list ->
            _glucoseValues.update { it.copy(glucoseValues = list.toPresentationModels()) }
        }
    }

    /** Trigger a one-shot remote sync; updates [isLoading] / [error] while in flight. */
    fun fetchGlucoseValues() = viewModelScope.launch {
        fetchGlucoseValues.invoke(getBaseUrl(), 24)
            .onStart { _glucoseValues.update { it.copy(isLoading = true, error = null) } }
            .catch { e -> _glucoseValues.update { it.copy(isLoading = false, error = e.message) } }
            .collect { result ->
                result.onFailure { e ->
                    _glucoseValues.update { it.copy(isLoading = false, error = e.message) }
                }.onSuccess {
                    _glucoseValues.update { it.copy(isLoading = false, error = null) }
                }
            }
    }

    fun isUnitMmol() = isUnitMmol.invoke()
    fun getThresholds() = getThresholds.invoke()
    fun getTimeFormat(): Long = getTimeFormat.invoke()

    companion object {
        private const val TAG = "WearableMonitorViewModel"
    }
}
