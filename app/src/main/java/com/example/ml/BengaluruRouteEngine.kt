package com.example.ml

import com.example.model.BENGALURU_SERVICES_CATALOG
import com.example.model.BengaluruLocation
import com.example.model.RoutePoint
import com.example.model.ServiceType
import com.example.model.UiRouteOption
import com.example.model.UiServiceLocation
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

object BengaluruRouteEngine {

    private val ROADS = listOf(
        "MG Road", "Brigade Road", "Residency Road", "Richmond Road",
        "Commercial Street", "Vittal Mallya Road", "Kasturba Road", "Infantry Road",
        "Church Street", "Mission Road", "Double Road", "CMH Road", "HAL Road"
    )

    private val OUTER_ROADS = listOf(
        "Outer Ring Road", "Inner Ring Road", "Intermediate Ring Road", "Bellary Road",
        "Tumkur Road", "Hosur Road", "Old Madras Road", "Mysore Road", "Bannerghatta Road",
        "Kanakapura Road", "Magadi Road", "Airport Expressway"
    )

    private val LANDMARKS = listOf(
        "KR Market", "Cubbon Park", "Vidhana Soudha", "MG Road Metro", "Trinity Circle",
        "Dairy Circle", "Sony Signal", "Silk Board Junction", "Marathahalli Bridge",
        "K.R. Puram Bridge", "Hebbal Flyover", "Tin Factory", "Shivaji Nagar",
        "Jayanagar 4th Block", "BTM Layout"
    )

    data class RouteConfig(
        val name: String,
        val distanceMult: Double,
        val pointsCount: Int,
        val variation: Double,
        val trafficLevel: String,
        val baseSpeed: Double
    )

    private val ROUTE_PRESETS = listOf(
        RouteConfig("Direct Corridor", 1.00, 2, 0.005, "low", 52.0),
        RouteConfig("Via Outer Ring Road", 1.15, 3, 0.012, "moderate", 45.0),
        RouteConfig("Via City Center CBD", 1.08, 3, 0.010, "medium", 35.0),
        RouteConfig("Express Highway Tollway", 1.20, 2, 0.015, "low", 55.0),
        RouteConfig("Scenic Cubbon Park Bypass", 1.12, 4, 0.008, "moderate", 42.0),
        RouteConfig("Elevated Flyover Bypass", 1.18, 3, 0.013, "medium", 38.0),
        RouteConfig("Local Arterial Streets", 1.05, 4, 0.007, "high", 26.0),
        RouteConfig("Service Road Alternate", 1.10, 3, 0.009, "medium", 32.0),
        RouteConfig("Inner Intermediate Circle", 1.06, 3, 0.008, "moderate", 41.0),
        RouteConfig("Alternate National Highway", 1.22, 2, 0.014, "low", 50.0),
        RouteConfig("Metro Adjacent Transit Line", 1.07, 3, 0.006, "moderate", 43.0),
        RouteConfig("Commercial Tech Zone Route", 1.09, 4, 0.008, "heavy", 19.0)
    )

    fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * asin(sqrt(a))
        return r * c
    }

    fun generateRouteOptions(
        origin: BengaluruLocation,
        destination: BengaluruLocation,
        seedModifier: Int = 0
    ): List<UiRouteOption> {
        val directDistance = haversineKm(origin.lat, origin.lng, destination.lat, destination.lng).coerceAtLeast(1.5)
        val random = Random(origin.id.hashCode() + destination.id.hashCode() + seedModifier)

        val routes = ROUTE_PRESETS.mapIndexed { idx, config ->
            val routeDistance = directDistance * config.distanceMult
            val speedVariance = random.nextDouble(-3.0, 3.0)
            val speed = (config.baseSpeed + speedVariance).coerceIn(15.0, 60.0)
            val durationMin = ((routeDistance / speed) * 60).toInt().coerceAtLeast(3)

            // Generate intermediate waypoints
            val waypoints = mutableListOf<RoutePoint>()
            waypoints.add(RoutePoint(origin.lat, origin.lng))
            for (i in 1..config.pointsCount) {
                val ratio = i.toDouble() / (config.pointsCount + 1)
                val baseLat = origin.lat + (destination.lat - origin.lat) * ratio
                val baseLng = origin.lng + (destination.lng - origin.lng) * ratio
                val latOffset = (random.nextDouble() - 0.5) * 2 * config.variation
                val lngOffset = (random.nextDouble() - 0.5) * 2 * config.variation
                waypoints.add(RoutePoint(baseLat + latOffset, baseLng + lngOffset))
            }
            waypoints.add(RoutePoint(destination.lat, destination.lng))

            // Generate navigation steps
            val steps = mutableListOf<String>()
            steps.add("Head out from ${origin.name} toward main corridor")
            steps.add("Turn ${if (random.nextBoolean()) "right" else "left"} onto ${ROADS[idx % ROADS.size]}")
            steps.add("Continue for ${(routeDistance * 0.25).format(1)} km past ${LANDMARKS[(idx + 1) % LANDMARKS.size]}")
            steps.add("Merge onto ${OUTER_ROADS[idx % OUTER_ROADS.size]} and maintain lane")
            steps.add("Take exit toward ${LANDMARKS[(idx + 2) % LANDMARKS.size]}")
            steps.add("Keep ${if (random.nextBoolean()) "left" else "right"} at the intersection for ${(routeDistance * 0.3).format(1)} km")
            steps.add("Pass ${LANDMARKS[(idx + 3) % LANDMARKS.size]} on your left")
            steps.add("Arrive at ${destination.name} - destination reached")

            val trafficScore = when (config.trafficLevel) {
                "low" -> 0.22 + random.nextDouble(0.0, 0.12)
                "moderate" -> 0.45 + random.nextDouble(0.0, 0.10)
                "medium" -> 0.60 + random.nextDouble(0.0, 0.08)
                "high" -> 0.78 + random.nextDouble(0.0, 0.08)
                else -> 0.90 + random.nextDouble(0.0, 0.08)
            }

            UiRouteOption(
                routeId = "route_${idx + 1}",
                name = config.name,
                distanceKm = (routeDistance * 10.0).toInt() / 10.0,
                durationMin = durationMin,
                predictedSpeedKmph = (speed * 10.0).toInt() / 10.0,
                trafficLevel = config.trafficLevel,
                trafficScore = (trafficScore * 100.0).toInt() / 100.0,
                isFastest = false, // will mark fastest
                waypoints = waypoints,
                steps = steps,
                hospitalsCount = 3,
                fuelCount = 3,
                restaurantsCount = 4,
                pharmaciesCount = 2
            )
        }

        // Sort by duration (fastest first)
        val sorted = routes.sortedBy { it.durationMin }.toMutableList()
        if (sorted.isNotEmpty()) {
            sorted[0] = sorted[0].copy(isFastest = true)
        }
        return sorted
    }

    fun findServicesNearRoute(
        route: UiRouteOption,
        filterType: ServiceType = ServiceType.ALL
    ): List<UiServiceLocation> {
        val midPoint = if (route.waypoints.isNotEmpty()) {
            route.waypoints[route.waypoints.size / 2]
        } else {
            RoutePoint(12.9352, 77.6245)
        }

        return BENGALURU_SERVICES_CATALOG.filter { service ->
            filterType == ServiceType.ALL || service.type == filterType
        }.map { service ->
            val dist = haversineKm(midPoint.lat, midPoint.lng, service.lat, service.lng)
            service.copy(
                distanceFromRouteKm = (dist.coerceAtLeast(0.3) * 10.0).toInt() / 10.0
            )
        }.sortedBy { it.distanceFromRouteKm }
    }

    private fun Double.format(decimals: Int): String = String.format("%.${decimals}f", this)
}
