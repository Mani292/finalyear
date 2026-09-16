package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FavoriteJunctionEntity
import com.example.model.CongestionLevel
import com.example.model.PUNE_JUNCTIONS
import com.example.model.TRAFFIC_DIRECTIONS
import com.example.ui.TrafficUiState
import com.example.ui.components.AdvancedGoogleTrafficMap
import com.example.ui.components.CongestionBadge
import com.example.ui.components.FavoriteJunctionsBar
import com.example.ui.components.SimulationControlBar
import com.example.ui.components.VehicleBreakdownCard
import com.example.ui.theme.TrafficAmberPrimary
import com.example.ui.theme.TrafficCyanAccent
import com.example.ui.theme.TrafficGreenSafe
import com.example.ui.theme.TrafficNavyCard
import com.example.ui.theme.TrafficNavyCardBorder
import com.example.ui.theme.TrafficNavyCardElevated
import com.example.ui.theme.TrafficNavyDark
import com.example.ui.theme.TrafficOrangeHigh
import com.example.ui.theme.TrafficRedCritical
import com.example.ui.theme.TrafficTextMuted
import com.example.ui.theme.TrafficTextPrimary
import com.example.ui.theme.TrafficTextSecondary

enum class DashboardViewMode {
    MAP_AND_TELEMETRY,
    MAP_EXPANDED,
    CAMERA_FEEDS
}

