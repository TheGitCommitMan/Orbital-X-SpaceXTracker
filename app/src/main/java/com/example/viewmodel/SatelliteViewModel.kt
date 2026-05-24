package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.db.PassAlert
import com.example.db.SatelliteDatabase as AppSatelliteDatabase
import com.example.db.SatelliteRepository
import com.example.db.TrackedLocation
import com.example.math.OrbitPhysics
import com.example.math.PredictedPass
import com.example.network.SpaceXClient
import com.example.network.SpaceTrackInfo
import com.example.network.StarlinkSatellite
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class Tab {
    MAP, DIRECTORY, ALERTS, LOCATIONS
}

class SatelliteViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppSatelliteDatabase.getDatabase(application)
    private val repository = SatelliteRepository(
        database.trackedLocationDao(),
        database.passAlertDao()
    )

    // UI States
    val activeTab = MutableStateFlow(Tab.MAP)
    val satellites = MutableStateFlow<List<StarlinkSatellite>>(emptyList())
    val isLoading = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)
    
    val selectedSatellite = MutableStateFlow<StarlinkSatellite?>(null)
    val selectedLocation = MutableStateFlow<TrackedLocation?>(null)
    
    val trackedLocations: StateFlow<List<TrackedLocation>> = repository.allLocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingPasses = MutableStateFlow<List<PredictedPass>>(emptyList())
    
    val passAlerts: StateFlow<List<PassAlert>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var coordinatesUpdateJob: Job? = null

    init {
        // Initialize default tracking locations if database is empty
        viewModelScope.launch {
            trackedLocations.collectLatest { locations ->
                if (locations.isEmpty()) {
                    val defaults = listOf(
                        TrackedLocation(name = "Munich, Germany", latitude = 48.1351, longitude = 11.5820, isDefault = true),
                        TrackedLocation(name = "Austin, Texas", latitude = 30.2672, longitude = -97.7431),
                        TrackedLocation(name = "London, UK", latitude = 51.5074, longitude = -0.1278),
                        TrackedLocation(name = "Kennedy Space Center", latitude = 28.5729, longitude = -80.6490),
                        TrackedLocation(name = "Tokyo, Japan", latitude = 35.6762, longitude = 139.6503)
                    )
                    defaults.forEach { repository.insertLocation(it) }
                } else if (selectedLocation.value == null) {
                    val defaultLoc = locations.find { it.isDefault } ?: locations.first()
                    selectedLocation.value = defaultLoc
                    recalculateupcomingPasses()
                }
            }
        }

        // Fetch Live Satellites on launch
        fetchSatellites()

        // Start dynamic orbital movement simulation for a real-time tracking look
        startRealtimeSimulation()
    }

    fun selectTab(tab: Tab) {
        activeTab.value = tab
    }

    fun selectSatellite(satellite: StarlinkSatellite) {
        selectedSatellite.value = satellite
        recalculateupcomingPasses()
    }

    fun selectLocation(location: TrackedLocation) {
        viewModelScope.launch {
            selectedLocation.value = location
            repository.setDefaultLocation(location.id)
            recalculateupcomingPasses()
        }
    }

    fun addTrackedLocation(name: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            val newLoc = TrackedLocation(name = name, latitude = lat, longitude = lng)
            repository.insertLocation(newLoc)
        }
    }

    fun deleteTrackedLocation(location: TrackedLocation) {
        viewModelScope.launch {
            repository.deleteLocation(location)
            if (selectedLocation.value?.id == location.id) {
                selectedLocation.value = null
            }
        }
    }

    fun fetchSatellites() {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null
            try {
                val response = SpaceXClient.service.getStarlinkSatellites()
                // Filter out satellites without valid orbital info so we don't display deorbited ones
                val activeSats = response.filter { it.latitude != null && it.longitude != null }
                if (activeSats.isNotEmpty()) {
                    satellites.value = activeSats
                    selectedSatellite.value = activeSats.first()
                    recalculateupcomingPasses()
                } else {
                    loadFallbackSatellites()
                }
            } catch (e: Exception) {
                error.value = "SpaceX API error: ${e.localizedMessage}. Loading orbital fallback models."
                loadFallbackSatellites()
            } finally {
                isLoading.value = false
            }
        }
    }

    private fun loadFallbackSatellites() {
        // High fidelity simulated Starlink and satellite constellation
        val simulated = mutableListOf<StarlinkSatellite>()
        val baseTimeMs = System.currentTimeMillis()

        // Generate 32 realistic Starlink satellites
        val names = listOf(
            "Starlink-1042", "Starlink-1088", "Starlink-1123", "Starlink-1224",
            "Starlink-1456", "Starlink-1502", "Starlink-1704", "Starlink-1925",
            "Starlink-2104", "Starlink-2144", "Starlink-2219", "Starlink-2380",
            "Starlink-2419", "Starlink-2450", "Starlink-2501", "Starlink-2668",
            "Starlink-2804", "Starlink-2930", "Starlink-3022", "Starlink-3112",
            "Starlink-3209", "Starlink-3345", "Starlink-3419", "Starlink-3500",
            "Starlink-3677", "Starlink-3801", "Starlink-3914", "Starlink-4011",
            "Starlink-4122", "Starlink-4233", "Starlink-4412", "Sky-Track-V"
        )

        for (i in names.indices) {
            // Distribute satellites evenly across different orbital phase and nodal longitudes
            val phase = (i * 2 * Math.PI) / names.size
            val node = (i * 360.0) / names.size - 180.0
            
            // Calculate starting position using orbital formulas
            val inclination = 53.2
            val incRad = Math.toRadians(inclination)
            
            val latRad = Math.asin(Math.sin(incRad) * Math.sin(phase))
            val lonOffsetRad = Math.atan2(Math.cos(incRad) * Math.sin(phase), Math.cos(phase))
            
            val lat = Math.toDegrees(latRad)
            val lng = OrbitPhysics.normalizeLongitude(node + Math.toDegrees(lonOffsetRad))

            simulated.add(
                StarlinkSatellite(
                    id = "starlink_sim_$i",
                    version = "v1.5",
                    latitude = lat,
                    longitude = lng,
                    heightKm = 540.0 + (i % 5) * 4.0,
                    velocityKms = 7.6 + (i % 3) * 0.05,
                    spaceTrack = SpaceTrackInfo(
                        objectName = names[i],
                        launchDate = "2024-03-${10 + (i % 15)}",
                        inclination = inclination,
                        decayed = 0
                    )
                )
            )
        }

        // Add 1 extra special tracked orbital baseline: ISS (ZARYA)
        simulated.add(
            0,
            StarlinkSatellite(
                id = "iss_sim",
                version = "Station",
                latitude = -22.5,
                longitude = -42.0,
                heightKm = 418.0,
                velocityKms = 7.66,
                spaceTrack = SpaceTrackInfo(
                    objectName = "ISS (ZARYA)",
                    launchDate = "1998-11-20",
                    inclination = 51.64,
                    decayed = 0
                )
            )
        )

        satellites.value = simulated
        selectedSatellite.value = simulated.firstOrNull { it.id == "iss_sim" } ?: simulated.firstOrNull()
        recalculateupcomingPasses()
    }

    /**
     * Ticks the coordinates of all satellites slightly every second to simulate live flying orbits.
     */
    private fun startRealtimeSimulation() {
        coordinatesUpdateJob?.cancel()
        coordinatesUpdateJob = viewModelScope.launch {
            while (true) {
                delay(2000) // Update position every 2 seconds
                val currentSats = satellites.value
                if (currentSats.isNotEmpty()) {
                    val updated = currentSats.map { sat ->
                        val lat = sat.latitude ?: 0.0
                        val lng = sat.longitude ?: 0.0
                        val vel = sat.velocityKms ?: 7.6
                        val inc = sat.spaceTrack?.inclination ?: 53.2
                        
                        // Satellite travels approximately 15.2 km (7.6 km/s * 2 sec)
                        // This translates into an angular shift of ~0.13 degrees in its phase
                        val phaseShiftRad = 0.0024 // approx 0.13 degrees
                        
                        // Re-solve position by shifting phase slightly or just shifting longitude/latitude along inclination
                        val incRad = Math.toRadians(inc)
                        val latRad = Math.toRadians(lat)
                        
                        // Approximate next step
                        val sinDelta0 = (Math.sin(latRad) / Math.sin(incRad)).coerceIn(-1.0, 1.0)
                        val delta0 = Math.asin(sinDelta0)
                        val deltaNew = delta0 + phaseShiftRad
                        
                        val newLatRad = Math.asin(Math.sin(incRad) * Math.sin(deltaNew))
                        // Shift longitude eastward slightly to compensate Earth's tilt + satellite velocity
                        val newLng = OrbitPhysics.normalizeLongitude(lng + 0.15)
                        val newLat = Math.toDegrees(newLatRad)

                        sat.copy(
                            latitude = newLat,
                            longitude = newLng
                        )
                    }
                    satellites.value = updated

                    // Keep selected satellite reference updated
                    selectedSatellite.value = updated.find { it.id == selectedSatellite.value?.id }
                }
            }
        }
    }

    private fun recalculateupcomingPasses() {
        val sat = selectedSatellite.value ?: return
        val loc = selectedLocation.value ?: return
        val satLat = sat.latitude ?: return
        val satLng = sat.longitude ?: return
        
        viewModelScope.launch {
            val passes = OrbitPhysics.predictUpcomingPasses(
                userLat = loc.latitude,
                userLng = loc.longitude,
                satLat = satLat,
                satLng = satLng,
                inclinationDeg = sat.spaceTrack?.inclination ?: 53.2,
                heightKm = sat.heightKm ?: 550.0,
                satelliteId = sat.id,
                satelliteName = sat.spaceTrack?.objectName ?: "SpaceX Starlink"
            )
            upcomingPasses.value = passes
        }
    }

    /**
     * Configures a real alarm/system notification pass alert on the device.
     */
    fun createPassAlert(predicted: PredictedPass) {
        viewModelScope.launch {
            val alert = PassAlert(
                id = UUID.randomUUID().toString(),
                satelliteName = predicted.satelliteName,
                passTimeMs = predicted.passTimeMs,
                elevation = predicted.maxElevation,
                direction = predicted.direction,
                isEnabled = true
            )
            repository.insertAlert(alert)

            // Calculate alert trigger delay.
            // For testing/prototype visibility, if the pass is scheduled in the future,
            // we can trigger the alarm exactly. To make it highly impressive for users,
            // we can offer a demo "Trigger Now (5s delay)" toggle or schedule it exactly on the pass clock.
            // Let's schedule it exactly on the passTimeMs, or if user wants to test it, we can trigger
            // a tester notification immediately so the user can easily verify push alerts work perfectly on AI Studio!
            // Yes, let's schedule the real one:
            NotificationHelper.schedulePassNotification(
                context = getApplication(),
                passTimeMs = predicted.passTimeMs,
                title = "Satellite Pass Incoming! 🛰️",
                message = "${predicted.satelliteName} will pass directly over your location in 2 minutes at ${predicted.maxElevation.toInt()}° altitude!",
                alertId = alert.id
            )
        }
    }

    /**
     * Triggers a push notification immediately to let the user preview the amazing alert look.
     */
    fun triggerInstantDemoNotification(predicted: PredictedPass) {
        val demoTimeMs = System.currentTimeMillis() + 3000 // 3 seconds in future
        viewModelScope.launch {
            NotificationHelper.schedulePassNotification(
                context = getApplication(),
                passTimeMs = demoTimeMs,
                title = "Incoming Orbit: ${predicted.satelliteName} 🚀",
                message = "Live Pass Alert: Starlink transit approaching. Max altitude ${predicted.maxElevation.toInt()}° in direction ${predicted.direction}!",
                alertId = "demo_instant_${System.currentTimeMillis()}"
            )
        }
    }

    fun deletePassAlert(alert: PassAlert) {
        viewModelScope.launch {
            NotificationHelper.cancelPassNotification(getApplication(), alert.id)
            repository.deleteAlert(alert)
        }
    }

    override fun onCleared() {
        super.onCleared()
        coordinatesUpdateJob?.cancel()
    }
}
