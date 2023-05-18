package com.volvocars.wearablemonitor.domain.model

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
) : Comparable<com.volvocars.wearablemonitor.domain.model.Glucose> {
    override fun compareTo(other: com.volvocars.wearablemonitor.domain.model.Glucose): Int = this.date.compareTo(other.date)
}