@Composable
fun MainDashboardScreen(
    state: TrafficUiState,
    onLocationSelected: (String) -> Unit,
    onCameraSelected: (String) -> Unit,
    onDirectionSelected: (String) -> Unit,
    onToggleStreaming: () -> Unit,
    onStepForward: () -> Unit,
    onSimulateSurge: () -> Unit,
    onSyncBackend: () -> Unit,
    onToggleFavorite: (String, String) -> Unit = { _, _ -> },
    onRemoveFavorite: (String) -> Unit = {},
    onSelectFavorite: (FavoriteJunctionEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(DashboardViewMode.MAP_AND_TELEMETRY) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(TrafficNavyDark)
            .testTag("main_dashboard_screen")
    ) {
        val isExpandedLayout = maxWidth >= 640.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // 1. TOP-LEVEL NAVIGATION COMPONENTS (Location, Camera, Direction)
            TopLevelNavigationHeader(
                selectedLocation = state.selectedLocation,
                selectedCamera = state.selectedCamera,
                selectedDirection = state.selectedDirection,
                isCloudSynced = state.isCloudSynced,
                isSyncing = state.isSyncing,
                favoriteJunctions = state.favoriteJunctions,
                onLocationSelected = onLocationSelected,
                onCameraSelected = onCameraSelected,
                onDirectionSelected = onDirectionSelected,
                onSyncBackend = onSyncBackend,
                onToggleFavorite = onToggleFavorite,
                currentViewMode = viewMode,
                onViewModeChanged = { viewMode = it }
            )

            // 2. FAVORITE JUNCTIONS QUICK ACCESS (ROOM DB)
            val currentJunctionDisplay = PUNE_JUNCTIONS.firstOrNull { it.name == state.selectedLocation }?.displayName ?: state.selectedLocation
            FavoriteJunctionsBar(
                favorites = state.favoriteJunctions,
                selectedLocation = state.selectedLocation,
                currentLocationName = currentJunctionDisplay,
                onSelectFavorite = onSelectFavorite,
                onRemoveFavorite = onRemoveFavorite,
                onToggleCurrentFavorite = {
                    onToggleFavorite(state.selectedLocation, currentJunctionDisplay)
                },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
            )

            // 3. RESPONSIVE DASHBOARD CONTENT
            if (isExpandedLayout) {
                // Expanded / Tablet Layout: 2-column side-by-side
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Left Column: Advanced Google Maps
                    Box(
                        modifier = Modifier
                            .weight(1.35f)
                            .fillMaxHeight()
                    ) {
                        AdvancedGoogleTrafficMap(
                            selectedLocation = state.selectedLocation,
                            selectedCamera = state.selectedCamera,
                            selectedDirection = state.selectedDirection,
                            currentVolume = state.currentVolume,
                            predictedVolume = state.predictedVolume,
                            congestionLevel = state.congestionLevel,
                            isCloudSynced = state.isCloudSynced,
                            onLocationSelected = onLocationSelected,
                            onCameraSelected = onCameraSelected,
                            onDirectionSelected = onDirectionSelected,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Right Column: Live Telemetry & Control Pane
                    Column(
                        modifier = Modifier
                            .weight(1.0f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LivePredictionHeroCard(state = state)
                        LiveCameraStreamFeedCard(
                            location = state.selectedLocation,
                            camera = state.selectedCamera,
                            direction = state.selectedDirection,
                            congestionLevel = state.congestionLevel,
                            currentVolume = state.currentVolume.toInt()
                        )
                        val lastObs = state.observations.lastOrNull()
                        VehicleBreakdownCard(
                            cars = lastObs?.carCount ?: 38,
                            motorbikes = lastObs?.motorbikeCount ?: 58,
                            buses = lastObs?.busCount ?: 14,
                            trucks = lastObs?.truckCount ?: 8,
                            total = (lastObs?.totalVehicles ?: state.currentVolume.toInt()).coerceAtLeast(1)
                        )
                        SimulationControlBar(
                            isStreaming = state.isAutoStreaming,
                            onToggleStreaming = onToggleStreaming,
                            onStepForward = onStepForward,
                            onSimulateSurge = onSimulateSurge
                        )
                    }
                }
            } else {
                // Compact / Phone Layout: Adaptive vertically stacked layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Map Height dynamically adjusts based on selected view mode
                    val mapHeight = when (viewMode) {
                        DashboardViewMode.MAP_EXPANDED -> 480.dp
                        DashboardViewMode.MAP_AND_TELEMETRY -> 290.dp
                        DashboardViewMode.CAMERA_FEEDS -> 180.dp
                    }

                    // Advanced Google Traffic Map Viewport
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(mapHeight)
                    ) {
                        AdvancedGoogleTrafficMap(
                            selectedLocation = state.selectedLocation,
                            selectedCamera = state.selectedCamera,
                            selectedDirection = state.selectedDirection,
                            currentVolume = state.currentVolume,
                            predictedVolume = state.predictedVolume,
                            congestionLevel = state.congestionLevel,
                            isCloudSynced = state.isCloudSynced,
                            onLocationSelected = onLocationSelected,
                            onCameraSelected = onCameraSelected,
                            onDirectionSelected = onDirectionSelected,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Primary BiLSTM Prediction Hero Metrics
                    LivePredictionHeroCard(state = state)

                    // Live Camera View Card (if in camera view or split view)
                    if (viewMode != DashboardViewMode.MAP_EXPANDED) {
                        LiveCameraStreamFeedCard(
                            location = state.selectedLocation,
                            camera = state.selectedCamera,
                            direction = state.selectedDirection,
                            congestionLevel = state.congestionLevel,
                            currentVolume = state.currentVolume.toInt()
                        )
                    }

                    // Vehicle Breakdown
                    val lastObs = state.observations.lastOrNull()
                    VehicleBreakdownCard(
                        cars = lastObs?.carCount ?: 38,
                        motorbikes = lastObs?.motorbikeCount ?: 58,
                        buses = lastObs?.busCount ?: 14,
                        trucks = lastObs?.truckCount ?: 8,
                        total = (lastObs?.totalVehicles ?: state.currentVolume.toInt()).coerceAtLeast(1)
                    )

                    // Simulation Controls
                    SimulationControlBar(
                        isStreaming = state.isAutoStreaming,
                        onToggleStreaming = onToggleStreaming,
                        onStepForward = onStepForward,
                        onSimulateSurge = onSimulateSurge
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Top-Level Navigation Components
// -------------------------------------------------------------

@Composable
fun TopLevelNavigationHeader(
    selectedLocation: String,
    selectedCamera: String,
    selectedDirection: String,
    isCloudSynced: Boolean,
    isSyncing: Boolean,
    favoriteJunctions: List<FavoriteJunctionEntity> = emptyList(),
    onLocationSelected: (String) -> Unit,
    onCameraSelected: (String) -> Unit,
    onDirectionSelected: (String) -> Unit,
    onSyncBackend: () -> Unit,
    onToggleFavorite: (String, String) -> Unit = { _, _ -> },
    currentViewMode: DashboardViewMode,
    onViewModeChanged: (DashboardViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentJunction = PUNE_JUNCTIONS.firstOrNull { it.name == selectedLocation } ?: PUNE_JUNCTIONS.first()
    val isCurrentFav = favoriteJunctions.any { it.junctionId == selectedLocation }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("top_level_navigation_header"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TrafficNavyCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TrafficNavyCardBorder))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Row 1: Location Chips & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = null,
                        tint = TrafficCyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MONITORED INTERSECTIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TrafficCyanAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.9.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Star active junction button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCurrentFav) TrafficAmberPrimary.copy(alpha = 0.2f) else TrafficNavyCardElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCurrentFav) TrafficAmberPrimary else TrafficNavyCardBorder
                        ),
                        modifier = Modifier
                            .clickable { onToggleFavorite(selectedLocation, currentJunction.displayName) }
                            .testTag("btn_toggle_fav_header")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isCurrentFav) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = if (isCurrentFav) "Remove Favorite" else "Save Favorite",
                                modifier = Modifier.size(13.dp),
                                tint = if (isCurrentFav) TrafficAmberPrimary else TrafficTextMuted
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isCurrentFav) "PINNED" else "PIN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrentFav) TrafficAmberPrimary else TrafficTextSecondary
                            )
                        }
                    }

                    // Cloud Backend Sync Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCloudSynced) TrafficGreenSafe.copy(alpha = 0.15f) else TrafficNavyCardElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCloudSynced) TrafficGreenSafe.copy(alpha = 0.5f) else TrafficNavyCardBorder
                        ),
                        modifier = Modifier
                            .clickable(enabled = !isSyncing) { onSyncBackend() }
                            .testTag("btn_sync_backend")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = TrafficAmberPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = if (isCloudSynced) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                    contentDescription = "Sync Backend",
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isCloudSynced) TrafficGreenSafe else TrafficAmberPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isCloudSynced) "API LIVE" else "SYNC API",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCloudSynced) TrafficGreenSafe else TrafficAmberPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Junction Location Selection Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(PUNE_JUNCTIONS) { junction ->
                    val isSelected = junction.name == selectedLocation
                    val isFav = favoriteJunctions.any { it.junctionId == junction.name }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) TrafficAmberPrimary.copy(alpha = 0.22f) else TrafficNavyCardElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) TrafficAmberPrimary else if (isFav) TrafficAmberPrimary.copy(alpha = 0.45f) else TrafficNavyCardBorder
                        ),
                        modifier = Modifier
                            .clickable { onLocationSelected(junction.name) }
                            .testTag("nav_loc_${junction.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isFav) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Favorited",
                                    tint = TrafficAmberPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) TrafficAmberPrimary else TrafficTextMuted)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = junction.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TrafficAmberPrimary else TrafficTextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Camera & Direction Selectors + View Mode Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Camera Selector
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CCTV SENSORS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TrafficTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(currentJunction.cameras) { cam ->
                            val isSelected = cam == selectedCamera
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) TrafficCyanAccent.copy(alpha = 0.2f) else TrafficNavyCardElevated,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) TrafficCyanAccent else TrafficNavyCardBorder
                                ),
                                modifier = Modifier
                                    .clickable { onCameraSelected(cam) }
                                    .testTag("nav_cam_$cam")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = null,
                                        tint = if (isSelected) TrafficCyanAccent else TrafficTextMuted,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = cam.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) TrafficCyanAccent else TrafficTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Direction Selector
                Column(modifier = Modifier.weight(1.1f)) {
                    Text(
                        text = "FLOW DIRECTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = TrafficTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        items(TRAFFIC_DIRECTIONS) { dir ->
                            val isSelected = dir == selectedDirection
                            val dirIcon = when (dir) {
                                "UP" -> Icons.Default.ArrowUpward
                                "DOWN" -> Icons.Default.ArrowDownward
                                "LEFT" -> Icons.Default.ArrowForward
                                else -> Icons.Default.ArrowForward
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) TrafficAmberPrimary.copy(alpha = 0.2f) else TrafficNavyCardElevated,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) TrafficAmberPrimary else TrafficNavyCardBorder
                                ),
                                modifier = Modifier
                                    .clickable { onDirectionSelected(dir) }
                                    .testTag("nav_dir_$dir")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dir,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) TrafficAmberPrimary else TrafficTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Live Prediction Hero Card
