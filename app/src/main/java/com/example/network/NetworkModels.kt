package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SystemHealthDto(
    @Json(name = "system") val system: String = "TrafficSense AI",
    @Json(name = "status") val status: String = "Online",
    @Json(name = "version") val version: String = "2.0.0",
    @Json(name = "model") val model: String = "BiLSTM Time-Series Forecaster"
)

@JsonClass(generateAdapter = true)
data class ObservationDto(
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
    @Json(name = "time_string") val timeString: String = "",
    @Json(name = "total_vehicles") val totalVehicles: Int = 0,
    @Json(name = "motorbikes") val motorbikes: Int = 0,
    @Json(name = "cars") val cars: Int = 0,
    @Json(name = "buses") val buses: Int = 0,
    @Json(name = "trucks") val trucks: Int = 0
)

@JsonClass(generateAdapter = true)
data class PredictionRequest(
    @Json(name = "location") val location: String,
    @Json(name = "camera") val camera: String,
    @Json(name = "direction") val direction: String,
    @Json(name = "observations") val observations: List<ObservationDto>
)

@JsonClass(generateAdapter = true)
data class PredictionResponse(
    @Json(name = "location") val location: String? = null,
    @Json(name = "camera") val camera: String? = null,
    @Json(name = "direction") val direction: String? = null,
    @Json(name = "current_volume") val currentVolume: Float = 0f,
    @Json(name = "predicted_volume") val predictedVolume: Float = 0f,
    @Json(name = "volume_change_pct") val volumeChangePct: Float = 0f,
    @Json(name = "congestion_level") val congestionLevel: String = "LOW",
    @Json(name = "confidence_score") val confidenceScore: Float = 0.92f,
    @Json(name = "lookback_minutes") val lookbackMinutes: Int = 60,
    @Json(name = "alerts") val alerts: List<String> = emptyList(),
    @Json(name = "peak_hour") val peakHour: Boolean = false
)

@JsonClass(generateAdapter = true)
data class JunctionStatusDto(
    @Json(name = "junction_id") val junctionId: String,
    @Json(name = "name") val name: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "current_volume") val currentVolume: Float,
    @Json(name = "predicted_next_5min_volume") val predictedNext5MinVolume: Float,
    @Json(name = "congestion_level") val congestionLevel: String,
    @Json(name = "trend") val trend: String = "STABLE",
    @Json(name = "active_camera_count") val activeCameraCount: Int = 2,
    @Json(name = "top_congested_direction") val topCongestedDirection: String = "UP",
    @Json(name = "average_speed_kmph") val averageSpeedKmph: Float = 22.5f
)

@JsonClass(generateAdapter = true)
data class TrafficIncidentDto(
    @Json(name = "id") val id: String,
    @Json(name = "junction_id") val junctionId: String,
    @Json(name = "severity") val severity: String,
    @Json(name = "title") val title: String,
    @Json(name = "message") val message: String,
    @Json(name = "timestamp") val timestamp: Long
)

@JsonClass(generateAdapter = true)
data class TrafficStatusResponse(
    @Json(name = "system_status") val systemStatus: String = "ONLINE",
    @Json(name = "server_time") val serverTime: Long = System.currentTimeMillis(),
    @Json(name = "active_junctions") val activeJunctions: Int = 3,
    @Json(name = "overall_congestion_index") val overallCongestionIndex: Float = 65.0f,
    @Json(name = "junctions") val junctions: List<JunctionStatusDto> = emptyList(),
    @Json(name = "active_incidents") val activeIncidents: List<TrafficIncidentDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class JunctionInfoDto(
    @Json(name = "name") val name: String,
    @Json(name = "display_name") val displayName: String? = null,
    @Json(name = "cameras") val cameras: List<String> = emptyList(),
    @Json(name = "directions") val directions: List<String> = emptyList(),
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null
)

@JsonClass(generateAdapter = true)
data class JunctionListResponse(
    @Json(name = "locations") val locations: List<JunctionInfoDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ModelBenchmarkDto(
    @Json(name = "model_name") val modelName: String,
    @Json(name = "mae") val mae: Float,
    @Json(name = "rmse") val rmse: Float,
    @Json(name = "r2") val r2: Float,
    @Json(name = "smape") val smape: Float,
    @Json(name = "status") val status: String,
    @Json(name = "notes") val notes: String
)

// ================= Bengaluru Live API Models (Mani292 backend) =================

@JsonClass(generateAdapter = true)
data class RouteLocationDto(
    @Json(name = "lat") val lat: Double,
    @Json(name = "lng") val lng: Double,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class RouteRequestDto(
    @Json(name = "origin") val origin: RouteLocationDto,
    @Json(name = "destination") val destination: RouteLocationDto,
    @Json(name = "time_of_day") val timeOfDay: String = "now"
)

@JsonClass(generateAdapter = true)
data class RouteServiceCountsDto(
    @Json(name = "hospitals") val hospitals: Int = 0,
    @Json(name = "fuel_stations") val fuel_stations: Int = 0,
    @Json(name = "restaurants") val restaurants: Int = 0
)

@JsonClass(generateAdapter = true)
data class BengaluruRouteDto(
    @Json(name = "route_id") val route_id: Any? = null,
    @Json(name = "name") val name: String = "Route",
    @Json(name = "distance") val distance: Double? = null,
    @Json(name = "distance_km") val distance_km: Double? = null,
    @Json(name = "duration") val duration: Double? = null,
    @Json(name = "eta_minutes") val eta_minutes: Int? = null,
    @Json(name = "traffic_level") val traffic_level: String = "moderate",
    @Json(name = "traffic_score") val traffic_score: Double? = null,
    @Json(name = "predicted_speed") val predicted_speed: Double = 35.0,
    @Json(name = "is_fastest") val is_fastest: Boolean = false,
    @Json(name = "coordinates") val coordinates: List<List<Double>> = emptyList(),
    @Json(name = "steps") val steps: List<String> = emptyList(),
    @Json(name = "services") val services: RouteServiceCountsDto? = null
) {
    val displayDistanceKm: Double
        get() = distance ?: distance_km ?: 7.5

    val displayDurationMin: Int
        get() = duration?.toInt() ?: eta_minutes ?: 15

    val displayRouteId: String
        get() = route_id?.toString() ?: "0"
}

@JsonClass(generateAdapter = true)
data class BengaluruServiceDto(
    @Json(name = "type") val type: String = "service",
    @Json(name = "name") val name: String = "",
    @Json(name = "lat") val lat: Double = 0.0,
    @Json(name = "lng") val lng: Double = 0.0,
    @Json(name = "distance_from_route") val distance_from_route: Double = 0.5
)

@JsonClass(generateAdapter = true)
data class BengaluruServicesResponse(
    @Json(name = "services") val services: List<BengaluruServiceDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class BengaluruIncidentDto(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "type") val type: String = "congestion",
    @Json(name = "location") val location: String = "Bengaluru",
    @Json(name = "severity") val severity: String = "medium",
    @Json(name = "lat") val lat: Double = 12.9165,
    @Json(name = "lng") val lng: Double = 77.623,
    @Json(name = "description") val description: String = ""
)

@JsonClass(generateAdapter = true)
data class BengaluruStatsDto(
    @Json(name = "avg_speed") val avg_speed: Double = 38.5,
    @Json(name = "congestion_level") val congestion_level: String = "Medium",
    @Json(name = "active_incidents") val active_incidents: Int = 2,
    @Json(name = "routes_analyzed") val routes_analyzed: Int = 350,
    @Json(name = "last_updated") val last_updated: String = ""
)
