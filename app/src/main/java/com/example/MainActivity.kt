package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppTab
import com.example.ui.TrafficSenseViewModel
import com.example.ui.TrafficUiState
import com.example.ui.screens.IncidentsScreen
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.screens.MapRoutesScreen
import com.example.ui.screens.ServicesScreen
import com.example.ui.screens.SpeedAnalyticsScreen
import com.example.ui.theme.TrafficAmberPrimary
import com.example.ui.theme.TrafficCyanAccent
import com.example.ui.theme.TrafficDangerRed
import com.example.ui.theme.TrafficEmeraldSuccess
import com.example.ui.theme.TrafficIndigoPrimary
import com.example.ui.theme.TrafficNavyCard
import com.example.ui.theme.TrafficNavyDark
import com.example.ui.theme.TrafficRedCritical
import com.example.ui.theme.TrafficSenseTheme
import com.example.ui.theme.TrafficTextMuted
import com.example.ui.theme.TrafficTextPrimary
import com.example.ui.theme.TrafficTextSecondary
import com.example.ui.theme.TrafficWarningAmber

class MainActivity : ComponentActivity() {
    private val viewModel: TrafficSenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrafficSenseTheme {
                TrafficSenseApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrafficSenseApp(viewModel: TrafficSenseViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = TrafficNavyDark,
        topBar = {
            TrafficTopAppBar(uiState = uiState)
        },
        bottomBar = {
            TrafficBottomBar(
                currentTab = uiState.activeTab,
                pendingAlertCount = uiState.activeAlerts.size,
                incidentCount = uiState.trafficIncidents.size,
                onTabSelected = { viewModel.selectTab(it) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.activeTab) {
                AppTab.MAP_ROUTES -> MapRoutesScreen(
                    uiState = uiState,
                    onSelectOrigin = { viewModel.selectOrigin(it) },
                    onSelectDestination = { viewModel.selectDestination(it) },
                    onSwapOriginDestination = { viewModel.swapOriginDestination() },
                    onSelectRoute = { viewModel.selectRoute(it) },
                    onSyncCloudApi = { viewModel.syncWithLiveCloudApi() },
                    onNavigateToServices = { viewModel.selectTab(AppTab.SERVICES) },
                    onPermissionGranted = { viewModel.onLocationPermissionGranted() },
                    onSetLocationAsOrigin = { viewModel.setLocationAsOrigin(it) },
                    onSelectIsochroneMinutes = { viewModel.setIsochroneMinutes(it) },
                    onSelectTransportMode = { viewModel.setTransportMode(it) },
                    onCalculateIsochrone = { viewModel.computeTravelTimeCatchment() },
                    onClearIsochrone = { viewModel.clearIsochroneCatchment() }
                )
                AppTab.SERVICES -> ServicesScreen(
                    uiState = uiState,
                    onSelectFilter = { viewModel.selectServiceType(it) },
                    onNavigateBackToMap = { viewModel.selectTab(AppTab.MAP_ROUTES) }
                )
                AppTab.MONITOR -> MainDashboardScreen(
                    state = uiState,
                    onLocationSelected = { viewModel.selectLocation(it) },
                    onCameraSelected = { viewModel.selectCamera(it) },
                    onDirectionSelected = { viewModel.selectDirection(it) },
                    onToggleStreaming = { viewModel.toggleAutoStreaming() },
                    onStepForward = { viewModel.stepForwardSimulation(isSurge = false) },
                    onSimulateSurge = { viewModel.stepForwardSimulation(isSurge = true) },
                    onSyncBackend = { viewModel.syncWithBackendServer() },
                    onToggleFavorite = { junctionId, displayName -> viewModel.toggleFavorite(junctionId, displayName) },
                    onRemoveFavorite = { junctionId -> viewModel.removeFavorite(junctionId) },
                    onSelectFavorite = { fav -> viewModel.selectFavorite(fav) }
                )
                AppTab.ANALYTICS -> SpeedAnalyticsScreen(
                    uiState = uiState
                )
                AppTab.INCIDENTS -> IncidentsScreen(
                    uiState = uiState,
                    onSyncCloudApi = { viewModel.syncWithLiveCloudApi() },
                    onAcknowledgeAlert = { viewModel.acknowledgeAlert(it) },
                    onUpdateLowThreshold = { viewModel.updateThresholds(it, uiState.modThreshold, uiState.highThreshold, uiState.surgeThresholdPct) },
                    onUpdateModThreshold = { viewModel.updateThresholds(uiState.lowThreshold, it, uiState.highThreshold, uiState.surgeThresholdPct) },
                    onUpdateHighThreshold = { viewModel.updateThresholds(uiState.lowThreshold, uiState.modThreshold, it, uiState.surgeThresholdPct) },
                    onUpdateSurgeThreshold = { viewModel.updateThresholds(uiState.lowThreshold, uiState.modThreshold, uiState.highThreshold, it) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrafficTopAppBar(uiState: TrafficUiState) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Traffic,
                    contentDescription = null,
                    tint = TrafficAmberPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Bengaluru AI Traffic",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = TrafficTextPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Intelligent Traffic & Route Intelligence",
                        fontSize = 10.sp,
                        color = TrafficCyanAccent,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        actions = {
            Surface(
                color = if (uiState.isLiveCloudConnected) TrafficEmeraldSuccess.copy(alpha = 0.18f) else TrafficWarningAmber.copy(alpha = 0.18f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isLiveCloudConnected) TrafficEmeraldSuccess else TrafficWarningAmber)
                    )
                    Text(
                        text = if (uiState.isLiveCloudConnected) "LIVE" else "LOCAL",
                        color = if (uiState.isLiveCloudConnected) TrafficEmeraldSuccess else TrafficWarningAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = TrafficNavyCard
        )
    )
}

@Composable
private fun TrafficBottomBar(
    currentTab: AppTab,
    pendingAlertCount: Int,
    incidentCount: Int,
    onTabSelected: (AppTab) -> Unit
) {
    NavigationBar(
        containerColor = TrafficNavyCard,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("main_navigation_bar")
    ) {
        AppTab.values().forEach { tab ->
            val isSelected = currentTab == tab
            val (icon, label) = when (tab) {
                AppTab.MAP_ROUTES -> Pair(Icons.Default.Route, "Routes & Map")
                AppTab.SERVICES -> Pair(Icons.Default.LocalHospital, "Services")
                AppTab.MONITOR -> Pair(Icons.Default.Videocam, "Cameras")
                AppTab.ANALYTICS -> Pair(Icons.Default.Speed, "Analytics")
                AppTab.INCIDENTS -> Pair(Icons.Default.Warning, "Incidents")
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.testTag("nav_${tab.name.lowercase()}"),
                icon = {
                    if (tab == AppTab.INCIDENTS && incidentCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = TrafficDangerRed,
                                    contentColor = Color.White
                                ) {
                                    Text("$incidentCount")
                                }
                            }
                        ) {
                            Icon(icon, contentDescription = tab.title)
                        }
                    } else {
                        Icon(icon, contentDescription = tab.title)
                    }
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TrafficNavyDark,
                    selectedTextColor = TrafficCyanAccent,
                    indicatorColor = TrafficCyanAccent,
                    unselectedIconColor = TrafficTextMuted,
                    unselectedTextColor = TrafficTextMuted
                )
            )
        }
    }
}
