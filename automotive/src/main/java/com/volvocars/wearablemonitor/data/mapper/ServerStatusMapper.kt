package com.volvocars.wearablemonitor.data.mapper

import com.volvocars.wearablemonitor.data.remote.dto.ServerStatusDto
import com.volvocars.wearablemonitor.domain.model.ServerStatus

fun ServerStatusDto.toServerStatus() = ServerStatus(
    apiEnabled = apiEnabled,
    name = name,
    serverTime = serverTime,
    serverTimeEpoch = serverTimeEpoch,
    settings = settings.toSettings(),
    status = status,
    version = version
)