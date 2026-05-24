package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.db.PassAlert
import com.example.math.PredictedPass
import com.example.ui.theme.*
import com.example.viewmodel.SatelliteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PassAlertsTab(
    viewModel: SatelliteViewModel,
    modifier: Modifier = Modifier
) {
    val selectedSat by viewModel.selectedSatellite.collectAsState()
    val selectedLoc by viewModel.selectedLocation.collectAsState()
    val upcomingPasses by viewModel.upcomingPasses.collectAsState()
    val scheduledAlerts by viewModel.passAlerts.collectAsState()

    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss (MMM d)", Locale.getDefault()) }

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
                text = "ORBITAL ALERTS HUB",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = "Schedule precise alerts for overhead transits",
                fontSize = 13.sp,
                color = SubSlateText
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Configuration Info Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SpaceDarkGray)
                    .border(1.dp, Color(0x1F3B82F6), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "TRACKING PARAMETERS",
                        color = OrbitalBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.0.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Satellite: ${selectedSat?.spaceTrack?.objectName ?: "None"}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Observer Location: ${selectedLoc?.name ?: "None"}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Predictions represent transits within 1500 km radius of the tracking grid.",
                        fontSize = 10.sp,
                        color = SubSlateText
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PREDICTED OVERHEAD TRANSITS",
                color = SubSlateText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (upcomingPasses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No visible transits predicted over this location in the next 12 hours.",
                        color = SubSlateText,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(upcomingPasses) { pass ->
                PredictedPassRow(
                    pass = pass,
                    timeFormatter = timeFormatter,
                    onSchedule = { viewModel.createPassAlert(pass) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "ACTIVE SCHEDULED NOTIFICATIONS (${scheduledAlerts.size})",
                color = SubSlateText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (scheduledAlerts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No alarms scheduled yet. Tap '+' on transits above.",
                        color = SubSlateText,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(scheduledAlerts, key = { it.id }) { alert ->
                ScheduledAlertRow(
                    alert = alert,
                    timeFormatter = timeFormatter,
                    onDelete = { viewModel.deletePassAlert(alert) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun PredictedPassRow(
    pass: PredictedPass,
    timeFormatter: SimpleDateFormat,
    onSchedule: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SpaceDarkGray)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = timeFormatter.format(Date(pass.passTimeMs)),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Elevation: ${pass.maxElevation.toInt()}° (${pass.direction})",
                    fontSize = 12.sp,
                    color = SubSlateText
                )
                Text(
                    text = "Distance: ${pass.passDistanceKm.toInt()} km",
                    fontSize = 12.sp,
                    color = SubSlateText
                )
            }
        }

        Button(
            onClick = onSchedule,
            colors = ButtonDefaults.buttonColors(containerColor = OrbitalBlue),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = "Schedule pass",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Set Path Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ScheduledAlertRow(
    alert: PassAlert,
    timeFormatter: SimpleDateFormat,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xE6131D31)) // Translucent active alarm card
            .border(1.dp, Color(0x3310B981), RoundedCornerShape(16.dp)) // Pulsing green indicator border
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x2210B981)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = "Active Alarm",
                    tint = LiveGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = alert.satelliteName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = timeFormatter.format(Date(alert.passTimeMs)),
                    color = LiveGreen,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Cancel Alarm",
                tint = AlertOrange,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
