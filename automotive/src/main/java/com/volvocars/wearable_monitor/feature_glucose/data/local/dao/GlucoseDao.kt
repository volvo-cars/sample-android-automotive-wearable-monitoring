package com.volvocars.wearable_monitor.feature_glucose.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.volvocars.wearable_monitor.feature_glucose.data.local.entity.GlucoseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GlucoseDao {

    /**
     * Insert [GlucoseEntity] to room database
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGlucoseEntity(glucose: GlucoseEntity)

    /**
     * Insert a [List] of [GlucoseEntity] to the room database
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGlucoseEntityList(glucose: List<GlucoseEntity>)

    /**
     * Observe list with counts
     * @return A flow of glucose-list
     */
    @Query("SELECT * FROM glucose_values ORDER BY date DESC LIMIT :counts")
    fun fetchGlucoseEntities(counts: Int): Flow<List<GlucoseEntity>>


    /**
     * The idea is to wipe all the data before insert new
     * (so we only cache the data from the latest API call)
     */
    @Query("DELETE FROM glucose_values")
    fun deleteAllGlucoseEntities()
}