package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.DirectoryTab
import com.example.ui.OrbitalMapTab
import com.example.ui.PassAlertsTab
import com.example.ui.TrackedLocationsTab
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.SpaceDarkGray
import com.example.ui.theme.OrbitalBlue
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.SlateText
import com.example.ui.theme.SubSlateText
import com.example.viewmodel.SatelliteViewModel
import com.example.viewmodel.Tab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                SatelliteTrackerApp()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SatelliteTrackerApp() {
    val viewModel: SatelliteViewModel = viewModel()
    val activeTab by viewModel.activeTab.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBlack),
        topBar = {
            Column(
                modifier = Modifier
                    .background(SpaceBlack)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // High-tech logo box
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x1F3B82F6))
                                .border(1.dp, Color(0x333B82F6), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = "Logo",
                                tint = OrbitalBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "ORBITAL-X",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.0.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Pulse live indicator
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(LiveGreen)
                                )
                                Text(
                                    text = "LIVE TRACKING",
                                    color = LiveGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                            }
                        }
                    }

                    // Refresh Sync Action
                    IconButton(
                        onClick = { viewModel.fetchSatellites() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x0AFFFFFF))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = OrbitalBlue,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync SpaceX Satellites",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .border(1.dp, Color(0x0AFFFFFF), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                containerColor = SpaceDarkGray,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == Tab.MAP,
                    onClick = { viewModel.selectTab(Tab.MAP) },
                    icon = { Icon(Icons.Default.Explore, contentDescription = "Map") },
                    label = { Text("Map", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = OrbitalBlue,
                        unselectedIconColor = SubSlateText,
                        unselectedTextColor = SubSlateText
                    ),
                    modifier = Modifier.testTag("nav_btn_map")
                )
                NavigationBarItem(
                    selected = activeTab == Tab.DIRECTORY,
                    onClick = { viewModel.selectTab(Tab.DIRECTORY) },
                    icon = { Icon(Icons.Default.ListAlt, contentDescription = "Directory") },
                    label = { Text("Directory", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = OrbitalBlue,
                        unselectedIconColor = SubSlateText,
                        unselectedTextColor = SubSlateText
                    ),
                    modifier = Modifier.testTag("nav_btn_directory")
                )
                NavigationBarItem(
                    selected = activeTab == Tab.ALERTS,
                    onClick = { viewModel.selectTab(Tab.ALERTS) },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Alerts") },
                    label = { Text("Alerts", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = OrbitalBlue,
                        unselectedIconColor = SubSlateText,
                        unselectedTextColor = SubSlateText
                    ),
                    modifier = Modifier.testTag("nav_btn_alerts")
                )
                NavigationBarItem(
                    selected = activeTab == Tab.LOCATIONS,
                    onClick = { viewModel.selectTab(Tab.LOCATIONS) },
                    icon = { Icon(Icons.Default.MyLocation, contentDescription = "Radar Lab") },
                    label = { Text("Radar Lab", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = OrbitalBlue,
                        unselectedIconColor = SubSlateText,
                        unselectedTextColor = SubSlateText
                    ),
                    modifier = Modifier.testTag("nav_btn_locations")
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SpaceBlack)
                .padding(innerPadding)
        ) {
            when (activeTab) {
                Tab.MAP -> OrbitalMapTab(viewModel = viewModel)
                Tab.DIRECTORY -> DirectoryTab(viewModel = viewModel)
                Tab.ALERTS -> PassAlertsTab(viewModel = viewModel)
                Tab.LOCATIONS -> TrackedLocationsTab(viewModel = viewModel)
            }
        }
    }
}
