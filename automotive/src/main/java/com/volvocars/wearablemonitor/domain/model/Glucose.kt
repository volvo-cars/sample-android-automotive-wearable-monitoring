package com.volvocars.wearablemonitor.domain.model

/**
 * Pure domain model for a CGM glucose reading.
 * Display-specific conversions (mmol/L, unicode arrows) are computed in the presentation mapper.
 */
data class Glucose(
    val id: String,
    /** Sensor glucose value in mg/dL */
    val sgv: Int,
    val date: Long,
    val dateString: String,
    /** Raw trend string from the API (e.g. "Flat", "SingleUp") */
    val trend: String,
    val type: String,
) : Comparable<Glucose> {
    override fun compareTo(other: Glucose): Int = this.date.compareTo(other.date)
}
