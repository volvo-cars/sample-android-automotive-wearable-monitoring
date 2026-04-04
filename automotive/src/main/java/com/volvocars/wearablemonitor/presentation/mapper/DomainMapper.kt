package com.volvocars.wearablemonitor.presentation.mapper

import com.volvocars.wearablemonitor.domain.model.Glucose
import com.volvocars.wearablemonitor.presentation.model.GlucoseSummary
import kotlin.math.roundToInt

/** Convert a trend string (e.g. "SingleUp") to a unicode arrow for display. */
private fun trendToDirection(trend: String) = when (trend) {
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

fun Glucose.toPresentationModel() = GlucoseSummary(
    id = id,
    trend = trend,
    dateString = dateString,
    date = date,
    direction = trendToDirection(trend),
    sgv = sgv,
    sgvUnit = sgv.toFloat(),
    sgvMmol = ((sgv / 18.0f) * 10.0f).roundToInt() / 10.0f,
)

fun List<Glucose>.toPresentationModels() = map { it.toPresentationModel() }
