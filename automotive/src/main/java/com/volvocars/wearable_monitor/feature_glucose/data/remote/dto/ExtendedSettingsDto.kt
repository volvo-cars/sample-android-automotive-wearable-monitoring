package com.volvocars.wearable_monitor.feature_glucose.data.remote.dto

import android.os.Parcelable
import com.volvocars.wearable_monitor.feature_glucose.domain.model.ExtendedSettings
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class ExtendedSettingsDto(
    val basal: BasalDto,
    val devicestatus: DeviceStatusDto,
    val profile: @RawValue ProfileDto,
) : Parcelable {
    fun toExtendedSettings(): ExtendedSettings = ExtendedSettings(
        basal = basal.toBasal(),
        devicestatus = devicestatus.toDeviceStatus(),
        profile = profile.toProfile()
    )
}
