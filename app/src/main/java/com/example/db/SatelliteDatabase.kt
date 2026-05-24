package com.example.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TrackedLocation::class, PassAlert::class],
    version = 1,
    exportSchema = false
)
abstract class SatelliteDatabase : RoomDatabase() {
    abstract fun trackedLocationDao(): TrackedLocationDao
    abstract fun passAlertDao(): PassAlertDao

    companion object {
        @Volatile
        private var INSTANCE: SatelliteDatabase? = null

        fun getDatabase(context: Context): SatelliteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SatelliteDatabase::class.java,
                    "satellite_tracker_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
