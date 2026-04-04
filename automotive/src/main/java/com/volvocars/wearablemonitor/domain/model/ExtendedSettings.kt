package com.volvocars.wearablemonitor.domain.model

data class ExtendedSettings(
    val basal: Basal,
    val devicestatus: DeviceStatus,
    val profile: Profile,
)
