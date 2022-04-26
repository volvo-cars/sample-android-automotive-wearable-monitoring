package com.volvocars.diabetesmonitor.feature_glucose.data.remote.dto

import android.os.Parcelable
import com.volvocars.diabetesmonitor.feature_glucose.domain.model.Basal
import kotlinx.parcelize.Parcelize

@Parcelize
data class BasalDto(
    val render: String,
) : Parcelable {
    fun toBasal(): Basal = Basal(
        render
    )
}
