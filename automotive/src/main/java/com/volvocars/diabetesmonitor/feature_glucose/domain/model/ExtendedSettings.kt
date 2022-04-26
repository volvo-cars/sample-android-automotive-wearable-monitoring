package com.volvocars.diabetesmonitor.feature_glucose.domain.model

data class ExtendedSettings(
    val basal: Basal,
    val devicestatus: DeviceStatus,
    val profile: Profile,
)