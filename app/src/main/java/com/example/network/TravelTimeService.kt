package com.example.network

import android.util.Log
import com.example.BuildConfig
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class IsochroneCatchment(
    val minutes: Int,
    val transportMode: String,
    val displayMode: String,
    val polygons: List<List<LatLng>>,
    val originLat: Double,
    val originLng: Double,
    val originName: String,
    val reachableJunctionIds: Set<String> = emptySet(),
    val totalReachableJunctions: Int = 0,
    val totalReachableServices: Int = 0,
    val generatedAtMillis: Long = System.currentTimeMillis()
)

data class TravelTimeEstimate(
    val locationId: String,
    val travelTimeSeconds: Int,
    val travelTimeMinutes: Int,
    val distanceMeters: Int,
    val distanceKm: Float
)

object TravelTimeApiClient {

    private const val TAG = "TravelTimeApi"
    private const val BASE_URL = "https://api.traveltimeapp.com/v4"

    // Primary keys from BuildConfig with safe fallback
    private val appId: String
        get() = try {
            BuildConfig.TRAVELTIME_APP_ID.ifBlank { "b8d309da" }
        } catch (_: Exception) {
            "b8d309da"
        }

    private val apiKey: String
        get() = try {
            BuildConfig.TRAVELTIME_API_KEY.ifBlank { "3683c05be0972f52617a681a3b63e9af" }
        } catch (_: Exception) {
            "3683c05be0972f52617a681a3b63e9af"
        }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Fetch real reachable isochrone boundary polygon from TravelTime /v4/time-map/fast
     * transportMode can be:
     * - "driving+ferry" (Car/Cab)
     * - "public_transport" (BMTC Bus / Namma Metro)
     * - "cycling+ferry" (Bike/Two-wheeler)
     * - "walking+ferry" (Walking)
     */
    suspend fun getIsochroneReachableArea(
        lat: Double,
        lng: Double,
        travelTimeMinutes: Int = 30,
        transportMode: String = "driving+ferry",
        originName: String = "Selected Location"
    ): Result<IsochroneCatchment> = withContext(Dispatchers.IO) {
        try {
            val travelTimeSeconds = travelTimeMinutes * 60

            val requestJson = JSONObject().apply {
                val arrivalSearches = JSONObject().apply {
                    val searchObj = JSONObject().apply {
                        put("id", "bengaluru-isochrone-$travelTimeMinutes-min")
                        put("coords", JSONObject().apply {
                            put("lat", lat)
                            put("lng", lng)
                        })
                        put("arrival_time_period", "weekday_morning")
                        put("travel_time", travelTimeSeconds)
                        put("transportation", JSONObject().apply {
                            put("type", transportMode)
                        })
                    }
                    put("one_to_many", JSONArray().put(searchObj))
                    put("many_to_one", JSONArray())
                }
                put("arrival_searches", arrivalSearches)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL/time-map/fast")
                .header("Content-Type", "application/json")
                .header("Accept", "application/geo+json")
                .header("X-Application-Id", appId)
                .header("X-Api-Key", apiKey)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                Log.w(TAG, "TravelTime Isochrone HTTP ${response.code}: $responseBody")
                return@withContext Result.failure(
                    Exception("TravelTime API error HTTP ${response.code}: ${response.message}")
                )
            }

            val polygons = parseGeoJsonPolygons(responseBody)

            val displayMode = when (transportMode) {
                "driving+ferry" -> "Drive / Cab"
                "public_transport" -> "BMTC Bus / Metro"
                "cycling+ferry" -> "Two-Wheeler / Bike"
                "walking+ferry" -> "Walking"
                else -> "Transit"
            }

            val catchment = IsochroneCatchment(
                minutes = travelTimeMinutes,
                transportMode = transportMode,
                displayMode = displayMode,
                polygons = polygons,
                originLat = lat,
                originLng = lng,
                originName = originName
            )

            Result.success(catchment)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch TravelTime isochrone", e)
            Result.failure(e)
        }
    }

