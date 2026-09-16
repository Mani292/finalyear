package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CongestionLevel
import com.example.model.MapCameraFov
import com.example.model.MapCorridor
import com.example.model.MapJunction
import com.example.model.PUNE_CAMERA_FOVS
import com.example.model.PUNE_MAP_CORRIDORS
import com.example.model.PUNE_MAP_JUNCTIONS
import com.example.ui.theme.TrafficAmberPrimary
import com.example.ui.theme.TrafficCyanAccent
import com.example.ui.theme.TrafficGreenSafe
import com.example.ui.theme.TrafficNavyCard
import com.example.ui.theme.TrafficNavyCardBorder
import com.example.ui.theme.TrafficNavyDark
import com.example.ui.theme.TrafficOrangeHigh
import com.example.ui.theme.TrafficRedCritical
import com.example.ui.theme.TrafficTextMuted
import com.example.ui.theme.TrafficTextPrimary
import com.example.ui.theme.TrafficTextSecondary
import com.example.ui.theme.TrafficYellowMod
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class MapStyle {
    GOOGLE_DARK,
    SATELLITE_HYBRID,
    HEATMAP_ANALYTICS
}

@Composable
fun AdvancedGoogleTrafficMap(
    selectedLocation: String,
    selectedCamera: String,
    selectedDirection: String,
    currentVolume: Float,
    predictedVolume: Float,
    congestionLevel: CongestionLevel,
    isCloudSynced: Boolean,
    onLocationSelected: (String) -> Unit,
    onCameraSelected: (String) -> Unit,
    onDirectionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var mapStyle by remember { mutableStateOf(MapStyle.GOOGLE_DARK) }
    var isTrafficLayerVisible by remember { mutableStateOf(true) }
    var is3dPerspective by remember { mutableStateOf(false) }
    var isFovVisible by remember { mutableStateOf(true) }
    var mapRotation by remember { mutableFloatStateOf(0f) }

    val textMeasurer = rememberTextMeasurer()

    // Continuous animations for radar scan and vehicle movement
    val infiniteTransition = rememberInfiniteTransition(label = "map_animations")
    val vehicleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vehicle_progress"
    )

    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_pulse"
    )

    val fovSweep by infiniteTransition.animateFloat(
        initialValue = -25f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fov_sweep"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(TrafficNavyDark)
            .border(1.dp, TrafficNavyCardBorder, RoundedCornerShape(16.dp))
            .testTag("advanced_google_traffic_map")
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        // Main Map Canvas with touch gesture support
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        panOffset += dragAmount
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            // Check if tapped near any Pune junction pin
                            val effectiveWidth = width * zoomLevel
                            val effectiveHeight = height * zoomLevel
                            val baseOrigin = Offset(
                                (width - effectiveWidth) / 2f + panOffset.x,
                                (height - effectiveHeight) / 2f + panOffset.y
                            )

                            PUNE_MAP_JUNCTIONS.forEach { junction ->
                                val pinPos = Offset(
                                    baseOrigin.x + junction.normalizedPos.x * effectiveWidth,
                                    baseOrigin.y + junction.normalizedPos.y * effectiveHeight
                                )
                                val distance = (tapOffset - pinPos).getDistance()
                                if (distance < 45.dp.toPx()) {
                                    onLocationSelected(junction.name)
                                }
                            }
                        },
                        onDoubleTap = {
                            // Zoom toggle on double tap
                            zoomLevel = if (zoomLevel < 1.6f) 1.8f else 1.0f
                            if (zoomLevel == 1.0f) panOffset = Offset.Zero
                        }
                    )
                }
        ) {
            val effectiveWidth = size.width * zoomLevel
            val effectiveHeight = size.height * zoomLevel
            val mapOrigin = Offset(
                (size.width - effectiveWidth) / 2f + panOffset.x,
                (size.height - effectiveHeight) / 2f + panOffset.y
            )

            // 1. Draw Google Maps Cartography Base Layers
            drawGoogleCartographyBase(
                origin = mapOrigin,
                effectiveWidth = effectiveWidth,
                effectiveHeight = effectiveHeight,
                mapStyle = mapStyle,
                textMeasurer = textMeasurer
            )

            // 2. Draw Pune River (Mula-Mutha river system)
            drawPuneMulaMuthaRiver(
                origin = mapOrigin,
                effectiveWidth = effectiveWidth,
                effectiveHeight = effectiveHeight,
                mapStyle = mapStyle
            )

            // 3. Draw Railway Tracks & Station Yards
            drawPuneRailwayCorridor(
                origin = mapOrigin,
                effectiveWidth = effectiveWidth,
                effectiveHeight = effectiveHeight
            )

            // 4. Draw Road Network & Dynamic Google Traffic Flow Polylines
            drawTrafficCorridors(
                corridors = PUNE_MAP_CORRIDORS,
                origin = mapOrigin,
                effectiveWidth = effectiveWidth,
                effectiveHeight = effectiveHeight,
                selectedLocation = selectedLocation,
                currentVolume = currentVolume,
                predictedVolume = predictedVolume,
                globalCongestion = congestionLevel,
                isTrafficLayerVisible = isTrafficLayerVisible,
                mapStyle = mapStyle,
                textMeasurer = textMeasurer
            )

            // 5. Draw Animated Vehicle Particles moving along roads
            if (isTrafficLayerVisible) {
                drawLiveVehicleParticles(
                    corridors = PUNE_MAP_CORRIDORS,
                    origin = mapOrigin,
                    effectiveWidth = effectiveWidth,
                    effectiveHeight = effectiveHeight,
                    progress = vehicleProgress,
                    congestionLevel = congestionLevel
                )
            }

            // 6. Draw BiLSTM Predictive Isochrone & Radar Rings around active junctions
            drawPredictiveRadarRings(
                junctions = PUNE_MAP_JUNCTIONS,
                origin = mapOrigin,
                effectiveWidth = effectiveWidth,
                effectiveHeight = effectiveHeight,
                selectedLocation = selectedLocation,
                predictedVolume = predictedVolume,
                congestionLevel = congestionLevel,
                pulse = radarPulse
            )

            // 7. Draw CCTV Camera FOV (Field Of View) Cones
            if (isFovVisible) {
                drawCameraFovCones(
                    fovs = PUNE_CAMERA_FOVS,
                    junctions = PUNE_MAP_JUNCTIONS,
                    origin = mapOrigin,
                    effectiveWidth = effectiveWidth,
                    effectiveHeight = effectiveHeight,
                    selectedCamera = selectedCamera,
                    selectedLocation = selectedLocation,
                    sweepOffset = fovSweep
                )
            }

            // 8. Draw Junction Node Markers (Google Maps style pins & badges)
            drawJunctionPins(
                junctions = PUNE_MAP_JUNCTIONS,
                origin = mapOrigin,
                effectiveWidth = effectiveWidth,
                effectiveHeight = effectiveHeight,
                selectedLocation = selectedLocation,
                congestionLevel = congestionLevel,
                currentVolume = currentVolume,
                textMeasurer = textMeasurer
            )
        }

        // --- GOOGLE MAPS FLOATING OVERLAYS ---

        // Top Google Maps Search Bar & Status Header
        GoogleMapsSearchBar(
            selectedLocation = selectedLocation,
            selectedCamera = selectedCamera,
            selectedDirection = selectedDirection,
            isCloudSynced = isCloudSynced,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        )

        // Top-Left Floating Map Style Selector
        GoogleMapStyleChips(
            currentStyle = mapStyle,
            onStyleSelected = { mapStyle = it },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 64.dp)
        )

        // Google Maps Official Traffic Legend (Bottom Left)
        if (isTrafficLayerVisible) {
            GoogleTrafficLegendCard(
                congestionLevel = congestionLevel,
                predictedVolume = predictedVolume,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 12.dp)
            )
        }

        // Google Maps Floating Control Column (Right Side)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Compass Rose
            Surface(
                shape = CircleShape,
                color = TrafficNavyCard.copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder),
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        mapRotation = 0f
                        panOffset = Offset.Zero
                    }
                    .testTag("btn_compass_north")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Reset North",
                        tint = TrafficRedCritical,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(mapRotation)
                    )
                }
            }

            // 3D Perspective Tilt Button
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (is3dPerspective) TrafficCyanAccent else TrafficNavyCard.copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder),
                modifier = Modifier
                    .size(40.dp)
                    .clickable { is3dPerspective = !is3dPerspective }
                    .testTag("btn_toggle_3d")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Perspective Mode",
                        tint = if (is3dPerspective) TrafficNavyDark else TrafficTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Traffic Layer Toggle
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isTrafficLayerVisible) TrafficAmberPrimary else TrafficNavyCard.copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder),
                modifier = Modifier
                    .size(40.dp)
                    .clickable { isTrafficLayerVisible = !isTrafficLayerVisible }
                    .testTag("btn_toggle_traffic_layer")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Traffic,
                        contentDescription = "Traffic Layer",
                        tint = if (isTrafficLayerVisible) TrafficNavyDark else TrafficTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Camera FOV Radar Cones Toggle
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isFovVisible) TrafficCyanAccent.copy(alpha = 0.25f) else TrafficNavyCard.copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isFovVisible) TrafficCyanAccent else TrafficNavyCardBorder
                ),
                modifier = Modifier
                    .size(40.dp)
                    .clickable { isFovVisible = !isFovVisible }
                    .testTag("btn_toggle_fov")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "CCTV FOV",
                        tint = if (isFovVisible) TrafficCyanAccent else TrafficTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Zoom In (+)
            Surface(
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                color = TrafficNavyCard.copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder),
                modifier = Modifier
                    .size(width = 36.dp, height = 34.dp)
                    .clickable { if (zoomLevel < 2.5f) zoomLevel += 0.25f }
                    .testTag("btn_map_zoom_in")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = TrafficTextPrimary, modifier = Modifier.size(18.dp))
                }
            }

            // Zoom Out (-)
            Surface(
                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                color = TrafficNavyCard.copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder),
                modifier = Modifier
                    .size(width = 36.dp, height = 34.dp)
                    .clickable { if (zoomLevel > 0.75f) zoomLevel -= 0.25f }
                    .testTag("btn_map_zoom_out")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = TrafficTextPrimary, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Recenter on Selected Junction (Google Maps "My Location" FAB)
            SmallFloatingActionButton(
                onClick = {
                    panOffset = Offset.Zero
                    zoomLevel = 1.25f
                },
                containerColor = TrafficAmberPrimary,
                contentColor = TrafficNavyDark,
                modifier = Modifier.testTag("btn_recenter_junction")
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Recenter", modifier = Modifier.size(18.dp))
            }
        }
    }
}

