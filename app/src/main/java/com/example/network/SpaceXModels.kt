package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StarlinkSatellite(
    @Json(name = "id") val id: String,
    @Json(name = "version") val version: String?,
    @Json(name = "latitude") val latitude: Double?,
    @Json(name = "longitude") val longitude: Double?,
    @Json(name = "height_km") val heightKm: Double?,
    @Json(name = "velocity_kms") val velocityKms: Double?,
    @Json(name = "spaceTrack") val spaceTrack: SpaceTrackInfo?
)

@JsonClass(generateAdapter = true)
data class SpaceTrackInfo(
    @Json(name = "OBJECT_NAME") val objectName: String?,
    @Json(name = "LAUNCH_DATE") val launchDate: String?,
    @Json(name = "INCLINATION") val inclination: Double?,
    @Json(name = "DECAYED") val decayed: Int?
)
