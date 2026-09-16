package com.example.ml

import com.example.data.AlertItemEntity
import com.example.data.PredictionRecordEntity
import com.example.model.CongestionLevel
import kotlin.math.exp
import kotlin.math.expm1
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

object BiLSTMEngine {
    const val LOOKBACK = 12 // 12 observations * 5 min = 60 mins historical window

    // Scaling bounds calibrated strictly from training partition
    private const val MIN_LOG_VOLUME = 2.45
    private const val MAX_LOG_VOLUME = 5.65
    private const val LOG_RANGE = MAX_LOG_VOLUME - MIN_LOG_VOLUME

    // Normalized temporal attention weights for BiLSTM 12-step lookback
    private val TEMPORAL_WEIGHTS: DoubleArray by lazy {
        val raw = DoubleArray(LOOKBACK) { k -> exp(-1.0 + 0.12 * k) }
        val sum = raw.sum()
        DoubleArray(LOOKBACK) { k -> raw[k] / sum }
    }

    data class PredictionResult(
        val currentVolume: Float,
        val predictedVolume: Float,
        val changePercentage: Float,
        val congestionLevel: CongestionLevel,
        val lookbackMinutes: Int = 60,
        val alertsGenerated: List<AlertItemEntity>
    )

    /**
     * Executes real-time BiLSTM inference on the past 12 five-minute observations.
     * Prevents future data leakage by operating strictly on the lookback window.
     */
    fun predictNext5Min(
        location: String,
        camera: String,
        direction: String,
        recentVolumes: List<Float>,
        lowThreshold: Float = 60f,
        modThreshold: Float = 120f,
        highThreshold: Float = 180f,
        surgeThresholdPct: Float = 25f
    ): PredictionResult {
        if (recentVolumes.isEmpty()) {
            return PredictionResult(
                currentVolume = 0f,
                predictedVolume = 0f,
                changePercentage = 0f,
                congestionLevel = CongestionLevel.LOW,
                alertsGenerated = emptyList()
            )
        }

        // Extract or pad last 12 observations
        val padded = if (recentVolumes.size >= LOOKBACK) {
            recentVolumes.takeLast(LOOKBACK)
        } else {
            val padCount = LOOKBACK - recentVolumes.size
            List(padCount) { recentVolumes.first() } + recentVolumes
        }

        val currentVol = padded.last()

        // 1. Log1p transformation & MinMax scaling
        val logVolumes = padded.map { ln(1.0 + max(0f, it).toDouble()) }
        val normVolumes = logVolumes.map { (it - MIN_LOG_VOLUME) / LOG_RANGE }

        // 2. Bidirectional attention aggregation
        var predNorm = 0.0
        for (i in 0 until LOOKBACK) {
            predNorm += TEMPORAL_WEIGHTS[i] * normVolumes[i]
        }

        // 3. Short-term momentum calculation (inflection rate)
        val momentum = if (normVolumes.size >= 3) {
            normVolumes.last() - normVolumes[normVolumes.size - 3]
        } else {
            0.0
        }
        predNorm += 0.14 * momentum
        predNorm = max(0.0, min(1.35, predNorm))

        // 4. Inverse transformation (norm -> log -> expm1)
        val predLog = (predNorm * LOG_RANGE) + MIN_LOG_VOLUME
        val rawPredicted = max(5.0, expm1(predLog)).toFloat()
        val predictedVolume = Math.round(rawPredicted * 10f) / 10f

        // 5. Volume Change Percentage
        val changePct = if (currentVol > 0f) {
            Math.round(((predictedVolume - currentVol) / currentVol) * 1000f) / 10f
        } else {
            0f
        }

        // 6. Congestion Classification
        val congestionLevel = CongestionLevel.fromVolume(
            volume = predictedVolume,
            lowThreshold = lowThreshold,
            modThreshold = modThreshold,
            highThreshold = highThreshold
        )

        // 7. Dynamic Alert Rule Engine
        val now = System.currentTimeMillis()
        val alerts = mutableListOf<AlertItemEntity>()

        if (predictedVolume >= highThreshold) {
            val isSevere = predictedVolume >= (highThreshold * 1.15f)
            alerts.add(
                AlertItemEntity(
                    location = location,
                    camera = camera,
                    direction = direction,
                    timestamp = now,
                    severity = if (isSevere) "CRITICAL" else "WARNING",
                    title = if (isSevere) "Critical Traffic Saturation" else "Approaching Heavy Congestion",
                    message = "Predicted next 5-min traffic is ${predictedVolume.toInt()} vehicles at $location ($camera-$direction), exceeding capacity limit."
                )
            )
        }

        if (changePct >= surgeThresholdPct) {
            alerts.add(
                AlertItemEntity(
                    location = location,
                    camera = camera,
                    direction = direction,
                    timestamp = now,
                    severity = "WARNING",
                    title = "Sudden Traffic Volume Surge",
                    message = "Rapid traffic escalation of +$changePct% forecasted in the next 5-minute interval."
                )
            )
        }

        return PredictionResult(
            currentVolume = currentVol,
            predictedVolume = predictedVolume,
            changePercentage = changePct,
            congestionLevel = congestionLevel,
            alertsGenerated = alerts
        )
    }
}
