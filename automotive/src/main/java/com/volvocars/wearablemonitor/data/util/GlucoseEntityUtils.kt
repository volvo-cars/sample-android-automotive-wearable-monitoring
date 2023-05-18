package com.volvocars.wearablemonitor.data.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.pow
import kotlin.math.roundToInt

fun getUnicode(direction: String) = when (direction) {
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

fun round(value: Float, precision: Float): Float {
    val scale = 10.0f.pow(precision)
    return ((value * scale).roundToInt() / scale)
}

fun getDate(date: String): String {
    val instant = Instant.parse(date)
    val localDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    return localDate.toString()
}

fun sgvToUnitMmol(sgv: Int) = ((sgv / 18.0f) * 10.0f) / 10.0f
