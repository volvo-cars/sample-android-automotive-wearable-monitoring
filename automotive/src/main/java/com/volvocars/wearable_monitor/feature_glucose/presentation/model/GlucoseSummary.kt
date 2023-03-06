package com.volvocars.wearable_monitor.feature_glucose.presentation.model

data class GlucoseSummary(
    val id: String,
    val sgv: Int,
    val sgvMmol: Float,
    val sgvUnit: Float,
    val date: Long,
    val dateString: String,
    val trend: String,
    val direction: String,
) : Comparable<GlucoseSummary> {
    override fun compareTo(
        other: GlucoseSummary
    ): Int = this.date.compareTo(other.date)
}