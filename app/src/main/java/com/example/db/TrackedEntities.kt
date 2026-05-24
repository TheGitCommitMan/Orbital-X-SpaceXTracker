package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracked_locations")
data class TrackedLocation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val isDefault: Boolean = false
)

@Entity(tableName = "pass_alerts")
data class PassAlert(
    @PrimaryKey val id: String,
    val satelliteName: String,
    val passTimeMs: Long,
    val elevation: Double,
    val direction: String,
    val isEnabled: Boolean = true,
    val notified: Boolean = false
)
