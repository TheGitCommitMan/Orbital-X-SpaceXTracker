package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.db.TrackedLocation
import com.example.math.OrbitPhysics
import com.example.math.LatLng
import com.example.network.StarlinkSatellite
import com.example.ui.theme.*
import com.example.viewmodel.SatelliteViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable
fun OrbitalMapTab(
    viewModel: SatelliteViewModel,
    modifier: Modifier = Modifier
) {
    val satellites by viewModel.satellites.collectAsState()
    val selectedSat by viewModel.selectedSatellite.collectAsState()
    val selectedLoc by viewModel.selectedLocation.collectAsState()
    val upcomingPasses by viewModel.upcomingPasses.collectAsState()
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Toggle view mode: True for 3D Holographic Globe, False for 2D Tactical Flat Projection
    var use3DGlobeMode by remember { mutableStateOf(true) }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF03070C))) {
        
        if (use3DGlobeMode) {
            // High-fidelity 3D Interactive Holographic Globe Map
            ThreeDGlobeMap(
                satellites = satellites,
                selectedSat = selectedSat,
                selectedLoc = selectedLoc,
                onSelectSatellite = { sat -> viewModel.selectSatellite(sat) },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Elegant sci-fi interactive 2D world tracking projection
            VectorHudMap(
                satellites = satellites,
                selectedSat = selectedSat,
                selectedLoc = selectedLoc,
                onSelectSatellite = { sat -> viewModel.selectSatellite(sat) },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Floating Mode Switch (3D vs 2D Toggle) at Top
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xE60B0E14))
                .border(1.dp, Color(0x3360A5FA), RoundedCornerShape(16.dp))
                .padding(4.dp)
                .zIndex(10f),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (use3DGlobeMode) OrbitalBlue else Color.Transparent)
                    .clickable { use3DGlobeMode = true }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = "3D Hologram",
                        tint = if (use3DGlobeMode) Color.White else SubSlateText,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "3D HOLO GLOBE",
                        color = if (use3DGlobeMode) Color.White else SubSlateText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (!use3DGlobeMode) OrbitalBlue else Color.Transparent)
                    .clickable { use3DGlobeMode = false }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "2D HUD Projection",
                        tint = if (!use3DGlobeMode) Color.White else SubSlateText,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "2D HUD MAP",
                        color = if (!use3DGlobeMode) Color.White else SubSlateText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Upcoming Pass Overlay Card (Top overlay)
        val alertPass = upcomingPasses.firstOrNull()
        if (alertPass != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 76.dp, bottom = 12.dp)
                    .align(Alignment.TopCenter)
                    .zIndex(5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xE6131D31)) // Translucent overlay card
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                        .clickable {
                            viewModel.triggerInstantDemoNotification(alertPass)
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x33EA580C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Alert",
                                tint = Color(0xFFEA580C)
                            )
                        }
                        Column {
                            Text(
                                text = "UPCOMING PASS",
                                color = Color(0xFFFFA94D),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "${alertPass.satelliteName} Transit",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${alertPass.maxElevation.toInt()}° ALT",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Origin: ${alertPass.direction} (Tap to Test)",
                            color = SubSlateText,
                            fontSize = 9.sp,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }

        // Bottom Details Panel (Displays current selected satellite analytics)
        selectedSat?.let { sat ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp) // Avoid overlap with safe navigation insets
                    .testTag("current_selection_card"),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                colors = CardDefaults.cardColors(containerColor = SpaceDarkGray),
                border = BorderStroke(1.dp, Color(0x13FFFFFF))
            ) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    // Pull Handle Indicator
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = sat.spaceTrack?.objectName ?: "Starlink Satellite",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (sat.id == "iss_sim") "International Space Station" else "SpaceX Constellation (${sat.version ?: "v1.5"})",
                                color = SubSlateText,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = {
                                Toast.makeText(
                                    context, 
                                    "Target locked on ${sat.spaceTrack?.objectName ?: "Satellite"}. Center of camera aligned.", 
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrbitalBlue),
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SatelliteAlt,
                                contentDescription = "Focus",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Follow Orbit",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Three columns grid metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricItem(
                            title = "ALTITUDE",
                            value = "${"%.1f".format(sat.heightKm ?: 549.2)} km",
                            modifier = Modifier.weight(1f)
                        )
                        MetricItem(
                            title = "VELOCITY",
                            value = "${"%,d".format(((sat.velocityKms ?: 7.6) * 3600).toLong())} km/h",
                            modifier = Modifier.weight(1f)
                        )
                        MetricItem(
                            title = "INCLINATION",
                            value = "${"%.2f".format(sat.spaceTrack?.inclination ?: 53.21)}°",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x0AFFFFFF))
            .border(1.dp, Color(0x0AFFFFFF), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = SubSlateText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.0.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// 3D Projection Model Class
data class ProjectedPoint3D(
    val x: Float,
    val y: Float,
    val z: Float,
    val visible: Boolean
)

// Main Projection Helper function
fun projectPointOn3D(
    latitude: Double,
    longitude: Double,
    radius: Float,
    width: Float,
    height: Float,
    yawDeg: Float,
    pitchDeg: Float
): ProjectedPoint3D {
    val phi = Math.toRadians(latitude)
    val lambda = Math.toRadians(longitude)
    
    // Convert spherical parameters to cartesian coordinates (base unit sphere)
    val x0 = cos(phi) * sin(lambda)
    val y0 = sin(phi)
    val z0 = cos(phi) * cos(lambda)
    
    // Rotate around Y-axis (Yaw - Longitude spinning)
    val yawRad = Math.toRadians(yawDeg.toDouble())
    val x1 = x0 * cos(yawRad) - z0 * sin(yawRad)
    val z1 = x0 * sin(yawRad) + z0 * cos(yawRad)
    val y1 = y0
    
    // Rotate around X-axis (Pitch - Latitude tilt)
    val pitchRad = Math.toRadians(pitchDeg.toDouble())
    val x2 = x1
    val y2 = y1 * cos(pitchRad) - z1 * sin(pitchRad)
    val z2 = y1 * sin(pitchRad) + z1 * cos(pitchRad)
    
    // Translate from origin to viewport coordinates
    val sx = (width / 2f) + x2.toFloat() * radius
    val sy = (height / 2f) - y2.toFloat() * radius
    
    // Facing check: Positive depth values face the user
    val isFacing = z2 > 0.0
    
    return ProjectedPoint3D(sx, sy, z2.toFloat(), isFacing)
}

// Generate points on Earth's surface corresponding to a circle of angular radius theta
fun getGroundStationDomePoints(centerLat: Double, centerLng: Double, angularRadiusDeg: Double = 10.8): List<LatLng> {
    val listPoints = mutableListOf<LatLng>()
    val cLatRad = Math.toRadians(centerLat)
    val cLngRad = Math.toRadians(centerLng)
    val dRad = Math.toRadians(angularRadiusDeg)
    
    for (bearingDeg in 0..360 step 12) {
        val bRad = Math.toRadians(bearingDeg.toDouble())
        
        val pLatRad = asin(sin(cLatRad) * cos(dRad) + cos(cLatRad) * sin(dRad) * cos(bRad))
        val pLngRad = cLngRad + atan2(
            sin(bRad) * sin(dRad) * cos(cLatRad),
            cos(dRad) - sin(cLatRad) * sin(pLatRad)
        )
        
        listPoints.add(
            LatLng(
                Math.toDegrees(pLatRad), 
                OrbitPhysics.normalizeLongitude(Math.toDegrees(pLngRad))
            )
        )
    }
    return listPoints
}

@Composable
fun ThreeDGlobeMap(
    satellites: List<StarlinkSatellite>,
    selectedSat: StarlinkSatellite?,
    selectedLoc: TrackedLocation?,
    onSelectSatellite: (StarlinkSatellite) -> Unit,
    modifier: Modifier = Modifier
) {
    // Rotation States
    var yaw by remember { mutableStateOf(10f) }
    var pitch by remember { mutableStateOf(25f) }
    var autoSpinActive by remember { mutableStateOf(true) }
    var lastActivityTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Automatic smooth rotating loop
    LaunchedEffect(autoSpinActive) {
        while (true) {
            delay(16) // ~60fps smooth rotation
            val now = System.currentTimeMillis()
            if (now - lastActivityTime > 4000) {
                autoSpinActive = true
            }
            if (autoSpinActive) {
                yaw = (yaw + 0.15f) % 360f
            }
        }
    }

    // Continent contour coordinate databases
    val americasOutline = remember {
        listOf(
            LatLng(72.0, -120.0), LatLng(55.0, -125.0), LatLng(48.0, -125.0), LatLng(32.0, -117.0),
            LatLng(25.0, -110.0), LatLng(16.0, -95.0), LatLng(8.0, -78.0), LatLng(-5.0, -80.0),
            LatLng(-12.0, -75.0), LatLng(-33.0, -72.0), LatLng(-54.0, -70.0), LatLng(-54.0, -65.0),
            LatLng(-40.0, -45.0), LatLng(-23.0, -43.0), LatLng(-5.0, -35.0), LatLng(5.0, -52.0),
            LatLng(10.0, -72.0), LatLng(18.0, -65.0), LatLng(25.0, -80.0), LatLng(30.0, -81.0),
            LatLng(44.0, -68.0), LatLng(50.0, -55.0), LatLng(60.0, -64.0), LatLng(75.0, -74.0),
            LatLng(72.0, -120.0)
        )
    }

    val eurasiaAfricaOutline = remember {
        listOf(
            LatLng(71.0, 10.0), LatLng(75.0, 40.0), LatLng(78.0, 80.0), LatLng(70.0, 120.0),
            LatLng(70.0, 165.0), LatLng(60.0, 160.0), LatLng(40.0, 140.0), LatLng(35.0, 140.0),
            LatLng(22.0, 115.0), LatLng(10.0, 105.0), LatLng(1.0, 103.0), LatLng(-6.0, 106.0),
            LatLng(-8.0, 115.0), LatLng(-6.0, 120.0), LatLng(15.0, 120.0), LatLng(20.0, 110.0),
            LatLng(22.0, 80.0), LatLng(13.0, 80.0), LatLng(7.0, 78.0), LatLng(13.0, 48.0),
            LatLng(-5.0, 39.0), LatLng(-20.0, 31.0), LatLng(-34.0, 20.0), LatLng(-10.0, 12.0),
            LatLng(5.0, 9.0), LatLng(15.0, -17.0), LatLng(20.0, -17.0), LatLng(32.0, -16.0),
            LatLng(36.0, -5.0), LatLng(43.0, 9.0), LatLng(55.0, 10.0), LatLng(65.0, 20.0),
            LatLng(71.0, 10.0)
        )
    }

    val australiaOutline = remember {
        listOf(
            LatLng(-22.0, 114.0), LatLng(-12.0, 131.0), LatLng(-11.0, 142.0),
            LatLng(-25.0, 153.0), LatLng(-38.0, 148.0), LatLng(-35.0, 117.0),
            LatLng(-22.0, 114.0)
        )
    }

    // Radar Scanning Animation line on Globe
    val scanTransition = rememberInfiniteTransition(label = "GlobeRadarPulse")
    val pulseSize by scanTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseSize"
    )

    // Selection target pulsing animation
    val selectedPulseTransition = rememberInfiniteTransition(label = "SelectedTargetPulse")
    val targetPulseRadius by selectedPulseTransition.animateFloat(
        initialValue = 12f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "targetPulse"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF03070C))
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        val globeRadius = min(width, height) * 0.38f // Dynamically scale globe based on bounds

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // Gesture detector for spin & drag tilting
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            autoSpinActive = false
                            lastActivityTime = System.currentTimeMillis()
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            lastActivityTime = System.currentTimeMillis()
                            yaw = (yaw + dragAmount.x * 0.3f) % 360f
                            pitch = (pitch - dragAmount.y * 0.3f).coerceIn(-85f, 85f)
                        }
                    )
                }
                // Tap detector for choosing satellites
                .pointerInput(satellites, yaw, pitch, globeRadius, width, height) {
                    detectTapGestures { offset ->
                        var closestSat: StarlinkSatellite? = null
                        var minDistance = Float.MAX_VALUE

                        satellites.forEach { sat ->
                            val sLat = sat.latitude
                            val sLng = sat.longitude
                            if (sLat != null && sLng != null) {
                                val proj = projectPointOn3D(sLat, sLng, globeRadius, width, height, yaw, pitch)
                                if (proj.visible) {
                                    val dx = proj.x - offset.x
                                    val dy = proj.y - offset.y
                                    val dist = sqrt(dx * dx + dy * dy)
                                    if (dist < minDistance) {
                                        minDistance = dist
                                        closestSat = sat
                                    }
                                }
                            }
                        }

                        // Select if inside a reasonable hit target radius (48dp in pixels equivalent)
                        if (minDistance < 60f) {
                            closestSat?.let { onSelectSatellite(it) }
                        }
                    }
                }
        ) {
            val cx = width / 2f
            val cy = height / 2f

            // 1. Earth Sphere Backdrop (Deep space gradient circle)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF07121F), Color(0xFF040A10)),
                    center = Offset(cx, cy),
                    radius = globeRadius
                ),
                radius = globeRadius,
                center = Offset(cx, cy)
            )

            // Earth atmosphere rim/limb lighting glow ring
            drawCircle(
                color = Color(0xFF1E3A8A),
                radius = globeRadius,
                center = Offset(cx, cy),
                style = Stroke(width = 2.5f)
            )
            drawCircle(
                color = Color(0x3D60A5FA),
                radius = globeRadius + 6f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f)
            )

            // 2. Translucent Lat/Lng Coordinate lines grid mesh
            // Latitude mesh loops (Every 30 degrees)
            for (meshLat in -60..60 step 30) {
                var previousPt: ProjectedPoint3D? = null
                val samples = 60
                
                for (step in 0..samples) {
                    val sampleLng = -180.0 + (360.0 * step / samples)
                    val proj = projectPointOn3D(meshLat.toDouble(), sampleLng, globeRadius, width, height, yaw, pitch)
                    
                    if (previousPt != null) {
                        // Blend depth values of adjacent endpoints
                        val drawAlpha = if (proj.visible && previousPt.visible) 0.16f else 0.05f
                        drawLine(
                            color = Color(0xFF60A5FA).copy(alpha = drawAlpha),
                            start = Offset(previousPt.x, previousPt.y),
                            end = Offset(proj.x, proj.y),
                            strokeWidth = 1f
                        )
                    }
                    previousPt = proj
                }
            }

            // Longitude Meridian loops (Every 30 degrees)
            for (meshLng in -180..150 step 30) {
                var previousPt: ProjectedPoint3D? = null
                val samples = 40
                
                for (step in 0..samples) {
                    val sampleLat = -80.0 + (160.0 * step / samples)
                    val proj = projectPointOn3D(sampleLat, meshLng.toDouble(), globeRadius, width, height, yaw, pitch)
                    
                    if (previousPt != null) {
                        val drawAlpha = if (proj.visible && previousPt.visible) 0.16f else 0.05f
                        drawLine(
                            color = Color(0xFF60A5FA).copy(alpha = drawAlpha),
                            start = Offset(previousPt.x, previousPt.y),
                            end = Offset(proj.x, proj.y),
                            strokeWidth = 1f
                        )
                    }
                    previousPt = proj
                }
            }

            // 3. Draw outlined land continent boundaries in 3D Space
            listOf(americasOutline, eurasiaAfricaOutline, australiaOutline).forEach { outline ->
                for (i in 0 until outline.size) {
                    val p1 = outline[i]
                    val p2 = outline[(i + 1) % outline.size]
                    
                    val proj1 = projectPointOn3D(p1.latitude, p1.longitude, globeRadius, width, height, yaw, pitch)
                    val proj2 = projectPointOn3D(p2.latitude, p2.longitude, globeRadius, width, height, yaw, pitch)
                    
                    val bothFacing = proj1.visible && proj2.visible
                    val drawAlpha = if (bothFacing) 0.35f else 0.08f
                    val strokeW = if (bothFacing) 2f else 1f
                    
                    drawLine(
                        color = Color(0xFF60A5FA).copy(alpha = drawAlpha),
                        start = Offset(proj1.x, proj1.y),
                        end = Offset(proj2.x, proj2.y),
                        strokeWidth = strokeW
                    )
                }
            }

            // 4. Ground Tracking Observer Dome Range Ring
            selectedLoc?.let { loc ->
                val baseProj = projectPointOn3D(loc.latitude, loc.longitude, globeRadius, width, height, yaw, pitch)
                
                // Draw horizon visibility dome footprint (1200km surface circle)
                val domePoints = getGroundStationDomePoints(loc.latitude, loc.longitude, angularRadiusDeg = 10.8)
                var previousDomePt: ProjectedPoint3D? = null
                
                for (i in 0..domePoints.size) {
                    val pt = domePoints[i % domePoints.size]
                    val proj = projectPointOn3D(pt.latitude, pt.longitude, globeRadius, width, height, yaw, pitch)
                    
                    if (previousDomePt != null) {
                        val bothVisible = proj.visible && previousDomePt.visible
                        val alphaVal = if (bothVisible) 0.45f else 0.10f
                        
                        drawLine(
                            color = Color(0xFFEA580C).copy(alpha = alphaVal),
                            start = Offset(previousDomePt.x, previousDomePt.y),
                            end = Offset(proj.x, proj.y),
                            strokeWidth = 1.5f
                        )
                    }
                    previousDomePt = proj
                }

                // Render ground station pin node
                if (baseProj.visible) {
                    // Outer flashing radar rings
                    drawCircle(
                        color = Color(0xFF10B981).copy(alpha = 0.3f),
                        radius = 12f,
                        center = Offset(baseProj.x, baseProj.y)
                    )
                    drawCircle(
                        color = Color(0xFF10B981),
                        radius = 5f,
                        center = Offset(baseProj.x, baseProj.y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2f,
                        center = Offset(baseProj.x, baseProj.y)
                    )
                }
            }

            // 5. Draw Orbit loop trajectory path of the selected Satellite
            selectedSat?.let { sat ->
                sat.latitude?.let { satLat ->
                    sat.longitude?.let { satLng ->
                        val inclinationVal = sat.spaceTrack?.inclination ?: 53.2
                        val points = OrbitPhysics.generateOrbitTrajectory(satLat, satLng, inclinationVal)
                        
                        var previousOrbitPt: ProjectedPoint3D? = null
                        for (i in 0..points.size) {
                            val pt = points[i % points.size]
                            val proj = projectPointOn3D(pt.latitude, pt.longitude, globeRadius * 1.06f, width, height, yaw, pitch)
                            
                            if (previousOrbitPt != null) {
                                val isFront = proj.z > 0.0f && previousOrbitPt.z > 0.0f
                                val colorVal = if (isFront) OrbitalBlue else Color(0x3B3B82F6)
                                val strokeW = if (isFront) 2.5f else 1.2f
                                
                                drawLine(
                                    color = colorVal,
                                    start = Offset(previousOrbitPt.x, previousOrbitPt.y),
                                    end = Offset(proj.x, proj.y),
                                    strokeWidth = strokeW
                                )
                            }
                            previousOrbitPt = proj
                        }
                    }
                }
            }

            // 6. Draw all floating Starlink Telemetry Nodes in Orbit heights
            satellites.forEach { sat ->
                val sLat = sat.latitude
                val sLng = sat.longitude
                if (sLat != null && sLng != null) {
                    // Satellites float slightly above physical radius (approx 1.06 scale)
                    val proj = projectPointOn3D(sLat, sLng, globeRadius * 1.06f, width, height, yaw, pitch)
                    
                    val isSelected = selectedSat?.id == sat.id
                    val isIss = sat.id == "iss_sim"
                    
                    if (proj.visible) {
                        if (isSelected) {
                            // Pulsing green/orange radar focal ring around the selected Target satellite
                            drawCircle(
                                color = Color(0xFFEA580C).copy(alpha = 0.25f),
                                radius = targetPulseRadius,
                                center = Offset(proj.x, proj.y)
                            )
                            
                            // High-tech sci-fi targeting reticle crosshair brackets
                            val bF = 11f // bracket offset
                            val bL = 5f // bracket length
                            
                            // Top-Left corner bracket
                            drawLine(Color(0xFFEA580C), Offset(proj.x - bF, proj.y - bF), Offset(proj.x - bF + bL, proj.y - bF), 2f)
                            drawLine(Color(0xFFEA580C), Offset(proj.x - bF, proj.y - bF), Offset(proj.x - bF, proj.y - bF + bL), 2f)
                            // Top-Right corner bracket
                            drawLine(Color(0xFFEA580C), Offset(proj.x + bF, proj.y - bF), Offset(proj.x + bF - bL, proj.y - bF), 2f)
                            drawLine(Color(0xFFEA580C), Offset(proj.x + bF, proj.y - bF), Offset(proj.x + bF, proj.y - bF + bL), 2f)
                            // Bottom-Left corner bracket
                            drawLine(Color(0xFFEA580C), Offset(proj.x - bF, proj.y + bF), Offset(proj.x - bF + bL, proj.y + bF), 2f)
                            drawLine(Color(0xFFEA580C), Offset(proj.x - bF, proj.y + bF), Offset(proj.x - bF, proj.y + bF - bL), 2f)
                            // Bottom-Right corner bracket
                            drawLine(Color(0xFFEA580C), Offset(proj.x + bF, proj.y + bF), Offset(proj.x + bF - bL, proj.y + bF), 2f)
                            drawLine(Color(0xFFEA580C), Offset(proj.x + bF, proj.y + bF), Offset(proj.x + bF, proj.y + bF - bL), 2f)

                            // Main core node dot
                            drawCircle(
                                color = Color(0xFFEA580C),
                                radius = 5.5f,
                                center = Offset(proj.x, proj.y)
                            )
                        } else {
                            // Standard node nodes
                            val nColor = if (isIss) Color(0xFFEF4444) else Color(0xFF5AB4FF)
                            val nSize = if (isIss) 6.5f else 4f
                            
                            drawCircle(
                                color = nColor,
                                radius = nSize,
                                center = Offset(proj.x, proj.y)
                            )
                        }
                    } else if (isSelected) {
                        // Drawing hidden side selected target indicator with low opacity dashed halo
                        drawCircle(
                            color = Color(0x3AEA580C),
                            radius = 4f,
                            center = Offset(proj.x, proj.y)
                        )
                    }
                }
            }

            // 7. Tactical Scanning sweeping overlay
            drawCircle(
                color = Color(0x1F3B82F6).copy(alpha = 0.05f * sin(pulseSize * PI).toFloat()),
                radius = globeRadius + 15f * pulseSize,
                center = Offset(cx, cy),
                style = Stroke(width = 2f)
            )
        }

        // Overlay Interactive Guide Instructions for rotating Globe
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = if (selectedSat != null) 210.dp else 24.dp)
                .zIndex(2f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "HOLOGRAPH MODEL 3D V.2",
                color = OrbitalBlue,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Swipe to rotate Earth sphere • Tap nodes to lock",
                color = SubSlateText,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun VectorHudMap(
    satellites: List<StarlinkSatellite>,
    selectedSat: StarlinkSatellite?,
    selectedLoc: TrackedLocation?,
    onSelectSatellite: (StarlinkSatellite) -> Unit,
    modifier: Modifier = Modifier
) {
    // 2D Earth Boundaries (Stretched contours of major land continents)
    val americasOutline = remember {
        listOf(
            LatLng(72.0, -120.0), LatLng(55.0, -125.0), LatLng(48.0, -125.0), LatLng(32.0, -117.0),
            LatLng(25.0, -110.0), LatLng(16.0, -95.0), LatLng(8.0, -78.0), LatLng(-5.0, -80.0),
            LatLng(-12.0, -75.0), LatLng(-33.0, -72.0), LatLng(-54.0, -70.0), LatLng(-54.0, -65.0),
            LatLng(-40.0, -45.0), LatLng(-23.0, -43.0), LatLng(-5.0, -35.0), LatLng(5.0, -52.0),
            LatLng(10.0, -72.0), LatLng(18.0, -65.0), LatLng(25.0, -80.0), LatLng(30.0, -81.0),
            LatLng(44.0, -68.0), LatLng(50.0, -55.0), LatLng(60.0, -64.0), LatLng(75.0, -74.0),
            LatLng(72.0, -120.0)
        )
    }

    val eurasiaAfricaOutline = remember {
        listOf(
            LatLng(71.0, 10.0), LatLng(75.0, 40.0), LatLng(78.0, 80.0), LatLng(70.0, 120.0),
            LatLng(70.0, 165.0), LatLng(60.0, 160.0), LatLng(40.0, 140.0), LatLng(35.0, 140.0),
            LatLng(22.0, 115.0), LatLng(10.0, 105.0), LatLng(1.0, 103.0), LatLng(-6.0, 106.0),
            LatLng(-8.0, 115.0), LatLng(-6.0, 120.0), LatLng(15.0, 120.0), LatLng(20.0, 110.0),
            LatLng(22.0, 80.0), LatLng(13.0, 80.0), LatLng(7.0, 78.0), LatLng(13.0, 48.0),
            LatLng(-5.0, 39.0), LatLng(-20.0, 31.0), LatLng(-34.0, 20.0), LatLng(-10.0, 12.0),
            LatLng(5.0, 9.0), LatLng(15.0, -17.0), LatLng(20.0, -17.0), LatLng(32.0, -16.0),
            LatLng(36.0, -5.0), LatLng(43.0, 9.0), LatLng(55.0, 10.0), LatLng(65.0, 20.0),
            LatLng(71.0, 10.0)
        )
    }

    val australiaOutline = remember {
        listOf(
            LatLng(-22.0, 114.0), LatLng(-12.0, 131.0), LatLng(-11.0, 142.0),
            LatLng(-25.0, 153.0), LatLng(-38.0, 148.0), LatLng(-35.0, 117.0),
            LatLng(-22.0, 114.0)
        )
    }

    val continuousScanIndex = rememberInfiniteTransition(label = "MapScan2D")
    val sweep2DX by continuousScanIndex.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep2DX"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF03070C))
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        // 2D Equirectangular projection coordinates
        fun project2DX(lng: Double): Float = ((lng + 180.0) / 360.0).toFloat() * width
        fun project2DY(lat: Double): Float = ((90.0 - lat) / 180.0).toFloat() * height

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(satellites) {
                    detectTapGestures { offset ->
                        val clickedLng = (offset.x / width) * 360f - 180f
                        val clickedLat = 90f - (offset.y / height) * 180f

                        var closestSat: StarlinkSatellite? = null
                        var minDistance = Double.MAX_VALUE

                        satellites.forEach { sat ->
                            val sLat = sat.latitude
                            val sLng = sat.longitude
                            if (sLat != null && sLng != null) {
                                val dLat = sLat - clickedLat
                                val dLng = sLng - clickedLng
                                val dist = dLat * dLat + dLng * dLng
                                if (dist < minDistance) {
                                    minDistance = dist
                                    closestSat = sat
                                }
                            }
                        }

                        // Tap tolerance radius squared
                        if (minDistance < 400.0) {
                            closestSat?.let { onSelectSatellite(it) }
                        }
                    }
                }
        ) {
            // Draw Lat/Lng matrix divisions grid
            for (latG in -60..60 step 30) {
                val y = project2DY(latG.toDouble())
                drawLine(
                    color = Color(0x1360A5FA),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }
            for (lngG in -150..150 step 30) {
                val x = project2DX(lngG.toDouble())
                drawLine(
                    color = Color(0x1360A5FA),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
            }

            // High-contrast coordinates axes crossings
            val equatorY = project2DY(0.0)
            drawLine(
                color = Color(0x383B82F6),
                start = Offset(0f, equatorY),
                end = Offset(width, equatorY),
                strokeWidth = 2f
            )

            val primeX = project2DX(0.0)
            drawLine(
                color = Color(0x383B82F6),
                start = Offset(primeX, 0f),
                end = Offset(primeX, height),
                strokeWidth = 2f
            )

            // Draw Continents outlined bounds on 2D planes
            listOf(americasOutline, eurasiaAfricaOutline, australiaOutline).forEach { pathList ->
                val drawPath = Path()
                if (pathList.isNotEmpty()) {
                    drawPath.moveTo(project2DX(pathList[0].longitude), project2DY(pathList[0].latitude))
                    for (i in 1 until pathList.size) {
                        drawPath.lineTo(project2DX(pathList[i].longitude), project2DY(pathList[i].latitude))
                    }
                    drawPath.close()
                    drawPath(
                        path = drawPath,
                        color = Color(0x1F60A5FA),
                        style = Stroke(width = 1.5f)
                    )
                }
            }

            // Draw tracking footprint bounds of ground radar Station
            selectedLoc?.let { loc ->
                val locX = project2DX(loc.longitude)
                val locY = project2DY(loc.latitude)

                // 1200km is ~ 10.8 degrees footprint radius on 360 deg width grid
                val rx = (10.8 / 360.0) * width
                val ry = (10.8 / 180.0) * height

                drawOval(
                    color = Color(0x12EA580C),
                    topLeft = Offset((locX - rx).toFloat(), (locY - ry).toFloat()),
                    size = Size((rx * 2).toFloat(), (ry * 2).toFloat())
                )
                drawOval(
                    color = Color(0x42EA580C),
                    topLeft = Offset((locX - rx).toFloat(), (locY - ry).toFloat()),
                    size = Size((rx * 2).toFloat(), (ry * 2).toFloat()),
                    style = Stroke(width = 1.5f)
                )

                // Station Pin Node Dot
                drawCircle(
                    color = Color(0xFF10B981),
                    radius = 8f,
                    center = Offset(locX, locY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3f,
                    center = Offset(locX, locY)
                )
            }

            // Draw selected Satellite's complete orbital loop line
            selectedSat?.let { sat ->
                sat.latitude?.let { sLat ->
                    sat.longitude?.let { sLng ->
                        val orbitPath = Path()
                        val points = OrbitPhysics.generateOrbitTrajectory(sLat, sLng, sat.spaceTrack?.inclination ?: 53.2)
                        
                        if (points.isNotEmpty()) {
                            orbitPath.moveTo(project2DX(points[0].longitude), project2DY(points[0].latitude))
                            for (p in points) {
                                orbitPath.lineTo(project2DX(p.longitude), project2DY(p.latitude))
                            }
                            drawPath(
                                path = orbitPath,
                                color = Color(0xFF3B82F6),
                                style = Stroke(width = 2.5f)
                            )
                        }
                    }
                }
            }

            // Draw flying telemetry Node dots representation
            satellites.forEach { sat ->
                val sLat = sat.latitude
                val sLng = sat.longitude
                if (sLat != null && sLng != null) {
                    val satX = project2DX(sLng)
                    val satY = project2DY(sLat)

                    val isSelected = selectedSat?.id == sat.id
                    if (isSelected) {
                        drawRect(
                            color = Color(0xFFEA580C),
                            topLeft = Offset(satX - 12f, satY - 12f),
                            size = Size(24f, 24f),
                            style = Stroke(width = 2f)
                        )
                        drawLine(
                            color = Color(0xFFEA580C),
                            start = Offset(satX - 18f, satY),
                            end = Offset(satX + 18f, satY),
                            strokeWidth = 1.5f
                        )
                        drawLine(
                            color = Color(0xFFEA580C),
                            start = Offset(satX, satY - 18f),
                            end = Offset(satX, satY + 18f),
                            strokeWidth = 1.5f
                        )
                        drawCircle(
                            color = Color(0xFFEA580C),
                            radius = 5f,
                            center = Offset(satX, satY)
                        )
                    } else {
                        val sizeRadius = if (sat.id == "iss_sim") 6f else 4.5f
                        val colorNode = if (sat.id == "iss_sim") Color(0xFFEF4444) else Color(0xFF60A5FA)
                        
                        drawCircle(
                            color = colorNode,
                            radius = sizeRadius,
                            center = Offset(satX, satY)
                        )
                    }
                }
            }

            // Scanning swept line indicator
            val scanningX = sweep2DX * width
            drawLine(
                color = Color(0x3360A5FA),
                start = Offset(scanningX, 0f),
                end = Offset(scanningX, height),
                strokeWidth = 3f
            )
        }
    }
}
