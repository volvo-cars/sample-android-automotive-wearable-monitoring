package com.volvocars.wearablemonitor.domain.model

data class Thresholds(
    val bgHigh: Long,
    val bgLow: Long,
    val bgTargetBottom: Long,
    val bgTargetTop: Long,
)