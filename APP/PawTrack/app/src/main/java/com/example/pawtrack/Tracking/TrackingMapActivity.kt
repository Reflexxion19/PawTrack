package com.example.pawtrack.Tracking

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pawtrack.HomePageActivity
import com.example.pawtrack.Map.MapActivity
import com.example.pawtrack.Pet.PetProfileActivity
import com.example.pawtrack.R
import com.example.pawtrack.User.SubscriptionActivity
import com.example.pawtrack.User.UserProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Timer
import kotlin.concurrent.timerTask
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class CustomLocationOverlay(provider: IMyLocationProvider, map: MapView, private val updatePathCallback: (GeoPoint) -> Unit) : MyLocationNewOverlay(provider, map) {
    override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
        super.onLocationChanged(location, source)
        location?.let {
            val geoPoint = GeoPoint(location.latitude, location.longitude)
            updatePathCallback(geoPoint)
        }
    }
}

data class TimedGeoPoint(
    val geoPoint: GeoPoint,
    val timestamp: Long
)

class TrackingMapActivity : AppCompatActivity() {

    private var map: MapView? = null
    private var myLocationOverlay: MyLocationNewOverlay? = null
    private var pathOverlay: Polyline? = null
    private var isTracking = false
    private var startGeoPoint: GeoPoint? = null
    private var endGeoPoint: GeoPoint? = null
    private var timer: Timer? = null
    private var startTime = 0L
    private var endTime = 0L
    private val pathPoints = mutableListOf<TimedGeoPoint>()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tracking_map_layout)
        sharedPreferences = getSharedPreferences("PawTrackPrefs", Context.MODE_PRIVATE)
        val pet_id = sharedPreferences.getString("LastSelectedPetId", null)
        val username = sharedPreferences.getString("USERNAME", null)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.tracking
        setupMap()

        val petprofileButton = findViewById<FloatingActionButton>(R.id.pet_profile)
        petprofileButton.setOnClickListener {
            val intent = Intent(applicationContext, PetProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        val profileButton = findViewById<FloatingActionButton>(R.id.floatingActionButton2)
        profileButton.setOnClickListener {
            val intent = Intent(applicationContext, UserProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        val startButton = findViewById<Button>(R.id.btnPetStart)
        startButton.setOnClickListener {
            toggleTracking(startButton, pet_id)
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
    }

    private fun setupMap() {
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        map = findViewById(R.id.map)
        map?.setTileSource(TileSourceFactory.MAPNIK)
        map?.setBuiltInZoomControls(false)
        map?.setMultiTouchControls(true)

        val mapController = map?.controller
        mapController?.setZoom(18.0)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            enableLocationTracking()
            setupPathOverlay()
        }
    }

    private fun enableLocationTracking() {
        val locationOverlay =
            map?.let {
                CustomLocationOverlay(GpsMyLocationProvider(this), it) { location ->
                    updatePath(location)
                }
            }
        map?.overlays?.add(locationOverlay)
        locationOverlay?.enableMyLocation()
        locationOverlay?.enableFollowLocation()
        myLocationOverlay = locationOverlay
    }

    private fun setupPathOverlay() {
        pathOverlay = Polyline()
        pathOverlay?.color = ContextCompat.getColor(this, R.color.app_theme)
        map?.overlays?.add(pathOverlay)
    }

    private fun updatePath(location: GeoPoint) {
        if (isTracking) {
            val currentTime = System.currentTimeMillis()
            val timedGeoPoint = TimedGeoPoint(location, currentTime)
            pathOverlay?.addPoint(location)
            pathPoints.add(timedGeoPoint)
            map?.invalidate()
        }
    }

    private fun toggleTracking(startButton: Button, pet_id: String?) {
        isTracking = !isTracking

        if (isTracking) {
            startGeoPoint = myLocationOverlay?.myLocation
            pathOverlay?.points?.clear()
            pathPoints.clear()
            startTime = System.currentTimeMillis()
            myLocationOverlay?.enableFollowLocation()
            startLocationPolling()
        } else {
            endGeoPoint = myLocationOverlay?.myLocation
            endTime = System.currentTimeMillis()  // Save the end time
            myLocationOverlay?.disableFollowLocation()
            stopLocationPolling()
            // Redirect to Home Page after stopping
            val intent = Intent(applicationContext, HomePageActivity::class.java)
            startActivity(intent)
        }

        updateButtonAndIcon(startButton)
    }

    private fun startLocationPolling() {
        timer = Timer()
        timer?.schedule(timerTask {
            runOnUiThread {
                myLocationOverlay?.myLocation?.let {
                    updatePath(it)
                }
            }
        }, 0, 10000)
    }

    private fun stopLocationPolling() {
        timer?.cancel()
        timer = null
    }

    private fun updateButtonAndIcon(startButton: Button) {
        if (isTracking) {
            val stopIcon: Drawable? = ContextCompat.getDrawable(this, R.drawable.stop_icon)
            startButton.setCompoundDrawablesWithIntrinsicBounds(stopIcon, null, null, null)
            startButton.text = getString(R.string.map_stop)
        } else {
            val startIcon: Drawable? = ContextCompat.getDrawable(this, R.drawable.start_icon)
            startButton.setCompoundDrawablesWithIntrinsicBounds(startIcon, null, null, null)
            startButton.text = getString(R.string.map_start)
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun haversine(start: GeoPoint?, end: GeoPoint?): Double {
        if (start == null || end == null) {
            throw IllegalArgumentException("Start and end points must not be null")
        }

        val R = 6371.0 // Radius of the Earth in kilometers

        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.toRadians(end.latitude)
        val lon2 = Math.toRadians(end.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c // Distance in kilometers
    }

    private fun saveTrip(start: GeoPoint?, end: GeoPoint?, pet_id: String?) {
        try {
            val JSON = "application/json; charset=utf-8".toMediaType()
            val elapsedMillis = endTime - startTime
            val hours = elapsedMillis / 3600000
            val minutes = (elapsedMillis % 3600000) / 60000
            val seconds = (elapsedMillis % 60000) / 1000
            val a_t = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            val d_t = formatDate(startTime)

            val averageWeight = 75
            val distance = haversine(start, end)
            val calories = distance * averageWeight * 0.8

            val json = JSONObject()
            json.put("type", "r")
            json.put("dt", "$d_t")
            json.put("d_w", "$distance")
            json.put("c_b", "$calories")
            json.put("a_t", "$a_t")
            json.put("p", pet_id)

            Log.d("PostData", json.toString())
            val body: RequestBody = json.toString().toRequestBody(JSON)
            val request = Request.Builder()
                .url("https://pvp.seriouss.am")
                .post(body)
                .build()

            val client = OkHttpClient()
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    runOnUiThread {
                        if (response.isSuccessful) {
                            val responseString = response.body?.string()
                            try {
                                val postJson = JSONObject()
                                postJson.put("type", "g_d")
                                postJson.put("c", pathPoints.size)
                                if (responseString != null) {
                                    postJson.put("f_a_r", responseString.toInt())
                                }

                                pathPoints.forEachIndexed { index, timedGeoPoint ->
                                    val pointData = JSONArray().apply {
                                        put(timedGeoPoint.geoPoint.latitude)
                                        put(timedGeoPoint.geoPoint.longitude)
                                        put(formatDate(timedGeoPoint.timestamp))
                                    }
                                    postJson.put((index + 1).toString(), pointData)
                                }
                                Log.d("PostData", postJson.toString())

                                val client = OkHttpClient()
                                val mediaType = "application/json; charset=utf-8".toMediaType()
                                val requestBody = postJson.toString().toRequestBody(mediaType)
                                val request = Request.Builder()
                                    .url("https://pvp.seriouss.am")
                                    .post(requestBody)
                                    .build()

                                client.newCall(request).enqueue(object : okhttp3.Callback {
                                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                                        e.printStackTrace()
                                    }

                                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                                        val responseBody = response.body?.string()
                                        if (response.isSuccessful) {
                                            Log.d("PostData", "Successfully sent" + postJson.toString())
                                        } else {
                                            Log.d("PostData", responseBody.toString())
                                        }
                                    }
                                })

                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
