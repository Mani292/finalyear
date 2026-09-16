package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.location.UserLiveLocation
import com.example.model.BENGALURU_HOTSPOTS
import com.example.model.BengaluruLocation
import com.example.model.UiRouteOption
import com.example.model.UiTrafficIncident
import com.example.network.IsochroneCatchment
import com.example.network.TravelTimeEstimate
import com.example.ui.theme.TrafficAmberPrimary
import com.example.ui.theme.TrafficCyanAccent
import com.example.ui.theme.TrafficDangerRed
import com.example.ui.theme.TrafficDarkCard
import com.example.ui.theme.TrafficDarkSurface
import com.example.ui.theme.TrafficEmeraldSuccess
import com.example.ui.theme.TrafficIndigoPrimary
import com.example.ui.theme.TrafficNavyCard
import com.example.ui.theme.TrafficNavyCardBorder
import com.example.ui.theme.TrafficNavyCardElevated
import com.example.ui.theme.TrafficNavyDark
import com.example.ui.theme.TrafficTextMuted
import com.example.ui.theme.TrafficTextPrimary
import com.example.ui.theme.TrafficTextSecondary
import com.example.ui.theme.TrafficWarningAmber
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

data class RealTimeJunctionData(
    val id: String,
    val name: String,
    val area: String,
    val lat: Double,
    val lng: Double,
    val liveVolumeVehicles: Int,
    val avgSpeedKmph: Int,
    val congestionLevel: String,
    val activeCamerasCount: Int,
    val signalGreenSec: Int,
    val signalRedSec: Int
)

val REAL_TIME_BENGALURU_JUNCTIONS = listOf(
    RealTimeJunctionData("silk_board", "Silk Board Junction", "Hosur Road - Outer Ring Road", 12.9165, 77.6230, 186, 18, "HEAVY", 4, 40, 75),
    RealTimeJunctionData("electronic_city", "Electronic City Phase 1", "Elevated Expressway", 12.8399, 77.6770, 48, 62, "LOW", 3, 70, 25),
    RealTimeJunctionData("koramangala", "Sony World Signal", "100ft Road Koramangala", 12.9352, 77.6245, 128, 32, "MODERATE", 3, 50, 50),
    RealTimeJunctionData("indiranagar", "100ft Road - CMH Junction", "Indiranagar Metro", 12.9784, 77.6408, 112, 36, "MODERATE", 2, 55, 45),
    RealTimeJunctionData("mg_road", "MG Road - Anil Kumble Circle", "CBD Metro Station", 12.9756, 77.6066, 74, 46, "LOW", 4, 60, 35),
    RealTimeJunctionData("whitefield", "ITPL Main Gate Junction", "Whitefield Tech Corridor", 12.9864, 77.7380, 198, 15, "SEVERE", 4, 35, 80),
    RealTimeJunctionData("hebbal", "Hebbal Flyover Junction", "Airport Road - Outer Ring Road", 13.0358, 77.5970, 142, 38, "MODERATE", 5, 55, 50),
    RealTimeJunctionData("marathahalli", "Marathahalli Bridge", "Old Airport Rd - ORR", 12.9591, 77.6974, 175, 21, "HEAVY", 3, 40, 65)
)

enum class MapRendererMode {
    GOOGLE_MAPS,
    TACTICAL_VECTOR
}

