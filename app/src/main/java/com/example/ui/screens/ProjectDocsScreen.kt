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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.School
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun ProjectDocsScreen(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("screen_project_docs"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.School, contentDescription = null, tint = TrafficAmberPrimary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "B.TECH FINAL-YEAR PROJECT REPORT",
                        style = MaterialTheme.typography.titleMedium,
                        color = TrafficAmberPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                }
                Text(
                    text = "TrafficSense AI: Intelligent Traffic Prediction & Management System",
                    style = MaterialTheme.typography.bodySmall,
                    color = TrafficTextSecondary
                )
            }
        }

        // Abstract Card
        item {
            DocCard(
                title = "PROJECT ABSTRACT & PROBLEM STATEMENT",
                icon = Icons.Default.Description,
                iconColor = TrafficCyanAccent
            ) {
                Text(
                    text = "High-density urban bottlenecks at critical metropolitan junctions (Pune's Alankar Chowk, Jehangir Chowk, RTO Chowk) incur acute economic delays, fuel wastage, and emergency route blockades. Existing management systems rely on fixed-interval timers or post-hoc reactive adjustments without predictive foresight.\n\n" +
                            "TrafficSense AI formulates an end-to-end multi-stream intelligent traffic prediction and management pipeline. Ingesting 5-minute counts across classified vehicle types (Cars, Motorbikes, Buses, Trucks) across 32 independent camera-direction streams, it employs a Bidirectional LSTM neural network with a 12-step lookback window (60 mins) and log1p variance stabilization to forecast the next 5-minute traffic volume and alert controllers to impending congestion.",
                    fontSize = 12.sp,
                    color = TrafficTextSecondary,
                    lineHeight = 17.sp
                )
            }
        }

        // Objectives Card
        item {
            DocCard(
                title = "CORE PROJECT OBJECTIVES",
                icon = Icons.Default.Lightbulb,
                iconColor = TrafficAmberPrimary
            ) {
                val objectives = listOf(
                    "Ingest and clean multi-sensor CCTV vehicle counts, resolving duplicated records across 32 independent streams.",
                    "Engineer temporal features: 5-min volumes, lag features, rolling 15-min volume, morning/evening peak indicators.",
                    "Develop BiLSTM time-series model (Lookback=12) with chronological 80/20 train/test split without future data leakage.",
                    "Benchmark against Random Forest baselines (Baseline, V2, V3) on MAE, RMSE, R², and zero-safe SMAPE.",
                    "Implement configurable congestion classification (Low, Moderate, High, Severe) and early warning alerts.",
                    "Deliver native Android command dashboard with Room DB persistence and FastAPI backend service."
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    objectives.forEachIndexed { i, obj ->
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(TrafficAmberPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = obj,
                                fontSize = 12.sp,
                                color = TrafficTextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // 12-Module Architecture Card
        item {
            DocCard(
                title = "COMPLETE SYSTEM ARCHITECTURE (MODULES A - L)",
                icon = Icons.Default.AccountTree,
                iconColor = TrafficGreenSafe
            ) {
                val modules = listOf(
                    "Module A (Data Acquisition)" to "Historical and streaming 5-min traffic ingestion across 32 streams.",
                    "Module B (Preprocessing)" to "Timestamp validation, duplicate aggregation (1449 raw -> 1208 clean).",
                    "Module C (Feature Engineering)" to "1176 feature rows, lag features, rolling 15m volume, peak flags.",
                    "Module D (ML Prediction)" to "BiLSTM model with lookback=12 (60m) and log1p transformation.",
                    "Module E (Congestion Status)" to "Configurable classification: Low, Moderate, High, Severe.",
                    "Module F (Traffic Monitoring)" to "Real-time volume tracking, camera switching, vehicle mix.",
                    "Module G (Visualization)" to "Actual vs predicted curves, sequence window cards, volume trends.",
                    "Module H (Alerts & Rules)" to "Severe saturation warnings, sudden surge detection (+25%).",
                    "Module I (Junction Module)" to "Location -> Camera -> Direction selection across Pune nodes.",
                    "Module J (Backend API)" to "FastAPI REST microservice with prediction and benchmark endpoints.",
                    "Module K (Storage & DB)" to "Local Room SQLite database for observations, predictions, alerts.",
                    "Module L (Frontend Dashboard)" to "Native Jetpack Compose command interface with live simulation."
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    modules.forEach { (mod, desc) ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TrafficNavyCardElevated, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(mod, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TrafficCyanAccent)
                            Text(desc, fontSize = 11.sp, color = TrafficTextSecondary)
                        }
                    }
                }
            }
        }

        // Demonstration Flow Guide
        item {
            DocCard(
                title = "EXAMINER DEMONSTRATION WALKTHROUGH",
                icon = Icons.Default.PlayCircle,
                iconColor = Color(0xFF8B5CF6)
            ) {
                val steps = listOf(
                    "Step 1: Open Live Monitor tab and observe initial Alankar Chowk feed.",
                    "Step 2: Switch Junction to Jehangir Chowk or RTO Chowk; notice dynamic camera and direction options.",
                    "Step 3: Inspect current 5-min volume and next 5-min BiLSTM prediction with +/-% delta.",
                    "Step 4: Tap '+5 Min' to step the simulation forward and watch the prediction update reactively.",
                    "Step 5: Tap 'Surge' to inject a sudden 45% traffic burst and observe the Warning Alert trigger.",
                    "Step 6: Navigate to 'Predictions & Trends' to inspect the 12-step lookback window and canvas chart.",
                    "Step 7: Navigate to 'Alerts & Rules' to adjust congestion threshold sliders and acknowledge alerts.",
                    "Step 8: Navigate to 'ML Benchmark' to review the empirical proof comparing BiLSTM vs Random Forest."
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    steps.forEach { step ->
                        Text(step, fontSize = 12.sp, color = TrafficTextSecondary, lineHeight = 16.sp)
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
private fun DocCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TrafficNavyCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TrafficNavyCardBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = iconColor,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
