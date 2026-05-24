package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.StarlinkSatellite
import com.example.ui.theme.*
import com.example.viewmodel.SatelliteViewModel
import com.example.viewmodel.Tab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectoryTab(
    viewModel: SatelliteViewModel,
    modifier: Modifier = Modifier
) {
    val satellites by viewModel.satellites.collectAsState()
    val selectedSat by viewModel.selectedSatellite.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredSatellites = remember(satellites, searchQuery) {
        satellites.filter { sat ->
            val name = sat.spaceTrack?.objectName ?: "Starlink"
            name.contains(searchQuery, ignoreCase = true) || 
            sat.id.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("directory_search_input"),
            placeholder = { Text("Search SpaceX Satellites...", color = SubSlateText) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = SubSlateText) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrbitalBlue,
                unfocusedBorderColor = Color(0x33FFFFFF),
                focusedContainerColor = SpaceDarkGray,
                unfocusedContainerColor = SpaceDarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ORBITERS DIRECTORY (${filteredSatellites.size})",
            color = SubSlateText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredSatellites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No starlink satellites found matching \"$searchQuery\"",
                    color = SubSlateText,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredSatellites, key = { it.id }) { sat ->
                    val isSelected = selectedSat?.id == sat.id
                    val objectName = sat.spaceTrack?.objectName ?: "Starlink Satellite"
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0x1F3B82F6) else SpaceDarkGray)
                            .clickable {
                                viewModel.selectSatellite(sat)
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = objectName,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Launch: ${sat.spaceTrack?.launchDate ?: "Unknown"}",
                                color = SubSlateText,
                                fontSize = 12.sp
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column {
                                    Text("Alt", color = SubSlateText, fontSize = 10.sp)
                                    Text(
                                        "${sat.heightKm?.toInt() ?: 550} km",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Column {
                                    Text("Speed", color = SubSlateText, fontSize = 10.sp)
                                    Text(
                                        "${"%.2f".format(sat.velocityKms ?: 7.58)} km/s",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Column {
                                    Text("Inc", color = SubSlateText, fontSize = 10.sp)
                                    Text(
                                        "${"%.1f".format(sat.spaceTrack?.inclination ?: 53.21)}°",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.selectSatellite(sat)
                                viewModel.selectTab(Tab.MAP)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) OrbitalBlue else Color(0x1AFFFFFF)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PinDrop,
                                contentDescription = "Plot on Map",
                                modifier = Modifier.size(14.dp),
                                tint = if (isSelected) Color.White else SubSlateText
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Plot",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else SubSlateText
                            )
                        }
                    }
                }
            }
        }
    }
}
