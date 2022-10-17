package com.volvocars.wearable_monitor.feature_glucose.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.volvocars.wearable_monitor.feature_glucose.domain.model.Glucose
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.pow
import kotlin.math.roundToInt

@Entity(tableName = "glucose_values")
data class GlucoseEntity(
    @PrimaryKey val id: String,
    val sgv: Int,
    val date: Long,
    val dateString: String,
    val trend: String,
    val direction: String,
    val type: String,
) {
    fun toGlucose(): Glucose = Glucose(
        id = id,
        sgv = sgv,
        sgvMmol = round(sgvToUnitMmol(sgv), 1f),
        sgvUnit = sgv.toFloat(),
        date = date,
        dateString = getDate(dateString),
        trend = trend,
        direction = getUnicode(trend),
        type = type
    )

    private fun getUnicode(direction: String) = when (direction) {
        "NONE" -> "-"
        "DoubleUp" -> "⇈"
        "SingleUp" -> "↑"
        "FortyFiveUp" -> "↗"
        "Flat" -> "→"
        "FortyFiveDown" -> "↘"
        "SingleDown" -> "↓"
        "DoubleDown" -> "⇊"
        "RATE OUT OF RANGE" -> "⇕"
        else -> "-"
    }

    private fun round(value: Float, precision: Float): Float {
        val scale = 10.0f.pow(precision)
        return ((value * scale).roundToInt() / scale)
    }

    private fun getDate(date: String): String {
        val instant = Instant.parse(date)
        val localDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        return localDate.toString()
    }

    private fun sgvToUnitMmol(sgv: Int): Float {
        return ((sgv / 18.0f) * 10.0f) / 10.0f
    }
}
