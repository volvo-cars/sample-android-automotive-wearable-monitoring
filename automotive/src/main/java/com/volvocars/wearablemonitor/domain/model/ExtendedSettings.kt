package com.volvocars.wearablemonitor.domain.model

data class ExtendedSettings(
    val basal: com.volvocars.wearablemonitor.domain.model.Basal,
    val devicestatus: com.volvocars.wearablemonitor.domain.model.DeviceStatus,
    val profile: com.volvocars.wearablemonitor.domain.model.Profile,
)