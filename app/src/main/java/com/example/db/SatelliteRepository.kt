package com.example.db

import kotlinx.coroutines.flow.Flow

class SatelliteRepository(
    private val locationDao: TrackedLocationDao,
    private val alertDao: PassAlertDao
) {
    val allLocations: Flow<List<TrackedLocation>> = locationDao.getAllLocations()
    val allAlerts: Flow<List<PassAlert>> = alertDao.getAllAlerts()

    suspend fun insertLocation(location: TrackedLocation) {
        locationDao.insertLocation(location)
    }

    suspend fun deleteLocation(location: TrackedLocation) {
        locationDao.deleteLocation(location)
    }

    suspend fun setDefaultLocation(id: Int) {
        locationDao.setDefaultLocation(id)
    }

    suspend fun insertAlert(alert: PassAlert) {
        alertDao.insertAlert(alert)
    }

    suspend fun deleteAlert(alert: PassAlert) {
        alertDao.deleteAlert(alert)
    }

    suspend fun markAsNotified(id: String) {
        alertDao.markAsNotified(id)
    }

    suspend fun deleteExpiredAlerts(currentTimeMs: Long) {
        alertDao.deleteExpiredAlerts(currentTimeMs)
    }
}
