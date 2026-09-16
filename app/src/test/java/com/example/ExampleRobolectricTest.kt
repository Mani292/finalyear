package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ml.BiLSTMEngine
import com.example.model.CongestionLevel
import androidx.room.Room
import com.example.data.FavoriteJunctionEntity
import com.example.data.TrafficSenseDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TrafficSense AI", appName)
  }

  @Test
  fun `test Room database favorite junctions CRUD`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, TrafficSenseDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    val dao = db.trafficDao()

    try {
      // 1. Initial table should be empty
      val initial = dao.getAllFavorites().first()
      assertEquals(0, initial.size)
      assertEquals(false, dao.isFavorite("Swargate"))

      // 2. Insert favorite junction
      val favorite = FavoriteJunctionEntity(
        junctionId = "Swargate",
        displayName = "Swargate Chowk",
        defaultCamera = "s1",
        defaultDirection = "NORTH",
        note = "Major BRTS junction"
      )
      dao.insertFavorite(favorite)

      // 3. Verify insertion
      val listAfterInsert = dao.getAllFavorites().first()
      assertEquals(1, listAfterInsert.size)
      assertEquals("Swargate", listAfterInsert[0].junctionId)
      assertEquals("Swargate Chowk", listAfterInsert[0].displayName)
      assertEquals(true, dao.isFavorite("Swargate"))

      // 4. Delete favorite junction
      dao.deleteFavorite("Swargate")
      val listAfterDelete = dao.getAllFavorites().first()
      assertEquals(0, listAfterDelete.size)
      assertEquals(false, dao.isFavorite("Swargate"))
    } finally {
      db.close()
    }
  }

  @Test
  fun `test BiLSTM engine inference and alert generation`() {
    val sampleVolumes = listOf(
      80f, 85f, 90f, 92f, 95f, 100f, 110f, 115f, 120f, 130f, 140f, 155f
    )
    val result = BiLSTMEngine.predictNext5Min(
      location = "AlankarChowk",
      camera = "a2",
      direction = "UP",
      recentVolumes = sampleVolumes,
      lowThreshold = 60f,
      modThreshold = 120f,
      highThreshold = 180f,
      surgeThresholdPct = 25f
    )

    assertTrue("Predicted volume should be positive", result.predictedVolume > 0f)
    assertEquals("Lookback should be 60 minutes", 60, result.lookbackMinutes)
    assertNotNull("Congestion level should not be null", result.congestionLevel)
  }

  @Test
  fun `test congestion level classification thresholds`() {
    val low = CongestionLevel.fromVolume(45f, 60f, 120f, 180f)
    val mod = CongestionLevel.fromVolume(90f, 60f, 120f, 180f)
    val high = CongestionLevel.fromVolume(150f, 60f, 120f, 180f)
    val severe = CongestionLevel.fromVolume(210f, 60f, 120f, 180f)

    assertEquals(CongestionLevel.LOW, low)
    assertEquals(CongestionLevel.MODERATE, mod)
    assertEquals(CongestionLevel.HIGH, high)
    assertEquals(CongestionLevel.SEVERE, severe)
  }

  @Test
  fun `test BengaluruRouteEngine generates multiple alternative routes with waypoints and services`() {
    val origin = com.example.model.BENGALURU_HOTSPOTS.first { it.id == "silk_board" }
    val destination = com.example.model.BENGALURU_HOTSPOTS.first { it.id == "mg_road" }

    val routes = com.example.ml.BengaluruRouteEngine.generateRouteOptions(origin, destination)
    assertTrue("Should generate at least 2 alternative routes", routes.size >= 2)
    assertTrue("Each route must have at least 2 waypoints", routes.all { it.waypoints.size >= 2 })
    assertTrue("At least one route marked as fastest", routes.any { it.isFastest })

    // Test services filtering
    val selectedRoute = routes.first()
    val allServices = com.example.ml.BengaluruRouteEngine.findServicesNearRoute(selectedRoute, com.example.model.ServiceType.ALL)
    assertTrue("Should find services along route", allServices.isNotEmpty())

    val hospitalServices = com.example.ml.BengaluruRouteEngine.findServicesNearRoute(selectedRoute, com.example.model.ServiceType.HOSPITAL)
    assertTrue("All filtered items must be hospitals", hospitalServices.all { it.type == com.example.model.ServiceType.HOSPITAL })
  }

  @Test
  fun `test real-time junctions dataset and live location state`() {
    val junctions = com.example.ui.components.REAL_TIME_BENGALURU_JUNCTIONS
    assertTrue("Should contain real-time Bengaluru junctions", junctions.size >= 5)
    assertTrue("All junctions must have valid coordinates", junctions.all { it.lat in 12.0..14.0 && it.lng in 77.0..78.0 })
    assertTrue("All junctions have positive vehicle volume flow", junctions.all { it.liveVolumeVehicles > 0 })

    val userLoc = com.example.location.UserLiveLocation(
      latitude = 12.9352,
      longitude = 77.6245,
      accuracyMeters = 8.5f,
      speedKmph = 32.0f,
      areaName = "Koramangala 4th Block",
      fullAddress = "80ft Road, Koramangala, Bengaluru",
      isRealTimeGps = true
    )
    assertEquals(12.9352, userLoc.latitude, 0.0001)
    assertEquals("Koramangala 4th Block", userLoc.areaName)
    assertTrue(userLoc.isRealTimeGps)
  }

  @Test
  fun `test TravelTimeApiClient point-in-polygon ray-casting algorithm`() {
    val polygonSquare = listOf(
      com.google.android.gms.maps.model.LatLng(12.90, 77.60),
      com.google.android.gms.maps.model.LatLng(12.90, 77.70),
      com.google.android.gms.maps.model.LatLng(13.00, 77.70),
      com.google.android.gms.maps.model.LatLng(13.00, 77.60)
    )

    val insidePoint = com.google.android.gms.maps.model.LatLng(12.95, 77.65)
    val outsidePoint = com.google.android.gms.maps.model.LatLng(13.05, 77.75)

    val isInside = com.example.network.TravelTimeApiClient.isPointInPolygon(insidePoint, polygonSquare)
    val isOutside = com.example.network.TravelTimeApiClient.isPointInPolygon(outsidePoint, polygonSquare)

    assertTrue("Point (12.95, 77.65) should be inside polygon", isInside)
    assertTrue("Point (13.05, 77.75) should be outside polygon", !isOutside)
  }
}
