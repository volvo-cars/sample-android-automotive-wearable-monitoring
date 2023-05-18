package com.volvocars.wearablemonitor.domain.model

data class Settings(
    val alarmHigh: Boolean,
    val alarmHighMins: List<Long>,
    val alarmLow: Boolean,
    val alarmLowMins: List<Long>,
    val alarmPumpBatteryLow: Boolean,
    val alarmTimeagoUrgent: Boolean,
    val alarmTimeagoUrgentMins: Long,
    val alarmTimeagoWarn: Boolean,
    val alarmTimeagoWarnMins: Long,
    val alarmUrgentHigh: Boolean,
    val alarmUrgentHighMins: List<Long>,
    val alarmUrgentLow: Boolean,
    val alarmUrgentLowMins: List<Long>,
    val alarmUrgentMins: List<Long>,
    val alarmWarnMins: List<Long>,
    val baseURL: String,
    val thresholds: com.volvocars.wearablemonitor.domain.model.Thresholds,
    val timeFormat: Long,
    val units: String,
)