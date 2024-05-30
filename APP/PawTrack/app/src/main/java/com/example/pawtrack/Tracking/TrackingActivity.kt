package com.example.pawtrack.Tracking

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.pawtrack.HomePageActivity
import com.example.pawtrack.Map.MapActivity
import com.example.pawtrack.Pet.PetProfileActivity
import com.example.pawtrack.R
import com.example.pawtrack.User.SubscriptionActivity
import com.example.pawtrack.User.UserProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TrackingActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tracking_layout)
        sharedPreferences = getSharedPreferences("PawTrackPrefs", Context.MODE_PRIVATE)
        val pet_id = sharedPreferences.getString("LastSelectedPetId", null)
        val username = sharedPreferences.getString("USERNAME", null)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.tracking

        performStatisticsGetRequest(object : StatisticsActivity.OnDataFetched {
            override fun onDataFetched(parsedList: List<Map<String, String?>>) {
                // Handle the fetched data if needed
            }
        }, pet_id)

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

        val routeButton = findViewById<Button>(R.id.button5)
        routeButton.setOnClickListener {
            val intent = Intent(applicationContext, RouteActivity::class.java)
            startActivity(intent)
            finish()
        }

        val trackingButton = findViewById<Button>(R.id.button6)
        trackingButton.setOnClickListener {
            val intent = Intent(applicationContext, TrackingMapActivity::class.java)
            startActivity(intent)
            finish()
        }

        val trackingSwitch = findViewById<Switch>(R.id.trackingSwitch)
        trackingSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                trackingButton.isEnabled = false
                performTrackingSetPostRequest(1)
            } else {
                trackingButton.isEnabled = true
                performTrackingSetPostRequest(2)
            }
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
        performGetRequest(username, pet_id)
    }



    private fun performTrackingSetPostRequest(status : Int) {
        sharedPreferences = getSharedPreferences("PawTrackPrefs", Context.MODE_PRIVATE)
        val petPhoto = sharedPreferences.getString("LastSelectedPetProfile", null)
        val petName = sharedPreferences.getString("selectedPetName", null)
        val username = sharedPreferences.getString("USERNAME", null)
        val trackerID = sharedPreferences.getString("LastSelectedTrackerId", null)
        val category= sharedPreferences.getString("LastSelectedPetCategory", null)
        if (username.isNullOrEmpty()) {
            runOnUiThread {
                Toast.makeText(applicationContext, "Username is required", Toast.LENGTH_SHORT).show()
            }
            return
        }


            val jsonMediaType = "application/json; charset=utf-8".toMediaType()
            var json = """
            {
                "type":"p_u",
                "p_n":"$petName",
                "n_p_n":"$petName",
                "p_p":"$petPhoto",
                "t_i":"$trackerID",
                "t_s":"$status",
                "a_c":$category,
                "u_n":"$username"
            }
            """.trimIndent()

            Log.d("UpdatePetInfo", "$json")
            val body = json.toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url("https://pvp.seriouss.am")
                .post(body)
                .build()

            val client = OkHttpClient()
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Failed to fetch data", Toast.LENGTH_SHORT)
                            .show()
                    }
                    e.printStackTrace()
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    if (response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(
                                applicationContext,
                                "Status changed successfully",
                                Toast.LENGTH_LONG
                            ).show()
                           // val intent = Intent(applicationContext, PetProfileActivity::class.java)
                           // startActivity(intent)
                           // finish()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                applicationContext,
                                "Error editing pet information",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            })

    }

    private fun performStatisticsGetRequest(onDataFetched: StatisticsActivity.OnDataFetched, pet_id: String?) {
        val httpUrl = HttpUrl.Builder()
            .scheme("https")
            .host("pvp.seriouss.am")
            .addQueryParameter("type", "g_s") // get statistics
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
                            //Log.d("StatisticsActivity", "Map Data: $map")
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
                                Log.d("StatisticsActivity", value.toString())
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
        caloriesBurnedTextView.text = (data["calories_burned"] + " kcal") ?: "0"
        distanceWalkedTextView.text = (data["distance_walked"] + " km") ?: "0"
    }

    fun updateCircularProgressBars(dataList: List<Map<String, String?>>) {
        val today = Calendar.getInstance()
        val dayOfWeekNumber = today.get(Calendar.DAY_OF_WEEK)
        val progressBarIds = listOf(R.id.progressBar)

        progressBarIds.forEachIndexed { index, progressBarId ->
            if (index == dayOfWeekNumber) {
                val mainProgressBar = findViewById<CircularProgressBar>(R.id.progressBar9)
                val caloriesBurned = dataList.getOrNull(index)?.get("calories_burned")?.toIntOrNull() ?: 0
                mainProgressBar.progress = caloriesBurned.toFloat()
            }
            val progressBar = findViewById<CircularProgressBar>(progressBarId)
            val caloriesBurned = dataList.getOrNull(index)?.get("calories_burned")?.toIntOrNull() ?: 0

            progressBar.apply {
                progress = caloriesBurned.toFloat()
            }
        }
    }
    private fun createCardViews(parsedData: List<Map<String, String?>>) {
        val cardContainer = findViewById<LinearLayout>(R.id.recentActivietiesContainer)
        cardContainer.removeAllViews()

        for (activity in parsedData) {
            val cardView = CardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.setMargins(30, 50, 30, 60) }
                cardElevation = 4f
                radius = 30f
                setCardBackgroundColor(ContextCompat.getColor(context, R.color.edit_text_background))
                isClickable = true // Make the card view clickable
            }

            val cardContentLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val tableLayout = TableLayout(this).apply {
                layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(30, 30, 30, 30)
            }

            val headerRow = TableRow(this).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
            }

            val caloriesHeader = TextView(this).apply {
                text = "Calories burned"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(20, 10, 15, 10)
            }

            val distanceHeader = TextView(this).apply {
                text = "Distance walked"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(20, 10, 15, 10)
            }

            val activeTimeHeader = TextView(this).apply {
                text = "Active time"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(20, 10, 15, 10)
            }

            headerRow.addView(caloriesHeader)
            headerRow.addView(distanceHeader)
            headerRow.addView(activeTimeHeader)
            tableLayout.addView(headerRow)

            val dataRow = TableRow(this).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
            }

            val caloriesData = TextView(this).apply {
                text = activity["calories"] ?: "N/A"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                setPadding(20, 10, 15, 10)
            }

            val distanceData = TextView(this).apply {
                text = activity["distance"] ?: "N/A"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, R.color.app_theme))
                setPadding(20, 10, 15, 10)
            }

            val activeTimeData = TextView(this).apply {
                text = activity["active_time"] ?: "N/A"
                textSize = 16f
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                setPadding(20, 10, 15, 10)
            }

            dataRow.addView(caloriesData)
            dataRow.addView(distanceData)
            dataRow.addView(activeTimeData)
            tableLayout.addView(dataRow)

            cardContentLayout.addView(tableLayout)
            cardView.addView(cardContentLayout)
            cardContainer.addView(cardView)

            cardView.setOnClickListener {
                cardView.isClickable = false // Disable further clicks
                performGetLongLatRequest(cardView, activity["id"])
            }
        }
    }
    private fun performGetRequest(username: String?, pet_id: String?) {
        if (pet_id.isNullOrEmpty()) {
            runOnUiThread {
                Toast.makeText(applicationContext, "Select a pet first", Toast.LENGTH_SHORT).show()
                val intent = Intent(applicationContext, PetProfileActivity::class.java)
                startActivity(intent)
                finish()
            }
            return
        }

        val httpUrl = HttpUrl.Builder()
            .scheme("https")
            .host("pvp.seriouss.am")
            .addQueryParameter("type", "g_w_a")
            .addQueryParameter("p", pet_id)
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
                    Toast.makeText(applicationContext, "Failed to fetch data", Toast.LENGTH_SHORT)
                        .show()
                }
                e.printStackTrace()
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseBodyString = response.body?.string() ?: ""
                    val parsedData = parseResponse(responseBodyString)
                    runOnUiThread {
                        createCardViews(parsedData)
                    }
                }
            }
        })
    }

    private fun parseResponse(response: String): List<Map<String, String?>> {
        val lines = response.split("\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedActivities = mutableListOf<Map<String, String?>>()

        for (line in lines) {
            if (line.isBlank()) continue

            val parts = line.split(";")
            val idPart = parts[0].split("=")[1]
            val datePart = parts[1].split("=")[1]
            val distancePart = parts[2].split("=")[1]
            val caloriesPart = parts[3].split("=")[1]
            val activeTimePart = parts[4].split("=")[1]

            val activity = mapOf(
                "id" to idPart,
                "date" to datePart,
                "distance" to distancePart,
                "calories" to caloriesPart,
                "active_time" to activeTimePart
            )

            parsedActivities.add(activity)
        }

        return parsedActivities
    }
    private fun performGetLongLatRequest(cardView: CardView, id: String?) {
        val httpUrl = HttpUrl.Builder()
            .scheme("https")
            .host("pvp.seriouss.am")
            .addQueryParameter("type", "g_l_p")
            .addQueryParameter("a_r_i", id)
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
                    Toast.makeText(applicationContext, "Failed to fetch data", Toast.LENGTH_SHORT)
                        .show()
                }
                e.printStackTrace()
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val pointsList = parsePoints(responseBody)
                    runOnUiThread {
                        if(pointsList.isNotEmpty())
                        {
                            AddMapViewToCardView(cardView, pointsList)
                        }
                    }
                }
            }
        })
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
    private fun AddMapViewToCardView(cardView: CardView, mapPoints: List<GeoPoint>) {
        val mapView = MapView(this).apply {
            id = View.generateViewId()

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                300
            ).also {
                it.setMargins(0, 20, 0, 0)
            }
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            isClickable = true
        }

        val existingMapView = cardView.findViewById<MapView>(mapView.id)
        if (existingMapView == null) {
            setupDynamicMap(mapView, mapPoints)

            val cardContentLayout = cardView.getChildAt(0) as LinearLayout
            cardContentLayout.addView(mapView)

            cardView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.setMargins(30, 50, 30, 60) }
        } else {
            existingMapView.visibility = if (existingMapView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
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
}
