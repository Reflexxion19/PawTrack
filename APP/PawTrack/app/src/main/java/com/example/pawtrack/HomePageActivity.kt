package com.example.pawtrack

import android.annotation.SuppressLint
import android.content.Intent
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

class HomePageActivity: AppCompatActivity() {
    private var map: MapView? = null
    private var myLocationOverlay: MyLocationNewOverlay? = null
    private var parsedList: MutableList<Map<String, String?>> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_layout)
        val username = intent.getStringExtra("USERNAME")
        val pet_id = intent.getStringExtra("PET_ID")
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.home

        loadOsmdroidConfig()
        performGetRequest(username, pet_id)

        val CurrentTime = findViewById<TextView>(R.id.textView)
        CurrentTime.text = getCurrentTime()

        val petProfileButton = findViewById<FloatingActionButton>(R.id.pet_profile)
        petProfileButton.setOnClickListener(){
            val intent = Intent(applicationContext, PetProfileActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
            finish()
        }

        val profileButton = findViewById<FloatingActionButton>(R.id.floatingActionButton2)
        profileButton.setOnClickListener(){
            val intent = Intent(applicationContext, UserProfileActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
            finish()
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    val intent = Intent(applicationContext, HomePageActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    intent.putExtra("PET_ID", pet_id)
                    startActivity(intent)
                    true
                }
                R.id.map -> {
                    val intent = Intent(applicationContext, MapActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    intent.putExtra("PET_ID", pet_id)
                    startActivity(intent)
                    true
                }
                R.id.tracking -> {
                    val intent = Intent(applicationContext, TrackingActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    intent.putExtra("PET_ID", pet_id)
                    startActivity(intent)
                    true
                }
                R.id.statistics -> {
                    val intent = Intent(applicationContext, StatisticsActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    intent.putExtra("PET_ID", pet_id)
                    startActivity(intent)
                    true
                }
                R.id.subscription -> {
                    val intent = Intent(applicationContext, SubscriptionActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    intent.putExtra("PET_ID", pet_id)
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
        if (pet_id.isNullOrEmpty()) {
            runOnUiThread {
                Toast.makeText(applicationContext, "Select a pet first", Toast.LENGTH_SHORT).show()
                val intent = Intent(applicationContext, PetProfileActivity::class.java)
                intent.putExtra("USERNAME", username)
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
                        Log.d("GetReq", parsedList.toString())
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
                        Log.d("GetReq", pointsList.toString())
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
            Log.e("RouteActivity", "GeoPoint list is empty, cannot create map view.")
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
            Log.d("GetReq", geoPoints.toString())
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
}