@Composable
fun MapComposable(
    userLiveLocation: UserLiveLocation?,
    selectedRoute: UiRouteOption?,
    allRoutes: List<UiRouteOption>,
    incidents: List<UiTrafficIncident>,
    catchment: IsochroneCatchment? = null,
    travelTimeEstimates: Map<String, TravelTimeEstimate> = emptyMap(),
    origin: BengaluruLocation? = null,
    destination: BengaluruLocation? = null,
    onSelectOrigin: (BengaluruLocation) -> Unit,
    onSelectDestination: (BengaluruLocation) -> Unit,
    onSelectRoute: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedJunction by remember { mutableStateOf<RealTimeJunctionData?>(null) }
    var isTrafficEnabled by remember { mutableStateOf(true) }
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var isMapTypeDropdownOpen by remember { mutableStateOf(false) }

    val hasValidGoogleMapsKey = remember {
        try {
            com.example.BuildConfig.MAPS_API_KEY.isNotBlank() &&
            com.example.BuildConfig.MAPS_API_KEY != "DEFAULT_MAPS_KEY" &&
            !com.example.BuildConfig.MAPS_API_KEY.contains("YOUR_")
        } catch (_: Throwable) {
            false
        }
    }

    var rendererMode by remember {
        mutableStateOf(if (hasValidGoogleMapsKey) MapRendererMode.GOOGLE_MAPS else MapRendererMode.TACTICAL_VECTOR)
    }

    // Initial camera position centered on Bengaluru Central / User Location
    val initialTarget = if (userLiveLocation != null) {
        LatLng(userLiveLocation.latitude, userLiveLocation.longitude)
    } else {
        LatLng(12.9716, 77.5946) // Bengaluru center
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialTarget, 12f)
    }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        runCatching {
            MapsInitializer.initialize(context)
        }
    }

    // Auto center when user live location changes
    LaunchedEffect(userLiveLocation?.latitude, userLiveLocation?.longitude) {
        if (userLiveLocation != null) {
            val target = LatLng(userLiveLocation.latitude, userLiveLocation.longitude)
            try {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(target, 13f)
                )
            } catch (_: Throwable) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(target, 13f)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, TrafficNavyCardBorder, RoundedCornerShape(16.dp))
            .background(TrafficNavyDark)
            .testTag("map_composable_root")
    ) {
        if (rendererMode == MapRendererMode.GOOGLE_MAPS) {
            // Google Maps SDK Compose View
            GoogleMap(
                modifier = Modifier.fillMaxSize().testTag("google_map_view"),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isTrafficEnabled = isTrafficEnabled,
                    mapType = mapType
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    compassEnabled = true,
                    myLocationButtonEnabled = false
                ),
                onMapClick = {
                    selectedJunction = null
                }
            ) {
                // User Live Location Marker & Accuracy Ring
                if (userLiveLocation != null) {
                    val userLatLng = LatLng(userLiveLocation.latitude, userLiveLocation.longitude)

                    // Accuracy Circle
                    Circle(
                        center = userLatLng,
                        radius = userLiveLocation.accuracyMeters.toDouble().coerceAtLeast(30.0),
                        fillColor = Color(0x3306B6D4),
                        strokeColor = Color(0xFF06B6D4),
                        strokeWidth = 2f
                    )

                    // User Location Marker
                    Marker(
                        state = MarkerState(position = userLatLng),
                        title = "My Live Location",
                        snippet = userLiveLocation.areaName,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }

                // TravelTime Isochrone Catchment Polygon (GeoJSON / TravelTime API)
                catchment?.polygons?.forEach { ring ->
                    if (ring.size >= 3) {
                        Polygon(
                            points = ring,
                            fillColor = Color(0x3506B6D4),
                            strokeColor = Color(0xFF06B6D4),
                            strokeWidth = 3f
                        )
                    }
                }

                // Real-Time Traffic Junctions Markers
                REAL_TIME_BENGALURU_JUNCTIONS.forEach { junction ->
                    val markerHue = when (junction.congestionLevel) {
                        "LOW" -> BitmapDescriptorFactory.HUE_GREEN
                        "MODERATE" -> BitmapDescriptorFactory.HUE_ORANGE
                        else -> BitmapDescriptorFactory.HUE_RED
                    }

                    val est = travelTimeEstimates[junction.id]
                    val isReachableInCatchment = catchment?.reachableJunctionIds?.contains(junction.id) == true
                    val snippetText = when {
                        est != null -> "⏱ TravelTime: ${est.travelTimeMinutes} min (${String.format("%.1f", est.distanceKm)} km) • ${junction.congestionLevel}"
                        isReachableInCatchment -> "⏱ Reachable in ${catchment?.minutes ?: 30}m (${catchment?.displayMode ?: "Transit"}) • ${junction.congestionLevel}"
                        catchment != null -> "Out of ${catchment.minutes}m range • ${junction.congestionLevel}"
                        else -> "${junction.congestionLevel} • ${junction.liveVolumeVehicles} veh/min • ${junction.avgSpeedKmph} km/h"
                    }

                    Marker(
                        state = MarkerState(position = LatLng(junction.lat, junction.lng)),
                        title = junction.name,
                        snippet = snippetText,
                        icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                        onClick = {
                            selectedJunction = junction
                            true
                        }
                    )
                }

                // Active Route Polyline
                if (selectedRoute != null && selectedRoute.waypoints.size >= 2) {
                    val routePoints = selectedRoute.waypoints.map { LatLng(it.lat, it.lng) }
                    val polylineColor = when (selectedRoute.trafficLevel.lowercase()) {
                        "low" -> TrafficEmeraldSuccess
                        "moderate" -> TrafficWarningAmber
                        else -> TrafficDangerRed
                    }

                    Polyline(
                        points = routePoints,
                        color = polylineColor,
                        width = 12f
                    )
                }

                // Traffic Incidents
                incidents.forEach { inc ->
                    Marker(
                        state = MarkerState(position = LatLng(inc.lat, inc.lng)),
                        title = "⚠️ ${inc.type}",
                        snippet = "${inc.location} (+${inc.delayMinutes}m)",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                    )
                }
            }
        } else {
            // Tactical Vector Map View (Offline & High-Contrast Fallback)
            val originLoc = origin ?: BENGALURU_HOTSPOTS.firstOrNull() ?: BengaluruLocation("silk_board", "Silk Board", 12.9165, 77.6230, "BTM", "Silk Board Flyover")
            val destLoc = destination ?: BENGALURU_HOTSPOTS.getOrNull(4) ?: BengaluruLocation("mg_road", "MG Road", 12.9756, 77.6066, "CBD", "Trinity Circle")

            BengaluruRouteMap(
                origin = originLoc,
                destination = destLoc,
                selectedRoute = selectedRoute,
                allRoutes = allRoutes,
                services = emptyList(),
                incidents = incidents,
                catchment = catchment,
                onSelectRoute = onSelectRoute,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Google Maps Key Advisory Banner (when user views Google Maps mode without key)
        if (rendererMode == MapRendererMode.GOOGLE_MAPS && !hasValidGoogleMapsKey) {
            Surface(
                color = TrafficNavyDark.copy(alpha = 0.92f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TrafficWarningAmber.copy(alpha = 0.7f)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clickable { rendererMode = MapRendererMode.TACTICAL_VECTOR }
                    .testTag("banner_maps_key_notice")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = TrafficWarningAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Google Maps key not set • Tap for Tactical Radar View",
                        color = TrafficCyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Live Real-Time Telemetry Bar at Top
        Surface(
            color = TrafficNavyDark.copy(alpha = 0.90f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .testTag("map_live_telemetry_badge")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (catchment != null) TrafficCyanAccent else TrafficEmeraldSuccess)
                )
                Text(
                    text = if (catchment != null) "TRAVELTIME ${catchment.minutes}M CATCHMENT" else "REAL-TIME JUNCTIONS",
                    color = if (catchment != null) TrafficCyanAccent else TrafficEmeraldSuccess,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "•",
                    color = TrafficTextMuted,
                    fontSize = 11.sp
                )
                Text(
                    text = if (catchment != null) catchment.displayMode else (userLiveLocation?.areaName ?: "Bengaluru Corridor"),
                    color = if (catchment != null) TrafficAmberPrimary else TrafficCyanAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Floating Map Controls (Right Side)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mode Switcher (Google Maps SDK vs Tactical Canvas)
            Surface(
                shape = CircleShape,
                color = TrafficNavyCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder),
                modifier = Modifier
                    .size(38.dp)
                    .clickable {
                        rendererMode = if (rendererMode == MapRendererMode.GOOGLE_MAPS) {
                            MapRendererMode.TACTICAL_VECTOR
                        } else {
                            MapRendererMode.GOOGLE_MAPS
                        }
                    }
                    .testTag("btn_switch_renderer_mode")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Switch Map Engine",
                        tint = TrafficCyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Google Maps Layer Type Switcher (Normal, Hybrid, Satellite, Terrain)
            if (rendererMode == MapRendererMode.GOOGLE_MAPS) {
                Box {
                    Surface(
                        shape = CircleShape,
                        color = TrafficNavyCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder),
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { isMapTypeDropdownOpen = true }
                            .testTag("btn_map_type_selector")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Traffic,
                                contentDescription = "Map Style",
                                tint = if (isTrafficEnabled) TrafficEmeraldSuccess else TrafficTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isMapTypeDropdownOpen,
                        onDismissRequest = { isMapTypeDropdownOpen = false },
                        modifier = Modifier.background(TrafficNavyCard)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Toggle Google Live Traffic", color = TrafficTextPrimary) },
                            onClick = {
                                isTrafficEnabled = !isTrafficEnabled
                                isMapTypeDropdownOpen = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Standard Map View", color = TrafficTextPrimary) },
                            onClick = {
                                mapType = MapType.NORMAL
                                isMapTypeDropdownOpen = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Satellite View", color = TrafficTextPrimary) },
                            onClick = {
                                mapType = MapType.SATELLITE
                                isMapTypeDropdownOpen = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Terrain View", color = TrafficTextPrimary) },
                            onClick = {
                                mapType = MapType.TERRAIN
                                isMapTypeDropdownOpen = false
                            }
                        )
                    }
                }
            }

            // Recenter / Focus My Live GPS Location
            Surface(
                shape = CircleShape,
                color = if (userLiveLocation != null) TrafficCyanAccent else TrafficNavyCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder),
                modifier = Modifier
                    .size(38.dp)
                    .clickable {
                        if (userLiveLocation != null) {
                            val target = LatLng(userLiveLocation.latitude, userLiveLocation.longitude)
                            coroutineScope.launch {
                                try {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(target, 14.5f)
                                    )
                                } catch (_: Throwable) {
                                    cameraPositionState.position = CameraPosition.fromLatLngZoom(target, 14.5f)
                                }
                            }
                        }
                    }
                    .testTag("btn_focus_user_gps")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "Center on My Location",
                        tint = if (userLiveLocation != null) TrafficNavyDark else TrafficTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Selected Junction Detail Bottom Card
        AnimatedVisibility(
            visible = selectedJunction != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
        ) {
            selectedJunction?.let { junc ->
                val color = when (junc.congestionLevel) {
                    "LOW" -> TrafficEmeraldSuccess
                    "MODERATE" -> TrafficWarningAmber
                    else -> TrafficDangerRed
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("junction_detail_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TrafficNavyDark.copy(alpha = 0.95f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = color.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = junc.congestionLevel,
                                        color = color,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = junc.name,
                                    color = TrafficTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = { selectedJunction = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TrafficTextMuted, modifier = Modifier.size(18.dp))
                            }
                        }

                        Text(junc.area, color = TrafficTextSecondary, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        // Metrics row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("${junc.liveVolumeVehicles} veh/m", color = TrafficTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Live Flow", color = TrafficTextMuted, fontSize = 10.sp)
                            }
                            Column {
                                Text("${junc.avgSpeedKmph} km/h", color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Avg Speed", color = TrafficTextMuted, fontSize = 10.sp)
                            }
                            Column {
                                Text("${junc.activeCamerasCount} Cams", color = TrafficCyanAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("CCTV Feeds", color = TrafficTextMuted, fontSize = 10.sp)
                            }
                            Column {
                                Text("G:${junc.signalGreenSec}s R:${junc.signalRedSec}s", color = TrafficAmberPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Signal Cycle", color = TrafficTextMuted, fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val est = travelTimeEstimates[junc.id]
                        if (est != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TrafficIndigoPrimary.copy(alpha = 0.2f))
                                    .border(1.dp, TrafficIndigoPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        tint = TrafficCyanAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "TravelTime Reachability:",
                                        color = TrafficCyanAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "~${est.travelTimeMinutes} mins (${String.format("%.1f", est.distanceKm)} km)",
                                    color = TrafficAmberPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Action Buttons: Set as Origin or Destination
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val loc = BengaluruLocation(junc.id, junc.name, junc.lat, junc.lng, junc.area, junc.area)

                            Button(
                                onClick = {
                                    onSelectOrigin(loc)
                                    selectedJunction = null
                                },
                                modifier = Modifier.weight(1f).testTag("btn_set_origin_${junc.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = TrafficEmeraldSuccess.copy(alpha = 0.25f)),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, TrafficEmeraldSuccess)
                            ) {
                                Text("Set as Start", color = TrafficEmeraldSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    onSelectDestination(loc)
                                    selectedJunction = null
                                },
                                modifier = Modifier.weight(1f).testTag("btn_set_dest_${junc.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = TrafficIndigoPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Set as Finish", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