// -------------------------------------------------------------
// Canvas Drawing Routines
// -------------------------------------------------------------

private fun DrawScope.drawGoogleCartographyBase(
    origin: Offset,
    effectiveWidth: Float,
    effectiveHeight: Float,
    mapStyle: MapStyle,
    textMeasurer: TextMeasurer
) {
    val baseBgColor = when (mapStyle) {
        MapStyle.GOOGLE_DARK -> Color(0xFF1B2129)
        MapStyle.SATELLITE_HYBRID -> Color(0xFF14191F)
        MapStyle.HEATMAP_ANALYTICS -> Color(0xFF0F141C)
    }
    drawRect(color = baseBgColor)

    // City blocks / Urban parcel polygons
    val parcelColor = when (mapStyle) {
        MapStyle.GOOGLE_DARK -> Color(0xFF222B36)
        MapStyle.SATELLITE_HYBRID -> Color(0xFF1F2833)
        MapStyle.HEATMAP_ANALYTICS -> Color(0xFF161E29)
    }

    val parcels = listOf(
        // Block 1: Near Alankar / Station
        Offset(origin.x + effectiveWidth * 0.45f, origin.y + effectiveHeight * 0.65f) to Size(effectiveWidth * 0.22f, effectiveHeight * 0.18f),
        // Block 2: Sassoon / Medical zone
        Offset(origin.x + effectiveWidth * 0.60f, origin.y + effectiveHeight * 0.25f) to Size(effectiveWidth * 0.25f, effectiveHeight * 0.16f),
        // Block 3: RTO / Transit depot zone
        Offset(origin.x + effectiveWidth * 0.15f, origin.y + effectiveHeight * 0.50f) to Size(effectiveWidth * 0.22f, effectiveHeight * 0.22f),
        // Block 4: CoEP area
        Offset(origin.x + effectiveWidth * 0.12f, origin.y + effectiveHeight * 0.20f) to Size(effectiveWidth * 0.20f, effectiveHeight * 0.18f),
        // Block 5: Bund Garden park reserve (Greenish tint)
        Offset(origin.x + effectiveWidth * 0.78f, origin.y + effectiveHeight * 0.08f) to Size(effectiveWidth * 0.18f, effectiveHeight * 0.16f)
    )

    parcels.forEachIndexed { idx, (pos, size) ->
        val color = if (idx == 4) Color(0xFF162E24) else parcelColor
        drawRoundRect(
            color = color,
            topLeft = pos,
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
        )
    }

    // Secondary Street Grid (Muted urban veins)
    val gridColor = Color(0xFF2C3746).copy(alpha = 0.6f)
    for (i in 1..8) {
        val y = origin.y + effectiveHeight * (i / 9f)
        drawLine(
            color = gridColor,
            start = Offset(origin.x, y),
            end = Offset(origin.x + effectiveWidth, y),
            strokeWidth = 1.5f
        )
    }
}

