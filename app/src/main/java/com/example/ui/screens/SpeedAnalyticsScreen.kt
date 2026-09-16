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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.model.BENCHMARK_MODELS
import com.example.model.ModelBenchmark
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

data class CorridorSpeedMetric(
    val corridorName: String,
    val avgSpeedKmph: Int,
    val congestionPct: Int,
    val status: String
)

val BENGALURU_CORRIDORS = listOf(
    CorridorSpeedMetric("Outer Ring Road (Silk Board - Marathahalli)", 28, 76, "HEAVY"),
    CorridorSpeedMetric("Electronic City Elevated Tollway", 62, 22, "FREE FLOW"),
    CorridorSpeedMetric("Hosur Road (BTM - Madiwala)", 34, 65, "MODERATE"),
    CorridorSpeedMetric("Old Madras Road - Indiranagar", 38, 55, "MODERATE"),
    CorridorSpeedMetric("Bellary Road (Hebbal - Airport Expressway)", 54, 30, "GOOD"),
    CorridorSpeedMetric("Whitefield ITPL Main Road", 24, 82, "CONGESTED")
)

data class TimeOfDayMetric(
    val timeSlot: String,
    val avgSpeedKmph: Int,
    val volumeIndex: Int,
    val peakTag: String
)

val TIME_OF_DAY_STATS = listOf(
    TimeOfDayMetric("06:00 - 08:00 (Early Morning)", 52, 25, "Off-Peak"),
    TimeOfDayMetric("08:00 - 11:30 (Morning Rush)", 24, 94, "Peak Congestion"),
    TimeOfDayMetric("11:30 - 16:30 (Midday Transit)", 40, 50, "Moderate"),
    TimeOfDayMetric("16:30 - 20:30 (Evening Peak)", 21, 98, "Critical Rush"),
    TimeOfDayMetric("20:30 - 23:30 (Late Evening)", 39, 45, "Easing"),
    TimeOfDayMetric("23:30 - 06:00 (Night Traffic)", 58, 15, "Clear")
)

@Composable
fun SpeedAnalyticsScreen(
    uiState: TrafficUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TrafficNavyDark)
            .testTag("speed_analytics_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // City Overview Cards
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("city_analytics_overview"),
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
                        Text(
                            text = "BENGALURU CITY TRAFFIC TELEMETRY",
                            color = TrafficCyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = TrafficEmeraldSuccess.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "REAL-TIME AI",
                                color = TrafficEmeraldSuccess,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Average Speed
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = TrafficCyanAccent, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "${uiState.cityAvgSpeed.toInt()} km/h",
                                    color = TrafficTextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text("City Avg Speed", color = TrafficTextMuted, fontSize = 11.sp)
                        }

                        // Congestion
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.ElectricMeter, contentDescription = null, tint = TrafficAmberPrimary, modifier = Modifier.size(16.dp))
                                Text(
                                    text = uiState.cityCongestion,
                                    color = TrafficAmberPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text("City Congestion", color = TrafficTextMuted, fontSize = 11.sp)
                        }

                        // Routes Analyzed
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Route, contentDescription = null, tint = TrafficIndigoPrimary, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "${uiState.cityRoutesAnalyzed}",
                                    color = TrafficIndigoPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text("Routes Analyzed", color = TrafficTextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Corridor Speeds
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("corridor_speeds_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TrafficNavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ARTERIAL CORRIDOR SPEEDS & CONGESTION",
                        color = TrafficTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        BENGALURU_CORRIDORS.forEach { corridor ->
                            val color = when (corridor.status) {
                                "FREE FLOW" -> TrafficEmeraldSuccess
                                "GOOD" -> TrafficCyanAccent
                                "MODERATE" -> TrafficAmberPrimary
                                else -> TrafficDangerRed
                            }

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = corridor.corridorName,
                                        color = TrafficTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${corridor.avgSpeedKmph} km/h",
                                        color = color,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { corridor.congestionPct / 100f },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = color,
                                    trackColor = TrafficNavyDark
                                )
                            }
                        }
                    }
                }
            }
        }

        // Congestion by Time of Day
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("time_of_day_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TrafficNavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CONGESTION PATTERNS BY TIME OF DAY",
                        color = TrafficTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TIME_OF_DAY_STATS.forEach { stat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TrafficNavyDark)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(stat.timeSlot, color = TrafficTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text(stat.peakTag, color = TrafficTextMuted, fontSize = 10.sp)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${stat.avgSpeedKmph} km/h",
                                        color = if (stat.avgSpeedKmph > 35) TrafficEmeraldSuccess else TrafficDangerRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        color = if (stat.volumeIndex > 75) TrafficDangerRed.copy(alpha = 0.2f) else TrafficNavyCardElevated,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "${stat.volumeIndex}% load",
                                            color = if (stat.volumeIndex > 75) TrafficDangerRed else TrafficTextSecondary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ML Model Benchmark
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("ml_models_benchmark_card"),
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
                        Text(
                            text = "ML PREDICTIVE MODELS BENCHMARK",
                            color = TrafficTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "MAE / RMSE / R²",
                            color = TrafficCyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    BENCHMARK_MODELS.forEach { model ->
                        val isBiLSTM = model.modelName.contains("BiLSTM", ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isBiLSTM) TrafficIndigoPrimary.copy(alpha = 0.15f) else Color.Transparent)
                                .border(
                                    width = if (isBiLSTM) 1.dp else 0.dp,
                                    color = if (isBiLSTM) TrafficIndigoPrimary.copy(alpha = 0.5f) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = model.modelName,
                                        color = if (isBiLSTM) TrafficCyanAccent else TrafficTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isBiLSTM) FontWeight.Bold else FontWeight.Medium
                                    )
                                    if (isBiLSTM) {
                                        Surface(
                                            color = TrafficEmeraldSuccess.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "BEST",
                                                color = TrafficEmeraldSuccess,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "R²: ${model.r2} • sMAPE: ${model.smape}%",
                                    color = TrafficTextMuted,
                                    fontSize = 10.sp
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("MAE: ${model.mae}", color = TrafficTextSecondary, fontSize = 11.sp)
                                    Text("RMSE: ${model.rmse}", color = TrafficTextMuted, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
