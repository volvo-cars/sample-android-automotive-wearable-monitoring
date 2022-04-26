package com.volvocars.diabetesmonitor.feature_glucose.data.remote.dto

import android.os.Parcelable
import com.volvocars.diabetesmonitor.feature_glucose.data.local.entity.GlucoseEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class GlucoseDto(
    val _id: String,
    val sgv: Int,
    val date: Long,
    val dateString: String,
    val trend: String,
    val direction: String,
    val type: String,
) : Parcelable {
    fun toGlucoseEntity() = GlucoseEntity(
        id = _id,
        sgv = sgv,
        date = date,
        dateString = dateString,
        trend = trend,
        direction = direction,
        type = type
    )
}
