package com.example.pawtrack

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


public class TrackingActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tracking_layout)
        sharedPreferences = getSharedPreferences("PawTrackPrefs", Context.MODE_PRIVATE)
        val pet_id = sharedPreferences.getString("LastSelectedPetId", null)
        val username = sharedPreferences.getString("USERNAME", null)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.tracking


        performGetRequest(object : StatisticsActivity.OnDataFetched {
            override fun onDataFetched(parsedList: List<Map<String, String?>>) {
            }
        }, pet_id)
        performGetRequest(username, pet_id)


        val petprofileButton = findViewById<FloatingActionButton>(R.id.pet_profile)
        petprofileButton.setOnClickListener(){
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

        val routeButton = findViewById<Button>(R.id.button5)
        routeButton.setOnClickListener(){
            val intent = Intent(applicationContext, RouteActivity::class.java)
            startActivity(intent)
            finish()
        }

        val trackingButton = findViewById<Button>(R.id.button6)
        trackingButton.setOnClickListener(){
            val intent = Intent(applicationContext, TrackingMapActivity::class.java)
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
        caloriesBurnedTextView.text = (data["calories_burned"] + " kcal") ?: "0"
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
                progress = caloriesBurned.toFloat()
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
            .addQueryParameter("type", "g_a_r")
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
                    Toast.makeText(applicationContext, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseBodyString = response.body?.string() ?: ""
                    val parsedData = parseResponse(responseBodyString)
                    runOnUiThread {
                        updateActivityTextViews(parsedData)
                    }
                }
            }
        })
    }

    private fun parseResponse(response: String): Map<String, String?> {
        val lines = response.split("\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var mostRecentActivity: Map<String, String?>? = null

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

            if (mostRecentActivity == null || dateFormat.parse(datePart)!! > dateFormat.parse(mostRecentActivity["date"])!!) {
                mostRecentActivity = activity
            }
        }

        return mostRecentActivity ?: emptyMap()
    }
    private fun updateActivityTextViews(data: Map<String, String?>) {
        val dateTextView : TextView = findViewById(R.id.textView17)
        val distanceTextView: TextView = findViewById(R.id.textView19)
        val caloriesTextView: TextView = findViewById(R.id.textView21)
        val activeTimeTextView: TextView = findViewById(R.id.textView23)

        dateTextView.text = data["date"] ?: "0000/00/00"
        distanceTextView.text = (data["calories"] + " kcal") ?: "0"
        caloriesTextView.text = (data["distance"] + " km")  ?: "0"
        activeTimeTextView.text = data["active_time"] ?: "0"
    }
}
