package com.volvocars.wearablemonitor.data.remote.dto

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class DeviceStatusDto(
    @SerialName("advanced")
    val advanced: Boolean,
)
