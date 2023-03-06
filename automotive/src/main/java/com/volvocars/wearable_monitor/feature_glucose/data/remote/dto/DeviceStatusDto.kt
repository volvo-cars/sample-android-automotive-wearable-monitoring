package com.volvocars.wearable_monitor.feature_glucose.data.remote.dto

import com.volvocars.wearable_monitor.feature_glucose.domain.model.DeviceStatus
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class DeviceStatusDto(
    @SerialName("advanced")
    val advanced: Boolean,
) {
    fun toDeviceStatus(): DeviceStatus = DeviceStatus(
        advanced = advanced
    )
}
