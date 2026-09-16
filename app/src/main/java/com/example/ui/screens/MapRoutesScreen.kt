package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BENGALURU_HOTSPOTS
import com.example.model.BengaluruLocation
import com.example.model.ServiceType
import com.example.model.UiRouteOption
import com.example.network.TravelTimeEstimate
import com.example.location.UserLiveLocation
import com.example.ui.AppTab
import com.example.ui.TrafficUiState
import com.example.ui.components.BengaluruRouteMap
import com.example.ui.components.LocationPermissionFlow
import com.example.ui.components.MapComposable
import com.example.ui.components.TravelTimeCatchmentCard
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
import com.example.ui.theme.TrafficPurpleSecondary
import com.example.ui.theme.TrafficTextMuted
import com.example.ui.theme.TrafficTextPrimary
import com.example.ui.theme.TrafficTextSecondary
import com.example.ui.theme.TrafficWarningAmber

@Composable
fun MapRoutesScreen(
    uiState: TrafficUiState,
    onSelectOrigin: (BengaluruLocation) -> Unit,
    onSelectDestination: (BengaluruLocation) -> Unit,
    onSwapOriginDestination: () -> Unit,
    onSelectRoute: (Int) -> Unit,
    onSyncCloudApi: () -> Unit,
    onNavigateToServices: () -> Unit,
    onPermissionGranted: () -> Unit,
    onSetLocationAsOrigin: (UserLiveLocation) -> Unit,
    onSelectIsochroneMinutes: (Int) -> Unit = {},
    onSelectTransportMode: (String) -> Unit = {},
    onCalculateIsochrone: () -> Unit = {},
    onClearIsochrone: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isOriginDropdownOpen by remember { mutableStateOf(false) }
    var isDestDropdownOpen by remember { mutableStateOf(false) }
    var isTurnByTurnExpanded by remember { mutableStateOf(true) }

    val currentRoute = uiState.routeOptions.getOrNull(uiState.selectedRouteIndex)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TrafficNavyDark)
            .testTag("map_routes_screen"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Hero Header: Origin & Destination Selector
        item {
            RouteSelectorHeader(
                origin = uiState.bengaluruOrigin,
                destination = uiState.bengaluruDestination,
                isSyncing = uiState.isSyncing,
                isCloudConnected = uiState.isLiveCloudConnected,
                backendStatus = uiState.backendHealthStatus,
                onSwap = onSwapOriginDestination,
                onOpenOriginSelect = { isOriginDropdownOpen = true },
                onOpenDestSelect = { isDestDropdownOpen = true },
                onRefreshRoutes = onSyncCloudApi
            )

            // Dropdown menus
            DropdownMenu(
                expanded = isOriginDropdownOpen,
                onDismissRequest = { isOriginDropdownOpen = false },
                modifier = Modifier.background(TrafficNavyCard)
            ) {
                if (uiState.userLiveLocation != null) {
                    val userLoc = uiState.userLiveLocation
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text("📍 My Current Location", color = TrafficCyanAccent, fontWeight = FontWeight.Bold)
                                Text(userLoc.areaName, color = TrafficTextMuted, fontSize = 11.sp)
                            }
                        },
                        onClick = {
                            onSetLocationAsOrigin(userLoc)
                            isOriginDropdownOpen = false
                        }
                    )
                }

                BENGALURU_HOTSPOTS.forEach { loc ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(loc.name, color = TrafficTextPrimary, fontWeight = FontWeight.SemiBold)
                                Text(loc.area, color = TrafficTextMuted, fontSize = 11.sp)
                            }
                        },
                        onClick = {
                            onSelectOrigin(loc)
                            isOriginDropdownOpen = false
                        }
                    )
                }
            }

            DropdownMenu(
                expanded = isDestDropdownOpen,
                onDismissRequest = { isDestDropdownOpen = false },
                modifier = Modifier.background(TrafficNavyCard)
            ) {
                BENGALURU_HOTSPOTS.forEach { loc ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(loc.name, color = TrafficTextPrimary, fontWeight = FontWeight.SemiBold)
                                Text(loc.area, color = TrafficTextMuted, fontSize = 11.sp)
                            }
                        },
                        onClick = {
                            onSelectDestination(loc)
                            isDestDropdownOpen = false
                        }
                    )
                }
            }
        }

        // Location Permission Flow & Live GPS Area Detection Banner
        item {
            LocationPermissionFlow(
                userLiveLocation = uiState.userLiveLocation,
                onPermissionGranted = onPermissionGranted,
                onSetLocationAsOrigin = onSetLocationAsOrigin,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Quick Preset Chips for Origin
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text(
                    text = "QUICK ORIGIN PRESETS",
                    color = TrafficTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.userLiveLocation != null) {
                        item {
                            val isSelected = uiState.bengaluruOrigin.id == "my_live_location"
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSetLocationAsOrigin(uiState.userLiveLocation) },
                                label = { Text("📍 My Area") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TrafficCyanAccent.copy(alpha = 0.2f),
                                    selectedLabelColor = TrafficCyanAccent,
                                    containerColor = TrafficNavyCard,
                                    labelColor = TrafficTextSecondary
                                )
                            )
                        }
                    }

                    items(BENGALURU_HOTSPOTS.take(6)) { hotspot ->
                        val isSelected = hotspot.id == uiState.bengaluruOrigin.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectOrigin(hotspot) },
                            label = { Text(hotspot.name.substringBefore(" ")) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TrafficEmeraldSuccess.copy(alpha = 0.2f),
                                selectedLabelColor = TrafficEmeraldSuccess,
                                containerColor = TrafficNavyCard,
                                labelColor = TrafficTextSecondary
                            )
                        )
                    }
                }
            }
        }

        // Interactive Real-Time Map (Google Maps SDK & Traffic Junctions)
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE BENGALURU TRAFFIC JUNCTIONS",
                        color = TrafficTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.routeOptions.size} Routes Generated",
                        color = TrafficCyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                MapComposable(
                    userLiveLocation = uiState.userLiveLocation,
                    selectedRoute = currentRoute,
                    allRoutes = uiState.routeOptions,
                    incidents = uiState.trafficIncidents,
                    catchment = uiState.travelTimeCatchment,
                    travelTimeEstimates = uiState.travelTimeEstimates,
                    origin = uiState.bengaluruOrigin,
                    destination = uiState.bengaluruDestination,
                    onSelectOrigin = onSelectOrigin,
                    onSelectDestination = onSelectDestination,
                    onSelectRoute = onSelectRoute,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                )
            }
        }

        // TravelTime Isochrone Reachability Catchment Tool
        item {
            TravelTimeCatchmentCard(
                originName = uiState.bengaluruOrigin.name,
                catchment = uiState.travelTimeCatchment,
                isLoading = uiState.isCatchmentLoading,
                selectedMinutes = uiState.selectedIsochroneMinutes,
                selectedMode = uiState.selectedTransportMode,
                statusMessage = uiState.isochroneStatusMessage,
                onSelectMinutes = onSelectIsochroneMinutes,
                onSelectMode = onSelectTransportMode,
                onCalculate = onCalculateIsochrone,
                onClear = onClearIsochrone,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Route Comparison Section
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ROUTE ALTERNATIVES & ETAS",
                        color = TrafficTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "BiLSTM Speed Forecast",
                        color = TrafficAmberPrimary,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Route Cards List
        itemsIndexed(uiState.routeOptions) { idx, route ->
            val isSelected = idx == uiState.selectedRouteIndex
            RouteOptionCard(
                route = route,
                isSelected = isSelected,
                travelTimeEstimate = uiState.travelTimeEstimates[uiState.bengaluruDestination.id],
                onClick = { onSelectRoute(idx) },
                onExploreServices = onNavigateToServices,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Turn-by-turn Navigation for the active route
        if (currentRoute != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("turn_by_turn_card"),
                    colors = CardDefaults.cardColors(containerColor = TrafficNavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isTurnByTurnExpanded = !isTurnByTurnExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(TrafficIndigoPrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        tint = TrafficIndigoPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "TURN-BY-TURN GUIDANCE",
                                        color = TrafficTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${currentRoute.steps.size} navigation steps along ${currentRoute.name}",
                                        color = TrafficTextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = TrafficTextSecondary
                            )
                        }

                        AnimatedVisibility(visible = isTurnByTurnExpanded) {
                            Column(
                                modifier = Modifier.padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                currentRoute.steps.forEachIndexed { stepIdx, step ->
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            color = if (stepIdx == 0) TrafficEmeraldSuccess.copy(alpha = 0.2f)
                                            else if (stepIdx == currentRoute.steps.size - 1) TrafficDangerRed.copy(alpha = 0.2f)
                                            else TrafficNavyCardElevated,
                                            shape = CircleShape,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${stepIdx + 1}",
                                                    color = if (stepIdx == 0) TrafficEmeraldSuccess
                                                    else if (stepIdx == currentRoute.steps.size - 1) TrafficDangerRed
                                                    else TrafficTextSecondary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Text(
                                            text = step,
                                            color = TrafficTextPrimary,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onNavigateToServices,
                                    colors = ButtonDefaults.buttonColors(containerColor = TrafficIndigoPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_services_from_route")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalHospital,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("View Services Near This Route (Hospitals, Fuel, Food)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteSelectorHeader(
    origin: BengaluruLocation,
    destination: BengaluruLocation,
    isSyncing: Boolean,
    isCloudConnected: Boolean,
    backendStatus: String,
    onSwap: () -> Unit,
    onOpenOriginSelect: () -> Unit,
    onOpenDestSelect: () -> Unit,
    onRefreshRoutes: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("route_selector_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = TrafficNavyCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title and Live Cloud Status Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isCloudConnected) TrafficEmeraldSuccess else TrafficWarningAmber)
                    )
                    Text(
                        text = if (isCloudConnected) "LIVE CLOUD API CONNECTED" else "OFFLINE LOCAL AI",
                        color = if (isCloudConnected) TrafficEmeraldSuccess else TrafficWarningAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onRefreshRoutes,
                    modifier = Modifier.size(32.dp).testTag("btn_refresh_routes")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Routes",
                        tint = TrafficCyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (isSyncing) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = TrafficCyanAccent,
                    trackColor = TrafficNavyDark
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Origin Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TrafficNavyDark)
                    .border(1.dp, TrafficNavyCardBorder, RoundedCornerShape(12.dp))
                    .clickable { onOpenOriginSelect() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(TrafficEmeraldSuccess.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(TrafficEmeraldSuccess)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("START / ORIGIN", color = TrafficTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(origin.name, color = TrafficTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(origin.area, color = TrafficTextSecondary, fontSize = 11.sp)
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = TrafficTextMuted
                )
            }

            // Swap Button Center Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = TrafficNavyCardElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder),
                    modifier = Modifier
                        .size(34.dp)
                        .clickable { onSwap() }
                        .testTag("btn_swap_locations")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Swap Origin & Destination",
                            tint = TrafficCyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Destination Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TrafficNavyDark)
                    .border(1.dp, TrafficNavyCardBorder, RoundedCornerShape(12.dp))
                    .clickable { onOpenDestSelect() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(TrafficDangerRed.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(TrafficDangerRed)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("FINISH / DESTINATION", color = TrafficTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(destination.name, color = TrafficTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(destination.area, color = TrafficTextSecondary, fontSize = 11.sp)
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = TrafficTextMuted
                )
            }
        }
    }
}

@Composable
private fun RouteOptionCard(
    route: UiRouteOption,
    isSelected: Boolean,
    travelTimeEstimate: TravelTimeEstimate? = null,
    onClick: () -> Unit,
    onExploreServices: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trafficColor = when (route.trafficLevel.lowercase()) {
        "low" -> TrafficEmeraldSuccess
        "moderate" -> TrafficWarningAmber
        "medium" -> TrafficAmberPrimary
        "high" -> Color(0xFFF97316)
        else -> TrafficDangerRed
    }

    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("route_card_${route.routeId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TrafficNavyCardElevated else TrafficNavyCard
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) TrafficCyanAccent else TrafficNavyCardBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Name and Fastest Route Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(trafficColor)
                    )
                    Text(
                        text = route.name,
                        color = TrafficTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                if (route.isFastest) {
                    Surface(
                        color = TrafficEmeraldSuccess.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TrafficEmeraldSuccess.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "⭐ FASTEST ROUTE",
                            color = TrafficEmeraldSuccess,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics Grid: ETA, Distance, Predicted Speed, Traffic Condition
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Duration
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = TrafficAmberPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${route.durationMin} min",
                            color = TrafficAmberPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }
                    Text("Duration", color = TrafficTextMuted, fontSize = 10.sp)
                }

                // Distance
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Route,
                            contentDescription = null,
                            tint = TrafficCyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${route.distanceKm} km",
                            color = TrafficCyanAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Text("Distance", color = TrafficTextMuted, fontSize = 10.sp)
                }

                // Speed
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = TrafficTextPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${route.predictedSpeedKmph.toInt()} km/h",
                            color = TrafficTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Text("Predicted Speed", color = TrafficTextMuted, fontSize = 10.sp)
                }

                // Traffic Level
                Surface(
                    color = trafficColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, trafficColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = route.trafficLevel.uppercase(),
                        color = trafficColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (travelTimeEstimate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TrafficIndigoPrimary.copy(alpha = 0.15f))
                        .border(1.dp, TrafficCyanAccent.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ TravelTime Matrix Model",
                        color = TrafficCyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${travelTimeEstimate.travelTimeMinutes} min (${String.format("%.1f", travelTimeEstimate.distanceKm)} km)",
                        color = TrafficAmberPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Services along this route summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TrafficNavyDark)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🏥 ${route.hospitalsCount} Hosp", color = TrafficTextSecondary, fontSize = 11.sp)
                    Text("⛽ ${route.fuelCount} Fuel", color = TrafficTextSecondary, fontSize = 11.sp)
                    Text("🍽️ ${route.restaurantsCount} Food", color = TrafficTextSecondary, fontSize = 11.sp)
                    Text("💊 ${route.pharmaciesCount} Rx", color = TrafficTextSecondary, fontSize = 11.sp)
                }

                if (isSelected) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = TrafficCyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text("ACTIVE", color = TrafficCyanAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
