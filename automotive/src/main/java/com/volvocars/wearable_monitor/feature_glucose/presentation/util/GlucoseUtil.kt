package com.volvocars.wearable_monitor.feature_glucose.presentation.util

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
