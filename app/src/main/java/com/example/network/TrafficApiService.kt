package com.example.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TrafficApiService {

    @GET("/")
    suspend fun getSystemHealth(): Response<SystemHealthDto>

    @GET("api/v1/junctions")
    suspend fun getJunctions(): Response<JunctionListResponse>

    @GET("api/v1/traffic-status")
    suspend fun getTrafficStatus(
        @Query("location") location: String? = null
    ): Response<TrafficStatusResponse>

    @POST("api/v1/predict")
    suspend fun predictTraffic(
        @Body request: PredictionRequest
    ): Response<PredictionResponse>

    @GET("api/v1/predict/latest")
    suspend fun getLatestPrediction(
        @Query("location") location: String,
        @Query("camera") camera: String,
        @Query("direction") direction: String
    ): Response<PredictionResponse>

    @GET("api/v1/model-benchmark")
    suspend fun getModelBenchmarks(): Response<List<ModelBenchmarkDto>>

    // Live Bengaluru API endpoints (traffic-g8bi.onrender.com)
    @POST("api/routes")
    suspend fun getBengaluruRoutes(
        @Body request: RouteRequestDto
    ): Response<List<BengaluruRouteDto>>

    @GET("api/services/{route_id}")
    suspend fun getBengaluruServices(
        @retrofit2.http.Path("route_id") routeId: String
    ): Response<BengaluruServicesResponse>

    @GET("api/incidents")
    suspend fun getBengaluruIncidents(): Response<List<BengaluruIncidentDto>>

    @GET("api/stats")
    suspend fun getBengaluruStats(): Response<BengaluruStatsDto>
}
