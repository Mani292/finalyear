package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrafficDao {
    @Query("SELECT * FROM traffic_observations WHERE location = :location AND camera = :camera AND direction = :direction ORDER BY timestamp ASC")
    fun getStreamObservations(location: String, camera: String, direction: String): Flow<List<TrafficObservationEntity>>

    @Query("SELECT * FROM traffic_observations WHERE location = :location AND camera = :camera AND direction = :direction ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentStreamObservations(location: String, camera: String, direction: String, limit: Int): List<TrafficObservationEntity>

    @Query("SELECT COUNT(*) FROM traffic_observations")
    suspend fun getObservationsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservations(observations: List<TrafficObservationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservation(observation: TrafficObservationEntity): Long

    // Predictions
    @Query("SELECT * FROM traffic_predictions ORDER BY timestamp DESC LIMIT 50")
    fun getRecentPredictions(): Flow<List<PredictionRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: PredictionRecordEntity)

    // Alerts
    @Query("SELECT * FROM traffic_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertItemEntity>>

    @Query("SELECT * FROM traffic_alerts WHERE isAcknowledged = 0 ORDER BY timestamp DESC")
    fun getActiveAlerts(): Flow<List<AlertItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertItemEntity)

    @Query("UPDATE traffic_alerts SET isAcknowledged = 1 WHERE id = :alertId")
    suspend fun acknowledgeAlert(alertId: Long)

    @Query("DELETE FROM traffic_alerts")
    suspend fun clearAlerts()

    // Favorite Junctions
    @Query("SELECT * FROM favorite_junctions ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteJunctionEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_junctions WHERE junctionId = :junctionId)")
    fun isFavoriteFlow(junctionId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_junctions WHERE junctionId = :junctionId)")
    suspend fun isFavorite(junctionId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteJunctionEntity)

    @Query("DELETE FROM favorite_junctions WHERE junctionId = :junctionId")
    suspend fun deleteFavorite(junctionId: String)

    @Query("SELECT COUNT(*) FROM favorite_junctions")
    suspend fun getFavoritesCount(): Int
}
