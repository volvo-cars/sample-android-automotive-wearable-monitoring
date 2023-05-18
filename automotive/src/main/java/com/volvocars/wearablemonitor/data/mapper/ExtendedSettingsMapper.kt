package com.volvocars.wearablemonitor.data.mapper

import com.volvocars.wearablemonitor.data.remote.dto.ExtendedSettingsDto
import com.volvocars.wearablemonitor.domain.model.ExtendedSettings

fun ExtendedSettingsDto.toExtendedSettings() = ExtendedSettings(
    basal = basal.toBasal(),
    devicestatus = deviceStatus.toDeviceStatus(),
    profile = profile.toProfile()
)