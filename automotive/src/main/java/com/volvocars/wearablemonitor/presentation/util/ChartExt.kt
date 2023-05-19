package com.volvocars.wearablemonitor.presentation.util

import android.graphics.Color
import android.graphics.Typeface
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import com.volvocars.wearablemonitor.core.util.Constants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

fun YAxis.configureGlucoseValues(
    limitLines: List<LimitLine>,
    isUnitMmol: Boolean
) {
    removeAllLimitLines()
    addLimitLine(limitLines[0])
    addLimitLine(limitLines[1])
    addLimitLine(limitLines[2])
    addLimitLine(limitLines[3])
    setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
    typeface = Typeface.DEFAULT
    textColor = Color.WHITE
    textSize = 28f
    setDrawGridLines(false)
    setDrawAxisLine(false)
    isGranularityEnabled = false
    axisMinimum = if (isUnitMmol) -1f else 18f
    axisMaximum = if (isUnitMmol) 22f else 360f
    textColor = Color.WHITE
}

fun XAxis.configure(timeFormat: Long, locale: Locale) {
    position = XAxis.XAxisPosition.BOTTOM_INSIDE
    typeface = Typeface.DEFAULT_BOLD
    textSize = 28f
    textColor = Color.WHITE
    setDrawAxisLine(false)
    setDrawGridLines(false)
    setCenterAxisLabels(false)
    granularity = 6f // one hour
    isGranularityEnabled = true
    valueFormatter = xAxisValueFormatter(timeFormat, locale)
}

private fun xAxisValueFormatter(timeFormat: Long, locale: Locale) = object : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return when (timeFormat) {
            Constants.TIME_FORMAT_DEFAULT_VALUE -> SimpleDateFormat("H:mm", locale)
            else -> SimpleDateFormat("hh.mm aa", locale)
        }.apply {
            timeZone = TimeZone.getDefault()
        }.format(Calendar.getInstance().apply { timeInMillis = value.toLong() }.time)
    }
}
