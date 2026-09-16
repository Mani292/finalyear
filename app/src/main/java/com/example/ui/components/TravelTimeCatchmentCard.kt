package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Polyline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.IsochroneCatchment
import com.example.ui.theme.TrafficAmberPrimary
import com.example.ui.theme.TrafficCyanAccent
import com.example.ui.theme.TrafficDarkCard
import com.example.ui.theme.TrafficEmeraldSuccess
import com.example.ui.theme.TrafficIndigoPrimary
import com.example.ui.theme.TrafficNavyCard
import com.example.ui.theme.TrafficNavyCardBorder
import com.example.ui.theme.TrafficNavyDark
import com.example.ui.theme.TrafficTextMuted
import com.example.ui.theme.TrafficTextPrimary
import com.example.ui.theme.TrafficTextSecondary

data class TransportModeOption(
    val modeKey: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

val TRANSPORT_MODES = listOf(
    TransportModeOption("driving+ferry", "Car / Cab", Icons.Default.DirectionsCar),
    TransportModeOption("public_transport", "BMTC / Metro", Icons.Default.DirectionsBus),
    TransportModeOption("cycling+ferry", "Two-Wheeler", Icons.Default.DirectionsBike),
    TransportModeOption("walking+ferry", "Walking", Icons.Default.DirectionsWalk)
)

val DURATION_MINUTES_OPTIONS = listOf(15, 30, 45)

@Composable
fun TravelTimeCatchmentCard(
    originName: String,
    catchment: IsochroneCatchment?,
    isLoading: Boolean,
    selectedMinutes: Int,
    selectedMode: String,
    statusMessage: String?,
    onSelectMinutes: (Int) -> Unit,
    onSelectMode: (String) -> Unit,
    onCalculate: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("travel_time_catchment_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TrafficNavyCard),
        border = BorderStroke(1.dp, TrafficCyanAccent.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = TrafficCyanAccent.copy(alpha = 0.15f),
                        shape = CircleShape,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = TrafficCyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "TRAVELTIME REACHABILITY",
                                color = TrafficCyanAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Surface(
                                color = TrafficEmeraldSuccess.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "ISOCHRONE API",
                                    color = TrafficEmeraldSuccess,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Origin: $originName",
                            color = TrafficTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                if (catchment != null) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(28.dp).testTag("btn_clear_catchment")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Isochrone",
                            tint = TrafficTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Duration Selection Chips (15m, 30m, 45m)
            Text(
                text = "COMMUTE TIME LIMIT",
                color = TrafficTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DURATION_MINUTES_OPTIONS.forEach { mins ->
                    val isSelected = mins == selectedMinutes
                    val chipColor = when (mins) {
                        15 -> TrafficEmeraldSuccess
                        30 -> TrafficCyanAccent
                        else -> TrafficAmberPrimary
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectMinutes(mins) },
                        label = {
                            Text(
                                text = "$mins Minutes",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipColor.copy(alpha = 0.25f),
                            selectedLabelColor = chipColor,
                            containerColor = TrafficNavyDark,
                            labelColor = TrafficTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) chipColor else TrafficNavyCardBorder,
                            selectedBorderColor = chipColor,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.5.dp,
                            enabled = true,
                            selected = isSelected
                        ),
                        modifier = Modifier.weight(1f).testTag("chip_minutes_$mins")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Transport Mode Selection
            Text(
                text = "TRANSPORTATION MODE",
                color = TrafficTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TRANSPORT_MODES) { modeOpt ->
                    val isSelected = modeOpt.modeKey == selectedMode
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectMode(modeOpt.modeKey) },
                        leadingIcon = {
                            Icon(
                                imageVector = modeOpt.icon,
                                contentDescription = null,
                                tint = if (isSelected) TrafficCyanAccent else TrafficTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(
                                text = modeOpt.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TrafficCyanAccent.copy(alpha = 0.2f),
                            selectedLabelColor = TrafficCyanAccent,
                            containerColor = TrafficNavyDark,
                            labelColor = TrafficTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) TrafficCyanAccent else TrafficNavyCardBorder,
                            selectedBorderColor = TrafficCyanAccent,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.5.dp,
                            enabled = true,
                            selected = isSelected
                        ),
                        modifier = Modifier.testTag("chip_mode_${modeOpt.modeKey}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button
            Button(
                onClick = onCalculate,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TrafficCyanAccent,
                    disabledContainerColor = TrafficCyanAccent.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_calculate_isochrone")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = TrafficNavyDark,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fetching TravelTime API...", color = TrafficNavyDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                } else {
                    Icon(
                        imageVector = Icons.Default.Polyline,
                        contentDescription = null,
                        tint = TrafficNavyDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (catchment == null) "Show Reachable Catchment Area" else "Refresh $selectedMinutes-Min Reachable Polygon",
                        color = TrafficNavyDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Results summary box if catchment active
            AnimatedVisibility(visible = catchment != null) {
                catchment?.let { c ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(TrafficNavyDark)
                            .border(1.dp, TrafficCyanAccent.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Catchment Results (${c.minutes}m • ${c.displayMode})",
                                color = TrafficCyanAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = TrafficEmeraldSuccess.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "${c.polygons.size} Isochrone Polygons",
                                    color = TrafficEmeraldSuccess,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("${c.totalReachableJunctions} / 8", color = TrafficTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Junctions in Reach", color = TrafficTextMuted, fontSize = 11.sp)
                            }
                            Column {
                                Text("${c.totalReachableServices} Nearby", color = TrafficAmberPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Services Reachable", color = TrafficTextMuted, fontSize = 11.sp)
                            }
                            Column {
                                Text("Real-Time", color = TrafficEmeraldSuccess, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("TravelTime Model", color = TrafficTextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            if (!statusMessage.isNullOrBlank() && catchment == null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = statusMessage,
                    color = TrafficTextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
