package com.volvocars.wearablemonitor.presentation.util

import android.content.Context
import com.volvocars.wearablemonitor.R
import com.volvocars.wearablemonitor.domain.model.Thresholds
import kotlin.math.pow
import kotlin.math.roundToInt

fun calculateDifference(sgv1: Int, sgv2: Int, isUnitMmol: Boolean): String {
    return round(sgvToUnit(sgv1, isUnitMmol) - sgvToUnit(sgv2, isUnitMmol), 1.0f).toString()
}

private fun round(value: Float, precision: Float): Float {
    val scale = 10.0f.pow(precision)
    return ((value * scale).roundToInt() / scale)
}

fun <T> sgvToUnit(sgv: T, isUnitMmol: Boolean): Float where T : Number {
    return if (isUnitMmol) {
        ((sgv.toFloat() / 18.0f) * 10.0f) / 10.0f
    } else {
        sgv.toFloat()
    }
}

fun unitToSvg(unit: Float, isUnitMmol: Boolean): Float {
    return if (isUnitMmol) {
        val converted = unit * 18.0182f
        round(converted, 0f)
    } else {
        unit
    }
}

fun Float.oneDecimalPrecision(): String {
    return "%.1f".format(this)
}

fun Int.toColor(context: Context, thresholds: Thresholds): Int {
    val colorValueOutOfRange = context.getColor(R.color.value_out_of_range)
    val colorValueInRangeAndOk = context.getColor(R.color.value_is_in_range_and_ok)
    val colorValueInRangeAndNok = context.getColor(R.color.value_is_in_range_and_nok)

    return when {
        this >= thresholds.bgHigh || this <= thresholds.bgLow -> colorValueOutOfRange
        this in thresholds.bgTargetBottom..thresholds.bgTargetTop -> colorValueInRangeAndOk
        else -> colorValueInRangeAndNok
    }
}