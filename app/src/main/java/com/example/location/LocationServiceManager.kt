package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import com.example.model.BENGALURU_HOTSPOTS
import com.example.model.BengaluruLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class UserLiveLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 0f,
    val speedKmph: Float = 0f,
    val areaName: String = "Bengaluru",
    val fullAddress: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRealTimeGps: Boolean = true
)

class LocationServiceManager(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _liveLocation = MutableStateFlow<UserLiveLocation?>(null)
    val liveLocation: StateFlow<UserLiveLocation?> = _liveLocation.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { loc ->
                updateWithRealLocation(loc)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationTracking() {
        try {
            _isTracking.value = true
            // Request high accuracy location
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                .setMinUpdateIntervalMillis(2500L)
                .setMinUpdateDistanceMeters(5f)
                .build()

            fusedClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )

            // Also request last known location immediately for instant response
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null && _liveLocation.value == null) {
                    updateWithRealLocation(loc)
                }
            }
        } catch (_: SecurityException) {
            _isTracking.value = false
        }
    }

    fun stopLocationTracking() {
        try {
            fusedClient.removeLocationUpdates(locationCallback)
            _isTracking.value = false
        } catch (_: Exception) {
            // Ignore
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocationOnce(onResult: (UserLiveLocation) -> Unit = {}) {
        try {
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        updateWithRealLocation(loc)
                        _liveLocation.value?.let { onResult(it) }
                    } else {
                        // Fallback to default Koramangala if emulator has no GPS fix yet
                        val fallback = UserLiveLocation(
                            latitude = 12.9352,
                            longitude = 77.6245,
                            accuracyMeters = 15f,
                            speedKmph = 0f,
                            areaName = "Koramangala, Bengaluru",
                            fullAddress = "Near Sony World Junction, Bengaluru",
                            isRealTimeGps = false
                        )
                        _liveLocation.value = fallback
                        onResult(fallback)
                    }
                }
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }

    private fun updateWithRealLocation(loc: Location) {
        scope.launch {
            val speedKmH = if (loc.hasSpeed()) (loc.speed * 3.6f) else 0f
            val (area, address) = resolveAreaAndAddress(loc.latitude, loc.longitude)

            val updated = UserLiveLocation(
                latitude = loc.latitude,
                longitude = loc.longitude,
                accuracyMeters = if (loc.hasAccuracy()) loc.accuracy else 10f,
                speedKmph = speedKmH,
                areaName = area,
                fullAddress = address,
                timestamp = loc.time,
                isRealTimeGps = true
            )
            _liveLocation.value = updated
        }
    }

    private fun resolveAreaAndAddress(lat: Double, lng: Double): Pair<String, String> {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val subLocality = addr.subLocality ?: addr.locality ?: addr.subAdminArea ?: "Bengaluru"
                    val line = addr.getAddressLine(0) ?: "$subLocality, Bengaluru"
                    return Pair(subLocality, line)
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val subLocality = addr.subLocality ?: addr.locality ?: addr.subAdminArea ?: "Bengaluru"
                    val line = addr.getAddressLine(0) ?: "$subLocality, Bengaluru"
                    return Pair(subLocality, line)
                }
            }
        } catch (_: Exception) {
            // Geocoder service might be unavailable in emulator environment
        }

        // Mathematical nearest Bengaluru hotspot detection
        val nearestHotspot = BENGALURU_HOTSPOTS.minByOrNull { hotspot ->
            distanceKm(lat, lng, hotspot.lat, hotspot.lng)
        }

        return if (nearestHotspot != null) {
            Pair(nearestHotspot.name, "${nearestHotspot.area}, Bengaluru")
        } else {
            Pair("Bengaluru Urban", "Karnataka, India")
        }
    }

    private fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        return r * 2 * asin(sqrt(a))
    }
}
