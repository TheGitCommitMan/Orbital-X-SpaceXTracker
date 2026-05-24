package com.example.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedLocationDao {
    @Query("SELECT * FROM tracked_locations ORDER BY id ASC")
    fun getAllLocations(): Flow<List<TrackedLocation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: TrackedLocation)

    @Delete
    suspend fun deleteLocation(location: TrackedLocation)

    @Query("UPDATE tracked_locations SET isDefault = (id = :selectedId)")
    suspend fun setDefaultLocation(selectedId: Int)
}

@Dao
interface PassAlertDao {
    @Query("SELECT * FROM pass_alerts ORDER BY passTimeMs ASC")
    fun getAllAlerts(): Flow<List<PassAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: PassAlert)

    @Delete
    suspend fun deleteAlert(alert: PassAlert)

    @Query("UPDATE pass_alerts SET notified = 1 WHERE id = :id")
    suspend fun markAsNotified(id: String)

    @Query("DELETE FROM pass_alerts WHERE passTimeMs < :currentTimeMs")
    suspend fun deleteExpiredAlerts(currentTimeMs: Long)
}
