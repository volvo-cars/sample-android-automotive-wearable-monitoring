package com.volvocars.wearablemonitor.data.remote.dto

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class GlucoseDto(
    @SerialName("_id")
    val id: String,
    @SerialName("sgv")
    val sgv: Int,
    @SerialName("date")
    val date: Long,
    @SerialName("dateString")
    val dateString: String,
    @SerialName("trend")
    val trend: String,
    @SerialName("direction")
    val direction: String,
    @SerialName("type")
    val type: String,
)
