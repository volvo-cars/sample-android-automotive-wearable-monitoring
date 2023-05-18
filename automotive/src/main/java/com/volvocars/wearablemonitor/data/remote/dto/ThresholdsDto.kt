package com.volvocars.wearablemonitor.data.remote.dto

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class ThresholdsDto(
    @SerialName("bgHigh") val bgHigh: Long,
    @SerialName("bgLow") val bgLow: Long,
    @SerialName("bgTargetBottom") val bgTargetBottom: Long,
    @SerialName("bgTargetTop") val bgTargetTop: Long
)