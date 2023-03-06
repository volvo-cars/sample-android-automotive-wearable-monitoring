package com.volvocars.wearable_monitor.feature_glucose.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.volvocars.wearable_monitor.feature_glucose.data.local.dao.GlucoseDao
import com.volvocars.wearable_monitor.feature_glucose.data.local.entity.GlucoseEntity

@Database(
    entities = [GlucoseEntity::class],
    version = 2,
    exportSchema = false
)
abstract class GlucoseDatabase : RoomDatabase() {
    abstract fun glucoseDao(): GlucoseDao
}