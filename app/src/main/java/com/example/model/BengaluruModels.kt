package com.example.model

import androidx.compose.ui.geometry.Offset

enum class ServiceType(val label: String, val iconEmoji: String) {
    ALL("All Services", "🔍"),
    HOSPITAL("Hospitals", "🏥"),
    FUEL("Fuel & EV", "⛽"),
    RESTAURANT("Food & Rest", "🍽️"),
    PHARMACY("Pharmacies", "💊")
}

data class BengaluruLocation(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val area: String,
    val landmark: String = ""
)

data class RoutePoint(
    val lat: Double,
    val lng: Double
)

data class RouteNavigationStep(
    val stepIndex: Int,
    val instruction: String,
    val distanceKm: Double,
    val iconType: String // STRAIGHT, TURN_LEFT, TURN_RIGHT, MERGE, ROUNDABOUT, ARRIVE
)

data class UiRouteOption(
    val routeId: String,
    val name: String,
    val distanceKm: Double,
    val durationMin: Int,
    val predictedSpeedKmph: Double,
    val trafficLevel: String, // low, moderate, medium, high, heavy
    val trafficScore: Double,
    val isFastest: Boolean,
    val waypoints: List<RoutePoint>,
    val steps: List<String>,
    val hospitalsCount: Int = 3,
    val fuelCount: Int = 3,
    val restaurantsCount: Int = 4,
    val pharmaciesCount: Int = 2
)

data class UiServiceLocation(
    val id: String,
    val type: ServiceType,
    val name: String,
    val lat: Double,
    val lng: Double,
    val distanceFromRouteKm: Double,
    val area: String,
    val contact: String,
    val is24x7: Boolean = false,
    val tag: String = ""
)

data class UiTrafficIncident(
    val id: Int,
    val type: String, // accident, roadblock, congestion
    val location: String,
    val severity: String, // high, medium, low
    val lat: Double,
    val lng: Double,
    val description: String,
    val delayMinutes: Int = 12,
    val timeAgo: String = "15m ago"
)

// Default Bengaluru Hotspots (matching web app & backend)
val BENGALURU_HOTSPOTS = listOf(
    BengaluruLocation("silk_board", "Silk Board Junction", 12.9165, 77.6230, "Hosur Road", "Major Interchange Hub"),
    BengaluruLocation("ecity", "Electronic City", 12.8456, 77.6632, "South Tech Corridor", "Elevated Tollway End"),
    BengaluruLocation("koramangala", "Koramangala Sony Signal", 12.9352, 77.6245, "Koramangala 4th Block", "Commercial Core"),
    BengaluruLocation("mg_road", "MG Road Metro", 12.9716, 77.5946, "CBD Central", "Trinity Metro Circle"),
    BengaluruLocation("indiranagar", "Indiranagar 100ft Road", 12.9784, 77.6408, "East Bengaluru", "CMH Metro Corridor"),
    BengaluruLocation("whitefield", "Whitefield ITPL", 12.9698, 77.7500, "East Tech Zone", "Hope Farm Junction"),
    BengaluruLocation("hebbal", "Hebbal Flyover", 13.0358, 77.5970, "North Bengaluru", "Airport Expressway Gate"),
    BengaluruLocation("marathahalli", "Marathahalli Bridge", 12.9591, 77.6974, "Outer Ring Road", "HAL Heritage Gate"),
    BengaluruLocation("btm", "BTM Layout Ring Road", 12.9166, 77.6101, "South Bengaluru", "Udupi Garden Junction"),
    BengaluruLocation("basavanagudi", "Basavanagudi Gandhi Bazaar", 12.9406, 77.5738, "Old Bengaluru", "Bull Temple Road")
)