// -------------------------------------------------------------

@Composable
private fun LivePredictionHeroCard(
    state: TrafficUiState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_prediction_hero_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TrafficNavyCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TrafficNavyCardBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "REAL-TIME TRAFFIC INTELLIGENCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TrafficCyanAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${state.selectedLocation} • Cam ${state.selectedCamera.uppercase()} (${state.selectedDirection})",
                        fontSize = 11.sp,
                        color = TrafficTextMuted
                    )
                }

                CongestionBadge(congestionLevel = state.congestionLevel)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Volume comparison metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Current Volume
                Column {
                    Text(
                        text = "Current 5-Min",
                        style = MaterialTheme.typography.bodySmall,
                        color = TrafficTextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${state.currentVolume.toInt()}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = TrafficTextPrimary
                    )
                    Text(
                        text = "vehicles observed",
                        fontSize = 10.sp,
                        color = TrafficTextSecondary
                    )
                }

                // BiLSTM Next 5-Min Forecast
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "BiLSTM +5m Forecast",
                        style = MaterialTheme.typography.bodySmall,
                        color = TrafficAmberPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${state.predictedVolume.toInt()}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = TrafficAmberPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Percentage Change indicator
                        val isRising = state.volumeChangePct >= 0f
                        val deltaColor = if (isRising) TrafficRedCritical else TrafficGreenSafe
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isRising) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = deltaColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${if (isRising) "+" else ""}${String.format("%.1f", state.volumeChangePct)}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = deltaColor
                            )
                        }
                    }
                    Text(
                        text = "Confidence: 91.5% • Lookback: 60m",
                        fontSize = 10.sp,
                        color = TrafficTextSecondary
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Live Camera Stream Feed Card
// -------------------------------------------------------------

@Composable
private fun LiveCameraStreamFeedCard(
    location: String,
    camera: String,
    direction: String,
    congestionLevel: CongestionLevel,
    currentVolume: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cam_rec")
    val recAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rec_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_camera_feed_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TrafficNavyCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TrafficNavyCardBorder))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(TrafficRedCritical.copy(alpha = recAlpha))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE CCTV FEED • CAM ${camera.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TrafficTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = TrafficNavyCardElevated
                ) {
                    Text(
                        text = "1080p • 30 FPS",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        color = TrafficCyanAccent,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Simulated Camera Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, TrafficNavyCardBorder, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Background grid lines simulating digital CCTV HUD
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val lineColor = Color(0xFF1E293B)
                    val w = this.size.width
                    val h = this.size.height
                    drawLine(lineColor, Offset(0f, h / 2f), Offset(w, h / 2f), strokeWidth = 1f)
                    drawLine(lineColor, Offset(w / 2f, 0f), Offset(w / 2f, h), strokeWidth = 1f)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = TrafficCyanAccent.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$location Corridor ($direction)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrafficTextPrimary
                    )
                    Text(
                        text = "Optical Flow Tracking: $currentVolume targets in frame",
                        fontSize = 10.sp,
                        color = TrafficTextSecondary
                    )
                }

                // Timestamp HUD overlay
                Text(
                    text = "CAM-$camera // REC",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    fontSize = 9.sp,
                    color = TrafficCyanAccent,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "PUNE TRAFFIC POLICE TMC",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    fontSize = 8.sp,
                    color = TrafficTextMuted
                )
            }
        }
    }
}
