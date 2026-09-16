package com.example.alerts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.ml.BiLSTMEngine
import com.example.model.CongestionLevel
import java.util.concurrent.ConcurrentHashMap

/**
 * TrafficAlertManager monitors predicted data from the BiLSTM time-series model
 * and triggers high-priority local notifications when traffic congestion thresholds
 * are exceeded at specific junctions.
 */
class TrafficAlertManager(private val context: Context) {

    companion object {
        private const val TAG = "TrafficAlertManager"
        const val CHANNEL_ID = "traffic_congestion_alerts_channel"
        const val CHANNEL_NAME = "Traffic Congestion Alerts"
        const val CHANNEL_DESC = "Real-time alerts when junction congestion thresholds are breached"

        // Debounce: prevent duplicate notification for the same junction within 60 seconds
        private const val ALERT_DEBOUNCE_MS = 60_000L
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    // Stores timestamp of last triggered alert per junction: junctionKey -> timestamp
    private val lastAlertTimestamp = ConcurrentHashMap<String, Long>()

    init {
        createNotificationChannel()
    }

    /**
     * Set up Notification Channel for Android 8.0 (API 26) and above.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setShowBadge(true)
            }
            notificationManager?.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created: $CHANNEL_ID")
        }
    }

    /**
     * Monitors a single junction prediction directly from BiLSTMEngine.PredictionResult.
     * Triggers a local push notification if predicted volume or congestion level breaches threshold.
     */
    fun checkAndTriggerAlert(
        location: String,
        prediction: BiLSTMEngine.PredictionResult,
        threshold: Float = 120f
    ): Boolean {
        return monitorPrediction(
            junctionId = location,
            junctionName = location,
            currentVolume = prediction.currentVolume,
            predictedVolume = prediction.predictedVolume,
            congestionLevel = prediction.congestionLevel,
            changePercentage = prediction.changePercentage,
            threshold = threshold
        )
    }

    /**
     * Monitors predicted data from the BiLSTM model and triggers a local notification
     * when the predicted volume exceeds the specified threshold or congestion is severe.
     */
    fun monitorPrediction(
        junctionId: String,
        junctionName: String,
        currentVolume: Float,
        predictedVolume: Float,
        congestionLevel: CongestionLevel,
        changePercentage: Float = 0f,
        threshold: Float = 120f
    ): Boolean {
        // Condition: predicted volume exceeds threshold OR severe/high congestion predicted
        val isThresholdExceeded = predictedVolume >= threshold
        val isHighCongestion = congestionLevel == CongestionLevel.HIGH || congestionLevel == CongestionLevel.SEVERE

        if (!isThresholdExceeded && !isHighCongestion) {
            return false
        }

        // Check debounce per junction
        val now = System.currentTimeMillis()
        val lastTime = lastAlertTimestamp[junctionId] ?: 0L
        if (now - lastTime < ALERT_DEBOUNCE_MS) {
            Log.d(TAG, "Alert for $junctionName debounced (last sent ${(now - lastTime) / 1000}s ago)")
            return false
        }

        // Check POST_NOTIFICATIONS runtime permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Cannot trigger notification: POST_NOTIFICATIONS permission not granted")
                return false
            }
        }

        // Trigger local notification
        val notificationId = junctionId.hashCode()
        triggerLocalNotification(
            notificationId = notificationId,
            junctionName = junctionName,
            currentVolume = currentVolume,
            predictedVolume = predictedVolume,
            congestionLevel = congestionLevel,
            changePercentage = changePercentage,
            threshold = threshold
        )

        lastAlertTimestamp[junctionId] = now
        return true
    }

    /**
     * Builds and posts the high-priority local notification.
     */
    private fun triggerLocalNotification(
        notificationId: Int,
        junctionName: String,
        currentVolume: Float,
        predictedVolume: Float,
        congestionLevel: CongestionLevel,
        changePercentage: Float,
        threshold: Float
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_JUNCTION_ALERT", junctionName)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "⚠️ Traffic Surge Alert: $junctionName"
        val surgeText = if (changePercentage > 0) "+${changePercentage.toInt()}% surge" else "Heavy volume"
        val shortContent = "BiLSTM forecast: ${predictedVolume.toInt()} veh/5min (${congestionLevel.label}). $surgeText."

        val bigText = """
            🚨 Congestion Threshold Exceeded at $junctionName
            • Predicted 5-min Volume: ${predictedVolume.toInt()} vehicles (Threshold: ${threshold.toInt()})
            • Current Volume: ${currentVolume.toInt()} vehicles
            • Congestion Level: ${congestionLevel.label}
            • Forecasted Change: ${if (changePercentage >= 0) "+$changePercentage%" else "$changePercentage%"}
            
            Recommended: Consider alternate corridors or metro connectivity.
        """.trimIndent()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle(title)
            .setContentText(shortContent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            Log.i(TAG, "Local notification successfully dispatched for junction: $junctionName (ID: $notificationId)")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while posting notification", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post notification", e)
        }
    }

    /**
     * Clear debounce cache (useful for tests and reset)
     */
    fun resetDebounce() {
        lastAlertTimestamp.clear()
    }
}
