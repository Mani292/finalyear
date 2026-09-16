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
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BengaluruLocation
import com.example.model.RoutePoint
import com.example.model.ServiceType
import com.example.model.UiRouteOption
import com.example.model.UiServiceLocation
import com.example.model.UiTrafficIncident
import com.example.network.IsochroneCatchment
import com.example.ui.theme.TrafficAmberPrimary
import com.example.ui.theme.TrafficCyanAccent
import com.example.ui.theme.TrafficDangerRed
import com.example.ui.theme.TrafficDarkCard
import com.example.ui.theme.TrafficDarkSurface
import com.example.ui.theme.TrafficEmeraldSuccess
import com.example.ui.theme.TrafficGreenSafe
import com.example.ui.theme.TrafficNavyCard
import com.example.ui.theme.TrafficNavyCardBorder
import com.example.ui.theme.TrafficNavyDark
import com.example.ui.theme.TrafficRedCritical
import com.example.ui.theme.TrafficTextMuted
import com.example.ui.theme.TrafficTextPrimary
import com.example.ui.theme.TrafficWarningAmber

enum class RouteMapStyle {
    NIGHT_NAV,
    CONTRAST_SATELLITE,
    MINIMAL_GRID
}

@Composable
fun BengaluruRouteMap(
    origin: BengaluruLocation,
    destination: BengaluruLocation,
    selectedRoute: UiRouteOption?,
    allRoutes: List<UiRouteOption>,
    services: List<UiServiceLocation>,
    incidents: List<UiTrafficIncident>,
    catchment: IsochroneCatchment? = null,
    onSelectRoute: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }
    var showServices by remember { mutableStateOf(true) }
    var showIncidents by remember { mutableStateOf(true) }
    var mapStyle by remember { mutableStateOf(RouteMapStyle.NIGHT_NAV) }

    val infiniteTransition = rememberInfiniteTransition(label = "vehicleMovement")
    val vehicleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vehicleProgress"
    )

    val beaconPulse by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beaconPulse"
    )

    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, TrafficNavyCardBorder, RoundedCornerShape(16.dp))
            .background(TrafficNavyDark)
            .testTag("bengaluru_route_map_container")
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        // Calculate bounding box for origin & destination & all waypoints
        val allPoints = mutableListOf(RoutePoint(origin.lat, origin.lng), RoutePoint(destination.lat, destination.lng))
        selectedRoute?.waypoints?.let { allPoints.addAll(it) }

        var minLat = allPoints.minOf { it.lat }
        var maxLat = allPoints.maxOf { it.lat }
        var minLng = allPoints.minOf { it.lng }
        var maxLng = allPoints.maxOf { it.lng }

        // Add 20% margin to prevent edge clipping
        val latMargin = ((maxLat - minLat) * 0.25).coerceAtLeast(0.015)
        val lngMargin = ((maxLng - minLng) * 0.25).coerceAtLeast(0.015)
        minLat -= latMargin
        maxLat += latMargin
        minLng -= lngMargin
        maxLng += lngMargin

        fun projectPoint(lat: Double, lng: Double): Offset {
            val normX = ((lng - minLng) / (maxLng - minLng)).toFloat().coerceIn(0f, 1f)
            // Note: Lat increases upward, but screen Y increases downward
            val normY = (1f - ((lat - minLat) / (maxLat - minLat)).toFloat()).coerceIn(0f, 1f)

            val centerX = widthPx / 2f
            val centerY = heightPx / 2f

            val scaledX = centerX + (normX * widthPx - centerX) * zoomLevel + panOffsetX
            val scaledY = centerY + (normY * heightPx - centerY) * zoomLevel + panOffsetY
            return Offset(scaledX, scaledY)
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        panOffsetX += dragAmount.x
                        panOffsetY += dragAmount.y
                    }
                }
                .testTag("route_map_canvas")
        ) {
            // Draw background & grid
            drawMapBackground(widthPx, heightPx, mapStyle)

            // Draw arterial corridors & ring road backdrop
            drawBengaluruArterials(widthPx, heightPx, ::projectPoint, mapStyle)

            // Draw unselected alternative routes (dimmed)
            allRoutes.forEachIndexed { idx, route ->
                if (route != selectedRoute && route.waypoints.size >= 2) {
                    val path = Path()
                    val firstPt = projectPoint(route.waypoints[0].lat, route.waypoints[0].lng)
                    path.moveTo(firstPt.x, firstPt.y)
                    for (i in 1 until route.waypoints.size) {
                        val pt = projectPoint(route.waypoints[i].lat, route.waypoints[i].lng)
                        path.lineTo(pt.x, pt.y)
                    }
                    drawPath(
                        path = path,
                        color = Color(0x334B5563),
                        style = Stroke(width = 4f * zoomLevel.coerceIn(0.8f, 2f), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }

            // Draw TravelTime Isochrone Catchment Polygon
            catchment?.polygons?.forEach { ring ->
                if (ring.size >= 3) {
                    val polyPath = Path()
                    val firstPt = projectPoint(ring[0].latitude, ring[0].longitude)
                    polyPath.moveTo(firstPt.x, firstPt.y)
                    for (k in 1 until ring.size) {
                        val pt = projectPoint(ring[k].latitude, ring[k].longitude)
                        polyPath.lineTo(pt.x, pt.y)
                    }
                    polyPath.close()

                    drawPath(
                        path = polyPath,
                        color = TrafficCyanAccent.copy(alpha = 0.20f),
                        style = Fill
                    )
                    drawPath(
                        path = polyPath,
                        color = TrafficCyanAccent.copy(alpha = 0.80f),
                        style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }

            // Draw the ACTIVE selected route polyline
            if (selectedRoute != null && selectedRoute.waypoints.size >= 2) {
                val polylineColor = when (selectedRoute.trafficLevel.lowercase()) {
                    "low" -> TrafficEmeraldSuccess
                    "moderate" -> TrafficWarningAmber
                    "medium" -> TrafficAmberPrimary
                    "high" -> Color(0xFFF97316)
                    else -> TrafficDangerRed
                }

                val path = Path()
                val firstPt = projectPoint(selectedRoute.waypoints[0].lat, selectedRoute.waypoints[0].lng)
                path.moveTo(firstPt.x, firstPt.y)
                for (i in 1 until selectedRoute.waypoints.size) {
                    val pt = projectPoint(selectedRoute.waypoints[i].lat, selectedRoute.waypoints[i].lng)
                    path.lineTo(pt.x, pt.y)
                }

                // Polyline glow shadow
                drawPath(
                    path = path,
                    color = polylineColor.copy(alpha = 0.35f),
                    style = Stroke(width = 14f * zoomLevel.coerceIn(0.8f, 2f), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Main route polyline
                drawPath(
                    path = path,
                    color = polylineColor,
                    style = Stroke(width = 6f * zoomLevel.coerceIn(0.8f, 2f), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Route center dash effect
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.6f),
                    style = Stroke(
                        width = 2f,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), vehicleProgress * 30f)
                    )
                )

                // Intermediate waypoints
                for (i in 1 until selectedRoute.waypoints.size - 1) {
                    val pt = projectPoint(selectedRoute.waypoints[i].lat, selectedRoute.waypoints[i].lng)
                    drawCircle(color = TrafficNavyDark, radius = 5f, center = pt)
                    drawCircle(color = polylineColor, radius = 3.5f, center = pt)
                }

                // Draw animated traveling vehicle along the path
                drawAnimatedVehicle(selectedRoute.waypoints, vehicleProgress, beaconPulse, ::projectPoint)
            }

            // Draw Services along route (Hospitals, Fuel, Restaurants, Pharmacies)
            if (showServices) {
                services.take(10).forEach { s ->
                    val pos = projectPoint(s.lat, s.lng)
                    drawServiceMarker(s, pos, textMeasurer)
                }
            }

            // Draw Traffic Incidents (Accident, Roadblock)
            if (showIncidents) {
                incidents.forEach { incident ->
                    val pos = projectPoint(incident.lat, incident.lng)
                    drawIncidentMarker(incident, pos, textMeasurer)
                }
            }

            // Draw Origin Pin (Green)
            val originPos = projectPoint(origin.lat, origin.lng)
            drawLocationPin(
                pos = originPos,
                isOrigin = true,
                title = origin.name,
                subtitle = "ORIGIN",
                pinColor = TrafficEmeraldSuccess,
                textMeasurer = textMeasurer
            )

            // Draw Destination Pin (Red)
            val destPos = projectPoint(destination.lat, destination.lng)
            drawLocationPin(
                pos = destPos,
                isOrigin = false,
                title = destination.name,
                subtitle = "DESTINATION",
                pinColor = TrafficDangerRed,
                textMeasurer = textMeasurer
            )
        }

        // Overlay Map Header with Route Specs
        if (selectedRoute != null) {
            Surface(
                color = TrafficNavyCard.copy(alpha = 0.92f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .testTag("map_route_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (selectedRoute.trafficLevel.lowercase()) {
                                    "low" -> TrafficEmeraldSuccess
                                    "moderate" -> TrafficWarningAmber
                                    else -> TrafficDangerRed
                                }
                            )
                    )
                    Text(
                        text = selectedRoute.name,
                        color = TrafficTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "•",
                        color = TrafficTextMuted,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${selectedRoute.distanceKm} km",
                        color = TrafficCyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "•",
                        color = TrafficTextMuted,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${selectedRoute.durationMin} min",
                        color = TrafficAmberPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Map Control Floating Actions (Zoom in, Zoom out, Recenter, Style, Services toggle)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Toggle Services Pin
            Surface(
                shape = CircleShape,
                color = if (showServices) TrafficCyanAccent else TrafficNavyCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder),
                modifier = Modifier
                    .size(40.dp)
                    .clickable { showServices = !showServices }
                    .testTag("btn_toggle_services")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = "Toggle Services",
                        tint = if (showServices) TrafficNavyDark else TrafficTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Toggle Incidents Pin
            Surface(
                shape = CircleShape,
                color = if (showIncidents) TrafficAmberPrimary else TrafficNavyCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder),
                modifier = Modifier
                    .size(40.dp)
                    .clickable { showIncidents = !showIncidents }
                    .testTag("btn_toggle_incidents")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Toggle Incidents",
                        tint = if (showIncidents) TrafficNavyDark else TrafficTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Map Style Switcher
            Surface(
                shape = CircleShape,
                color = TrafficNavyCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder),
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        mapStyle = when (mapStyle) {
                            RouteMapStyle.NIGHT_NAV -> RouteMapStyle.CONTRAST_SATELLITE
                            RouteMapStyle.CONTRAST_SATELLITE -> RouteMapStyle.MINIMAL_GRID
                            RouteMapStyle.MINIMAL_GRID -> RouteMapStyle.NIGHT_NAV
                        }
                    }
                    .testTag("btn_map_style")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Map Style",
                        tint = TrafficTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Recenter
            Surface(
                shape = CircleShape,
                color = TrafficNavyCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder),
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        zoomLevel = 1.0f
                        panOffsetX = 0f
                        panOffsetY = 0f
                    }
                    .testTag("btn_recenter_map")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Recenter Map",
                        tint = TrafficCyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Zoom In (+)
            Surface(
                shape = CircleShape,
                color = TrafficNavyCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder),
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        if (zoomLevel < 3.0f) zoomLevel += 0.3f
                    }
                    .testTag("btn_zoom_in")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = TrafficTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Zoom Out (-)
            Surface(
                shape = CircleShape,
                color = TrafficNavyCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder),
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        if (zoomLevel > 0.7f) zoomLevel -= 0.3f
                    }
                    .testTag("btn_zoom_out")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = TrafficTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawMapBackground(width: Float, height: Float, style: RouteMapStyle) {
    val bgBrush = when (style) {
        RouteMapStyle.NIGHT_NAV -> Brush.verticalGradient(
            listOf(Color(0xFF0D1527), Color(0xFF090E1A))
        )
        RouteMapStyle.CONTRAST_SATELLITE -> Brush.verticalGradient(
            listOf(Color(0xFF111827), Color(0xFF030712))
        )
        RouteMapStyle.MINIMAL_GRID -> Brush.verticalGradient(
            listOf(Color(0xFF161F33), Color(0xFF0F172A))
        )
    }
    drawRect(brush = bgBrush, size = size)

    // Subtle coordinate grid
    val gridStep = 60f
    val gridColor = Color(0x12FFFFFF)
    var x = 0f
    while (x < width) {
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 1f
        )
        x += gridStep
    }
    var y = 0f
    while (y < height) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1f
        )
        y += gridStep
    }
}

