package com.volvocars.wearable_monitor.feature_glucose.presentation.wearable_monitor

import com.volvocars.wearable_monitor.feature_glucose.presentation.model.GlucoseSummary

data class WearableMonitorState(
    val glucoseValues: List<GlucoseSummary> = emptyList(),
    val isLoading: Boolean = false,
)