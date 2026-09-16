package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.TrafficGreenSafe
import com.example.ui.theme.TrafficOrangeHigh
import com.example.ui.theme.TrafficRedCritical
import com.example.ui.theme.TrafficYellowMod

data class JunctionInfo(
    val name: String,
    val displayName: String,
    val cameras: List<String>,
    val description: String
)

val PUNE_JUNCTIONS = listOf(
    JunctionInfo(
        name = "AlankarChowk",
        displayName = "Alankar Chowk",
        cameras = listOf("a2", "a3"),
        description = "Key transit connecting central Pune to rail terminals"
    ),
    JunctionInfo(
        name = "JehangirChowk",
        displayName = "Jehangir Chowk",
        cameras = listOf("j1", "j2", "j3"),
        description = "High-density medical and commercial junction near Jehangir Hospital"
    ),
    JunctionInfo(
        name = "RTOChowk",
        displayName = "RTO Chowk",
        cameras = listOf("r1", "r2", "r3"),
        description = "Major transport corridor with heavy multi-modal bus and transit flow"
    )
)

val TRAFFIC_DIRECTIONS = listOf("UP", "DOWN", "LEFT", "RIGHT")

enum class CongestionLevel(val label: String, val color: Color, val description: String) {
    LOW("Low", TrafficGreenSafe, "Smooth traffic flow, normal speed"),
    MODERATE("Moderate", TrafficYellowMod, "Steady volume, minor delays possible"),
    HIGH("High", TrafficOrangeHigh, "Dense traffic, signal queues building"),
    SEVERE("Severe", TrafficRedCritical, "Critical bottleneck, severe stoppage");

    companion object {
        fun fromVolume(
            volume: Float,
            lowThreshold: Float = 60f,
            modThreshold: Float = 120f,
            highThreshold: Float = 180f
        ): CongestionLevel {
            return when {
                volume < lowThreshold -> LOW
                volume < modThreshold -> MODERATE
                volume < highThreshold -> HIGH
                else -> SEVERE
            }
        }
    }
}

data class ModelBenchmark(
    val modelName: String,
    val mae: Float,
    val rmse: Float,
    val r2: Float,
    val smape: Float,
    val status: String,
    val notes: String
)

val BENCHMARK_MODELS = listOf(
    ModelBenchmark(
        modelName = "Random Forest Baseline",
        mae = 50121.55f,
        rmse = 122169.63f,
        r2 = 0.5847f,
        smape = 48.20f,
        status = "Baseline Only",
        notes = "Suffers from variance on raw counts, tree models unable to extrapolate continuous time trends."
    ),
    ModelBenchmark(
        modelName = "Random Forest V2",
        mae = 116403.57f,
        rmse = 253911.56f,
        r2 = -1.0440f,
        smape = 163.06f,
        status = "Failed Test Split",
        notes = "Negative R² demonstrates complete out-of-time forecasting failure on test sequences."
    ),
    ModelBenchmark(
        modelName = "Random Forest V3",
        mae = 146088.74f,
        rmse = 266605.90f,
        r2 = -1.2535f,
        smape = 1103.66f,
        status = "Catastrophic Breakdown",
        notes = "Severe error magnification due to lack of sequential memory and temporal ordering."
    ),
    ModelBenchmark(
        modelName = "TrafficSense BiLSTM V2 (Proposed)",
        mae = 8.98f,
        rmse = 11.53f,
        r2 = 0.9157f,
        smape = 5.55f,
        status = "Production Champion",
        notes = "Bidirectional LSTM with 12-step lookback (60m) and log1p scaling captures temporal cycles with 91.5% variance explained."
    )
)
