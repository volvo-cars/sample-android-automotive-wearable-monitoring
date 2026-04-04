package com.volvocars.wearablemonitor.domain.usecase

import com.volvocars.wearablemonitor.domain.model.Thresholds
import com.volvocars.wearablemonitor.domain.util.ThresholdUnit
import javax.inject.Inject

class GetThresholds @Inject constructor(
    private val getThreshold: GetThreshold,
) {

    operator fun invoke(): Thresholds = Thresholds(
        bgLow = getThreshold(ThresholdUnit.LOW),
        bgHigh = getThreshold(ThresholdUnit.HIGH),
        bgTargetBottom = getThreshold(ThresholdUnit.TARGET_LOW),
        bgTargetTop = getThreshold(ThresholdUnit.TARGET_HIGH),
    )
}
