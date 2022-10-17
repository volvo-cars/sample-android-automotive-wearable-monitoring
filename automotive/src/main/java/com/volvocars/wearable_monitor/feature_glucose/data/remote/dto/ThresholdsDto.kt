package com.volvocars.wearable_monitor.feature_glucose.data.remote.dto

import com.volvocars.wearable_monitor.feature_glucose.domain.model.Thresholds
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class ThresholdsDto(
    @SerialName("bgHigh") val bgHigh: Long,
    @SerialName("bgLow") val bgLow: Long,
    @SerialName("bgTargetBottom") val bgTargetBottom: Long,
    @SerialName("bgTargetTop") val bgTargetTop: Long
) {
    fun toThresholds(): Thresholds = Thresholds(
        bgHigh,
        bgLow,
        bgTargetBottom,
        bgTargetTop
    )
}
