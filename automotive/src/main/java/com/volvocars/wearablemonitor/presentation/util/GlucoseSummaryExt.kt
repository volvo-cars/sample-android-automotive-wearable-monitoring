package com.volvocars.wearablemonitor.presentation.util

import android.text.format.DateUtils
import com.volvocars.wearablemonitor.domain.model.Thresholds
import com.volvocars.wearablemonitor.presentation.model.GlucoseSummary
import java.util.Calendar

fun GlucoseSummary.isOutOfRange(threshold: Thresholds): Boolean {
    return sgv >= threshold.bgHigh || sgv <= threshold.bgLow
}

fun GlucoseSummary.getRelativeTimeSpan() = DateUtils.getRelativeTimeSpanString(
    date, Calendar.getInstance().timeInMillis, 0
).toString()
