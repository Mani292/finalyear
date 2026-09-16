package com.example.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {

    // Live Render backend for Bengaluru Traffic Intelligence
    private const val DEFAULT_BASE_URL = "https://traffic-g8bi.onrender.com/"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    @Volatile
    private var retrofitInstance: Retrofit = buildRetrofit(DEFAULT_BASE_URL)

    @Volatile
    var apiService: TrafficApiService = retrofitInstance.create(TrafficApiService::class.java)
        private set

    private fun buildRetrofit(baseUrl: String): Retrofit {
        val sanitized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(sanitized)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    fun updateBaseUrl(newBaseUrl: String) {
        retrofitInstance = buildRetrofit(newBaseUrl)
        apiService = retrofitInstance.create(TrafficApiService::class.java)
    }
}
