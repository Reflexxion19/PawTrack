package com.example.pawtrack

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.pawtrack.Map.MapActivity
import com.example.pawtrack.Pet.PetProfileActivity
import com.example.pawtrack.Tracking.StatisticsActivity
import com.example.pawtrack.Tracking.TrackingActivity
import com.example.pawtrack.User.SubscriptionActivity
import com.example.pawtrack.User.UserProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

class HomePageActivity: AppCompatActivity() {
    private var parsedList: MutableList<Map<String, String?>> = mutableListOf()
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_layout)
        sharedPreferences = getSharedPreferences("PawTrackPrefs", Context.MODE_PRIVATE)
        val pet_id = sharedPreferences.getString("LastSelectedPetId", null)
        val username = sharedPreferences.getString("USERNAME", null)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.home

        loadOsmdroidConfig()
        performGetRequest(username, pet_id)
        performGetRequest(object : StatisticsActivity.OnDataFetched {
            override fun onDataFetched(parsedList: List<Map<String, String?>>) {
            }
        }, pet_id)

        val CurrentTime = findViewById<TextView>(R.id.textView)
        CurrentTime.text = getCurrentTime()

        val petProfileButton = findViewById<FloatingActionButton>(R.id.pet_profile)
        petProfileButton.setOnClickListener(){
            val intent = Intent(applicationContext, PetProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        val profileButton = findViewById<FloatingActionButton>(R.id.floatingActionButton2)
        profileButton.setOnClickListener(){
            val intent = Intent(applicationContext, UserProfileActivity::class.java)
            startActivity(intent)
            finish()
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
    fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE, dd MMM")
        dateFormat.timeZone = TimeZone.getDefault()
        return dateFormat.format(calendar.time)
    }
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {

    }

    private fun loadOsmdroidConfig() {
        val osmdroidBasePath = File(Environment.getExternalStorageDirectory(), "osmdroid")
        val osmdroidTileCacheDir = File(osmdroidBasePath, "tiles")

        if (!osmdroidBasePath.exists()) osmdroidBasePath.mkdirs()
        if (!osmdroidTileCacheDir.exists()) osmdroidTileCacheDir.mkdirs()

        Configuration.getInstance().osmdroidBasePath = osmdroidBasePath
        Configuration.getInstance().osmdroidTileCache = osmdroidTileCacheDir

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun performGetRequest(username: String?, pet_id: String?) {
        Log.d("MainActivity", username.toString() + pet_id.toString())
        if (pet_id.isNullOrEmpty()) {
            runOnUiThread {
                val intent = Intent(applicationContext, PetProfileActivity::class.java)
                startActivity(intent)
                finish()
            }
            return
        }

        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        Log.d("GetReq", "Current month: $month")

        val httpUrl = HttpUrl.Builder()
            .scheme("https")
            .host("pvp.seriouss.am")
            .addQueryParameter("type", "g_a_r_i")
            .addQueryParameter("p_i", pet_id)
            .addQueryParameter("m", month.toString())
            .addQueryParameter("y", year.toString())
            .build()
        Log.d("GetReq", "$httpUrl")
        val request = Request.Builder()
            .url(httpUrl)
            .get()
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseBodyString = response.body?.string() ?: ""
                    val newParsedList = parseResponseToList(responseBodyString)
                    runOnUiThread {
                        parsedList.clear()
                        parsedList.addAll(newParsedList)
                        performGetLongLatRequest()
                    }
                }
            }
        })
    }
    private fun performGetLongLatRequest() {
        if (parsedList.isNotEmpty()) {
            val map = parsedList.first()
            val httpUrl = HttpUrl.Builder()
                .scheme("https")
                .host("pvp.seriouss.am")
                .addQueryParameter("type", "g_l_p")
                .addQueryParameter("a_r_i", map["id"])
                .build()
            Log.d("GetReq", "$httpUrl")
            val request = Request.Builder()
                .url(httpUrl)
                .get()
                .build()

            val client = OkHttpClient()
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                    }
                    e.printStackTrace()
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val pointsList = parsePoints(responseBody)
                        runOnUiThread { createCardViewWithMap(pointsList) }
                    }
                }
            })
        }
    }
    private fun parsePoints(jsonString: String?): List<GeoPoint> {
        val pointsList = mutableListOf<GeoPoint>()
        jsonString?.let {
            val lines = it.split("\n")
            for (line in lines) {
                if (line.isNotBlank()) {
                    val latLong = line.split(";")
                    if (latLong.size == 2) {
                        val lat = latLong[0].split("=")[1].toDoubleOrNull()
                        val long = latLong[1].split("=")[1].toDoubleOrNull()
                        if (lat != null && long != null) {
                            pointsList.add(GeoPoint(lat, long))
                        }
                    }
                }
            }
        }
        return pointsList
    }

    private fun createCardViewWithMap(geoPoints: List<GeoPoint>) {
        if (geoPoints.isEmpty()) {
            return
        }

        val cardView = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.setMargins(30, 50, 30, 60) }
            cardElevation = 4f
            radius = 30f
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.edit_text_background))
        }

        val mapView = MapView(this).apply {
            id = View.generateViewId()
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                600
            )
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            isClickable = true
        }

        setupDynamicMap(mapView, geoPoints)

        cardView.addView(mapView)
        findViewById<LinearLayout>(R.id.cardContainer).addView(cardView)
    }

    private fun setupDynamicMap(mapView: MapView, geoPoints: List<GeoPoint>) {
        mapView.apply {
            setBuiltInZoomControls(false)
            setMultiTouchControls(true)
            controller.setCenter(geoPoints.first())
            controller.setZoom(15)
            val polyline = Polyline().also { line ->
                line.setPoints(geoPoints)
                overlays.add(line)
            }

            val boundingBox = BoundingBox.fromGeoPoints(geoPoints)
            controller.setCenter(boundingBox.centerWithDateLine)
            controller.zoomToSpan(boundingBox.latitudeSpan, boundingBox.longitudeSpan)
            controller.setZoom(15)
        }
    }
    private fun parseResponseToList(response: String): List<Map<String, String?>> {
        return response.split("\n")
            .filter { it.isNotBlank() }
            .map { id ->
                mapOf("id" to id.trim())
            }
    }

    private fun performGetRequest(onDataFetched: StatisticsActivity.OnDataFetched, pet_id: String?) {
        val httpUrl = HttpUrl.Builder()
            .scheme("https")
            .host("pvp.seriouss.am")
            .addQueryParameter("type", "g_s") //get statistics
            .addQueryParameter("p", pet_id)
            .build()

        val request = Request.Builder()
            .url(httpUrl)
            .get()
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseBodyString = response.body?.string() ?: ""
                    val parsedList = parseStatisticsResponseToList(responseBodyString)
                    runOnUiThread {
                        parsedList.forEach { map ->
                            Log.d("StatisticsActivity", "Map Data: $map")
                        }
                        val rootView = findViewById<View>(R.id.statistics)
                        onDataFetched.onDataFetched(parsedList)
                        updateTextViewsWithData(parsedList.first())
                        updateCircularProgressBars(parsedList)
                    }
                }
            }
        })
    }
    private fun parseStatisticsResponseToList(response: String): List<Map<String, String?>> {
        return response.split("\n")
            .mapNotNull { line ->
                if (line.isNotBlank()) {
                    line.split(";")
                        .mapNotNull { entry ->
                            val parts = entry.split("=")
                            if (parts.size == 2) {
                                val key = parts[0].trim()
                                val value = parts[1].trim().ifEmpty { null }
                                when (key) {
                                    "c_b" -> "calories_burned" to value
                                    "d_w" -> "distance_walked" to value
                                    else -> null
                                }
                            } else {
                                null
                            }
                        }
                        .toMap()
                        .takeIf { it.isNotEmpty() }
                } else {
                    null
                }
            }
    }
    fun updateTextViewsWithData(data: Map<String, String?>) {

        val caloriesBurnedTextView: TextView = findViewById(R.id.textView10)
        val distanceWalkedTextView: TextView = findViewById(R.id.textView14)
        caloriesBurnedTextView.text = (data["calories_burned"] + "/200 kcal") ?: "0"
        distanceWalkedTextView.text = (data["distance_walked"] + " km")  ?: "0"


    }
    fun updateCircularProgressBars(dataList: List<Map<String, String?>>) {
        val today = Calendar.getInstance()
        val dayOfWeekNumber = today.get(Calendar.DAY_OF_WEEK)
        val progressBarIds = listOf(R.id.progressBar)

        progressBarIds.forEachIndexed { index, progressBarId ->
            if(index == dayOfWeekNumber)
            {
                val mainProgressBar = findViewById<CircularProgressBar>(R.id.progressBar9)
                val caloriesBurned = dataList.getOrNull(index)?.get("calories_burned")?.toIntOrNull() ?: 0
                mainProgressBar.progress = caloriesBurned.toFloat()
            }
            val progressBar = findViewById<CircularProgressBar>(progressBarId)
            val caloriesBurned = dataList.getOrNull(index)?.get("calories_burned")?.toIntOrNull() ?: 0

            progressBar.apply {
                if(progress >= 200)
                {
                    progressBar.progressBarColor = Color.GREEN
                }
                progress = caloriesBurned.toFloat()
            }
        }
    }
}