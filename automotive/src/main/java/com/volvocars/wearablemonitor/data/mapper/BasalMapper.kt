package com.volvocars.wearablemonitor.data.mapper

import com.volvocars.wearablemonitor.data.remote.dto.BasalDto
import com.volvocars.wearablemonitor.domain.model.Basal

fun BasalDto.toBasal() = Basal(
    render
)