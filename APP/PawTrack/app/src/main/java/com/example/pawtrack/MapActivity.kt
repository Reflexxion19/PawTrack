package com.example.pawtrack

import android.Manifest
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import android.location.LocationManager
import android.content.Context
import org.osmdroid.views.overlay.Marker
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MapActivity : AppCompatActivity(), OverpassQueryTask.OverpassQueryListener {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var mapView: MapView
    private lateinit var overpassQueryTask: OverpassQueryTask

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        mapView = findViewById<MapView>(R.id.map)

        // Request location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission has been granted
            initializeMapWithLocation()
        }
    }

    private fun initializeMapWithLocation() {
        // Set the map tile source
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)

        // Enable the built-in zoom controls
        mapView.setBuiltInZoomControls(true)

        // Get the map controller
        val mapController = mapView.controller

        // Get the current location using LocationManager
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the missing permissions from the user
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            null // Return null for the location until the permissions are granted
        } else {
            // Permission is granted, proceed with accessing the location
            // For example, obtain the location using LocationManager
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }

        // If location is not null, set the map center to the current location
        location?.let {
            /*:TODO replce the mock data - >>> */
            val currentLocation = GeoPoint(54.922831,23.901035)
            mapController.setCenter(currentLocation)
            mapController.setZoom(16.0)

            // Initialize OverpassQueryTask to find veterinaries
            overpassQueryTask = OverpassQueryTask(this)
            val VetUrl = "http://overpass-api.de/api/interpreter?data=[out:json];node[amenity=veterinary](around:1000,54.922831,23.901035);out;"
            @Suppress("DEPRECATION")
            overpassQueryTask.execute(VetUrl )


            overpassQueryTask = OverpassQueryTask(this)
            val parkURL = "http://overpass-api.de/api/interpreter?data=[out:json];node[amenity=park](around:1000,54.922831,23.901035);out;"
            @Suppress("DEPRECATION")
            overpassQueryTask.execute(parkURL)
            // Add marker to current location
            addMarkerToCurrentLocation(currentLocation)
        }
    }
    private fun fetchPlaceInfo(url: String): JSONObject? {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            val inputStream = connection.inputStream
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val response = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                response.append(line)
            }
            bufferedReader.close()
            inputStream.close()
            return JSONObject(response.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection.disconnect()
        }
        return null
    }
    private fun addMarkerToCurrentLocation(currentLocation: GeoPoint) {
        val marker = Marker(mapView)
        marker.position = currentLocation

        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

        mapView.overlays.add(marker)
        mapView.invalidate()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, initialize map with location
                initializeMapWithLocation()
            } else {
                // Permission denied, handle accordingly

            }
        }
    }

    override fun onResume() {
        super.onResume()
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))
    }

    override fun onPause() {
        super.onPause()
        Configuration.getInstance().save(applicationContext, getPreferences(MODE_PRIVATE))
    }

    override fun onPlaceFound(placeInfo: JSONObject) {
        val lat = placeInfo.getDouble("lat")
        val lon = placeInfo.getDouble("lon")
        val name = placeInfo.getJSONObject("tags").optString("name", "")
        val Marker = Marker(mapView)
        Marker.position = GeoPoint(lat, lon)
        Marker.title = placeInfo.optString("display_name", name)
        mapView.overlays.add(Marker)
        mapView.invalidate()
    }

}
