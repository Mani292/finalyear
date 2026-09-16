package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "traffic_observations")
data class TrafficObservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val location: String,
    val camera: String,
    val direction: String,
    val timestamp: Long,
    val timeString: String,
    val carCount: Int,
    val motorbikeCount: Int,
    val busCount: Int,
    val truckCount: Int,
    val totalVehicles: Int,
    val hour: Int,
    val minute: Int,
    val isMorningPeak: Boolean,
    val isEveningPeak: Boolean,
    val previousVolume: Int,
    val rolling15minVolume: Float
)

@Entity(tableName = "traffic_predictions")
data class PredictionRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val location: String,
    val camera: String,
    val direction: String,
    val timestamp: Long,
    val currentVolume: Float,
    val predictedNext5MinVolume: Float,
    val volumeChangePct: Float,
    val congestionLevel: String,
    val modelUsed: String = "BiLSTM_V2"
)

@Entity(tableName = "traffic_alerts")
data class AlertItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val location: String,
    val camera: String,
    val direction: String,
    val timestamp: Long,
    val severity: String, // INFO, WARNING, CRITICAL
    val title: String,
    val message: String,
    val isAcknowledged: Boolean = false
)

@Entity(tableName = "favorite_junctions")
data class FavoriteJunctionEntity(
    @PrimaryKey val junctionId: String,
    val displayName: String,
    val defaultCamera: String = "a2",
    val defaultDirection: String = "UP",
    val note: String = "",
    val addedAt: Long = System.currentTimeMillis()
)
