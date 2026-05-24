package com.example.math

import kotlin.math.*

object OrbitPhysics {

    const val EARTH_RADIUS_KM = 6371.0
    const val SATELLITE_SPEED_KMS_DEFAULT = 7.6
    const val INCLINATION_STARLINK_DEFAULT = 53.2

    /**
     * Normalizes a longitude value to the range [-180, 180].
     */
    fun normalizeLongitude(lng: Double): Double {
        var normalized = (lng + 180) % 360
        if (normalized < 0) {
            normalized += 360
        }
        return normalized - 180
    }

    /**
     * Distance between two coordinates in km using Haversine formula.
     */
    fun haversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    /**
     * Generates a smooth set of LatLng points representing the orbital trajectory.
     * The orbit passes exactly through the satellite's current coordinate and is bound
     * by its inclination.
     */
    fun generateOrbitTrajectory(
        satLat: Double,
        satLng: Double,
        inclinationDeg: Double = INCLINATION_STARLINK_DEFAULT
    ): List<LatLng> {
        val incRad = Math.toRadians(inclinationDeg.coerceIn(0.1, 89.9))
        val satLatRad = Math.toRadians(satLat.coerceIn(-inclinationDeg, inclinationDeg))

        // Get phase angle delta_0
        val sinDelta0 = (sin(satLatRad) / sin(incRad)).coerceIn(-1.0, 1.0)
        val delta0 = asin(sinDelta0)

        // Calculate nodal longitude (where the ascending node is)
        // lon = node + atan2(cos(i) * sin(delta), cos(delta))
        val orbitalLonOffsetRad = atan2(cos(incRad) * sin(delta0), cos(delta0))
        val orbitalLonOffsetDeg = Math.toDegrees(orbitalLonOffsetRad)
        val nodalLng = satLng - orbitalLonOffsetDeg

        val points = mutableListOf<LatLng>()
        val stepCount = 120 // Smooth 3-degree intervals

        for (i in 0..stepCount) {
            val t = (2 * PI * i) / stepCount
            val latRad = asin(sin(incRad) * sin(t))
            val lonOffsetRad = atan2(cos(incRad) * sin(t), cos(t))
            
            val finalLat = Math.toDegrees(latRad)
            val finalLng = normalizeLongitude(nodalLng + Math.toDegrees(lonOffsetRad))
            points.add(LatLng(finalLat, finalLng))
        }

        return points
    }

    /**
     * Predicts the upcoming passes of a satellite over a given user tracking location in the next 24 hours.
     */
    fun predictUpcomingPasses(
        userLat: Double,
        userLng: Double,
        satLat: Double,
        satLng: Double,
        inclinationDeg: Double = INCLINATION_STARLINK_DEFAULT,
        heightKm: Double = 550.0,
        satelliteId: String,
        satelliteName: String
    ): List<PredictedPass> {
        val list = mutableListOf<PredictedPass>()
        
        // Starlink satellites orbit in approx 90 minutes (1.5 hours)
        // Let's generate passes by sampling the trajectory over future orbits
        val userLatRad = Math.toRadians(userLat)
        val userLngRad = Math.toRadians(userLng)
        
        val incRad = Math.toRadians(inclinationDeg.coerceIn(0.1, 89.9))
        val satLatRad = Math.toRadians(satLat.coerceIn(-inclinationDeg, inclinationDeg))

        val sinDelta0 = (sin(satLatRad) / sin(incRad)).coerceIn(-1.0, 1.0)
        val delta0 = asin(sinDelta0)
        val orbitalLonOffsetRad = atan2(cos(incRad) * sin(delta0), cos(delta0))
        val orbitalLonOffsetDeg = Math.toDegrees(orbitalLonOffsetRad)
        val nodalLng = satLng - orbitalLonOffsetDeg

        // Simulate 8 future orbits (approx 12 hours)
        val baseTime = System.currentTimeMillis()
        val orbitPeriodMs = 93 * 60 * 1000 // approx 93 mins orbit period
        
        for (orbitIndex in 1..8) {
            // Earth rotates westward by 360 deg in 24 hours (0.25 deg per minute, ~23.25 deg per orbit)
            val earthRotationDeg = orbitIndex * (360.0 / 24.0) * (93.0 / 60.0)
            val shiftedNodalLng = nodalLng - earthRotationDeg // shift nodal longitude westward
            
            // Search the closest approach in this orbit
            var minDistance = Double.MAX_VALUE
            var bestTimeMs = 0L
            var bestLat = 0.0
            var bestLng = 0.0
            
            // Sample 24 points in this single orbit
            val samples = 24
            for (step in 0 until samples) {
                val t = (2 * PI * step) / samples
                val latRad = asin(sin(incRad) * sin(t))
                val lonOffsetRad = atan2(cos(incRad) * sin(t), cos(t))
                
                val currentLat = Math.toDegrees(latRad)
                val currentLng = normalizeLongitude(shiftedNodalLng + Math.toDegrees(lonOffsetRad))
                
                val dist = haversineDistanceKm(userLat, userLng, currentLat, currentLng)
                if (dist < minDistance) {
                    minDistance = dist
                    bestLat = currentLat
                    bestLng = currentLng
                    // Time offset for this orbit step
                    val timeFraction = (step.toDouble() / samples) * orbitPeriodMs
                    bestTimeMs = baseTime + (orbitIndex * orbitPeriodMs) + timeFraction.toLong()
                }
            }
            
            // If the closest approach is within visibility dome (approx 1500 km radius for 550km altitude)
            if (minDistance < 1500.0) {
                // Calculate approximate altitude angle (max elevation) of the pass
                // Elevation = atan((height - distance^2/2R) / sqrt(distance^2 - (distance^2/2R)^2)) ... simple approx:
                val earthCentralAngle = minDistance / EARTH_RADIUS_KM
                val peakElevationDeg = Math.toDegrees(
                    atan2(
                        (EARTH_RADIUS_KM + heightKm) * cos(earthCentralAngle) - EARTH_RADIUS_KM,
                        (EARTH_RADIUS_KM + heightKm) * sin(earthCentralAngle)
                    )
                ).coerceIn(5.0, 90.0)
                
                // Determine direction of approach (NE, NW, SE, SW, etc.)
                val dLat = bestLat - userLat
                val dLng = normalizeLongitude(bestLng - userLng)
                val ns = if (dLat >= 0) "N" else "S"
                val ew = if (dLng >= 0) "E" else "W"
                val direction = "$ns$ew"

                list.add(
                    PredictedPass(
                        satelliteId = satelliteId,
                        satelliteName = satelliteName,
                        passTimeMs = bestTimeMs,
                        maxElevation = peakElevationDeg,
                        direction = direction,
                        passDistanceKm = minDistance
                    )
                )
            }
        }
        
        // Sort passes by chronological time
        return list.sortedBy { it.passTimeMs }
    }
}

data class PredictedPass(
    val satelliteId: String,
    val satelliteName: String,
    val passTimeMs: Long,
    val maxElevation: Double,
    val direction: String,
    val passDistanceKm: Double
)
