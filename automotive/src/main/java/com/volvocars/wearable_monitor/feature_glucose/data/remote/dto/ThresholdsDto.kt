package com.volvocars.wearable_monitor.feature_glucose.data.remote.dto

import android.os.Parcelable
import com.volvocars.wearable_monitor.feature_glucose.domain.model.Thresholds
import kotlinx.parcelize.Parcelize

@Parcelize
data class ThresholdsDto(
    val bgHigh: Long,
    val bgLow: Long,
    val bgTargetBottom: Long,
    val bgTargetTop: Long
) : Parcelable {
    fun toThresholds(): Thresholds = Thresholds(
        bgHigh,
        bgLow,
        bgTargetBottom,
        bgTargetTop
    )
}
