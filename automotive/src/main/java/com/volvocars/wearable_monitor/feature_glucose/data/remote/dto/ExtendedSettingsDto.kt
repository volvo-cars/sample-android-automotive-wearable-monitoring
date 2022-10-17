package com.volvocars.wearable_monitor.feature_glucose.data.remote.dto

import com.volvocars.wearable_monitor.feature_glucose.domain.model.ExtendedSettings
import kotlinx.parcelize.RawValue
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
    fun toExtendedSettings(): ExtendedSettings = ExtendedSettings(
        basal = basal.toBasal(),
        devicestatus = deviceStatus.toDeviceStatus(),
        profile = profile.toProfile()
    )
}
