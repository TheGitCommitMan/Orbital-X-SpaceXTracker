package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.db.TrackedLocation
import com.example.math.OrbitPhysics
import com.example.ui.theme.*
import com.example.viewmodel.SatelliteViewModel
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackedLocationsTab(
    viewModel: SatelliteViewModel,
    modifier: Modifier = Modifier
) {
    val locations by viewModel.trackedLocations.collectAsState()
    val selectedLoc by viewModel.selectedLocation.collectAsState()
    val selectedSat by viewModel.selectedSatellite.collectAsState()
    
    var showAddForm by remember { mutableStateOf(false) }
    
    var stationName by remember { mutableStateOf("") }
    var latString by remember { mutableStateOf("") }
    var lngString by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "TRACKING LAB & RADAR",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = "Manage anchor points and view real-time radar transits",
                fontSize = 13.sp,
                color = SubSlateText
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Beautiful Sky Radar Terminal Decoration
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("radar_terminal_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SpaceDarkGray),
                border = BorderStroke(1.dp, Color(0x1F60A5FA))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CELESTIAL SCOPE - OVERHEAD FLYPASS",
                        color = OrbitalBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Tracking: ${selectedSat?.spaceTrack?.objectName ?: "None"}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Radar Scope Composable Drawing
                    CelestialRadarScope(
                        satelliteLat = selectedSat?.latitude ?: 0.0,
                        satelliteLng = selectedSat?.longitude ?: 0.0,
                        anchorLat = selectedLoc?.latitude ?: 48.1351,
                        anchorLng = selectedLoc?.longitude ?: 11.5820,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Outer Ring represents 1200 km horizon dome. The rotating sweeping arc updates at orbit telemetry cycle.",
                        fontSize = 10.sp,
                        color = SubSlateText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Floating Action Form Add Station
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GROUND STATIONS GRID",
                    color = SubSlateText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                
                TextButton(
                    onClick = { showAddForm = !showAddForm },
                    colors = ButtonDefaults.textButtonColors(contentColor = OrbitalBlue)
                ) {
                    Icon(
                        imageVector = if (showAddForm) Icons.Default.Add else Icons.Default.Add,
                        contentDescription = "Add Ground Station",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showAddForm) "Close" else "Add Station", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showAddForm) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("ground_station_add_form"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SpaceDarkGray),
                    border = BorderStroke(1.dp, Color(0x13FFFFFF))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Register Ground Station",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = stationName,
                            onValueChange = { stationName = it },
                            placeholder = { Text("e.g., Munich Observatory", color = SubSlateText) },
                            label = { Text("Station Label", color = SubSlateText) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = OrbitalBlue,
                                unfocusedBorderColor = Color(0x1F60A5FA)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = latString,
                                onValueChange = { latString = it },
                                placeholder = { Text("48.13", color = SubSlateText) },
                                label = { Text("Latitude °", color = SubSlateText) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = OrbitalBlue,
                                    unfocusedBorderColor = Color(0x1F60A5FA)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = lngString,
                                onValueChange = { lngString = it },
                                placeholder = { Text("11.58", color = SubSlateText) },
                                label = { Text("Longitude °", color = SubSlateText) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = OrbitalBlue,
                                    unfocusedBorderColor = Color(0x1F60A5FA)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        formError?.let {
                            Text(
                                text = it,
                                color = AlertOrange,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val latNum = latString.toDoubleOrNull()
                                val lngNum = lngString.toDoubleOrNull()
                                if (stationName.isBlank() || latNum == null || lngNum == null) {
                                    formError = "Please fill in valid name and numeric coordinates."
                                } else if (latNum < -90.0 || latNum > 90.0 || lngNum < -180.0 || lngNum > 180.0) {
                                    formError = "Latitude must be [-90,90] and Longitude [-180,180]"
                                } else {
                                    viewModel.addTrackedLocation(stationName, latNum, lngNum)
                                    stationName = ""
                                    latString = ""
                                    lngString = ""
                                    formError = null
                                    showAddForm = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrbitalBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Initialize Tracker Coordinates", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        items(locations) { loc ->
            val isCurrent = selectedLoc?.id == loc.id
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isCurrent) Color(0x1A10B981) else SpaceDarkGray)
                    .clickable { viewModel.selectLocation(loc) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (isCurrent) Icons.Default.MyLocation else Icons.Default.PinDrop,
                        contentDescription = "Map station",
                        tint = if (isCurrent) LiveGreen else SubSlateText,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = loc.name,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${"%.4f".format(loc.latitude)}° N, ${"%.4f".format(loc.longitude)}° E",
                            color = SubSlateText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                
                if (isCurrent) {
                    Text(
                        text = "ACTIVE GRID",
                        color = LiveGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.0.sp
                    )
                }
            }
        }
    }
}

/**
 * Native Canvas drawing a spectacular high-resolution real-time sky radar transit tracking.
 */
@Composable
fun CelestialRadarScope(
    satelliteLat: Double,
    satelliteLng: Double,
    anchorLat: Double,
    anchorLng: Double,
    modifier: Modifier = Modifier
) {
    // Dynamic radar sweeps animation
    val infiniteTransition = rememberInfiniteTransition(label = "Radar Sweep")
    val angleSweep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angleSweep"
    )

    val orbitalGlide by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitalGlide"
    )

    Canvas(modifier = modifier) {
        val cx = size.width / 2
        val cy = size.height / 2
        val outerRadius = size.height.coerceAtMost(size.width) / 2 * 0.9f

        // Draw background radar outer ring grid circles
        drawCircle(
            color = Color(0x3360A5FA),
            radius = outerRadius,
            center = Offset(cx, cy),
            style = Stroke(width = 2f)
        )
        drawCircle(
            color = Color(0x1560A5FA),
            radius = outerRadius * 0.66f,
            center = Offset(cx, cy),
            style = Stroke(width = 1f)
        )
        drawCircle(
            color = Color(0x1560A5FA),
            radius = outerRadius * 0.33f,
            center = Offset(cx, cy),
            style = Stroke(width = 1f)
        )

        // Draw crosshair axes
        drawLine(
            color = Color(0x1F60A5FA),
            start = Offset(cx - outerRadius, cy),
            end = Offset(cx + outerRadius, cy),
            strokeWidth = 1f
        )
        drawLine(
            color = Color(0x1F60A5FA),
            start = Offset(cx, cy - outerRadius),
            end = Offset(cx, cy + outerRadius),
            strokeWidth = 1f
        )

        // Calculate angular coordinate of satellite pass relative to observer
        val distanceKm = OrbitPhysics.haversineDistanceKm(anchorLat, anchorLng, satelliteLat, satelliteLng)
        val maxVisibilityDomeKm = 1400.0 // horizon margin

        // Simple polar projection equations relative to the radar screen
        if (distanceKm < maxVisibilityDomeKm) {
            // Find azimuth bearing angle from observer to satellite
            val anchorLatRad = Math.toRadians(anchorLat)
            val satLatRad = Math.toRadians(satelliteLat)
            val dLngRad = Math.toRadians(satelliteLng - anchorLng)

            val y = sin(dLngRad) * cos(satLatRad)
            val x = cos(anchorLatRad) * sin(satLatRad) -
                    sin(anchorLatRad) * cos(satLatRad) * cos(dLngRad)
            val azimuthRad = atan2(y, x)

            // Convert to circle coordinates (bearing 0 = North = -pi/2 in standard angles)
            val projectedRadiusRatio = (distanceKm / maxVisibilityDomeKm).coerceAtMost(1.0)
            val r = outerRadius * projectedRadiusRatio.toFloat()
            val theta = azimuthRad - (PI / 2)

            // Draw simulated trajectory flight path line based on orbit bearing glide
            val trajectoryPoints = mutableListOf<Offset>()
            for (offset in -8..8) {
                val simulatedDistance = (distanceKm + offset * 120.0).coerceIn(0.0, maxVisibilityDomeKm)
                val testProjRatio = simulatedDistance / maxVisibilityDomeKm
                val testTheta = theta + (offset * 0.08)
                val testRx = cx + (outerRadius * testProjRatio * cos(testTheta)).toFloat()
                val testRy = cy + (outerRadius * testProjRatio * sin(testTheta)).toFloat()
                trajectoryPoints.add(Offset(testRx, testRy))
            }

            // Render simulated transit dashed trajectory polyline
            for (pIndex in 0 until trajectoryPoints.size - 1) {
                drawLine(
                    color = Color(0x803B82F6),
                    start = trajectoryPoints[pIndex],
                    end = trajectoryPoints[pIndex + 1],
                    strokeWidth = 3f,
                    pathEffect = null
                )
            }

            // Animate dynamic sliding dot following orbit vector
            val interpIdx = (orbitalGlide * (trajectoryPoints.size - 1)).toInt()
            val gliderPos = trajectoryPoints[interpIdx]
            
            drawCircle(
                color = Color(0x40FFA43A),
                radius = 16f,
                center = gliderPos
            )
            drawCircle(
                color = Color(0xFFEA580C),
                radius = 6f,
                center = gliderPos
            )

            // Dynamic satellite active location dots
            val sx = cx + r * cos(theta).toFloat()
            val sy = cy + r * sin(theta).toFloat()

            // Outer pulse
            drawCircle(
                color = Color(0x3360A5FA),
                radius = 18f,
                center = Offset(sx, sy)
            )
            // Glowing core
            drawCircle(
                color = Color(0xFF60A5FA),
                radius = 5f,
                center = Offset(sx, sy)
            )
        }

        // Radar Sweep Arc Draw
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(Color.Transparent, Color(0x4410B981), Color(0x0510B981)),
                center = Offset(cx, cy)
            ),
            startAngle = angleSweep,
            sweepAngle = 60f,
            useCenter = true,
            size = androidx.compose.ui.geometry.Size(outerRadius * 2f, outerRadius * 2f),
            topLeft = Offset(cx - outerRadius, cy - outerRadius),
            blendMode = BlendMode.SrcOver
        )
    }
}
