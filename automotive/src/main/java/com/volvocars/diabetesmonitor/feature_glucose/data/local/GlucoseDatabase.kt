package com.volvocars.diabetesmonitor.feature_glucose.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.volvocars.diabetesmonitor.feature_glucose.data.local.entity.GlucoseEntity

@Database(
    entities = [GlucoseEntity::class],
    version = 2,
    exportSchema = false
)
abstract class GlucoseDatabase : RoomDatabase() {
    abstract fun glucoseDao(): GlucoseDao
}