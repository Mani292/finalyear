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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
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
import com.example.model.BENCHMARK_MODELS
import com.example.model.ModelBenchmark
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

@Composable
fun BenchmarkScreen(
    state: TrafficUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("screen_model_benchmarks"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                Text(
                    text = "MACHINE LEARNING BENCHMARK",
                    style = MaterialTheme.typography.titleMedium,
                    color = TrafficCyanAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp
                )
                Text(
                    text = "Empirical evaluation: Random Forest baselines vs Proposed BiLSTM",
                    style = MaterialTheme.typography.bodySmall,
                    color = TrafficTextSecondary
                )
            }
        }

        // Champion Winner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("champion_model_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = TrafficNavyCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(TrafficCyanAccent, TrafficAmberPrimary))
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TrafficAmberPrimary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PROPOSED FINAL ARCHITECTURE", color = TrafficAmberPrimary, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = TrafficGreenSafe.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "R² = 0.9157",
                                color = TrafficGreenSafe,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "BiLSTM Time-Series Network (V2)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = TrafficTextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 Core Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricBox("MAE", "8.98", "veh", TrafficCyanAccent)
                        MetricBox("RMSE", "11.53", "veh", TrafficCyanAccent)
                        MetricBox("R² SCORE", "0.9157", "91.5%", TrafficGreenSafe)
                        MetricBox("SMAPE", "5.55%", "zero-safe", TrafficAmberPrimary)
                    }
                }
            }
        }

        // Comparison Table / Cards
        item {
            Text(
                text = "ALL EVALUATED CANDIDATE MODELS",
                style = MaterialTheme.typography.labelMedium,
                color = TrafficCyanAccent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }

        items(state.benchmarks) { model ->
            val isChampion = model.r2 > 0.8f
            val cardColor = if (isChampion) TrafficNavyCardElevated else TrafficNavyCard

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("benchmark_card_${model.modelName.replace(" ", "_")}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isChampion) TrafficCyanAccent.copy(alpha = 0.6f) else TrafficNavyCardBorder
                    )
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = model.modelName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isChampion) TrafficCyanAccent else TrafficTextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isChampion) TrafficGreenSafe.copy(alpha = 0.15f) else TrafficRedCritical.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = model.status,
                                color = if (isChampion) TrafficGreenSafe else TrafficRedCritical,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("MAE: ${String.format("%,.2f", model.mae)}", fontSize = 12.sp, color = TrafficTextSecondary)
                        Text("RMSE: ${String.format("%,.2f", model.rmse)}", fontSize = 12.sp, color = TrafficTextSecondary)
                        Text("R²: ${String.format("%.4f", model.r2)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (model.r2 > 0) TrafficGreenSafe else TrafficRedCritical)
                        Text("SMAPE: ${String.format("%.1f", model.smape)}%", fontSize = 12.sp, color = TrafficTextSecondary)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = model.notes,
                        fontSize = 11.sp,
                        color = TrafficTextMuted,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Academic Scientific Discussion Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("academic_rationale_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TrafficNavyCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TrafficNavyCardBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = TrafficAmberPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SCIENTIFIC INVESTIGATION & FINDINGS",
                            style = MaterialTheme.typography.labelMedium,
                            color = TrafficAmberPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Why Did Random Forest Collapse?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TrafficTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Tree-based ensembles assume independent and identically distributed (i.i.d.) observations, disregarding sequential autocorrelation.\n" +
                                "• When splitting chronologically (no future data leakage), RF encountered unseen distribution shifts and blew up with negative R² (-1.04 to -1.25) and extreme SMAPE (163% to 1103%).",
                        fontSize = 11.sp,
                        color = TrafficTextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Why Does BiLSTM Succeed?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TrafficGreenSafe
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Sequences are preserved strictly within each of the 32 streams (location + camera + direction).\n" +
                                "• 12-step lookback (60 mins) provides complete temporal cycle awareness.\n" +
                                "• log1p transformation stabilizes traffic volume variance and prevents numerical explosion.\n" +
                                "• Bidirectional memory links past accumulation with forward inflection momentum.",
                        fontSize = 11.sp,
                        color = TrafficTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .background(TrafficNavyCardElevated, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 10.sp, color = TrafficTextMuted, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Black, color = accentColor)
        Text(subtitle, fontSize = 9.sp, color = TrafficTextSecondary)
    }
}
