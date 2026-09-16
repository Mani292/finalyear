package com.example.data

import com.example.ml.BiLSTMEngine
import com.example.model.PUNE_JUNCTIONS
import com.example.model.TRAFFIC_DIRECTIONS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class TrafficRepository(
    private val trafficDao: TrafficDao
) {
    val activeAlerts: Flow<List<AlertItemEntity>> = trafficDao.getActiveAlerts()
    val allAlerts: Flow<List<AlertItemEntity>> = trafficDao.getAllAlerts()
    val recentPredictions: Flow<List<PredictionRecordEntity>> = trafficDao.getRecentPredictions()
    val favoriteJunctions: Flow<List<FavoriteJunctionEntity>> = trafficDao.getAllFavorites()

    fun getStreamObservations(location: String, camera: String, direction: String): Flow<List<TrafficObservationEntity>> {
        return trafficDao.getStreamObservations(location, camera, direction)
    }

    suspend fun ensureDatabasePopulated() = withContext(Dispatchers.IO) {
        val count = trafficDao.getObservationsCount()
        if (count > 0) return@withContext

        // Preload baseline Pune 5-minute time series dataset across 32 streams
        val observations = mutableListOf<TrafficObservationEntity>()
        val startCal = System.currentTimeMillis() - (38 * 5 * 60 * 1000L) // past ~3 hours in 5-min intervals
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        PUNE_JUNCTIONS.forEach { junction ->
            junction.cameras.forEach { camera ->
                TRAFFIC_DIRECTIONS.forEach { direction ->
                    val baseScale = when (junction.name) {
                        "RTOChowk" -> 130f
                        "JehangirChowk" -> 100f
                        else -> 85f
                    } * (if (direction in listOf("UP", "DOWN")) 1.2f else 0.88f)

                    var prevVol = (baseScale * 0.9f).toInt()
                    val rollingWindow = mutableListOf<Int>()

                    for (step in 0 until 38) {
                        val timestamp = startCal + (step * 5 * 60 * 1000L)
                        val cal = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
                        val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
                        val minute = cal.get(java.util.Calendar.MINUTE)

                        val isMorningPeak = (hour in 8..10)
                        val isEveningPeak = (hour in 17..20)
                        val peakMult = if (isMorningPeak) 1.45f else if (isEveningPeak) 1.35f else 0.92f

                        val noise = (Random.nextFloat() - 0.5f) * 0.15f
                        val totalVol = maxOf(15, (baseScale * peakMult * (1f + noise)).toInt())

                        // Vehicle breakdown
                        val pMb = 0.48f + (Random.nextFloat() * 0.06f)
                        val pCar = 0.32f + (Random.nextFloat() * 0.05f)
                        val pBus = 0.08f + (Random.nextFloat() * 0.03f)

                        val mbCount = (totalVol * pMb).toInt()
                        val carCount = (totalVol * pCar).toInt()
                        val busCount = (totalVol * pBus).toInt()
                        val truckCount = maxOf(1, totalVol - (mbCount + carCount + busCount))
                        val verifiedTotal = mbCount + carCount + busCount + truckCount

                        rollingWindow.add(verifiedTotal)
                        if (rollingWindow.size > 3) rollingWindow.removeAt(0)
                        val rollingAvg = rollingWindow.average().toFloat()

                        observations.add(
                            TrafficObservationEntity(
                                location = junction.name,
                                camera = camera,
                                direction = direction,
                                timestamp = timestamp,
                                timeString = timeFormat.format(Date(timestamp)),
                                carCount = carCount,
                                motorbikeCount = mbCount,
                                busCount = busCount,
                                truckCount = truckCount,
                                totalVehicles = verifiedTotal,
                                hour = hour,
                                minute = minute,
                                isMorningPeak = isMorningPeak,
                                isEveningPeak = isEveningPeak,
                                previousVolume = prevVol,
                                rolling15minVolume = rollingAvg
                            )
                        )
                        prevVol = verifiedTotal
                    }
                }
            }
        }

        trafficDao.insertObservations(observations)

        // Preload default favorite junction if none exists
        if (trafficDao.getFavoritesCount() == 0) {
            trafficDao.insertFavorite(
                FavoriteJunctionEntity(
                    junctionId = "AlankarChowk",
                    displayName = "Alankar Chowk",
                    defaultCamera = "a2",
                    defaultDirection = "UP",
                    note = "Central corridor hub near Pune Station",
                    addedAt = System.currentTimeMillis()
                )
            )
            trafficDao.insertFavorite(
                FavoriteJunctionEntity(
                    junctionId = "RTOChowk",
                    displayName = "RTO Chowk",
                    defaultCamera = "r1",
                    defaultDirection = "DOWN",
                    note = "High volume commercial junction",
                    addedAt = System.currentTimeMillis() - 1000
                )
            )
        }
    }

    suspend fun getRecentObservations(location: String, camera: String, direction: String, limit: Int = 12): List<TrafficObservationEntity> {
        return withContext(Dispatchers.IO) {
            trafficDao.getRecentStreamObservations(location, camera, direction, limit).reversed()
        }
    }

    suspend fun recordPrediction(prediction: PredictionRecordEntity) = withContext(Dispatchers.IO) {
        trafficDao.insertPrediction(prediction)
    }

    suspend fun recordAlerts(alerts: List<AlertItemEntity>) = withContext(Dispatchers.IO) {
        alerts.forEach { trafficDao.insertAlert(it) }
    }

    suspend fun acknowledgeAlert(alertId: Long) = withContext(Dispatchers.IO) {
        trafficDao.acknowledgeAlert(alertId)
    }

    suspend fun injectSimulatedObservation(
        location: String,
        camera: String,
        direction: String,
        variationFactor: Float = 1.0f
    ): TrafficObservationEntity = withContext(Dispatchers.IO) {
        val recent = trafficDao.getRecentStreamObservations(location, camera, direction, 3)
        val last = recent.firstOrNull()
        val lastVol = last?.totalVehicles ?: 100
        val lastTimestamp = last?.timestamp ?: System.currentTimeMillis()
        val newTimestamp = lastTimestamp + (5 * 60 * 1000L)

        val cal = java.util.Calendar.getInstance().apply { timeInMillis = newTimestamp }
        val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = cal.get(java.util.Calendar.MINUTE)
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val noise = (Random.nextFloat() - 0.48f) * 0.20f
        val newTotal = maxOf(15, (lastVol * (1f + noise) * variationFactor).toInt())

        val pMb = 0.50f
        val pCar = 0.33f
        val pBus = 0.08f
        val mb = (newTotal * pMb).toInt()
        val car = (newTotal * pCar).toInt()
        val bus = (newTotal * pBus).toInt()
        val truck = maxOf(1, newTotal - (mb + car + bus))
        val verified = mb + car + bus + truck

        val windowVols = recent.map { it.totalVehicles } + verified
        val rollingAvg = windowVols.takeLast(3).average().toFloat()

        val newObs = TrafficObservationEntity(
            location = location,
            camera = camera,
            direction = direction,
            timestamp = newTimestamp,
            timeString = timeFormat.format(Date(newTimestamp)),
            carCount = car,
            motorbikeCount = mb,
            busCount = bus,
            truckCount = truck,
            totalVehicles = verified,
            hour = hour,
            minute = minute,
            isMorningPeak = (hour in 8..10),
            isEveningPeak = (hour in 17..20),
            previousVolume = lastVol,
            rolling15minVolume = rollingAvg
        )
        trafficDao.insertObservation(newObs)
        newObs
    }

    suspend fun toggleFavorite(
        junctionId: String,
        displayName: String,
        defaultCamera: String = "a2",
        defaultDirection: String = "UP",
        note: String = ""
    ) = withContext(Dispatchers.IO) {
        if (trafficDao.isFavorite(junctionId)) {
            trafficDao.deleteFavorite(junctionId)
        } else {
            trafficDao.insertFavorite(
                FavoriteJunctionEntity(
                    junctionId = junctionId,
                    displayName = displayName,
                    defaultCamera = defaultCamera,
                    defaultDirection = defaultDirection,
                    note = note,
                    addedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun removeFavorite(junctionId: String) = withContext(Dispatchers.IO) {
        trafficDao.deleteFavorite(junctionId)
    }

    suspend fun isFavorite(junctionId: String): Boolean = withContext(Dispatchers.IO) {
        trafficDao.isFavorite(junctionId)
    }
}
