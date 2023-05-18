package com.volvocars.wearablemonitor.data.mapper

import com.volvocars.wearablemonitor.data.remote.dto.ThresholdsDto
import com.volvocars.wearablemonitor.domain.model.Thresholds

fun ThresholdsDto.toThresholds() = Thresholds(
    bgHigh,
    bgLow,
    bgTargetBottom,
    bgTargetTop
)