private fun DrawScope.drawBengaluruArterials(
    width: Float,
    height: Float,
    project: (Double, Double) -> Offset,
    style: RouteMapStyle
) {
    val arterialColor = if (style == RouteMapStyle.CONTRAST_SATELLITE) Color(0x2238BDF8) else Color(0x1F60A5FA)

    // Outer Ring Road loop approximation
    val ringRoadLats = listOf(12.9165, 12.9352, 12.9591, 12.9784, 13.0358, 12.9716, 12.9165)
    val ringRoadLngs = listOf(77.6230, 77.6245, 77.6974, 77.6408, 77.5970, 77.5946, 77.6230)

    val path = Path()
    val start = project(ringRoadLats[0], ringRoadLngs[0])
    path.moveTo(start.x, start.y)
    for (i in 1 until ringRoadLats.size) {
        val pt = project(ringRoadLats[i], ringRoadLngs[i])
        path.lineTo(pt.x, pt.y)
    }
    drawPath(
        path = path,
        color = arterialColor,
        style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawAnimatedVehicle(
    waypoints: List<RoutePoint>,
    progress: Float,
    pulse: Float,
    project: (Double, Double) -> Offset
) {
    if (waypoints.size < 2) return
    val totalSegments = waypoints.size - 1
    val currentSegmentIdx = (progress * totalSegments).toInt().coerceIn(0, totalSegments - 1)
    val segmentProgress = (progress * totalSegments) - currentSegmentIdx

    val p1 = waypoints[currentSegmentIdx]
    val p2 = waypoints[currentSegmentIdx + 1]

    val lat = p1.lat + (p2.lat - p1.lat) * segmentProgress
    val lng = p1.lng + (p2.lng - p1.lng) * segmentProgress
    val vehiclePos = project(lat, lng)

    // Vehicle beacon ring
    drawCircle(
        color = TrafficCyanAccent.copy(alpha = 0.3f),
        radius = pulse * 1.5f,
        center = vehiclePos
    )

    // Vehicle glowing core
    drawCircle(
        color = TrafficNavyDark,
        radius = 8f,
        center = vehiclePos
    )
    drawCircle(
        color = TrafficCyanAccent,
        radius = 6f,
        center = vehiclePos
    )
    drawCircle(
        color = Color.White,
        radius = 3f,
        center = vehiclePos
    )
}

private fun DrawScope.drawLocationPin(
    pos: Offset,
    isOrigin: Boolean,
    title: String,
    subtitle: String,
    pinColor: Color,
    textMeasurer: TextMeasurer
) {
    // Pulse ring
    drawCircle(
        color = pinColor.copy(alpha = 0.25f),
        radius = 16f,
        center = pos
    )

    // Solid pin base
    drawCircle(
        color = TrafficNavyDark,
        radius = 11f,
        center = pos
    )
    drawCircle(
        color = pinColor,
        radius = 9f,
        center = pos
    )
    drawCircle(
        color = Color.White,
        radius = 4f,
        center = pos
    )

    // Text label badge
    val text = if (title.length > 18) title.take(16) + "…" else title
    val labelLayout = textMeasurer.measure(
        text = text,
        style = TextStyle(
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    )

    val badgeOffset = Offset(pos.x - (labelLayout.size.width / 2f), pos.y - 34f)
    val bgPad = 6f
    drawRoundRect(
        color = TrafficNavyDark.copy(alpha = 0.88f),
        topLeft = Offset(badgeOffset.x - bgPad, badgeOffset.y - bgPad / 2f),
        size = androidx.compose.ui.geometry.Size(labelLayout.size.width + bgPad * 2, labelLayout.size.height + bgPad),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = pinColor,
        topLeft = Offset(badgeOffset.x - bgPad, badgeOffset.y - bgPad / 2f),
        size = androidx.compose.ui.geometry.Size(labelLayout.size.width + bgPad * 2, labelLayout.size.height + bgPad),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
        style = Stroke(width = 1.5f)
    )

    drawText(
        textLayoutResult = labelLayout,
        topLeft = badgeOffset
    )
}

private fun DrawScope.drawServiceMarker(
    service: UiServiceLocation,
    pos: Offset,
    textMeasurer: TextMeasurer
) {
    val (color, symbol) = when (service.type) {
        ServiceType.HOSPITAL -> Pair(TrafficDangerRed, "+")
        ServiceType.FUEL -> Pair(TrafficAmberPrimary, "F")
        ServiceType.RESTAURANT -> Pair(TrafficEmeraldSuccess, "R")
        ServiceType.PHARMACY -> Pair(TrafficCyanAccent, "P")
        else -> Pair(TrafficCyanAccent, "S")
    }

    drawCircle(
        color = TrafficNavyDark,
        radius = 9f,
        center = pos
    )
    drawCircle(
        color = color,
        radius = 7.5f,
        center = pos
    )

    val symLayout = textMeasurer.measure(
        text = symbol,
        style = TextStyle(color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    )
    drawText(
        textLayoutResult = symLayout,
        topLeft = Offset(pos.x - symLayout.size.width / 2f, pos.y - symLayout.size.height / 2f)
    )
}

private fun DrawScope.drawIncidentMarker(
    incident: UiTrafficIncident,
    pos: Offset,
    textMeasurer: TextMeasurer
) {
    val color = if (incident.severity == "high") TrafficDangerRed else TrafficWarningAmber

    drawCircle(
        color = color.copy(alpha = 0.35f),
        radius = 12f,
        center = pos
    )
    drawCircle(
        color = TrafficNavyDark,
        radius = 9f,
        center = pos
    )
    drawCircle(
        color = color,
        radius = 7.5f,
        center = pos
    )

    val warnLayout = textMeasurer.measure(
        text = "!",
        style = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
    )
    drawText(
        textLayoutResult = warnLayout,
        topLeft = Offset(pos.x - warnLayout.size.width / 2f, pos.y - warnLayout.size.height / 2f)
    )
}
