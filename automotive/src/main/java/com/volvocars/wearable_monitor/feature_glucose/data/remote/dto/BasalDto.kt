package com.volvocars.wearable_monitor.feature_glucose.data.remote.dto

import android.os.Parcelable
import com.volvocars.wearable_monitor.feature_glucose.domain.model.Basal
import kotlinx.parcelize.Parcelize

@Parcelize
data class BasalDto(
    val render: String,
) : Parcelable {
    fun toBasal(): Basal = Basal(
        render
    )
}
