package com.volvocars.wearablemonitor.presentation.util

import com.volvocars.wearablemonitor.domain.model.Thresholds
import com.volvocars.wearablemonitor.presentation.model.GlucoseSummary

fun GlucoseSummary.isOutOfRange(threshold: Thresholds): Boolean {
    return sgv >= threshold.bgHigh || sgv <= threshold.bgLow
}