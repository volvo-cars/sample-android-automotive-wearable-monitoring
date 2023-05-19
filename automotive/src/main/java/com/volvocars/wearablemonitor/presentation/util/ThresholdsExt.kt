package com.volvocars.wearablemonitor.presentation.util

import android.content.Context
import android.graphics.Color
import com.github.mikephil.charting.components.LimitLine
import com.volvocars.wearablemonitor.R
import com.volvocars.wearablemonitor.domain.model.Thresholds

fun Thresholds.toLimitLines(context: Context, isUnitMmol: Boolean): List<LimitLine> {
    return listOf(
        bgLow.toLimitLine(
            context.resources.getString(R.color.value_out_of_range),
            isUnitMmol
        ),
        bgTargetBottom.toLimitLine(
            context.resources.getString(R.color.value_is_in_range_and_nok),
            isUnitMmol
        ),
        bgHigh.toLimitLine(
            context.resources.getString(R.color.value_out_of_range),
            isUnitMmol
        ),
        bgTargetTop.toLimitLine(
            context.resources.getString(R.color.value_is_in_range_and_nok),
            isUnitMmol
        )
    )
}

private fun Long.toLimitLine(color: String, isUnitMmol: Boolean): LimitLine {
    return createLimitLine(this, color, isUnitMmol)
}

private fun createLimitLine(
    thresholdValue: Long, color: String, isUnitMmol: Boolean
): LimitLine {
    val (dashedLineLength, dashedSpaceLength, dashedLinePhase) = Triple(10f, 12f, 15f)
    val limit = sgvToUnit(thresholdValue.toInt(), isUnitMmol)

    return LimitLine(
        limit, ""
    ).apply {
        lineColor = Color.parseColor(color)
        textSize = 18f
        enableDashedLine(dashedLineLength, dashedSpaceLength, dashedLinePhase)
    }
}