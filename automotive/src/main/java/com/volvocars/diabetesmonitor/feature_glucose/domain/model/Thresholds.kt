package com.volvocars.diabetesmonitor.feature_glucose.domain.model

data class Thresholds(
    val bgHigh: Long,
    val bgLow: Long,
    val bgTargetBottom: Long,
    val bgTargetTop: Long,
)