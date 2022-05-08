package com.volvocars.wearable_monitor.feature_glucose.data.remote.dto

import android.os.Parcelable
import com.volvocars.wearable_monitor.feature_glucose.domain.model.ServerStatus
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class ServerStatusDto(
    val apiEnabled: Boolean,
    val authorized: String?,
    val boluscalcEnabled: Boolean,
    val careportalEnabled: Boolean,
    val extendedSettings: @RawValue ExtendedSettingsDto,
    val name: String,
    val serverTime: String,
    val serverTimeEpoch: Long,
    val settings: @RawValue SettingsDto,
    val status: String,
    val version: String,
) : Parcelable {
    fun toServerStatus(): ServerStatus = ServerStatus(
        apiEnabled = apiEnabled,
        name = name,
        serverTime = serverTime,
        serverTimeEpoch = serverTimeEpoch,
        settings = settings.toSettings(),
        status = status,
        version = version
    )
}



