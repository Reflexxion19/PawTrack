package com.example.pawtrack

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class StatisticsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.statistics_layout)
        val username = intent.getStringExtra("USERNAME")
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.statistics

        val petprofileButton = findViewById<FloatingActionButton>(R.id.pet_profile)
        petprofileButton.setOnClickListener(){
            val intent = Intent(applicationContext, PetProfileActivity::class.java)
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
                    startActivity(intent)
                    true
                }
                R.id.map -> {
                    val intent = Intent(applicationContext, MapActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    startActivity(intent)
                    true
                }
                R.id.tracking -> {
                    val intent = Intent(applicationContext, TrackingActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    startActivity(intent)
                    true
                }
                R.id.statistics -> {
                    val intent = Intent(applicationContext, StatisticsActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    startActivity(intent)
                    true
                }
                R.id.subscription -> {
                    val intent = Intent(applicationContext, SubscriptionActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
    private fun performGetRequest(username: String?, onDataFetched: PetProfileActivity.OnDataFetched) {
        if (username.isNullOrEmpty()) {
            runOnUiThread {
                Toast.makeText(applicationContext, "Username is required", Toast.LENGTH_SHORT).show()
            }
            return
        }


        val httpUrl = HttpUrl.Builder()
            .scheme("https")
            .host("pvp.seriouss.am")
            .addQueryParameter("type", "g_s") //get statistics
            .addQueryParameter("u", username)
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
                        val rootView = findViewById<View>(R.id.statistics)
                        onDataFetched.onDataFetched(parsedList)
                        updateTextViewsWithData(rootView, parsedList.first())
                    }
                }
            }
        })
    }
    private fun parseResponseToList(response: String): List<Map<String, String?>> {
        return response.split("\n").mapNotNull { line ->
            if (line.isNotBlank()) {
                line.split(";").mapNotNull { entry ->
                    val parts = entry.split("=")
                    if (parts.size == 2) {
                        val key = parts[0].trim()
                        val value = parts[1].trim().ifEmpty { null }
                        key to value
                    } else {
                        null
                    }
                }.toMap().takeIf { it.isNotEmpty() }
            } else {
                null
            }
        }
    }
    fun updateTextViewsWithData(view: View, data: Map<String, String?>) {
        val caloriesBurnedTextView: TextView = view.findViewById(R.id.textView10)
        val stepsWalkedTextView: TextView = view.findViewById(R.id.textView12)
        val distanceWalkedTextView: TextView = view.findViewById(R.id.textView14)

        caloriesBurnedTextView.text = data["calories_burned"] ?: "N/A"
        stepsWalkedTextView.text = data["steps_walked"] ?: "N/A"
        distanceWalkedTextView.text = data["distance_walked"] ?: "N/A"
    }
}