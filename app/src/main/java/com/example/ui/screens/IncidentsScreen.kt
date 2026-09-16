package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.data.AlertItemEntity
import com.example.model.UiTrafficIncident
import com.example.ui.TrafficUiState
import com.example.ui.theme.TrafficAmberPrimary
import com.example.ui.theme.TrafficCyanAccent
import com.example.ui.theme.TrafficDangerRed
import com.example.ui.theme.TrafficDarkCard
import com.example.ui.theme.TrafficEmeraldSuccess
import com.example.ui.theme.TrafficIndigoPrimary
import com.example.ui.theme.TrafficNavyCard
import com.example.ui.theme.TrafficNavyCardBorder
import com.example.ui.theme.TrafficNavyCardElevated
import com.example.ui.theme.TrafficNavyDark
import com.example.ui.theme.TrafficTextMuted
import com.example.ui.theme.TrafficTextPrimary
import com.example.ui.theme.TrafficTextSecondary
import com.example.ui.theme.TrafficWarningAmber

@Composable
fun IncidentsScreen(
    uiState: TrafficUiState,
    onSyncCloudApi: () -> Unit,
    onAcknowledgeAlert: (Long) -> Unit,
    onUpdateLowThreshold: (Float) -> Unit,
    onUpdateModThreshold: (Float) -> Unit,
    onUpdateHighThreshold: (Float) -> Unit,
    onUpdateSurgeThreshold: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TrafficNavyDark)
            .testTag("incidents_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Live Cloud API Connection Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("cloud_api_sync_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = TrafficNavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.isLiveCloudConnected) TrafficEmeraldSuccess else TrafficWarningAmber)
                            )
                            Text(
                                text = "CLOUD BACKEND SYNCHRONIZATION",
                                color = TrafficTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            color = if (uiState.isLiveCloudConnected) TrafficEmeraldSuccess.copy(alpha = 0.2f) else TrafficWarningAmber.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (uiState.isLiveCloudConnected) "ONLINE" else "LOCAL ONLY",
                                color = if (uiState.isLiveCloudConnected) TrafficEmeraldSuccess else TrafficWarningAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Endpoint: https://traffic-g8bi.onrender.com",
                        color = TrafficCyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = uiState.backendHealthStatus,
                        color = TrafficTextSecondary,
                        fontSize = 12.sp
                    )

                    if (uiState.isSyncing) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().height(3.dp),
                            color = TrafficCyanAccent,
                            trackColor = TrafficNavyDark
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onSyncCloudApi,
                        colors = ButtonDefaults.buttonColors(containerColor = TrafficIndigoPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_sync_now")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sync with Live Cloud API Now", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }

        // Live Incidents Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE ROADWAY INCIDENTS",
                    color = TrafficTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${uiState.trafficIncidents.size} Active",
                    color = TrafficDangerRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Live Incidents List
        items(uiState.trafficIncidents) { incident ->
            val color = if (incident.severity == "high") TrafficDangerRed else TrafficWarningAmber

            Card(
                modifier = Modifier.fillMaxWidth().testTag("incident_card_${incident.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TrafficNavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = color.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = incident.severity.uppercase(),
                                    color = color,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = incident.type.uppercase(),
                                color = TrafficTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, tint = TrafficAmberPrimary, modifier = Modifier.size(12.dp))
                            Text("+${incident.delayMinutes} min delay", color = TrafficAmberPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = incident.location,
                        color = TrafficTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = incident.description,
                        color = TrafficTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reported ${incident.timeAgo}",
                        color = TrafficTextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Threshold Configuration
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("threshold_config_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TrafficNavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = TrafficCyanAccent, modifier = Modifier.size(18.dp))
                            Text("ALERT THRESHOLD TUNING", color = TrafficTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Low Threshold
                    Text("Low Congestion Limit: ${uiState.lowThreshold.toInt()} vehicles", color = TrafficEmeraldSuccess, fontSize = 12.sp)
                    Slider(
                        value = uiState.lowThreshold,
                        onValueChange = onUpdateLowThreshold,
                        valueRange = 20f..100f,
                        colors = SliderDefaults.colors(thumbColor = TrafficEmeraldSuccess, activeTrackColor = TrafficEmeraldSuccess)
                    )

                    // Mod Threshold
                    Text("Moderate Congestion Limit: ${uiState.modThreshold.toInt()} vehicles", color = TrafficAmberPrimary, fontSize = 12.sp)
                    Slider(
                        value = uiState.modThreshold,
                        onValueChange = onUpdateModThreshold,
                        valueRange = 80f..160f,
                        colors = SliderDefaults.colors(thumbColor = TrafficAmberPrimary, activeTrackColor = TrafficAmberPrimary)
                    )

                    // High Threshold
                    Text("High Congestion Limit: ${uiState.highThreshold.toInt()} vehicles", color = TrafficDangerRed, fontSize = 12.sp)
                    Slider(
                        value = uiState.highThreshold,
                        onValueChange = onUpdateHighThreshold,
                        valueRange = 140f..250f,
                        colors = SliderDefaults.colors(thumbColor = TrafficDangerRed, activeTrackColor = TrafficDangerRed)
                    )
                }
            }
        }

        // Active Alerts List
        item {
            Text(
                text = "SYSTEM GENERATED ALERTS (${uiState.activeAlerts.size})",
                color = TrafficTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (uiState.activeAlerts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TrafficNavyCardElevated)
                ) {
                    Box(modifier = Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No active traffic surge alerts. Traffic flows within standard bounds.", color = TrafficTextMuted, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(uiState.activeAlerts) { alert ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("alert_item_${alert.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TrafficNavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TrafficDangerRed.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(alert.location, color = TrafficTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(alert.message, color = TrafficTextSecondary, fontSize = 11.sp)
                        }
                        IconButton(
                            onClick = { onAcknowledgeAlert(alert.id) },
                            modifier = Modifier.testTag("btn_ack_${alert.id}")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Acknowledge", tint = TrafficEmeraldSuccess)
                        }
                    }
                }
            }
        }
    }
}
