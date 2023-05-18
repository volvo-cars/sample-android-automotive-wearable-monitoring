package com.volvocars.wearablemonitor.data.mapper

import com.volvocars.wearablemonitor.data.remote.dto.DeviceStatusDto
import com.volvocars.wearablemonitor.domain.model.DeviceStatus

fun DeviceStatusDto.toDeviceStatus() = DeviceStatus(
    advanced = advanced
)