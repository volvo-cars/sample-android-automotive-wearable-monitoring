package com.volvocars.wearablemonitor.domain.usecase

import com.volvocars.wearablemonitor.domain.util.ThresholdUnit
import javax.inject.Inject

class GetThresholds @Inject constructor(
    private val getThreshold: GetThreshold
) {

    operator fun invoke(): com.volvocars.wearablemonitor.domain.model.Thresholds {
        return com.volvocars.wearablemonitor.domain.model.Thresholds(
            bgLow = getThreshold(ThresholdUnit.LOW),
            bgHigh = getThreshold(ThresholdUnit.HIGH),
            bgTargetBottom = getThreshold(ThresholdUnit.TARGET_LOW),
            bgTargetTop = getThreshold(ThresholdUnit.TARGET_HIGH)
        )
    }
}