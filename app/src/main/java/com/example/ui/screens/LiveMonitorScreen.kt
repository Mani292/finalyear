package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TrafficUiState
import com.example.ui.components.CongestionBadge
import com.example.ui.components.JunctionSelectorBar
import com.example.ui.components.SimulationControlBar
import com.example.ui.components.VehicleBreakdownCard
import com.example.ui.theme.TrafficAmberPrimary
import com.example.ui.theme.TrafficCyanAccent
import com.example.ui.theme.TrafficGreenSafe
import com.example.ui.theme.TrafficNavyCard
import com.example.ui.theme.TrafficNavyCardBorder
import com.example.ui.theme.TrafficNavyCardElevated
import com.example.ui.theme.TrafficOrangeHigh
import com.example.ui.theme.TrafficRedCritical
import com.example.ui.theme.TrafficTextMuted
import com.example.ui.theme.TrafficTextPrimary
import com.example.ui.theme.TrafficTextSecondary

@Composable
fun LiveMonitorScreen(
    state: TrafficUiState,
    onLocationSelected: (String) -> Unit,
    onCameraSelected: (String) -> Unit,
    onDirectionSelected: (String) -> Unit,
    onToggleStreaming: () -> Unit,
    onStepForward: () -> Unit,
    onSimulateSurge: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lastObs = state.observations.lastOrNull()
    val isRising = state.volumeChangePct >= 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("screen_live_monitor"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            JunctionSelectorBar(
                selectedLocation = state.selectedLocation,
                selectedCamera = state.selectedCamera,
                selectedDirection = state.selectedDirection,
                onLocationSelected = onLocationSelected,
                onCameraSelected = onCameraSelected,
                onDirectionSelected = onDirectionSelected
            )
        }

        // Active Alert Banner if any
        if (state.activeAlerts.isNotEmpty()) {
            item {
                val latestAlert = state.activeAlerts.first()
                val alertColor = if (latestAlert.severity == "CRITICAL") TrafficRedCritical else TrafficOrangeHigh
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, alertColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .testTag("live_alert_banner"),
                    color = alertColor.copy(alpha = 0.14f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = alertColor, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = latestAlert.title, color = alertColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = latestAlert.message, color = TrafficTextSecondary, fontSize = 11.sp, maxLines = 2)
                        }
                    }
                }
            }
        }

        // Hero Prediction Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_prediction_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TrafficNavyCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(TrafficCyanAccent.copy(alpha = 0.5f), TrafficNavyCardBorder)))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header with live feed pulse
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (state.isAutoStreaming) TrafficGreenSafe else TrafficCyanAccent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (state.isAutoStreaming) "LIVE STREAMING (5-MIN STEP)" else "STREAM CACHED",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (state.isAutoStreaming) TrafficGreenSafe else TrafficCyanAccent,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = lastObs?.timeString ?: "--:--",
                            style = MaterialTheme.typography.labelMedium,
                            color = TrafficTextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Primary Dual Metrics: Current vs Next-5-Min Predicted
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Current Volume
                        Column {
                            Text(
                                text = "CURRENT 5-MIN VOLUME",
                                style = MaterialTheme.typography.labelSmall,
                                color = TrafficTextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${state.currentVolume.toInt()}",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TrafficTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "veh",
                                    fontSize = 14.sp,
                                    color = TrafficTextSecondary,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }

                        // Divider Arrow
                        Icon(
                            imageVector = Icons.Default.AutoGraph,
                            contentDescription = null,
                            tint = TrafficCyanAccent,
                            modifier = Modifier
                                .size(28.dp)
                                .padding(bottom = 8.dp)
                        )

                        // Next 5-Min Predicted Volume (BiLSTM)
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "NEXT 5-MIN PREDICTION",
                                style = MaterialTheme.typography.labelSmall,
                                color = TrafficAmberPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${state.predictedVolume.toInt()}",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TrafficAmberPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "veh",
                                    fontSize = 14.sp,
                                    color = TrafficAmberPrimary.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status Bar: Congestion Badge & % Delta
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CongestionBadge(congestionLevel = state.congestionLevel)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isRising) TrafficRedCritical.copy(alpha = 0.15f) else TrafficGreenSafe.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isRising) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (isRising) TrafficRedCritical else TrafficGreenSafe,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${if (isRising) "+" else ""}${state.volumeChangePct}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRising) TrafficRedCritical else TrafficGreenSafe
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Model Badge
                    Text(
                        text = "Forecaster: BiLSTM Model V2 • Lookback = 12 steps (60m) • log1p scaled",
                        fontSize = 11.sp,
                        color = TrafficTextMuted
                    )
                }
            }
        }

        // Vehicle Breakdown Card
        item {
            VehicleBreakdownCard(
                cars = lastObs?.carCount ?: 0,
                motorbikes = lastObs?.motorbikeCount ?: 0,
                buses = lastObs?.busCount ?: 0,
                trucks = lastObs?.truckCount ?: 0,
                total = lastObs?.totalVehicles ?: 0
            )
        }

        // Simulation Controller
        item {
            SimulationControlBar(
                isStreaming = state.isAutoStreaming,
                onToggleStreaming = onToggleStreaming,
                onStepForward = onStepForward,
                onSimulateSurge = onSimulateSurge
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
