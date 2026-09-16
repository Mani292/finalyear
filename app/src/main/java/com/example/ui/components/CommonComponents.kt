package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CongestionLevel
import com.example.model.JunctionInfo
import com.example.model.PUNE_JUNCTIONS
import com.example.model.TRAFFIC_DIRECTIONS
import com.example.ui.theme.TrafficAmberPrimary
import com.example.ui.theme.TrafficCyanAccent
import com.example.ui.theme.TrafficNavyCard
import com.example.ui.theme.TrafficNavyCardBorder
import com.example.ui.theme.TrafficNavyCardElevated
import com.example.ui.theme.TrafficTextMuted
import com.example.ui.theme.TrafficTextPrimary
import com.example.ui.theme.TrafficTextSecondary

@Composable
fun JunctionSelectorBar(
    selectedLocation: String,
    selectedCamera: String,
    selectedDirection: String,
    onLocationSelected: (String) -> Unit,
    onCameraSelected: (String) -> Unit,
    onDirectionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentJunction = PUNE_JUNCTIONS.firstOrNull { it.name == selectedLocation } ?: PUNE_JUNCTIONS.first()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("junction_selector_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TrafficNavyCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TrafficNavyCardBorder))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Junction Locations
            Text(
                text = "INTERSECTION / JUNCTION",
                style = MaterialTheme.typography.labelSmall,
                color = TrafficCyanAccent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(PUNE_JUNCTIONS) { junction ->
                    val isSelected = junction.name == selectedLocation
                    FilterChip(
                        selected = isSelected,
                        onClick = { onLocationSelected(junction.name) },
                        label = { Text(junction.displayName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.testTag("chip_loc_${junction.name}"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TrafficAmberPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = TrafficAmberPrimary,
                            containerColor = TrafficNavyCardElevated,
                            labelColor = TrafficTextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Camera & Direction row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cameras
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CAMERA FEED",
                        style = MaterialTheme.typography.labelSmall,
                        color = TrafficTextMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(currentJunction.cameras) { cam ->
                            val isSelected = cam == selectedCamera
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onCameraSelected(cam) }
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) TrafficCyanAccent else TrafficNavyCardBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .testTag("chip_cam_$cam"),
                                color = if (isSelected) TrafficCyanAccent.copy(alpha = 0.18f) else TrafficNavyCardElevated
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Videocam,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isSelected) TrafficCyanAccent else TrafficTextMuted
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = cam.uppercase(),
                                        color = if (isSelected) TrafficCyanAccent else TrafficTextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Directions
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TRAFFIC DIRECTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = TrafficTextMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(TRAFFIC_DIRECTIONS) { dir ->
                            val isSelected = dir == selectedDirection
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onDirectionSelected(dir) }
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) TrafficAmberPrimary else TrafficNavyCardBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .testTag("chip_dir_$dir"),
                                color = if (isSelected) TrafficAmberPrimary.copy(alpha = 0.18f) else TrafficNavyCardElevated
                            ) {
                                Text(
                                    text = dir,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    color = if (isSelected) TrafficAmberPrimary else TrafficTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CongestionBadge(
    congestionLevel: CongestionLevel,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, congestionLevel.color.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .testTag("congestion_badge"),
        color = congestionLevel.color.copy(alpha = 0.16f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(congestionLevel.color.copy(alpha = if (congestionLevel == CongestionLevel.SEVERE) alphaPulse else 1f))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${congestionLevel.label.uppercase()} TRAFFIC",
                color = congestionLevel.color,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
fun VehicleBreakdownCard(
    cars: Int,
    motorbikes: Int,
    buses: Int,
    trucks: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val safeTotal = if (total > 0) total.toFloat() else 1f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vehicle_breakdown_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TrafficNavyCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TrafficNavyCardBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VEHICLE COMPOSITION",
                    style = MaterialTheme.typography.labelMedium,
                    color = TrafficCyanAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$total vehicles / 5-min",
                    style = MaterialTheme.typography.bodySmall,
                    color = TrafficTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Visual Segmented Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                if (motorbikes > 0) Box(Modifier.weight(motorbikes / safeTotal).background(TrafficAmberPrimary))
                if (cars > 0) Box(Modifier.weight(cars / safeTotal).background(TrafficCyanAccent))
                if (buses > 0) Box(Modifier.weight(buses / safeTotal).background(Color(0xFF8B5CF6)))
                if (trucks > 0) Box(Modifier.weight(trucks / safeTotal).background(Color(0xFFEC4899)))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grid breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                VehicleCountItem(
                    name = "Motorbikes",
                    count = motorbikes,
                    pct = (motorbikes / safeTotal * 100).toInt(),
                    color = TrafficAmberPrimary
                )
                VehicleCountItem(
                    name = "Cars",
                    count = cars,
                    pct = (cars / safeTotal * 100).toInt(),
                    color = TrafficCyanAccent
                )
                VehicleCountItem(
                    name = "Buses",
                    count = buses,
                    pct = (buses / safeTotal * 100).toInt(),
                    color = Color(0xFF8B5CF6)
                )
                VehicleCountItem(
                    name = "Trucks",
                    count = trucks,
                    pct = (trucks / safeTotal * 100).toInt(),
                    color = Color(0xFFEC4899)
                )
            }
        }
    }
}

@Composable
private fun VehicleCountItem(
    name: String,
    count: Int,
    pct: Int,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = name, fontSize = 11.sp, color = TrafficTextMuted)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = "$count", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TrafficTextPrimary)
        Text(text = "$pct%", fontSize = 10.sp, color = TrafficTextSecondary)
    }
}

@Composable
fun SimulationControlBar(
    isStreaming: Boolean,
    onToggleStreaming: () -> Unit,
    onStepForward: () -> Unit,
    onSimulateSurge: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("simulation_control_bar"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TrafficNavyCardElevated),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TrafficNavyCardBorder))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "REAL-TIME TRAFFIC SENSOR SIMULATOR",
                style = MaterialTheme.typography.labelSmall,
                color = TrafficAmberPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onToggleStreaming,
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("btn_toggle_streaming"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isStreaming) Color(0xFFEF4444) else TrafficCyanAccent
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (isStreaming) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF0A1128)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isStreaming) "Pause" else "Live Stream",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A1128),
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = onStepForward,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_step_forward"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TrafficTextPrimary)
                ) {
                    Icon(Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "+5 Min", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onSimulateSurge,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_simulate_surge"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TrafficAmberPrimary)
                ) {
                    Icon(Icons.Default.Whatshot, contentDescription = null, modifier = Modifier.size(14.dp), tint = TrafficAmberPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Surge", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TrafficAmberPrimary)
                }
            }
        }
    }
}
