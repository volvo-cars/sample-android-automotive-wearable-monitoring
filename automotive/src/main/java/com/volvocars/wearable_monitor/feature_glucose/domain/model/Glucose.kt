package com.volvocars.wearable_monitor.feature_glucose.domain.model

data class Glucose(
    val id: String,
    val sgv: Int,
    val sgvMmol: Float,
    val sgvUnit: Float,
    val date: Long,
    val dateString: String,
    val trend: String,
    val direction: String,
    val type: String,
) : Comparable<Glucose> {
    override fun compareTo(other: Glucose): Int = this.date.compareTo(other.date)
}