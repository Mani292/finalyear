package com.example.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.TrafficAmberPrimary
import com.example.ui.theme.TrafficCyanAccent
import com.example.ui.theme.TrafficGreenSafe
import com.example.ui.theme.TrafficOrangeHigh
import com.example.ui.theme.TrafficRedCritical

data class MapJunction(
    val id: String,
    val name: String,
    val displayName: String,
    val normalizedPos: Offset, // (0f..1f, 0f..1f) in normalized map space
    val latitude: Double,
    val longitude: Double,
    val cameras: List<String>,
    val directions: List<String> = listOf("UP", "DOWN", "LEFT", "RIGHT"),
    val landmarkName: String
)

data class MapCorridor(
    val id: String,
    val name: String,
    val fromJunctionId: String,
    val toJunctionId: String,
    val waypoints: List<Offset>, // Normalized coordinates
    val roadType: String = "ARTERIAL", // ARTERIAL, HIGHWAY, LOCAL
    val speedLimitKmph: Int = 50
)

data class MapCameraFov(
    val cameraId: String,
    val junctionId: String,
    val baseAngleDegrees: Float,
    val sweepAngleDegrees: Float = 60f,
    val radiusNormalized: Float = 0.12f
)

// Pune Junction Normalized Map Layout (centered around Central Pune / Station / Sassoon / RTO corridor)
val PUNE_MAP_JUNCTIONS = listOf(
    MapJunction(
        id = "AlankarChowk",
        name = "AlankarChowk",
        displayName = "Alankar Chowk",
        normalizedPos = Offset(0.55f, 0.58f),
        latitude = 18.5284,
        longitude = 73.8739,
        cameras = listOf("a2", "a3"),
        landmarkName = "Pune Central Rail Terminal"
    ),
    MapJunction(
        id = "JehangirChowk",
        name = "JehangirChowk",
        displayName = "Jehangir Chowk",
        normalizedPos = Offset(0.72f, 0.38f),
        latitude = 18.5312,
        longitude = 73.8765,
        cameras = listOf("j1", "j2", "j3"),
        landmarkName = "Jehangir & Sassoon Hospital Complex"
    ),
    MapJunction(
        id = "RTOChowk",
        name = "RTOChowk",
        displayName = "RTO Chowk",
        normalizedPos = Offset(0.28f, 0.42f),
        latitude = 18.5305,
        longitude = 73.8645,
        cameras = listOf("r1", "r2", "r3"),
        landmarkName = "Sangam Bridge & Transport Hub"
    )
)

val PUNE_MAP_CORRIDORS = listOf(
    MapCorridor(
        id = "c_alankar_jehangir",
        name = "Connaught & Station Rd",
        fromJunctionId = "AlankarChowk",
        toJunctionId = "JehangirChowk",
        waypoints = listOf(
            Offset(0.55f, 0.58f),
            Offset(0.62f, 0.48f),
            Offset(0.72f, 0.38f)
        ),
        roadType = "ARTERIAL",
        speedLimitKmph = 40
    ),
    MapCorridor(
        id = "c_rto_alankar",
        name = "Dr. Ambedkar Marg",
        fromJunctionId = "RTOChowk",
        toJunctionId = "AlankarChowk",
        waypoints = listOf(
            Offset(0.28f, 0.42f),
            Offset(0.40f, 0.52f),
            Offset(0.55f, 0.58f)
        ),
        roadType = "ARTERIAL",
        speedLimitKmph = 45
    ),
    MapCorridor(
        id = "c_rto_jehangir",
        name = "Sangam / Wellesley Corridor",
        fromJunctionId = "RTOChowk",
        toJunctionId = "JehangirChowk",
        waypoints = listOf(
            Offset(0.28f, 0.42f),
            Offset(0.48f, 0.32f),
            Offset(0.72f, 0.38f)
        ),
        roadType = "HIGHWAY",
        speedLimitKmph = 50
    ),
    MapCorridor(
        id = "c_jehangir_bundgarden",
        name = "Bund Garden Arterial",
        fromJunctionId = "JehangirChowk",
        toJunctionId = "BundGarden",
        waypoints = listOf(
            Offset(0.72f, 0.38f),
            Offset(0.85f, 0.22f),
            Offset(0.92f, 0.12f)
        ),
        roadType = "HIGHWAY",
        speedLimitKmph = 55
    ),
    MapCorridor(
        id = "c_alankar_south",
        name = "Camp / Maldhakka South",
        fromJunctionId = "AlankarChowk",
        toJunctionId = "CampZone",
        waypoints = listOf(
            Offset(0.55f, 0.58f),
            Offset(0.58f, 0.76f),
            Offset(0.60f, 0.90f)
        ),
        roadType = "ARTERIAL",
        speedLimitKmph = 35
    ),
    MapCorridor(
        id = "c_rto_sangam_bridge",
        name = "Old Pune-Mumbai Hwy",
        fromJunctionId = "RTOChowk",
        toJunctionId = "CoEPBridge",
        waypoints = listOf(
            Offset(0.28f, 0.42f),
            Offset(0.18f, 0.32f),
            Offset(0.08f, 0.22f)
        ),
        roadType = "HIGHWAY",
        speedLimitKmph = 60
    )
)

val PUNE_CAMERA_FOVS = listOf(
    MapCameraFov("a2", "AlankarChowk", baseAngleDegrees = 315f), // Pointing toward Station/North
    MapCameraFov("a3", "AlankarChowk", baseAngleDegrees = 135f), // Pointing toward Camp/South
    MapCameraFov("j1", "JehangirChowk", baseAngleDegrees = 45f),  // Bund garden road
    MapCameraFov("j2", "JehangirChowk", baseAngleDegrees = 190f), // Sassoon approach
    MapCameraFov("j3", "JehangirChowk", baseAngleDegrees = 280f), // Connaught approach
    MapCameraFov("r1", "RTOChowk", baseAngleDegrees = 90f),       // Toward Ambedkar road
    MapCameraFov("r2", "RTOChowk", baseAngleDegrees = 220f),      // Toward Sangam bridge
    MapCameraFov("r3", "RTOChowk", baseAngleDegrees = 340f)       // Toward CoEP & North
)
