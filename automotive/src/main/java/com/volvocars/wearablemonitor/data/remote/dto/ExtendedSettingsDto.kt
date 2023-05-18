package com.volvocars.wearablemonitor.data.remote.dto

import com.volvocars.wearablemonitor.domain.model.ExtendedSettings
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class ExtendedSettingsDto(
    @SerialName("basal")
    val basal: BasalDto,
    @SerialName("devicestatus")
    val deviceStatus: DeviceStatusDto,
    @SerialName("profile")
    val profile: ProfileDto,
) {
    fun toExtendedSettings() = ExtendedSettings(
        basal = basal.toBasal(),
        devicestatus = deviceStatus.toDeviceStatus(),
        profile = profile.toProfile()
    )
}
