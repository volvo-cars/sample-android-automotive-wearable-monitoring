package com.volvocars.wearable_monitor.feature_glucose.data.remote.dto

import com.volvocars.wearable_monitor.feature_glucose.domain.model.Settings
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class SettingsDto(
    @SerialName("alarmHigh")
    val alarmHigh: Boolean,

    @SerialName("alarmHighMins")
    val alarmHighMins: List<Long>,

    @SerialName("alarmLow")
    val alarmLow: Boolean,

    @SerialName("alarmLowMins")
    val alarmLowMins: List<Long>,

    @SerialName("alarmPumpBatteryLow")
    val alarmPumpBatteryLow: Boolean,

    @SerialName("alarmTimeagoUrgent")
    val alarmTimeagoUrgent: Boolean,

    @SerialName("alarmTimeagoUrgentMins")
    val alarmTimeagoUrgentMins: Long,

    @SerialName("alarmTimeagoWarn")
    val alarmTimeagoWarn: Boolean,

    @SerialName("alarmTimeagoWarnMins")
    val alarmTimeagoWarnMins: Long,

    @SerialName("alarmTypes")
    val alarmTypes: List<String>,

    @SerialName("alarmUrgentHigh")
    val alarmUrgentHigh: Boolean,

    @SerialName("alarmUrgentHighMins")
    val alarmUrgentHighMins: List<Long>,

    @SerialName("alarmUrgentLow")
    val alarmUrgentLow: Boolean,

    @SerialName("alarmUrgentLowMins")
    val alarmUrgentLowMins: List<Long>,

    @SerialName("alarmUrgentMins")
    val alarmUrgentMins: List<Long>,

    @SerialName("alarmWarnMins")
    val alarmWarnMins: List<Long>,

    @SerialName("authDefaultRoles")
    val authDefaultRoles: String,

    @SerialName("baseURL")
    val baseURL: String,

    @SerialName("customTitle")
    val customTitle: String,

    @SerialName("DEFAULT_FEATURES")
    val DEFAULT_FEATURES: List<String>,

    @SerialName("editMode")
    val editMode: Boolean,

    @SerialName("enable")
    val enable: List<String>,

    @SerialName("focusHours")
    val focusHours: Long,

    @SerialName("heartbeat")
    val heartbeat: Long,

    @SerialName("insecureUseHttp")
    val insecureUseHttp: Boolean,

    @SerialName("language")
    val language: String,

    @SerialName("nightMode")
    val nightMode: Boolean,

    @SerialName("scaleY")
    val scaleY: String,

    @SerialName("secureCsp")
    val secureCsp: Boolean,

    @SerialName("secureHstsHeader")
    val secureHstsHeader: Boolean,

    @SerialName("secureHstsHeaderIncludeSubdomains")
    val secureHstsHeaderIncludeSubdomains: Boolean,

    @SerialName("secureHstsHeaderPreload")
    val secureHstsHeaderPreload: Boolean,

    @SerialName("showForecast")
    val showForecast: String,

    @SerialName("showPlugins")
    val showPlugins: String,

    @SerialName("showRawbg")
    val showRawbg: String,

    @SerialName("theme")
    val theme: String,

    @SerialName("thresholds")
    val thresholds: ThresholdsDto,

    @SerialName("timeFormat")
    val timeFormat: Long,

    @SerialName("units")
    val units: String,
) {
    fun toSettings(): Settings = Settings(
        alarmHigh = alarmHigh,
        alarmHighMins = alarmHighMins,
        alarmLow = alarmLow,
        alarmLowMins = alarmLowMins,
        alarmPumpBatteryLow = alarmPumpBatteryLow,
        alarmTimeagoUrgent = alarmTimeagoUrgent,
        alarmTimeagoUrgentMins = alarmTimeagoUrgentMins,
        alarmTimeagoWarn = alarmTimeagoWarn,
        alarmTimeagoWarnMins = alarmTimeagoWarnMins,
        alarmUrgentHigh = alarmUrgentHigh,
        alarmUrgentHighMins = alarmUrgentHighMins,
        alarmUrgentLow = alarmUrgentLow,
        alarmUrgentLowMins = alarmUrgentLowMins,
        alarmUrgentMins = alarmUrgentMins,
        alarmWarnMins = alarmWarnMins,
        baseURL = baseURL,
        thresholds = thresholds.toThresholds(),
        timeFormat = timeFormat,
        units = units,
    )
}