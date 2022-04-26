package com.volvocars.diabetesmonitor.feature_glucose.data.remote.dto

import android.os.Parcelable
import com.volvocars.diabetesmonitor.feature_glucose.domain.model.DeviceStatus
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeviceStatusDto(
    val advanced: Boolean,
) : Parcelable {
    fun toDeviceStatus(): DeviceStatus = DeviceStatus(
        advanced = advanced
    )
}
