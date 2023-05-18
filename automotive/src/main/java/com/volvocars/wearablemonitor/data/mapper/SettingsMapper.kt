package com.volvocars.wearablemonitor.data.mapper

import com.volvocars.wearablemonitor.data.remote.dto.SettingsDto
import com.volvocars.wearablemonitor.domain.model.Settings

fun SettingsDto.toSettings() = Settings(
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