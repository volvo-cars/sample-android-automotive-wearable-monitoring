package com.volvocars.wearablemonitor.data.remote.dto

import com.volvocars.wearablemonitor.domain.model.DeviceStatus
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class DeviceStatusDto(
    @SerialName("advanced")
    val advanced: Boolean,
) {
    fun toDeviceStatus() = DeviceStatus(
        advanced = advanced
    )
}
