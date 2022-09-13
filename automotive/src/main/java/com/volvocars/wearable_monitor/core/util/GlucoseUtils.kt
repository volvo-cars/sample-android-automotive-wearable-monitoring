package com.volvocars.wearable_monitor.core.util

import android.content.Context
import com.volvocars.wearable_monitor.R
import com.volvocars.wearable_monitor.feature_glucose.data.storage.SharedPreferenceStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.roundToInt

class GlucoseUtils @Inject constructor(
    private val sharedPreferenceStorage: SharedPreferenceStorage,
    @ApplicationContext context: Context,
) {
    private val colorValueOutOfRange = context.getColor(R.color.value_out_of_range)
    private val colorValueInRangeAndOk = context.getColor(R.color.value_is_in_range_and_ok)
    private val colorValueInRangeAndNok = context.getColor(R.color.value_is_in_range_and_nok)

    /**
     * Convert sgv value to a color
     * @param sgv Glucose point sgv color
     * @return color value
     */
    fun sgvColor(sgv: Int): Int {
        return when {
            sgv >= sharedPreferenceStorage.getThresholdHigh() || sgv <= sharedPreferenceStorage.getThresholdLow() -> colorValueOutOfRange
            sgv in sharedPreferenceStorage.getThresholdTargetLow()..sharedPreferenceStorage.getThresholdTargetHigh() -> colorValueInRangeAndOk
            else -> colorValueInRangeAndNok
        }
    }

    /**
     *  Calculate the difference between 2 glucose points sgv values
     *  @param sgv1 first sgv color
     *  @param sgv2 second sgv color
     *  @return the difference between the two values to string
     */
    fun calculateDifference(sgv1: Int, sgv2: Int): String {
        return round(sgvToUnit(sgv1) - sgvToUnit(sgv2), 1.0f).toString()
    }

    private fun round(value: Float, precision: Float): Float {
        val scale = 10.0f.pow(precision)
        return ((value * scale).roundToInt() / scale)
    }

    fun sgvToUnit(sgv: Int): Float {
        return when (checkIsMmol()) {
            false -> sgv.toFloat()
            true -> ((sgv / 18.0f) * 10.0f) / 10.0f
        }
    }

    fun sgvToUnitText(sgv: String): String {
        return when (checkIsMmol()) {
            false -> sgv
            true -> {
                val unit = ((sgv.toFloat() / 18.0f) * 10.0f) / 10.0f
                "%.1f".format(unit)
            }
        }
    }

    fun unitToSvg(unit: Float): Float {
        return when (checkIsMmol()) {
            false -> unit
            true -> {
                val converted = unit * 18.0182f
                round(converted, 0f)
            }
        }
    }

    /**
     * Check if the current value of key unit is mmol
     * @return true if unit value is mmol else false
     */
    fun checkIsMmol(): Boolean {
        return when (sharedPreferenceStorage.getUnit() == Constants.KEY_MMOL) {
            false -> false
            true -> true
        }
    }
}