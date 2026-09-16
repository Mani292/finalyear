package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TrafficUiState
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
import com.example.ui.theme.TrafficYellowMod

@Composable
fun AlertsRulesScreen(
    state: TrafficUiState,
    onUpdateThresholds: (Float, Float, Float, Float) -> Unit,
    onAcknowledgeAlert: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var lowVal by remember(state.lowThreshold) { mutableFloatStateOf(state.lowThreshold) }
    var modVal by remember(state.modThreshold) { mutableFloatStateOf(state.modThreshold) }
    var highVal by remember(state.highThreshold) { mutableFloatStateOf(state.highThreshold) }
    var surgeVal by remember(state.surgeThresholdPct) { mutableFloatStateOf(state.surgeThresholdPct) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("screen_alerts_rules"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                Text(
                    text = "TRAFFIC ALERTS & THRESHOLDS",
                    style = MaterialTheme.typography.titleMedium,
                    color = TrafficCyanAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp
                )
                Text(
                    text = "Configurable congestion boundaries and early warning rules",
                    style = MaterialTheme.typography.bodySmall,
                    color = TrafficTextSecondary
                )
            }
        }

        // Active Alerts Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_alerts_section"),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = TrafficRedCritical, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ACTIVE JUNCTION ALERTS",
                                style = MaterialTheme.typography.labelMedium,
                                color = TrafficRedCritical,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                        Text(
                            text = "${state.activeAlerts.size} Pending",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.activeAlerts.isNotEmpty()) TrafficRedCritical else TrafficGreenSafe
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (state.activeAlerts.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TrafficNavyCardElevated, RoundedCornerShape(10.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TrafficGreenSafe, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("All Junction Streams Operating Normally", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TrafficTextPrimary)
                                Text("No severe bottlenecks or sudden surges detected.", fontSize = 11.sp, color = TrafficTextMuted)
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.activeAlerts.forEach { alert ->
                                val color = if (alert.severity == "CRITICAL") TrafficRedCritical else TrafficOrangeHigh
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                    color = color.copy(alpha = 0.12f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(alert.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
                                            Text(alert.message, fontSize = 11.sp, color = TrafficTextSecondary)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("${alert.location} • ${alert.camera}-${alert.direction}", fontSize = 10.sp, color = TrafficTextMuted)
                                        }
                                        Button(
                                            onClick = { onAcknowledgeAlert(alert.id) },
                                            modifier = Modifier.testTag("btn_ack_alert_${alert.id}"),
                                            colors = ButtonDefaults.buttonColors(containerColor = color),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = TrafficTextPrimary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Acknowledge", fontSize = 11.sp, color = TrafficTextPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Configurable Thresholds Panel
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("configurable_thresholds_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TrafficNavyCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TrafficNavyCardBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = TrafficAmberPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CONFIGURABLE CONGESTION THRESHOLDS",
                            style = MaterialTheme.typography.labelSmall,
                            color = TrafficAmberPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Low Threshold
                    ThresholdSliderRow(
                        label = "Low -> Moderate Threshold",
                        value = lowVal,
                        unit = "veh/5m",
                        range = 30f..90f,
                        color = TrafficGreenSafe,
                        onValueChange = { lowVal = it }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Moderate Threshold
                    ThresholdSliderRow(
                        label = "Moderate -> High Threshold",
                        value = modVal,
                        unit = "veh/5m",
                        range = 90f..150f,
                        color = TrafficYellowMod,
                        onValueChange = { modVal = it }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // High / Severe Threshold
                    ThresholdSliderRow(
                        label = "High -> Severe Threshold",
                        value = highVal,
                        unit = "veh/5m",
                        range = 150f..240f,
                        color = TrafficRedCritical,
                        onValueChange = { highVal = it }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Surge Sensitivity
                    ThresholdSliderRow(
                        label = "Surge Alert Sensitivity",
                        value = surgeVal,
                        unit = "% increase",
                        range = 15f..50f,
                        color = TrafficOrangeHigh,
                        onValueChange = { surgeVal = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onUpdateThresholds(lowVal, modVal, highVal, surgeVal) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_save_thresholds"),
                        colors = ButtonDefaults.buttonColors(containerColor = TrafficAmberPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Apply Updated Parameters", color = Color(0xFF0A1128), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ThresholdSliderRow(
    label: String,
    value: Float,
    unit: String,
    range: ClosedFloatingPointRange<Float>,
    color: androidx.compose.ui.graphics.Color,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 12.sp, color = TrafficTextSecondary)
            Text("${value.toInt()} $unit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = TrafficNavyCardElevated
            )
        )
    }
}