    /**
     * Compute Travel Time Matrix from one origin to multiple destinations
     * using TravelTime /v4/time-filter/fast
     */
    suspend fun calculateTravelTimes(
        originLat: Double,
        originLng: Double,
        destinations: List<Pair<String, Pair<Double, Double>>>, // id to (lat, lng)
        transportMode: String = "driving+ferry"
    ): Result<Map<String, TravelTimeEstimate>> = withContext(Dispatchers.IO) {
        try {
            if (destinations.isEmpty()) {
                return@withContext Result.success(emptyMap())
            }

            val locationsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("id", "origin")
                    put("coords", JSONObject().apply {
                        put("lat", originLat)
                        put("lng", originLng)
                    })
                })
                destinations.forEach { (destId, coords) ->
                    put(JSONObject().apply {
                        put("id", destId)
                        put("coords", JSONObject().apply {
                            put("lat", coords.first)
                            put("lng", coords.second)
                        })
                    })
                }
            }

            val destIdsArray = JSONArray().apply {
                destinations.forEach { put(it.first) }
            }

            val searchObj = JSONObject().apply {
                put("id", "bengaluru-time-matrix")
                put("departure_location_id", "origin")
                put("arrival_location_ids", destIdsArray)
                put("transportation", JSONObject().apply {
                    put("type", transportMode)
                })
                put("arrival_time_period", "weekday_morning")
                put("travel_time", 7200) // 2 hours ceiling
                put("properties", JSONArray().put("travel_time").put("distance"))
            }

            val requestJson = JSONObject().apply {
                put("locations", locationsArray)
                put("arrival_searches", JSONObject().apply {
                    put("one_to_many", JSONArray().put(searchObj))
                    put("many_to_one", JSONArray())
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL/time-filter/fast")
                .header("Content-Type", "application/json")
                .header("X-Application-Id", appId)
                .header("X-Api-Key", apiKey)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                Log.w(TAG, "TravelTime TimeFilter HTTP ${response.code}: $responseBody")
                return@withContext Result.failure(
                    Exception("TravelTime HTTP ${response.code}: ${response.message}")
                )
            }

            val resultMap = mutableMapOf<String, TravelTimeEstimate>()
            val root = JSONObject(responseBody)
            val resultsArr = root.optJSONArray("results")
            if (resultsArr != null && resultsArr.length() > 0) {
                val firstResult = resultsArr.getJSONObject(0)
                val locations = firstResult.optJSONArray("locations")
                if (locations != null) {
                    for (i in 0 until locations.length()) {
                        val locObj = locations.getJSONObject(i)
                        val id = locObj.getString("id")
                        val props = locObj.optJSONObject("properties")
                        if (props != null) {
                            val timeSec = props.optInt("travel_time", 0)
                            val distMeters = props.optInt("distance", 0)
                            resultMap[id] = TravelTimeEstimate(
                                locationId = id,
                                travelTimeSeconds = timeSec,
                                travelTimeMinutes = (timeSec + 30) / 60,
                                distanceMeters = distMeters,
                                distanceKm = distMeters / 1000f
                            )
                        }
                    }
                }
            }

            Result.success(resultMap)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to calculate travel times with TravelTime API", e)
            Result.failure(e)
        }
    }

    /**
     * Parses standard GeoJSON FeatureCollection containing MultiPolygon or Polygon geometries
     * and extracts outer rings as List<LatLng>.
     */
    private fun parseGeoJsonPolygons(geoJsonString: String): List<List<LatLng>> {
        val resultPolygons = mutableListOf<List<LatLng>>()
        val root = JSONObject(geoJsonString)
        val features = root.optJSONArray("features") ?: return emptyList()

        for (i in 0 until features.length()) {
            val feature = features.getJSONObject(i)
            val geometry = feature.optJSONObject("geometry") ?: continue
            val geomType = geometry.optString("type")

            if (geomType.equals("MultiPolygon", ignoreCase = true)) {
                val coords = geometry.optJSONArray("coordinates") ?: continue
                for (p in 0 until coords.length()) {
                    val polygonRings = coords.getJSONArray(p)
                    if (polygonRings.length() > 0) {
                        // Take the outer boundary ring (index 0)
                        val outerRing = polygonRings.getJSONArray(0)
                        val points = mutableListOf<LatLng>()
                        for (pt in 0 until outerRing.length()) {
                            val pair = outerRing.getJSONArray(pt)
                            // GeoJSON format is [longitude, latitude]
                            val lng = pair.getDouble(0)
                            val lat = pair.getDouble(1)
                            points.add(LatLng(lat, lng))
                        }
                        if (points.size >= 3) {
                            resultPolygons.add(points)
                        }
                    }
                }
            } else if (geomType.equals("Polygon", ignoreCase = true)) {
                val coords = geometry.optJSONArray("coordinates") ?: continue
                if (coords.length() > 0) {
                    val outerRing = coords.getJSONArray(0)
                    val points = mutableListOf<LatLng>()
                    for (pt in 0 until outerRing.length()) {
                        val pair = outerRing.getJSONArray(pt)
                        val lng = pair.getDouble(0)
                        val lat = pair.getDouble(1)
                        points.add(LatLng(lat, lng))
                    }
                    if (points.size >= 3) {
                        resultPolygons.add(points)
                    }
                }
            }
        }

        return resultPolygons
    }

    /**
     * Standard ray casting algorithm to check if point is within polygon boundary
     */
    fun isPointInPolygon(point: LatLng, polygon: List<LatLng>): Boolean {
        if (polygon.size < 3) return false
        var intersectCount = 0
        val n = polygon.size
        for (i in 0 until n) {
            val p1 = polygon[i]
            val p2 = polygon[(i + 1) % n]
            if ((p1.latitude > point.latitude) != (p2.latitude > point.latitude)) {
                val slope = (point.latitude - p1.latitude) / (p2.latitude - p1.latitude)
                val intersectLng = p1.longitude + slope * (p2.longitude - p1.longitude)
                if (point.longitude < intersectLng) {
                    intersectCount++
                }
            }
        }
        return (intersectCount % 2) == 1
    }
}
