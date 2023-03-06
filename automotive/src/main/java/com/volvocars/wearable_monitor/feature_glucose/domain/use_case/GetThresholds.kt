package com.volvocars.wearable_monitor.feature_glucose.domain.use_case

import com.volvocars.wearable_monitor.feature_glucose.domain.model.Thresholds
import com.volvocars.wearable_monitor.feature_glucose.domain.util.ThresholdUnit
import javax.inject.Inject

class GetThresholds @Inject constructor(
    private val getThreshold: GetThreshold
) {

    operator fun invoke(): Thresholds {
        return Thresholds(
            bgLow = getThreshold(ThresholdUnit.LOW),
            bgHigh = getThreshold(ThresholdUnit.HIGH),
            bgTargetBottom = getThreshold(ThresholdUnit.TARGET_LOW),
            bgTargetTop = getThreshold(ThresholdUnit.TARGET_HIGH)
        )
    }
}