private fun DrawScope.drawPuneMulaMuthaRiver(
    origin: Offset,
    effectiveWidth: Float,
    effectiveHeight: Float,
    mapStyle: MapStyle
) {
    val riverColor = when (mapStyle) {
        MapStyle.GOOGLE_DARK -> Color(0xFF18324A)
        MapStyle.SATELLITE_HYBRID -> Color(0xFF10283D)
        MapStyle.HEATMAP_ANALYTICS -> Color(0xFF112233)
    }

    val riverPath = Path().apply {
        moveTo(origin.x, origin.y + effectiveHeight * 0.18f)
        cubicTo(
            origin.x + effectiveWidth * 0.25f, origin.y + effectiveHeight * 0.14f,
            origin.x + effectiveWidth * 0.45f, origin.y + effectiveHeight * 0.22f,
            origin.x + effectiveWidth * 0.65f, origin.y + effectiveHeight * 0.12f
        )
        cubicTo(
            origin.x + effectiveWidth * 0.78f, origin.y + effectiveHeight * 0.08f,
            origin.x + effectiveWidth * 0.90f, origin.y + effectiveHeight * 0.05f,
            origin.x + effectiveWidth, origin.y + effectiveHeight * 0.02f
        )
    }

    // Outer river bank glow
    drawPath(
        path = riverPath,
        color = Color(0xFF00E5FF).copy(alpha = 0.08f),
        style = Stroke(width = 38f, cap = StrokeCap.Round)
    )
    // River body
    drawPath(
        path = riverPath,
        color = riverColor,
        style = Stroke(width = 28f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawPuneRailwayCorridor(
    origin: Offset,
    effectiveWidth: Float,
    effectiveHeight: Float
) {
    val trackPath = Path().apply {
        moveTo(origin.x + effectiveWidth * 0.42f, origin.y + effectiveHeight)
        cubicTo(
            origin.x + effectiveWidth * 0.48f, origin.y + effectiveHeight * 0.78f,
            origin.x + effectiveWidth * 0.52f, origin.y + effectiveHeight * 0.65f,
            origin.x + effectiveWidth * 0.58f, origin.y + effectiveHeight * 0.55f
        )
        lineTo(origin.x + effectiveWidth * 0.95f, origin.y + effectiveHeight * 0.32f)
    }

    // Base railway track
    drawPath(
        path = trackPath,
        color = Color(0xFF4A453A),
        style = Stroke(width = 5f, cap = StrokeCap.Square)
    )
    // Railway cross ties (dashed)
    drawPath(
        path = trackPath,
        color = Color(0xFF8C7F6A),
        style = Stroke(
            width = 3f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )
    )
}

private fun DrawScope.drawTrafficCorridors(
    corridors: List<MapCorridor>,
    origin: Offset,
    effectiveWidth: Float,
    effectiveHeight: Float,
    selectedLocation: String,
    currentVolume: Float,
    predictedVolume: Float,
    globalCongestion: CongestionLevel,
    isTrafficLayerVisible: Boolean,
    mapStyle: MapStyle,
    textMeasurer: TextMeasurer
) {
    corridors.forEach { corridor ->
        val points = corridor.waypoints.map { wp ->
            Offset(origin.x + wp.x * effectiveWidth, origin.y + wp.y * effectiveHeight)
        }
        if (points.size < 2) return@forEach

        val roadPath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        // Base road pavement
        val baseRoadWidth = if (corridor.roadType == "HIGHWAY") 18f else 14f
        drawPath(
            path = roadPath,
            color = Color(0xFF323D4D),
            style = Stroke(width = baseRoadWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Road centerline
        drawPath(
            path = roadPath,
            color = Color(0xFF49586D),
            style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f))
        )

        // Traffic Congestion Layer
        if (isTrafficLayerVisible) {
            // Determine corridor congestion status
            val isConnectedToSelected = corridor.fromJunctionId == selectedLocation || corridor.toJunctionId == selectedLocation
            val corridorLevel = if (isConnectedToSelected) globalCongestion else when (corridor.id) {
                "c_rto_jehangir" -> CongestionLevel.MODERATE
                "c_jehangir_bundgarden" -> CongestionLevel.LOW
                "c_alankar_south" -> CongestionLevel.HIGH
                else -> CongestionLevel.LOW
            }

            val flowColor = when (corridorLevel) {
                CongestionLevel.LOW -> TrafficGreenSafe
                CongestionLevel.MODERATE -> TrafficYellowMod
                CongestionLevel.HIGH -> TrafficOrangeHigh
                CongestionLevel.SEVERE -> TrafficRedCritical
            }

            // Outer Neon Glow
            drawPath(
                path = roadPath,
                color = flowColor.copy(alpha = if (isConnectedToSelected) 0.45f else 0.25f),
                style = Stroke(width = baseRoadWidth + 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Inner Traffic Flow Vector
            drawPath(
                path = roadPath,
                color = flowColor,
                style = Stroke(width = baseRoadWidth - 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

private fun DrawScope.drawLiveVehicleParticles(
    corridors: List<MapCorridor>,
    origin: Offset,
    effectiveWidth: Float,
    effectiveHeight: Float,
    progress: Float,
    congestionLevel: CongestionLevel
) {
    corridors.forEachIndexed { cIdx, corridor ->
        val points = corridor.waypoints.map { wp ->
            Offset(origin.x + wp.x * effectiveWidth, origin.y + wp.y * effectiveHeight)
        }
        if (points.size < 2) return@forEachIndexed

        // Interpolate position along segment based on progress
        for (v in 0..2) {
            val adjustedProgress = (progress + (v * 0.33f) + (cIdx * 0.15f)) % 1f
            val interpolated = interpolateAlongPath(points, adjustedProgress)

            // Vehicle particle dot
            val vehicleColor = when (v) {
                0 -> TrafficCyanAccent // Fast car
                1 -> TrafficAmberPrimary // Two-wheeler
                else -> Color(0xFFFFFFFF) // Transit bus
            }

            drawCircle(
                color = vehicleColor,
                radius = 3.5f,
                center = interpolated
            )
        }
    }
}

private fun interpolateAlongPath(points: List<Offset>, progress: Float): Offset {
    if (points.size == 2) {
        val x = points[0].x + (points[1].x - points[0].x) * progress
        val y = points[0].y + (points[1].y - points[0].y) * progress
        return Offset(x, y)
    }
    val segFraction = 1f / (points.size - 1)
    val segIndex = (progress / segFraction).toInt().coerceIn(0, points.size - 2)
    val subProgress = (progress - segIndex * segFraction) / segFraction
    val p1 = points[segIndex]
    val p2 = points[segIndex + 1]
    return Offset(
        p1.x + (p2.x - p1.x) * subProgress,
        p1.y + (p2.y - p1.y) * subProgress
    )
}

private fun DrawScope.drawPredictiveRadarRings(
    junctions: List<MapJunction>,
    origin: Offset,
    effectiveWidth: Float,
    effectiveHeight: Float,
    selectedLocation: String,
    predictedVolume: Float,
    congestionLevel: CongestionLevel,
    pulse: Float
) {
    val selectedJunction = junctions.firstOrNull { it.name == selectedLocation } ?: return
    val center = Offset(
        origin.x + selectedJunction.normalizedPos.x * effectiveWidth,
        origin.y + selectedJunction.normalizedPos.y * effectiveHeight
    )

    val maxRadius = 55f
    val currentRadius = maxRadius * pulse
    val alpha = (1f - pulse).coerceIn(0f, 1f)

    // Dynamic warning ring color based on BiLSTM predicted volume
    val ringColor = when (congestionLevel) {
        CongestionLevel.LOW -> TrafficGreenSafe
        CongestionLevel.MODERATE -> TrafficYellowMod
        CongestionLevel.HIGH -> TrafficOrangeHigh
        CongestionLevel.SEVERE -> TrafficRedCritical
    }

    // Pulsing radar ring
    drawCircle(
        color = ringColor.copy(alpha = alpha * 0.7f),
        radius = currentRadius,
        center = center,
        style = Stroke(width = 2.5f)
    )

    // Inner core zone
    drawCircle(
        color = ringColor.copy(alpha = 0.15f),
        radius = maxRadius * 0.45f,
        center = center
    )
}

private fun DrawScope.drawCameraFovCones(
    fovs: List<MapCameraFov>,
    junctions: List<MapJunction>,
    origin: Offset,
    effectiveWidth: Float,
    effectiveHeight: Float,
    selectedCamera: String,
    selectedLocation: String,
    sweepOffset: Float
) {
    fovs.forEach { fov ->
        val junction = junctions.firstOrNull { it.name == fov.junctionId } ?: return@forEach
        val isSelectedCam = fov.cameraId == selectedCamera && fov.junctionId == selectedLocation
        val center = Offset(
            origin.x + junction.normalizedPos.x * effectiveWidth,
            origin.y + junction.normalizedPos.y * effectiveHeight
        )

        val coneLength = if (isSelectedCam) 65f else 45f
        val effectiveAngle = fov.baseAngleDegrees + (if (isSelectedCam) sweepOffset else 0f)
        val halfSweep = fov.sweepAngleDegrees / 2f

        val startRad = (effectiveAngle - halfSweep) * (PI / 180f)
        val endRad = (effectiveAngle + halfSweep) * (PI / 180f)

        val pStart = Offset(
            center.x + coneLength * cos(startRad).toFloat(),
            center.y + coneLength * sin(startRad).toFloat()
        )
        val pEnd = Offset(
            center.x + coneLength * cos(endRad).toFloat(),
            center.y + coneLength * sin(endRad).toFloat()
        )

        val fovPath = Path().apply {
            moveTo(center.x, center.y)
            lineTo(pStart.x, pStart.y)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    center.x - coneLength, center.y - coneLength,
                    center.x + coneLength, center.y + coneLength
                ),
                startAngleDegrees = effectiveAngle - halfSweep,
                sweepAngleDegrees = fov.sweepAngleDegrees,
                forceMoveTo = false
            )
            close()
        }

        val coneColor = if (isSelectedCam) TrafficCyanAccent else Color(0xFF64748B)
        val fillAlpha = if (isSelectedCam) 0.28f else 0.12f

        drawPath(
            path = fovPath,
            color = coneColor.copy(alpha = fillAlpha),
            style = Fill
        )
        drawPath(
            path = fovPath,
            color = coneColor.copy(alpha = if (isSelectedCam) 0.8f else 0.35f),
            style = Stroke(width = if (isSelectedCam) 1.5f else 1f)
        )
    }
}

private fun DrawScope.drawJunctionPins(
    junctions: List<MapJunction>,
    origin: Offset,
    effectiveWidth: Float,
    effectiveHeight: Float,
    selectedLocation: String,
    congestionLevel: CongestionLevel,
    currentVolume: Float,
    textMeasurer: TextMeasurer
) {
    junctions.forEach { junction ->
        val isSelected = junction.name == selectedLocation
        val center = Offset(
            origin.x + junction.normalizedPos.x * effectiveWidth,
            origin.y + junction.normalizedPos.y * effectiveHeight
        )

        val pinColor = if (isSelected) {
            when (congestionLevel) {
                CongestionLevel.LOW -> TrafficGreenSafe
                CongestionLevel.MODERATE -> TrafficYellowMod
                CongestionLevel.HIGH -> TrafficOrangeHigh
                CongestionLevel.SEVERE -> TrafficRedCritical
            }
        } else {
            TrafficAmberPrimary
        }

        // Drop shadow under pin
        drawCircle(
            color = Color.Black.copy(alpha = 0.5f),
            radius = if (isSelected) 14f else 9f,
            center = center.copy(y = center.y + 3f)
        )

        // Outer halo ring
        drawCircle(
            color = pinColor.copy(alpha = if (isSelected) 0.35f else 0.2f),
            radius = if (isSelected) 18f else 11f,
            center = center
        )

        // Outer white border
        drawCircle(
            color = Color.White,
            radius = if (isSelected) 10f else 6.5f,
            center = center
        )

        // Solid pin center
        drawCircle(
            color = pinColor,
            radius = if (isSelected) 8f else 5f,
            center = center
        )

        // Junction Label Banner
        val labelStyle = TextStyle(
            color = if (isSelected) Color.White else TrafficTextSecondary,
            fontSize = if (isSelected) 11.sp else 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
        val textLayout = textMeasurer.measure(junction.displayName, labelStyle)
        val labelTopLeft = Offset(
            center.x - textLayout.size.width / 2f,
            center.y + (if (isSelected) 16f else 10f)
        )

        // Background pill behind text for readability
        drawRoundRect(
            color = TrafficNavyDark.copy(alpha = 0.85f),
            topLeft = Offset(labelTopLeft.x - 6f, labelTopLeft.y - 2f),
            size = Size(textLayout.size.width + 12f, textLayout.size.height + 4f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )

        drawText(
            textMeasurer = textMeasurer,
            text = junction.displayName,
            topLeft = labelTopLeft,
            style = labelStyle
        )
    }
}

// -------------------------------------------------------------
// Floating UI Components
// -------------------------------------------------------------

@Composable
private fun GoogleMapsSearchBar(
    selectedLocation: String,
    selectedCamera: String,
    selectedDirection: String,
    isCloudSynced: Boolean,
    modifier: Modifier = Modifier
) {
    val junction = PUNE_MAP_JUNCTIONS.firstOrNull { it.name == selectedLocation }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(24.dp))
            .testTag("google_maps_search_bar"),
        shape = RoundedCornerShape(24.dp),
        color = TrafficNavyCard.copy(alpha = 0.94f),
        border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TrafficAmberPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${junction?.displayName ?: selectedLocation} • Cam ${selectedCamera.uppercase()}",
                    color = TrafficTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Pune Corridor • Heading $selectedDirection",
                    color = TrafficTextSecondary,
                    fontSize = 10.sp
                )
            }

            // Cloud sync live badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isCloudSynced) TrafficGreenSafe.copy(alpha = 0.18f) else TrafficCyanAccent.copy(alpha = 0.18f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isCloudSynced) TrafficGreenSafe else TrafficCyanAccent)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCloudSynced) "CLOUD API" else "BiLSTM V2",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCloudSynced) TrafficGreenSafe else TrafficCyanAccent
                    )
                }
            }
        }
    }
}

@Composable
private fun GoogleMapStyleChips(
    currentStyle: MapStyle,
    onStyleSelected: (MapStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        MapStyleChipItem(
            title = "Map",
            isSelected = currentStyle == MapStyle.GOOGLE_DARK,
            onClick = { onStyleSelected(MapStyle.GOOGLE_DARK) }
        )
        MapStyleChipItem(
            title = "Satellite",
            isSelected = currentStyle == MapStyle.SATELLITE_HYBRID,
            onClick = { onStyleSelected(MapStyle.SATELLITE_HYBRID) }
        )
        MapStyleChipItem(
            title = "Heatmap",
            isSelected = currentStyle == MapStyle.HEATMAP_ANALYTICS,
            onClick = { onStyleSelected(MapStyle.HEATMAP_ANALYTICS) }
        )
    }
}

@Composable
private fun MapStyleChipItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) TrafficAmberPrimary else TrafficNavyCard.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) TrafficAmberPrimary else TrafficNavyCardBorder
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) TrafficNavyDark else TrafficTextSecondary
        )
    }
}

@Composable
private fun GoogleTrafficLegendCard(
    congestionLevel: CongestionLevel,
    predictedVolume: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.shadow(8.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = TrafficNavyCard.copy(alpha = 0.92f),
        border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(
                text = "TRAFFIC DENSITY (GOOGLE MAPS)",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = TrafficCyanAccent,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "Fast", fontSize = 9.sp, color = TrafficGreenSafe, fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.size(width = 16.dp, height = 4.dp).background(TrafficGreenSafe, RoundedCornerShape(2.dp)))
                Box(modifier = Modifier.size(width = 16.dp, height = 4.dp).background(TrafficYellowMod, RoundedCornerShape(2.dp)))
                Box(modifier = Modifier.size(width = 16.dp, height = 4.dp).background(TrafficOrangeHigh, RoundedCornerShape(2.dp)))
                Box(modifier = Modifier.size(width = 16.dp, height = 4.dp).background(TrafficRedCritical, RoundedCornerShape(2.dp)))
                Text(text = "Slow", fontSize = 9.sp, color = TrafficRedCritical, fontWeight = FontWeight.Bold)
            }
        }
    }
}
