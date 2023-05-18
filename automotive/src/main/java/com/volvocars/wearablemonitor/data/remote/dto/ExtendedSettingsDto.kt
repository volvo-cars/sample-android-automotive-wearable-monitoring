package com.volvocars.wearablemonitor.data.remote.dto

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class ExtendedSettingsDto(
    @SerialName("basal")
    val basal: BasalDto,
    @SerialName("devicestatus")
    val deviceStatus: DeviceStatusDto,
    @SerialName("profile")
    val profile: ProfileDto,
)
