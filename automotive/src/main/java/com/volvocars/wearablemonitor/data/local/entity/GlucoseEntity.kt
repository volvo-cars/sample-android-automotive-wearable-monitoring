package com.volvocars.wearablemonitor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "glucose_values")
data class GlucoseEntity(
    @PrimaryKey val id: String,
    val sgv: Int,
    val date: Long,
    val dateString: String,
    val trend: String,
    val direction: String,
    val type: String,
)
