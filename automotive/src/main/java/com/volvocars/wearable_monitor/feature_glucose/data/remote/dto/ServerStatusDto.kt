package com.volvocars.wearable_monitor.feature_glucose.data.remote.dto

import com.volvocars.wearable_monitor.feature_glucose.domain.model.ServerStatus
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class ServerStatusDto(
    @SerialName("apiEnabled")
    val apiEnabled: Boolean,
    @SerialName("authorized")
    val authorized: String?,
    @SerialName("boluscalcEnabled")
    val boluscalcEnabled: Boolean,
    @SerialName("careportalEnabled")
    val careportalEnabled: Boolean,
    @SerialName("extendedSettings")
    val extendedSettings: ExtendedSettingsDto,
    @SerialName("name")
    val name: String,
    @SerialName("serverTime")
    val serverTime: String,
    @SerialName("serverTimeEpoch")
    val serverTimeEpoch: Long,
    @SerialName("settings")
    val settings: SettingsDto,
    @SerialName("status")
    val status: String,
    @SerialName("version")
    val version: String,
) {
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



