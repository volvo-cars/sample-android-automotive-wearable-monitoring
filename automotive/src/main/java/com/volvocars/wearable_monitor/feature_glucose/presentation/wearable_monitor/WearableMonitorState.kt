package com.volvocars.wearable_monitor.feature_glucose.presentation.wearable_monitor

import com.volvocars.wearable_monitor.feature_glucose.domain.model.Glucose

data class WearableMonitorState(
    val glucoseValues: List<Glucose> = emptyList(),
    val isLoading: Boolean = false,
)