package com.example.network

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

/**
 * Data Transfer Objects for TravelTime API (/v4/time-filter/fast)
 */
@JsonClass(generateAdapter = true)
data class TravelTimeCoords(
    @Json(name = "lat") val lat: Double,
    @Json(name = "lng") val lng: Double
)

@JsonClass(generateAdapter = true)
data class TravelTimeLocation(
    @Json(name = "id") val id: String,
    @Json(name = "coords") val coords: TravelTimeCoords
)

@JsonClass(generateAdapter = true)
data class TravelTimeTransportation(
    @Json(name = "type") val type: String = "driving+ferry"
)

@JsonClass(generateAdapter = true)
data class OneToManySearch(
    @Json(name = "id") val id: String,
    @Json(name = "departure_location_id") val departureLocationId: String,
    @Json(name = "arrival_location_ids") val arrivalLocationIds: List<String>,
    @Json(name = "transportation") val transportation: TravelTimeTransportation = TravelTimeTransportation(),
    @Json(name = "arrival_time_period") val arrivalTimePeriod: String = "weekday_morning",
    @Json(name = "travel_time") val travelTimeSeconds: Int = 7200,
    @Json(name = "properties") val properties: List<String> = listOf("travel_time", "distance")
)

@JsonClass(generateAdapter = true)
data class ArrivalSearches(
    @Json(name = "one_to_many") val oneToMany: List<OneToManySearch> = emptyList(),
    @Json(name = "many_to_one") val manyToOne: List<Any> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TimeFilterFastRequest(
    @Json(name = "locations") val locations: List<TravelTimeLocation>,
    @Json(name = "arrival_searches") val arrivalSearches: ArrivalSearches
)

@JsonClass(generateAdapter = true)
data class LocationProperties(
    @Json(name = "travel_time") val travelTime: Int = 0,
    @Json(name = "distance") val distance: Int = 0
)

@JsonClass(generateAdapter = true)
data class TimeFilterFastLocationResult(
    @Json(name = "id") val id: String,
    @Json(name = "properties") val properties: LocationProperties? = null
)

@JsonClass(generateAdapter = true)
data class TimeFilterFastResult(
    @Json(name = "search_id") val searchId: String? = null,
    @Json(name = "locations") val locations: List<TimeFilterFastLocationResult> = emptyList(),
    @Json(name = "unreachable") val unreachable: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TimeFilterFastResponse(
    @Json(name = "results") val results: List<TimeFilterFastResult> = emptyList()
)

/**
 * Retrofit Service Interface for the TravelTime API
 */
interface TravelTimeApiService {

    @POST("v4/time-filter/fast")
    suspend fun getTimeFilterFast(
        @Body request: TimeFilterFastRequest,
        @Header("X-Application-Id") appId: String = TravelTimeRetrofitClient.appId,
        @Header("X-Api-Key") apiKey: String = TravelTimeRetrofitClient.apiKey
    ): Response<TimeFilterFastResponse>
}

/**
 * Configured Retrofit instance to interface with the TravelTime API using
 * TRAVELTIME_APP_ID and TRAVELTIME_API_KEY from BuildConfig.
 */
object TravelTimeRetrofitClient {

    private const val TAG = "TravelTimeRetrofit"
    private const val BASE_URL = "https://api.traveltimeapp.com/"

    val appId: String
        get() = try {
            BuildConfig.TRAVELTIME_APP_ID.ifBlank { "b8d309da" }
        } catch (_: Exception) {
            "b8d309da"
        }

    val apiKey: String
        get() = try {
            BuildConfig.TRAVELTIME_API_KEY.ifBlank { "3683c05be0972f52617a681a3b63e9af" }
        } catch (_: Exception) {
            "3683c05be0972f52617a681a3b63e9af"
        }

    private val authHeaderInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")

        // Add auth headers if not already added by annotations
        if (original.header("X-Application-Id").isNullOrEmpty()) {
            requestBuilder.header("X-Application-Id", appId)
        }
        if (original.header("X-Api-Key").isNullOrEmpty()) {
            requestBuilder.header("X-Api-Key", apiKey)
        }

        chain.proceed(requestBuilder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authHeaderInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: TravelTimeApiService = retrofit.create(TravelTimeApiService::class.java)

    /**
     * Fetch real-time travel duration and distance matrix from an origin to multiple destinations.
     */
    suspend fun fetchTravelDurations(
        originLat: Double,
        originLng: Double,
        destinations: List<Pair<String, Pair<Double, Double>>>,
        transportMode: String = "driving+ferry",
        timeLimitSeconds: Int = 7200
    ): Result<Map<String, TravelTimeEstimate>> {
        return try {
            if (destinations.isEmpty()) {
                return Result.success(emptyMap())
            }

            val locationsList = mutableListOf<TravelTimeLocation>()
            locationsList.add(
                TravelTimeLocation(
                    id = "origin",
                    coords = TravelTimeCoords(originLat, originLng)
                )
            )

            val destIds = destinations.map { it.first }
            destinations.forEach { (id, coords) ->
                locationsList.add(
                    TravelTimeLocation(
                        id = id,
                        coords = TravelTimeCoords(coords.first, coords.second)
                    )
                )
            }

            val request = TimeFilterFastRequest(
                locations = locationsList,
                arrivalSearches = ArrivalSearches(
                    oneToMany = listOf(
                        OneToManySearch(
                            id = "realtime-travel-duration-search",
                            departureLocationId = "origin",
                            arrivalLocationIds = destIds,
                            transportation = TravelTimeTransportation(type = transportMode),
                            arrivalTimePeriod = "weekday_morning",
                            travelTimeSeconds = timeLimitSeconds,
                            properties = listOf("travel_time", "distance")
                        )
                    )
                )
            )

            val response = apiService.getTimeFilterFast(
                request = request,
                appId = appId,
                apiKey = apiKey
            )

            if (response.isSuccessful && response.body() != null) {
                val results = response.body()!!.results
                val durationMap = mutableMapOf<String, TravelTimeEstimate>()

                if (results.isNotEmpty()) {
                    results[0].locations.forEach { loc ->
                        val durationSec = loc.properties?.travelTime ?: 0
                        val distMeters = loc.properties?.distance ?: 0
                        durationMap[loc.id] = TravelTimeEstimate(
                            locationId = loc.id,
                            travelTimeSeconds = durationSec,
                            travelTimeMinutes = (durationSec + 30) / 60,
                            distanceMeters = distMeters,
                            distanceKm = distMeters / 1000f
                        )
                    }
                }
                Log.d(TAG, "Successfully fetched ${durationMap.size} travel durations via Retrofit")
                Result.success(durationMap)
            } else {
                val errorMsg = "TravelTime Retrofit call failed: ${response.code()} ${response.message()}"
                Log.w(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching travel durations via Retrofit", e)
            Result.failure(e)
        }
    }

    /**
     * Helper to fetch real-time travel duration for a single destination.
     */
    suspend fun fetchSingleTravelDuration(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double,
        transportMode: String = "driving+ferry"
    ): TravelTimeEstimate? {
        val result = fetchTravelDurations(
            originLat = originLat,
            originLng = originLng,
            destinations = listOf("single_dest" to Pair(destLat, destLng)),
            transportMode = transportMode
        )
        return result.getOrNull()?.get("single_dest")
    }
}
