package com.volvocars.wearable_monitor.feature_glucose.domain.model

data class Thresholds(
    val bgHigh: Long,
    val bgLow: Long,
    val bgTargetBottom: Long,
    val bgTargetTop: Long,
)