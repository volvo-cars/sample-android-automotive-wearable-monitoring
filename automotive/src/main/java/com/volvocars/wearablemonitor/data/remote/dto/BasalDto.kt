package com.volvocars.wearablemonitor.data.remote.dto

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class BasalDto(
    @SerialName("render")
    val render: String,
)
