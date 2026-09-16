package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.ui.theme.TrafficTextMuted
import com.example.ui.theme.TrafficTextPrimary
import com.example.ui.theme.TrafficTextSecondary

@Composable
fun PredictionsTrendsScreen(
    state: TrafficUiState,
    modifier: Modifier = Modifier
) {
    val lastObs = state.observations.lastOrNull()
    val isMorning = lastObs?.isMorningPeak == true
    val isEvening = lastObs?.isEveningPeak == true

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("screen_predictions_trends"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Title Header
            Column {
                Text(
                    text = "TIME-SERIES FORECASTING",
                    style = MaterialTheme.typography.titleMedium,
                    color = TrafficCyanAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp
                )
                Text(
                    text = "${state.selectedLocation} • Camera ${state.selectedCamera.uppercase()} • Direction ${state.selectedDirection}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TrafficTextSecondary
                )
            }
        }

        // 12-Step Lookback Window Visualizer
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lookback_window_card"),
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
                            Icon(Icons.Default.AvTimer, contentDescription = null, tint = TrafficAmberPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "12-STEP LOOKBACK WINDOW (60 MINS)",
                                style = MaterialTheme.typography.labelSmall,
                                color = TrafficAmberPrimary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                        Text(
                            text = "BiLSTM Input Vector",
                            fontSize = 11.sp,
                            color = TrafficTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(state.lookbackWindow) { index, item ->
                            val stepLabel = if (index == state.lookbackWindow.lastIndex) "t (Now)" else "t-${state.lookbackWindow.size - 1 - index}"
                            val isLatest = index == state.lookbackWindow.lastIndex

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = 1.dp,
                                        color = if (isLatest) TrafficAmberPrimary else TrafficNavyCardBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                color = if (isLatest) TrafficAmberPrimary.copy(alpha = 0.15f) else TrafficNavyCardElevated
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = stepLabel,
                                        fontSize = 10.sp,
                                        color = if (isLatest) TrafficAmberPrimary else TrafficTextMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${item.totalVehicles}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isLatest) TrafficAmberPrimary else TrafficTextPrimary
                                    )
                                    Text(
                                        text = item.timeString,
                                        fontSize = 9.sp,
                                        color = TrafficTextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Custom Canvas Line Chart: Actual vs BiLSTM Predicted Volume
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("forecast_chart_card"),
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
                            text = "ACTUAL VS PREDICTED TRAFFIC CURVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TrafficCyanAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(TrafficCyanAccent))
                            Spacer(Modifier.width(4.dp))
                            Text("Actual", fontSize = 10.sp, color = TrafficTextSecondary)
                            Spacer(Modifier.width(8.dp))
                            Box(Modifier.size(8.dp).clip(CircleShape).background(TrafficAmberPrimary))
                            Spacer(Modifier.width(4.dp))
                            Text("BiLSTM Forecast", fontSize = 10.sp, color = TrafficAmberPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val volumes = state.lookbackWindow.map { it.totalVehicles.toFloat() }
                    val predicted = state.predictedVolume

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(TrafficNavyCardElevated.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            if (volumes.isEmpty()) return@Canvas

                            val maxVal = maxOf((volumes + predicted).maxOrNull() ?: 100f, 150f)
                            val minVal = 0f
                            val range = maxVal - minVal

                            val w = size.width
                            val h = size.height
                            val totalPoints = volumes.size + 1
                            val stepX = w / (totalPoints - 1)

                            // Grid Lines
                            for (i in 1..3) {
                                val yGrid = h * (i / 4f)
                                drawLine(
                                    color = Color(0xFF24335C),
                                    start = Offset(0f, yGrid),
                                    end = Offset(w, yGrid),
                                    strokeWidth = 1f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                                )
                            }

                            // Actual Volume Path
                            val actualPath = Path()
                            volumes.forEachIndexed { i, vol ->
                                val x = i * stepX
                                val y = h - ((vol - minVal) / range * h)
                                if (i == 0) actualPath.moveTo(x, y) else actualPath.lineTo(x, y)
                                drawCircle(
                                    color = TrafficCyanAccent,
                                    radius = 3.5f,
                                    center = Offset(x, y)
                                )
                            }
                            drawPath(
                                path = actualPath,
                                color = TrafficCyanAccent,
                                style = Stroke(width = 2.5f)
                            )

                            // Forecast segment (dashed line from last actual to predicted point)
                            if (volumes.isNotEmpty()) {
                                val lastActualX = (volumes.size - 1) * stepX
                                val lastActualY = h - ((volumes.last() - minVal) / range * h)
                                val predX = volumes.size * stepX
                                val predY = h - ((predicted - minVal) / range * h)

                                drawLine(
                                    color = TrafficAmberPrimary,
                                    start = Offset(lastActualX, lastActualY),
                                    end = Offset(predX, predY),
                                    strokeWidth = 3f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                                )
                                drawCircle(
                                    color = TrafficAmberPrimary,
                                    radius = 6f,
                                    center = Offset(predX, predY)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Forecast horizon: Next 5-min step • Inverted from log1p scaling",
                        fontSize = 11.sp,
                        color = TrafficTextMuted
                    )
                }
            }
        }

        // Peak Hour & Rolling Features Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("peak_hour_features_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TrafficNavyCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TrafficNavyCardBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TEMPORAL & TRAFFIC FEATURES",
                        style = MaterialTheme.typography.labelSmall,
                        color = TrafficCyanAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Morning Peak Indicator
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isMorning) TrafficAmberPrimary.copy(alpha = 0.18f) else TrafficNavyCardElevated
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.WbSunny, contentDescription = null, tint = if (isMorning) TrafficAmberPrimary else TrafficTextMuted, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Morning Peak", fontSize = 11.sp, color = TrafficTextMuted)
                                    Text(if (isMorning) "ACTIVE" else "Off-Peak", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isMorning) TrafficAmberPrimary else TrafficTextSecondary)
                                }
                            }
                        }

                        // Evening Peak Indicator
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isEvening) Color(0xFF8B5CF6).copy(alpha = 0.18f) else TrafficNavyCardElevated
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.WbTwilight, contentDescription = null, tint = if (isEvening) Color(0xFF8B5CF6) else TrafficTextMuted, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Evening Peak", fontSize = 11.sp, color = TrafficTextMuted)
                                    Text(if (isEvening) "ACTIVE" else "Off-Peak", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isEvening) Color(0xFF8B5CF6) else TrafficTextSecondary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Rolling 15-Min Volume
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TrafficNavyCardElevated, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Rolling 15-Min Moving Average", fontSize = 12.sp, color = TrafficTextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Smoothed average across last 3 observations", fontSize = 10.sp, color = TrafficTextMuted)
                        }
                        Text(
                            text = "${lastObs?.rolling15minVolume?.toInt() ?: 0} veh",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrafficGreenSafe
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
