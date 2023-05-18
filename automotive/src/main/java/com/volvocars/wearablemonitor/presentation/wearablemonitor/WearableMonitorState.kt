package com.volvocars.wearablemonitor.presentation.wearablemonitor

import com.volvocars.wearablemonitor.presentation.model.GlucoseSummary

data class WearableMonitorState(
    val glucoseValues: List<GlucoseSummary> = emptyList(),
    val isLoading: Boolean = false,
)