package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AlertItemEntity
import com.example.data.FavoriteJunctionEntity
import com.example.data.PredictionRecordEntity
import com.example.data.TrafficObservationEntity
import com.example.data.TrafficRepository
import com.example.data.TrafficSenseDatabase
import com.example.location.LocationServiceManager
import com.example.location.UserLiveLocation
import com.example.ml.BengaluruRouteEngine
import com.example.ml.BiLSTMEngine
import com.example.model.BENCHMARK_MODELS
import com.example.model.BENGALURU_ACTIVE_INCIDENTS
import com.example.model.BENGALURU_HOTSPOTS
import com.example.model.BengaluruLocation
import com.example.model.CongestionLevel
import com.example.model.ModelBenchmark
import com.example.model.PUNE_JUNCTIONS
import com.example.model.RoutePoint
import com.example.model.ServiceType
import com.example.model.TRAFFIC_DIRECTIONS
import com.example.model.UiRouteOption
import com.example.model.UiServiceLocation
import com.example.model.UiTrafficIncident
import com.example.network.NetworkClient
import com.example.network.IsochroneCatchment
import com.example.network.TravelTimeApiClient
import com.example.network.TravelTimeEstimate
import com.example.network.RouteLocationDto
import com.example.network.RouteRequestDto
import com.example.ui.components.REAL_TIME_BENGALURU_JUNCTIONS
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class AppTab(val title: String, val iconName: String) {
    MAP_ROUTES("AI Maps & Routes", "Map"),
    SERVICES("Route Services", "LocalHospital"),
    MONITOR("Live Cameras", "Videocam"),
    ANALYTICS("Speed & Analytics", "AutoGraph"),
    INCIDENTS("Alerts & Cloud", "Warning")
}

data class TrafficUiState(
    // Bengaluru AI Route Intelligence & Maps (Mani292)
    val bengaluruOrigin: BengaluruLocation = BENGALURU_HOTSPOTS[0], // Silk Board
    val bengaluruDestination: BengaluruLocation = BENGALURU_HOTSPOTS[3], // MG Road
    val userLiveLocation: UserLiveLocation? = null,
    val isLocationPermissionGranted: Boolean = false,
    val routeOptions: List<UiRouteOption> = emptyList(),
    val selectedRouteIndex: Int = 0,
    val selectedServiceType: ServiceType = ServiceType.ALL,
    val servicesAlongRoute: List<UiServiceLocation> = emptyList(),
    val trafficIncidents: List<UiTrafficIncident> = BENGALURU_ACTIVE_INCIDENTS,
    val isLiveCloudConnected: Boolean = true,
    val cityAvgSpeed: Double = 41.2,
    val cityCongestion: String = "Medium",
    val cityRoutesAnalyzed: Int = 384,

    // TravelTime Isochrone & Reachability Matrix (TravelTime API)
    val travelTimeCatchment: IsochroneCatchment? = null,
    val isCatchmentLoading: Boolean = false,
    val selectedIsochroneMinutes: Int = 30,
    val selectedTransportMode: String = "driving+ferry",
    val travelTimeEstimates: Map<String, TravelTimeEstimate> = emptyMap(),
    val isochroneStatusMessage: String? = null,

    // Junctions & Monitoring
    val selectedLocation: String = "AlankarChowk",
    val selectedCamera: String = "a2",
    val selectedDirection: String = "UP",
    val activeTab: AppTab = AppTab.MAP_ROUTES,
    val observations: List<TrafficObservationEntity> = emptyList(),
    val lookbackWindow: List<TrafficObservationEntity> = emptyList(),
    val currentVolume: Float = 0f,
    val predictedVolume: Float = 0f,
    val volumeChangePct: Float = 0f,
    val congestionLevel: CongestionLevel = CongestionLevel.LOW,
    val lowThreshold: Float = 60f,
    val modThreshold: Float = 120f,
    val highThreshold: Float = 180f,
    val surgeThresholdPct: Float = 25f,
    val isAutoStreaming: Boolean = false,
    val activeAlerts: List<AlertItemEntity> = emptyList(),
    val allAlerts: List<AlertItemEntity> = emptyList(),
    val benchmarks: List<ModelBenchmark> = BENCHMARK_MODELS,
    val favoriteJunctions: List<FavoriteJunctionEntity> = emptyList(),
    val isCloudSynced: Boolean = false,
    val isSyncing: Boolean = false,
    val backendHealthStatus: String = "Live Cloud API (Render)",
    val activeIncidentsCount: Int = 2,
    val statusMessage: String? = null
)

class TrafficSenseViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TrafficSenseDatabase.getDatabase(application)
    private val repository = TrafficRepository(database.trafficDao())
    private val locationServiceManager = LocationServiceManager(application)

    private val _uiState = MutableStateFlow(TrafficUiState())
    val uiState: StateFlow<TrafficUiState> = _uiState.asStateFlow()

    private var streamObservationJob: Job? = null
    private var autoStreamJob: Job? = null
    private var locationUpdatesJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureDatabasePopulated()
            bindStream()
            observeAlerts()
            observeFavorites()
            loadBengaluruRoutes()
            syncWithLiveCloudApi()
        }
    }

    fun onLocationPermissionGranted() {
        _uiState.value = _uiState.value.copy(isLocationPermissionGranted = true)
        locationServiceManager.startLocationTracking()
        locationUpdatesJob?.cancel()
        locationUpdatesJob = viewModelScope.launch {
            locationServiceManager.liveLocation.collectLatest { liveLoc ->
                if (liveLoc != null) {
                    _uiState.value = _uiState.value.copy(userLiveLocation = liveLoc)
                }
            }
        }
    }

    fun setLocationAsOrigin(loc: UserLiveLocation) {
        val originLoc = BengaluruLocation(
            id = "my_live_location",
            name = "📍 My Location",
            lat = loc.latitude,
            lng = loc.longitude,
            area = loc.areaName,
            landmark = loc.fullAddress.ifBlank { loc.areaName }
        )
        selectOrigin(originLoc)
    }

    override fun onCleared() {
        super.onCleared()
        locationServiceManager.stopLocationTracking()
    }

    fun selectTab(tab: AppTab) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    // ================= Bengaluru Maps & Routing Actions =================

    fun selectOrigin(origin: BengaluruLocation) {
        _uiState.value = _uiState.value.copy(bengaluruOrigin = origin)
        loadBengaluruRoutes()
    }

    fun selectDestination(dest: BengaluruLocation) {
        _uiState.value = _uiState.value.copy(bengaluruDestination = dest)
        loadBengaluruRoutes()
    }

    fun swapOriginDestination() {
        val currOrigin = _uiState.value.bengaluruOrigin
        val currDest = _uiState.value.bengaluruDestination
        _uiState.value = _uiState.value.copy(
            bengaluruOrigin = currDest,
            bengaluruDestination = currOrigin
        )
        loadBengaluruRoutes()
        if (_uiState.value.travelTimeCatchment != null) {
            computeTravelTimeCatchment()
        }
    }

    fun selectRoute(index: Int) {
        val options = _uiState.value.routeOptions
        if (index in options.indices) {
            _uiState.value = _uiState.value.copy(selectedRouteIndex = index)
            updateServicesForCurrentRoute()
        }
    }

    fun selectServiceType(type: ServiceType) {
        _uiState.value = _uiState.value.copy(selectedServiceType = type)
        updateServicesForCurrentRoute()
    }

    private fun loadBengaluruRoutes() {
        val origin = _uiState.value.bengaluruOrigin
        val dest = _uiState.value.bengaluruDestination
        val localRoutes = BengaluruRouteEngine.generateRouteOptions(origin, dest)
        _uiState.value = _uiState.value.copy(
            routeOptions = localRoutes,
            selectedRouteIndex = 0
        )
        updateServicesForCurrentRoute()
        viewModelScope.launch {
            fetchTravelTimeMatrix()
        }
    }

    // ================= TravelTime API Isochrones & Catchment =================

    fun setIsochroneMinutes(minutes: Int) {
        _uiState.value = _uiState.value.copy(selectedIsochroneMinutes = minutes)
        computeTravelTimeCatchment(minutes, _uiState.value.selectedTransportMode)
    }

    fun setTransportMode(mode: String) {
        _uiState.value = _uiState.value.copy(selectedTransportMode = mode)
        computeTravelTimeCatchment(_uiState.value.selectedIsochroneMinutes, mode)
    }

    fun clearIsochroneCatchment() {
        _uiState.value = _uiState.value.copy(travelTimeCatchment = null, isochroneStatusMessage = null)
    }

    fun computeTravelTimeCatchment(
        minutes: Int = _uiState.value.selectedIsochroneMinutes,
        mode: String = _uiState.value.selectedTransportMode
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCatchmentLoading = true,
                selectedIsochroneMinutes = minutes,
                selectedTransportMode = mode,
                isochroneStatusMessage = "Connecting to TravelTime API ($minutes min catchment)..."
            )
            val origin = _uiState.value.bengaluruOrigin
            val result = TravelTimeApiClient.getIsochroneReachableArea(
                lat = origin.lat,
                lng = origin.lng,
                travelTimeMinutes = minutes,
                transportMode = mode,
                originName = origin.name
            )

            result.fold(
                onSuccess = { catchment ->
                    val reachableJunctions = REAL_TIME_BENGALURU_JUNCTIONS.filter { junc ->
                        catchment.polygons.any { poly -> TravelTimeApiClient.isPointInPolygon(LatLng(junc.lat, junc.lng), poly) }
                    }
                    val reachableServices = _uiState.value.servicesAlongRoute.filter { srv ->
                        catchment.polygons.any { poly -> TravelTimeApiClient.isPointInPolygon(LatLng(srv.lat, srv.lng), poly) }
                    }
                    val enriched = catchment.copy(
                        reachableJunctionIds = reachableJunctions.map { it.id }.toSet(),
                        totalReachableJunctions = reachableJunctions.size,
                        totalReachableServices = reachableServices.size
                    )
                    _uiState.value = _uiState.value.copy(
                        travelTimeCatchment = enriched,
                        isCatchmentLoading = false,
                        isochroneStatusMessage = "TravelTime Reachable in $minutes min: ${reachableJunctions.size} junctions • ${catchment.polygons.size} isochrone rings"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isCatchmentLoading = false,
                        isochroneStatusMessage = "TravelTime response note: using predicted corridor fallback"
                    )
                }
            )

            fetchTravelTimeMatrix()
        }
    }

    private suspend fun fetchTravelTimeMatrix() {
        try {
            val origin = _uiState.value.bengaluruOrigin
            val destList = BENGALURU_HOTSPOTS.map { it.id to Pair(it.lat, it.lng) }
            val matrixResult = TravelTimeApiClient.calculateTravelTimes(
                originLat = origin.lat,
                originLng = origin.lng,
                destinations = destList,
                transportMode = _uiState.value.selectedTransportMode
            )
            matrixResult.onSuccess { estimates ->
                _uiState.value = _uiState.value.copy(travelTimeEstimates = estimates)
            }
        } catch (_: Exception) {
            // Non-blocking for matrix
        }
    }

    private fun updateServicesForCurrentRoute() {
        val currentRoute = _uiState.value.routeOptions.getOrNull(_uiState.value.selectedRouteIndex)
        val filter = _uiState.value.selectedServiceType
        if (currentRoute != null) {
            val services = BengaluruRouteEngine.findServicesNearRoute(currentRoute, filter)
            _uiState.value = _uiState.value.copy(servicesAlongRoute = services)
        }
    }

    fun syncWithLiveCloudApi() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            try {
                val origin = _uiState.value.bengaluruOrigin
                val dest = _uiState.value.bengaluruDestination
                val req = RouteRequestDto(
                    origin = RouteLocationDto(origin.lat, origin.lng, origin.name),
                    destination = RouteLocationDto(dest.lat, dest.lng, dest.name),
                    timeOfDay = "now"
                )
                val routesResp = NetworkClient.apiService.getBengaluruRoutes(req)
                if (routesResp.isSuccessful && !routesResp.body().isNullOrEmpty()) {
                    val dtoList = routesResp.body()!!
                    val mappedRoutes = dtoList.mapIndexed { idx, dto ->
                        val coords = dto.coordinates.map { pair ->
                            if (pair.size >= 2) RoutePoint(pair[0], pair[1]) else RoutePoint(origin.lat, origin.lng)
                        }
                        UiRouteOption(
                            routeId = dto.displayRouteId,
                            name = dto.name,
                            distanceKm = dto.displayDistanceKm,
                            durationMin = dto.displayDurationMin,
                            predictedSpeedKmph = dto.predicted_speed,
                            trafficLevel = dto.traffic_level,
                            trafficScore = dto.traffic_score ?: 0.45,
                            isFastest = dto.is_fastest || idx == 0,
                            waypoints = if (coords.isNotEmpty()) coords else listOf(RoutePoint(origin.lat, origin.lng), RoutePoint(dest.lat, dest.lng)),
                            steps = if (dto.steps.isNotEmpty()) dto.steps else listOf("Depart from ${origin.name}", "Proceed on corridor", "Arrive at ${dest.name}"),
                            hospitalsCount = dto.services?.hospitals ?: 3,
                            fuelCount = dto.services?.fuel_stations ?: 3,
                            restaurantsCount = dto.services?.restaurants ?: 4,
                            pharmaciesCount = 2
                        )
                    }

                    // Incidents
                    val incidentsResp = runCatching { NetworkClient.apiService.getBengaluruIncidents() }.getOrNull()
                    val mappedIncidents = incidentsResp?.body()?.map {
                        UiTrafficIncident(
                            id = it.id,
                            type = it.type,
                            location = it.location,
                            severity = it.severity,
                            lat = it.lat,
                            lng = it.lng,
                            description = it.description
                        )
                    } ?: _uiState.value.trafficIncidents

                    val statsResp = runCatching { NetworkClient.apiService.getBengaluruStats() }.getOrNull()
                    val avgSpeed = statsResp?.body()?.avg_speed ?: _uiState.value.cityAvgSpeed
                    val congestion = statsResp?.body()?.congestion_level ?: _uiState.value.cityCongestion

                    _uiState.value = _uiState.value.copy(
                        routeOptions = mappedRoutes,
                        selectedRouteIndex = 0,
                        trafficIncidents = mappedIncidents,
                        cityAvgSpeed = avgSpeed,
                        cityCongestion = congestion,
                        isLiveCloudConnected = true,
                        isSyncing = false,
                        backendHealthStatus = "Cloud API (traffic-g8bi.onrender.com)",
                        statusMessage = "Synced with Bengaluru AI Traffic Cloud API!"
                    )
                    updateServicesForCurrentRoute()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLiveCloudConnected = false,
                        isSyncing = false,
                        backendHealthStatus = "Local BiLSTM Simulation",
                        statusMessage = "Render API standby. Using on-device routing."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLiveCloudConnected = false,
                    isSyncing = false,
                    backendHealthStatus = "Local BiLSTM Simulation",
                    statusMessage = "Using on-device AI Traffic engine."
                )
            }
        }
    }

    fun selectLocation(location: String) {
        val junction = PUNE_JUNCTIONS.firstOrNull { it.name == location } ?: PUNE_JUNCTIONS.first()
        val defaultCam = junction.cameras.first()
        _uiState.value = _uiState.value.copy(
            selectedLocation = location,
            selectedCamera = defaultCam
        )
        bindStream()
    }

    fun selectCamera(camera: String) {
        _uiState.value = _uiState.value.copy(selectedCamera = camera)
        bindStream()
    }

    fun selectDirection(direction: String) {
        _uiState.value = _uiState.value.copy(selectedDirection = direction)
        bindStream()
    }

    private fun bindStream() {
        streamObservationJob?.cancel()
        val loc = _uiState.value.selectedLocation
        val cam = _uiState.value.selectedCamera
        val dir = _uiState.value.selectedDirection

        streamObservationJob = viewModelScope.launch {
            repository.getStreamObservations(loc, cam, dir).collectLatest { list ->
                val sorted = list.sortedBy { it.timestamp }
                val lookback = sorted.takeLast(BiLSTMEngine.LOOKBACK)
                val volumes = lookback.map { it.totalVehicles.toFloat() }

                val prediction = BiLSTMEngine.predictNext5Min(
                    location = loc,
                    camera = cam,
                    direction = dir,
                    recentVolumes = volumes,
                    lowThreshold = _uiState.value.lowThreshold,
                    modThreshold = _uiState.value.modThreshold,
                    highThreshold = _uiState.value.highThreshold,
                    surgeThresholdPct = _uiState.value.surgeThresholdPct
                )

                _uiState.value = _uiState.value.copy(
                    observations = sorted,
                    lookbackWindow = lookback,
                    currentVolume = prediction.currentVolume,
                    predictedVolume = prediction.predictedVolume,
                    volumeChangePct = prediction.changePercentage,
                    congestionLevel = prediction.congestionLevel
                )

                // Save prediction log to database
                if (prediction.predictedVolume > 0) {
                    repository.recordPrediction(
                        PredictionRecordEntity(
                            location = loc,
                            camera = cam,
                            direction = dir,
                            timestamp = System.currentTimeMillis(),
                            currentVolume = prediction.currentVolume,
                            predictedNext5MinVolume = prediction.predictedVolume,
                            volumeChangePct = prediction.changePercentage,
                            congestionLevel = prediction.congestionLevel.label
                        )
                    )
                }

                // Log any new alerts
                if (prediction.alertsGenerated.isNotEmpty()) {
                    repository.recordAlerts(prediction.alertsGenerated)
                }
            }
        }
    }

    private fun observeAlerts() {
        viewModelScope.launch {
            repository.activeAlerts.collectLatest { active ->
                _uiState.value = _uiState.value.copy(activeAlerts = active)
            }
        }
        viewModelScope.launch {
            repository.allAlerts.collectLatest { all ->
                _uiState.value = _uiState.value.copy(allAlerts = all)
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.favoriteJunctions.collectLatest { favorites ->
                _uiState.value = _uiState.value.copy(favoriteJunctions = favorites)
            }
        }
    }

    fun updateThresholds(low: Float, mod: Float, high: Float, surge: Float) {
        _uiState.value = _uiState.value.copy(
            lowThreshold = low,
            modThreshold = mod,
            highThreshold = high,
            surgeThresholdPct = surge,
            statusMessage = "Threshold parameters updated"
        )
        // Re-evaluate current volume with new thresholds
        val curPred = _uiState.value.predictedVolume
        val newLevel = CongestionLevel.fromVolume(curPred, low, mod, high)
        _uiState.value = _uiState.value.copy(congestionLevel = newLevel)
    }

    fun stepForwardSimulation(isSurge: Boolean = false) {
        viewModelScope.launch {
            val state = _uiState.value
            val factor = if (isSurge) 1.45f else 1.0f
            val newObs = repository.injectSimulatedObservation(
                location = state.selectedLocation,
                camera = state.selectedCamera,
                direction = state.selectedDirection,
                variationFactor = factor
            )
            _uiState.value = _uiState.value.copy(
                statusMessage = if (isSurge) "Simulated Traffic Surge (+45%)" else "Ingested +5 min interval (${newObs.timeString})"
            )
        }
    }

    fun toggleAutoStreaming() {
        val currentlyStreaming = _uiState.value.isAutoStreaming
        if (currentlyStreaming) {
            autoStreamJob?.cancel()
            _uiState.value = _uiState.value.copy(isAutoStreaming = false, statusMessage = "Simulation paused")
        } else {
            _uiState.value = _uiState.value.copy(isAutoStreaming = true, statusMessage = "Live sensor streaming active (3s intervals)")
            autoStreamJob = viewModelScope.launch {
                while (true) {
                    delay(3000L)
                    val state = _uiState.value
                    repository.injectSimulatedObservation(
                        location = state.selectedLocation,
                        camera = state.selectedCamera,
                        direction = state.selectedDirection
                    )
                }
            }
        }
    }

    fun syncWithBackendServer() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            try {
                val state = _uiState.value
                val obsList = state.lookbackWindow.map {
                    com.example.network.ObservationDto(
                        timestamp = it.timestamp,
                        timeString = it.timeString,
                        totalVehicles = it.totalVehicles,
                        motorbikes = it.motorbikeCount,
                        cars = it.carCount,
                        buses = it.busCount,
                        trucks = it.truckCount
                    )
                }
                val request = com.example.network.PredictionRequest(
                    location = state.selectedLocation,
                    camera = state.selectedCamera,
                    direction = state.selectedDirection,
                    observations = obsList
                )

                val response = com.example.network.NetworkClient.apiService.predictTraffic(request)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _uiState.value = _uiState.value.copy(
                        isCloudSynced = true,
                        isSyncing = false,
                        predictedVolume = body.predictedVolume,
                        volumeChangePct = body.volumeChangePct,
                        backendHealthStatus = "Cloud API Connected (FastAPI)",
                        statusMessage = "Synced with Backend Server (Pred: ${body.predictedVolume.toInt()} veh)"
                    )
                } else {
                    // Fallback to local inference engine if remote backend returns non-200
                    _uiState.value = _uiState.value.copy(
                        isCloudSynced = false,
                        isSyncing = false,
                        backendHealthStatus = "BiLSTM Engine (Local Fallback)",
                        statusMessage = "Backend response code ${response.code()}. Using on-device BiLSTM."
                    )
                }
            } catch (e: Exception) {
                // Connection exception (e.g. server offline on localhost) -> seamless local operation
                _uiState.value = _uiState.value.copy(
                    isCloudSynced = false,
                    isSyncing = false,
                    backendHealthStatus = "BiLSTM Engine (Edge Mode)",
                    statusMessage = "Live edge BiLSTM active (Offline sync ready)"
                )
            }
        }
    }

    fun acknowledgeAlert(alertId: Long) {
        viewModelScope.launch {
            repository.acknowledgeAlert(alertId)
            _uiState.value = _uiState.value.copy(statusMessage = "Alert acknowledged")
        }
    }

    fun toggleFavorite(
        junctionId: String,
        displayName: String,
        defaultCamera: String? = null,
        defaultDirection: String? = null
    ) {
        viewModelScope.launch {
            val cam = defaultCamera ?: _uiState.value.selectedCamera
            val dir = defaultDirection ?: _uiState.value.selectedDirection
            val isFav = _uiState.value.favoriteJunctions.any { it.junctionId == junctionId }
            repository.toggleFavorite(
                junctionId = junctionId,
                displayName = displayName,
                defaultCamera = cam,
                defaultDirection = dir
            )
            _uiState.value = _uiState.value.copy(
                statusMessage = if (isFav) "Removed $displayName from favorites" else "Saved $displayName to favorites"
            )
        }
    }

    fun removeFavorite(junctionId: String) {
        viewModelScope.launch {
            val fav = _uiState.value.favoriteJunctions.firstOrNull { it.junctionId == junctionId }
            val name = fav?.displayName ?: junctionId
            repository.removeFavorite(junctionId)
            _uiState.value = _uiState.value.copy(
                statusMessage = "Removed $name from favorites"
            )
        }
    }

    fun selectFavorite(favorite: FavoriteJunctionEntity) {
        _uiState.value = _uiState.value.copy(
            selectedLocation = favorite.junctionId,
            selectedCamera = favorite.defaultCamera,
            selectedDirection = favorite.defaultDirection,
            statusMessage = "Switched to favorite: ${favorite.displayName}"
        )
        bindStream()
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }
}
