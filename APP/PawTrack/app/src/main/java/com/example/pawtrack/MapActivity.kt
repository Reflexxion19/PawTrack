package com.example.pawtrack

import android.Manifest
import android.annotation.SuppressLint
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
import android.content.Intent
import android.widget.Button
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.osmdroid.views.overlay.Marker
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class MapActivity : AppCompatActivity(), OverpassQueryTask.OverpassQueryListener {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var mapView: MapView
    private lateinit var overpassQueryTask: OverpassQueryTask
    private var currentLocation = GeoPoint(0, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.map

        val btnParks = findViewById<Button>(R.id.btnParks)
        btnParks.setOnClickListener {
            searchForParks()
        }

        val btnShops = findViewById<Button>(R.id.btnPetShops)
        btnShops.setOnClickListener {
            searchForShops()
        }

        val btnVets = findViewById<Button>(R.id.btnVets)
        btnVets.setOnClickListener {
            searchForVets()
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    val intent = Intent(applicationContext, HomePageActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.map -> {
                    val intent = Intent(applicationContext, MapActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.tracking -> {
                    val intent = Intent(applicationContext, TrackingActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.statistics -> {
                    val intent = Intent(applicationContext, StatisticsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.subscription -> {
                    val intent = Intent(applicationContext, SubscriptionActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
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

    private fun searchForParks() {
        overpassQueryTask = OverpassQueryTask(this, R.drawable.park_marker)
        val parkURL = "http://overpass-api.de/api/interpreter?data=[out:json];\n" +
                "(\n" +
                "  node[leisure=park](around:5000,${currentLocation.latitude},${currentLocation.longitude});\n" +
                "  node[leisure=playground](around:5000,${currentLocation.latitude},${currentLocation.longitude});\n" +
                "  node[leisure=garden](around:5000,${currentLocation.latitude},${currentLocation.longitude});\n" +
                ");\n" +
                "out;\n"
        @Suppress("DEPRECATION")
        overpassQueryTask.execute(Pair(parkURL, R.drawable.park_marker))
    }
    private fun searchForShops(){
        overpassQueryTask = OverpassQueryTask(this, R.drawable.shop_marker)
        val shopURL = "http://overpass-api.de/api/interpreter?data=[out:json];node[shop=pet](around:5000,${currentLocation.latitude},${currentLocation.longitude});out;"
        @Suppress("DEPRECATION")
        overpassQueryTask.execute( Pair(shopURL, R.drawable.shop_marker))
        // Add marker to current location
    }
    private fun searchForVets() {
        // Initialize OverpassQueryTask to find veterinaries
        overpassQueryTask = OverpassQueryTask(this, R.drawable.vet_marker)
        val VetUrl =
            "http://overpass-api.de/api/interpreter?data=[out:json];node[amenity=veterinary](around:5000,${currentLocation.latitude},${currentLocation.longitude});out;"
        @Suppress("DEPRECATION")
        overpassQueryTask.execute(Pair(VetUrl, R.drawable.vet_marker))
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
            this.currentLocation = GeoPoint(location.latitude,location.longitude)
            mapController.setCenter(currentLocation)
            mapController.setZoom(16.0)





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

        // Set custom marker icon
        marker.icon = ContextCompat.getDrawable(this, R.drawable.marker_icon)

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

    override fun onPlaceFound(placeInfo: JSONObject, ic: Int ) {
        val lat = placeInfo.getDouble("lat")
        val lon = placeInfo.getDouble("lon")
        val name = placeInfo.getJSONObject("tags").optString("name", "")
        val Marker = Marker(mapView)
        Marker.position = GeoPoint(lat, lon)
        Marker.title = placeInfo.optString("display_name", name)
        Marker.icon = ContextCompat.getDrawable(this, ic)
        mapView.overlays.add(Marker)
        mapView.invalidate()
    }

    //Cia, kad back buttonas nenumestu userio atgal dviem ekranam atgal
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val intent = Intent(applicationContext, HomePageActivity::class.java)
        startActivity(intent)
        finish()

    }
}
