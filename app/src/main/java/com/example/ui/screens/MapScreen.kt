package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.example.model.BengaluruLocation
import com.example.ui.TrafficUiState
import com.example.ui.components.REAL_TIME_BENGALURU_JUNCTIONS
import com.example.ui.components.RealTimeJunctionData
import com.example.ui.theme.TrafficAmberPrimary
import com.example.ui.theme.TrafficCyanAccent
import com.example.ui.theme.TrafficDangerRed
import com.example.ui.theme.TrafficNavyDark
import com.example.ui.theme.TrafficNavyMedium
import com.example.ui.theme.TrafficSuccessGreen
import com.example.ui.theme.TrafficTextPrimary
import com.example.ui.theme.TrafficTextSecondary
import com.example.ui.theme.TrafficWarningAmber
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

/**
 * MapScreen utilizing GoogleMap from maps-compose.
 * - Requests fine and coarse location permissions via Accompanist Permissions.
 * - Centers the user's current GPS location on the map.
 * - Renders interactive markers for Bengaluru traffic junctions with congestion telemetry.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    uiState: TrafficUiState,
    onSelectOrigin: (BengaluruLocation) -> Unit = {},
    onSelectDestination: (BengaluruLocation) -> Unit = {},
    onPermissionGranted: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 1. Location Permissions via accompanist-permissions
    val locationPermissionsState: MultiplePermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    var hasRequestedPermissionOnStart by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!hasRequestedPermissionOnStart && !locationPermissionsState.allPermissionsGranted) {
            hasRequestedPermissionOnStart = true
            locationPermissionsState.launchMultiplePermissionRequest()
        }
        // Initialize Google Maps native components safely
        runCatching {
            MapsInitializer.initialize(context)
        }
    }

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            onPermissionGranted()
        }
    }

    // User's coordinates (either from state or freshly resolved from FusedLocation)
    var currentUserLocation by remember {
        mutableStateOf<LatLng?>(
            uiState.userLiveLocation?.let { LatLng(it.latitude, it.longitude) }
        )
    }

    // Initialize FusedLocationProviderClient
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Safe helper to obtain current device coordinates
    @SuppressLint("MissingPermission")
    fun fetchCurrentDeviceLocation(onLocationReceived: (LatLng) -> Unit) {
        if (locationPermissionsState.allPermissionsGranted) {
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { loc ->
                if (loc != null) {
                    val latLng = LatLng(loc.latitude, loc.longitude)
                    currentUserLocation = latLng
                    onLocationReceived(latLng)
                }
            }
        }
    }

    // 2. Camera position setup (centered on user location or Bengaluru Central fallback)
    val defaultCenter = LatLng(12.9716, 77.5946) // Bengaluru MG Road / Central
    val initialTarget = currentUserLocation ?: defaultCenter

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialTarget, 12.5f)
    }

    // Whenever user live location is updated in uiState, smoothly center
    LaunchedEffect(uiState.userLiveLocation?.latitude, uiState.userLiveLocation?.longitude) {
        uiState.userLiveLocation?.let { loc ->
            val target = LatLng(loc.latitude, loc.longitude)
            currentUserLocation = target
            try {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(target, 13.5f)
                )
            } catch (_: Throwable) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(target, 13.5f)
            }
        }
    }

    // Fetch initial device GPS coordinates on permission grant
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            fetchCurrentDeviceLocation { latLng ->
                coroutineScope.launch {
                    try {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(latLng, 14f)
                        )
                    } catch (_: Throwable) {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 14f)
                    }
                }
            }
        }
    }

    // UI Map Configuration
    var isTrafficEnabled by remember { mutableStateOf(true) }
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var isMapTypeMenuOpen by remember { mutableStateOf(false) }
    var selectedJunction by remember { mutableStateOf<RealTimeJunctionData?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("map_screen_container")
    ) {
        // 3. GoogleMap Composable
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isTrafficEnabled = isTrafficEnabled,
                mapType = mapType,
                isMyLocationEnabled = locationPermissionsState.allPermissionsGranted
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = true,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = true
            )
        ) {
            // User current location marker (if GPS permission granted and position known)
            currentUserLocation?.let { userPos ->
                Marker(
                    state = MarkerState(position = userPos),
                    title = "Your Current Location",
                    snippet = uiState.userLiveLocation?.areaName ?: "Live GPS Fix",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }

            // 4. Render Traffic Junction Markers
            REAL_TIME_BENGALURU_JUNCTIONS.forEach { junction ->
                val markerHue = when (junction.congestionLevel.uppercase()) {
                    "SEVERE" -> BitmapDescriptorFactory.HUE_ROSE
                    "HEAVY" -> BitmapDescriptorFactory.HUE_RED
                    "MODERATE" -> BitmapDescriptorFactory.HUE_ORANGE
                    else -> BitmapDescriptorFactory.HUE_GREEN
                }

                Marker(
                    state = MarkerState(position = LatLng(junction.lat, junction.lng)),
                    title = junction.name,
                    snippet = "${junction.congestionLevel} • ${junction.vehicleCount} veh/5m • ${junction.avgSpeedKmph} km/h",
                    icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                    onClick = {
                        selectedJunction = junction
                        false // Allow default camera behavior
                    }
                )
            }
        }

        // Top Header Bar: Telemetry & Controls
        Surface(
            color = TrafficNavyDark.copy(alpha = 0.92f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, TrafficCyanAccent.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(TrafficSuccessGreen)
                    )
                    Column {
                        Text(
                            text = "Bengaluru Live Traffic Grid",
                            color = TrafficTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${REAL_TIME_BENGALURU_JUNCTIONS.size} Junctions Monitored • Live GoogleMap",
                            color = TrafficTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Traffic Layer Toggle
                    IconButton(
                        onClick = { isTrafficEnabled = !isTrafficEnabled },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isTrafficEnabled) TrafficCyanAccent.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
                                CircleShape
                            )
                            .testTag("toggle_traffic_layer_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Traffic,
                            contentDescription = "Toggle Traffic Layer",
                            tint = if (isTrafficEnabled) TrafficCyanAccent else TrafficTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Map Type Selector Dropdown
                    Box {
                        IconButton(
                            onClick = { isMapTypeMenuOpen = true },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                .testTag("btn_map_layers")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Map Style",
                                tint = TrafficCyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isMapTypeMenuOpen,
                            onDismissRequest = { isMapTypeMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Standard Normal") },
                                onClick = {
                                    mapType = MapType.NORMAL
                                    isMapTypeMenuOpen = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Satellite View") },
                                onClick = {
                                    mapType = MapType.SATELLITE
                                    isMapTypeMenuOpen = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Hybrid Satellite") },
                                onClick = {
                                    mapType = MapType.HYBRID
                                    isMapTypeMenuOpen = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Terrain Contours") },
                                onClick = {
                                    mapType = MapType.TERRAIN
                                    isMapTypeMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Floating GPS Recenter Button
        Surface(
            color = TrafficNavyDark.copy(alpha = 0.95f),
            shape = CircleShape,
            border = BorderStroke(1.dp, TrafficCyanAccent.copy(alpha = 0.5f)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = if (selectedJunction != null) 240.dp else 24.dp)
                .size(46.dp)
                .clickable {
                    if (locationPermissionsState.allPermissionsGranted) {
                        fetchCurrentDeviceLocation { latLng ->
                            coroutineScope.launch {
                                try {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(latLng, 14.5f)
                                    )
                                } catch (_: Throwable) {
                                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 14.5f)
                                }
                            }
                        }
                    } else {
                        locationPermissionsState.launchMultiplePermissionRequest()
                    }
                }
                .testTag("btn_recenter_user_gps")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Center on My Location",
                    tint = if (locationPermissionsState.allPermissionsGranted) TrafficCyanAccent else TrafficWarningAmber,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Location Permission Banner (Prompt when permission not granted)
        if (!locationPermissionsState.allPermissionsGranted) {
            Surface(
                color = TrafficNavyDark.copy(alpha = 0.95f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, TrafficAmberPrimary.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 68.dp)
                    .align(Alignment.TopCenter)
                    .testTag("card_location_permission_prompt")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TrafficAmberPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Location Access Needed",
                            color = TrafficTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Enable GPS to center your live position and get accurate route travel times.",
                            color = TrafficTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                    Button(
                        onClick = { locationPermissionsState.launchMultiplePermissionRequest() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TrafficAmberPrimary,
                            contentColor = TrafficNavyDark
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp).testTag("btn_grant_location_permission")
                    ) {
                        Text("Grant", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Selected Junction Bottom Telemetry Card
        AnimatedVisibility(
            visible = selectedJunction != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            selectedJunction?.let { junction ->
                Surface(
                    color = TrafficNavyDark.copy(alpha = 0.96f),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    border = BorderStroke(1.dp, TrafficCyanAccent.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .testTag("card_selected_junction_details")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = junction.name,
                                    color = TrafficTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = junction.corridor,
                                    color = TrafficTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            IconButton(
                                onClick = { selectedJunction = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close junction details",
                                    tint = TrafficTextSecondary
                                )
                            }
                        }

                        // Metrics Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Congestion Pill
                            val (badgeBg, badgeText) = when (junction.congestionLevel.uppercase()) {
                                "SEVERE" -> TrafficDangerRed.copy(alpha = 0.25f) to TrafficDangerRed
                                "HEAVY" -> TrafficDangerRed.copy(alpha = 0.25f) to TrafficDangerRed
                                "MODERATE" -> TrafficAmberPrimary.copy(alpha = 0.25f) to TrafficAmberPrimary
                                else -> TrafficSuccessGreen.copy(alpha = 0.25f) to TrafficSuccessGreen
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = TrafficNavyMedium),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Status", color = TrafficTextSecondary, fontSize = 10.sp)
                                    Surface(
                                        color = badgeBg,
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Text(
                                            text = junction.congestionLevel,
                                            color = badgeText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Volume
                            Card(
                                colors = CardDefaults.cardColors(containerColor = TrafficNavyMedium),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("5-Min Volume", color = TrafficTextSecondary, fontSize = 10.sp)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.DirectionsCar, null, tint = TrafficCyanAccent, modifier = Modifier.size(14.dp))
                                        Text("${junction.vehicleCount} veh", color = TrafficTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Speed
                            Card(
                                colors = CardDefaults.cardColors(containerColor = TrafficNavyMedium),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Corridor Speed", color = TrafficTextSecondary, fontSize = 10.sp)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Speed, null, tint = TrafficAmberPrimary, modifier = Modifier.size(14.dp))
                                        Text("${junction.avgSpeedKmph} km/h", color = TrafficTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Signal Timers & Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🟢 Green: ${junction.signalGreenSec}s • 🔴 Red: ${junction.signalRedSec}s • ${junction.activeCamerasCount} CCTV",
                                color = TrafficTextSecondary,
                                fontSize = 11.sp
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        val loc = BengaluruLocation(
                                            id = junction.id,
                                            name = junction.name,
                                            lat = junction.lat,
                                            lng = junction.lng,
                                            area = junction.corridor,
                                            landmark = junction.name
                                        )
                                        onSelectOrigin(loc)
                                        selectedJunction = null
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TrafficCyanAccent.copy(alpha = 0.2f),
                                        contentColor = TrafficCyanAccent
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Set Origin", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        val loc = BengaluruLocation(
                                            id = junction.id,
                                            name = junction.name,
                                            lat = junction.lat,
                                            lng = junction.lng,
                                            area = junction.corridor,
                                            landmark = junction.name
                                        )
                                        onSelectDestination(loc)
                                        selectedJunction = null
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TrafficCyanAccent,
                                        contentColor = TrafficNavyDark
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Set Destination", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
