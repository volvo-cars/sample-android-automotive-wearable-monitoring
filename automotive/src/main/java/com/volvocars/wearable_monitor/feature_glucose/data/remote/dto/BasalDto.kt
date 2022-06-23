package com.volvocars.wearable_monitor.feature_glucose.data.remote.dto

import com.volvocars.wearable_monitor.feature_glucose.domain.model.Basal
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class BasalDto(
    @SerialName("render")
    val render: String,
) {
    fun toBasal(): Basal = Basal(
        render
    )
}
