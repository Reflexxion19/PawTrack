package com.example.pawtrack

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
import java.util.Calendar

class StatisticsActivity: AppCompatActivity() {

    interface OnDataFetched {
        fun onDataFetched(parsedList: List<Map<String, String?>>)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.statistics_layout)
        val username = intent.getStringExtra("USERNAME")
        val pet_id = intent.getStringExtra("PET_ID")
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.statistics

        performGetRequest(object : StatisticsActivity.OnDataFetched {
            override fun onDataFetched(parsedList: List<Map<String, String?>>) {
            }
        })
        val petprofileButton = findViewById<FloatingActionButton>(R.id.pet_profile)
        petprofileButton.setOnClickListener(){
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
                    intent.putExtra("USERNAME", pet_id)
                    startActivity(intent)
                    true
                }
                R.id.map -> {
                    val intent = Intent(applicationContext, MapActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    intent.putExtra("USERNAME", pet_id)
                    startActivity(intent)
                    true
                }
                R.id.tracking -> {
                    val intent = Intent(applicationContext, TrackingActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    intent.putExtra("USERNAME", pet_id)
                    startActivity(intent)
                    true
                }
                R.id.statistics -> {
                    val intent = Intent(applicationContext, StatisticsActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    intent.putExtra("USERNAME", pet_id)
                    startActivity(intent)
                    true
                }
                R.id.subscription -> {
                    val intent = Intent(applicationContext, SubscriptionActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    intent.putExtra("USERNAME", pet_id)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
    private fun performGetRequest(onDataFetched: OnDataFetched) {
        val httpUrl = HttpUrl.Builder()
            .scheme("https")
            .host("pvp.seriouss.am")
            .addQueryParameter("type", "g_s") //get statistics
            .addQueryParameter("p", "1")
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
                    val parsedList = parseResponseToList(responseBodyString)
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
    private fun parseResponseToList(response: String): List<Map<String, String?>> {
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
        caloriesBurnedTextView.text = data["calories_burned"] ?: "0"
        distanceWalkedTextView.text = data["distance_walked"] ?: "0"


    }
    fun updateCircularProgressBars(dataList: List<Map<String, String?>>) {
        val today = Calendar.getInstance()
        val dayOfWeekNumber = today.get(Calendar.DAY_OF_WEEK)
        val progressBarIds = listOf(R.id.progressBar2, R.id.progressBar4, R.id.progressBar5, R.id.progressBar6, R.id.progressBar7, R.id.progressBar8, R.id.progressBar10)

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
}