// Default Verified Services along routes (matching web app services DB)
val BENGALURU_SERVICES_CATALOG = listOf(
    UiServiceLocation("h1", ServiceType.HOSPITAL, "Apollo Hospital - Bannerghatta", 12.9140, 77.6220, 0.7, "Bannerghatta Rd", "080-26304050", true, "Level 1 Trauma Care"),
    UiServiceLocation("h2", ServiceType.HOSPITAL, "Fortis Hospital - Bannerghatta Road", 12.9352, 77.6245, 1.2, "Bannerghatta Rd", "080-66214444", true, "Cardiac Emergency 24x7"),
    UiServiceLocation("h3", ServiceType.HOSPITAL, "Manipal Hospital - HAL Airport Road", 12.9200, 77.6100, 1.8, "HAL Airport Rd", "080-25024444", true, "Multispeciality Emergency"),
    UiServiceLocation("h4", ServiceType.HOSPITAL, "Sakra World Hospital - Marathahalli", 12.9600, 77.6500, 1.5, "Outer Ring Rd", "080-49694969", true, "Advanced Neuro & Trauma"),
    UiServiceLocation("h5", ServiceType.HOSPITAL, "Columbia Asia - Whitefield", 12.9450, 77.6350, 2.1, "Whitefield", "080-61656262", true, "24x7 Emergency"),
    UiServiceLocation("h6", ServiceType.HOSPITAL, "St. John's Medical College", 12.9250, 77.6180, 0.9, "Sarjapur Rd", "080-22065000", true, "Central Hospital"),

    UiServiceLocation("f1", ServiceType.FUEL, "Shell Petrol Bunk - Koramangala", 12.9350, 77.6280, 0.6, "Koramangala", "Open 24/7", false, "V-Power + EV Supercharger"),
    UiServiceLocation("f2", ServiceType.FUEL, "HP Petrol Pump - MG Road", 12.9300, 77.6200, 0.9, "MG Road", "Open 24/7", false, "High-Speed Diesel / CNG"),
    UiServiceLocation("f3", ServiceType.FUEL, "Indian Oil Station - Brigade Road", 12.9250, 77.6150, 1.1, "Brigade Rd", "Open 24/7", false, "XP95 + Air Check"),
    UiServiceLocation("f4", ServiceType.FUEL, "Bharat Petroleum - Indiranagar", 12.9550, 77.6400, 1.3, "100ft Road", "Open 24/7", false, "Speed 97 + Clean Restroom"),
    UiServiceLocation("f5", ServiceType.FUEL, "Reliance Petrol - Electronic City", 12.8950, 77.5900, 1.7, "Hosur Rd", "Open 24/7", false, "Jio-bp Pulse EV Fast Charge"),
    UiServiceLocation("f6", ServiceType.FUEL, "Shell - Marathahalli", 12.9570, 77.7020, 0.8, "Outer Ring Rd", "Open 24/7", false, "Deli by Shell + EV DC Fast"),

    UiServiceLocation("r1", ServiceType.RESTAURANT, "Truffles Cafe - Koramangala", 12.9280, 77.6240, 0.5, "Koramangala 5th Block", "080-41466565", false, "Burgers & Shakes"),
    UiServiceLocation("r2", ServiceType.RESTAURANT, "Empire Restaurant - Church Street", 12.9150, 77.6120, 0.8, "Church St", "080-40414041", true, "Late Night Dining"),
    UiServiceLocation("r3", ServiceType.RESTAURANT, "MTR Restaurant - Lalbagh", 12.9050, 77.5950, 1.2, "Lalbagh Rd", "080-22220022", false, "Heritage South Indian"),
    UiServiceLocation("r4", ServiceType.RESTAURANT, "Vidyarthi Bhavan - Basavanagudi", 12.9100, 77.5800, 0.9, "Gandhi Bazaar", "080-26677588", false, "Iconic Masala Dosa"),
    UiServiceLocation("r5", ServiceType.RESTAURANT, "Toit Brewpub - Indiranagar", 12.9720, 77.6400, 1.4, "100ft Rd", "09019713388", false, "Woodfired Pizza & Food"),
    UiServiceLocation("r6", ServiceType.RESTAURANT, "Brahmin's Coffee Bar", 12.9430, 77.5730, 1.6, "Shankarapuram", "09845030234", false, "Filter Coffee & Idli"),

    UiServiceLocation("p1", ServiceType.PHARMACY, "Apollo Pharmacy - MG Road", 12.9220, 77.6180, 0.4, "MG Road", "1860-500-0101", true, "24x7 Prescription Counter"),
    UiServiceLocation("p2", ServiceType.PHARMACY, "MedPlus - Brigade Road", 12.9280, 77.6200, 0.7, "Brigade Rd", "080-67006700", true, "Generic & Surgical"),
    UiServiceLocation("p3", ServiceType.PHARMACY, "Wellness Forever - Koramangala", 12.9300, 77.6250, 0.6, "Koramangala", "080-48529900", true, "24x7 Day & Night Pharmacy"),
    UiServiceLocation("p4", ServiceType.PHARMACY, "1mg - Indiranagar", 12.9680, 77.6420, 1.1, "Indiranagar", "080-45678900", false, "Express Prescription")
)

// Default Active Incidents
val BENGALURU_ACTIVE_INCIDENTS = listOf(
    UiTrafficIncident(
        id = 1,
        type = "accident",
        location = "Silk Board Junction",
        severity = "high",
        lat = 12.9165,
        lng = 77.6230,
        description = "Multi-vehicle collision on flyover approach causing 25 min delays",
        delayMinutes = 25,
        timeAgo = "8m ago"
    ),
    UiTrafficIncident(
        id = 2,
        type = "roadblock",
        location = "Electronic City Toll Gate",
        severity = "medium",
        lat = 12.8456,
        lng = 77.6632,
        description = "Maintenance lane closure on northbound expressway",
        delayMinutes = 14,
        timeAgo = "22m ago"
    ),
    UiTrafficIncident(
        id = 3,
        type = "congestion",
        location = "Marathahalli Bridge",
        severity = "high",
        lat = 12.9591,
        lng = 77.6974,
        description = "Water-logging and heavy bottleneck heading toward Whitefield",
        delayMinutes = 18,
        timeAgo = "35m ago"
    )
)
