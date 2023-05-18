package com.volvocars.wearablemonitor.data.remote.dto

import com.volvocars.wearablemonitor.domain.model.Basal
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class BasalDto(
    @SerialName("render")
    val render: String,
) {
    fun toBasal() = Basal(
        render
    )
}
