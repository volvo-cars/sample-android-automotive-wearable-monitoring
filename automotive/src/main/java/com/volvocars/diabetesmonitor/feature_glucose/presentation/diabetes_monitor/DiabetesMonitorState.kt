package com.volvocars.diabetesmonitor.feature_glucose.presentation.diabetes_monitor

import com.volvocars.diabetesmonitor.feature_glucose.domain.model.Glucose

data class DiabetesMonitorState(
    val glucoseValues: List<Glucose> = emptyList(),
    val isLoading: Boolean = false,
)