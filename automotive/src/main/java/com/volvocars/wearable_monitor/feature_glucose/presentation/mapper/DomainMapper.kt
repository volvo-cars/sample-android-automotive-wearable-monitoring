package com.volvocars.wearable_monitor.feature_glucose.presentation.mapper

import com.volvocars.wearable_monitor.feature_glucose.domain.model.Glucose
import com.volvocars.wearable_monitor.feature_glucose.presentation.model.GlucoseSummary
import kotlin.math.roundToInt

fun Glucose.toPresentationModel(): GlucoseSummary {
    return GlucoseSummary(
        id = id,
        trend = trend,
        dateString = dateString,
        date = date,
        direction = direction,
        sgv = sgv,
        sgvUnit = sgv.toFloat(),
        sgvMmol = ((sgv / 18.0f) * 10.0f).roundToInt() / 10.0f
    )
}

fun List<Glucose>.toPresentationModels(): List<GlucoseSummary> {
    return map { it.toPresentationModel() }
}