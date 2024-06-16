package com.example.pawtrack.Tracking

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.pawtrack.HomePageActivity
import com.example.pawtrack.Map.MapActivity
import com.example.pawtrack.Pet.PetProfileActivity
import com.example.pawtrack.R
import com.example.pawtrack.User.SubscriptionActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.Calendar

class YearlyReviewActivity: AppCompatActivity() {
    private lateinit var distanceChart: BarChart
    private lateinit var caloriesChart: BarChart
    private lateinit var activeDaysChart: BarChart
    private val parsedList = mutableListOf<ParsedData>()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.year_review_layout)
        sharedPreferences = getSharedPreferences("PawTrackPrefs", Context.MODE_PRIVATE)
        val pet_id = sharedPreferences.getString("LastSelectedPetId", null)
        val username = sharedPreferences.getString("USERNAME", null)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.statistics

        performGetRequest(username, pet_id)

        distanceChart = findViewById(R.id.distanceChart)
        caloriesChart = findViewById(R.id.caloriesChart)
        activeDaysChart = findViewById(R.id.activeDaysChart)

        val textView31 = findViewById<TextView>(R.id.textView31)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
        textView31.text = currentYear

        val backButton = findViewById<Button>(R.id.button)
        backButton.setOnClickListener(){
            val intent = Intent(applicationContext, StatisticsActivity::class.java)
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

    private fun setupDistanceChart(entries: List<BarEntry>) {
        if (::distanceChart.isInitialized) {
            val dataSet = BarDataSet(entries, "Distance Covered (km)").apply {
                valueTextColor = ContextCompat.getColor(this@YearlyReviewActivity,
                    R.color.app_theme_light
                )
                valueTextSize = 12f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "$value km"
                    }
                }

                setColors(entries.map {
                    if (it.y >= 10) ContextCompat.getColor(this@YearlyReviewActivity, R.color.green)
                    else ContextCompat.getColor(this@YearlyReviewActivity, R.color.app_theme)
                })
            }

            val barData = BarData(dataSet)
            barData.barWidth = 0.9f

            distanceChart.apply {
                data = barData
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawGridLines(false)
                    valueFormatter = IndexAxisValueFormatter(getMonths())
                    labelCount = 12
                    textSize = 12f
                }
                axisLeft.apply {
                    axisMinimum = 0f
                    setDrawGridLines(false)
                    setDrawTopYLabelEntry(true)
                    setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                    granularity = 1f
                    val highestValue = entries.maxByOrNull { it.y }?.y ?: 0f
                    axisMaximum = highestValue
                    labelCount = 1
                }
                axisRight.isEnabled = false
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(true)
                animateY(1400)
                invalidate()
            }
        } else {
            Log.e("YearlyReviewActivity", "Distance chart not initialized")
        }
    }

    private fun setupCaloriesChart(entries: List<BarEntry>) {
        if (::caloriesChart.isInitialized) {
            val dataSet = BarDataSet(entries, "Distance Covered (km)").apply {
                valueTextColor = ContextCompat.getColor(this@YearlyReviewActivity,
                    R.color.app_theme_light
                )
                valueTextSize = 12f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "$value km"
                    }
                }

                setColors(entries.map {
                    if (it.y >= 10) ContextCompat.getColor(this@YearlyReviewActivity, R.color.green)
                    else ContextCompat.getColor(this@YearlyReviewActivity, R.color.app_theme)
                })
            }

            val barData = BarData(dataSet)
            barData.barWidth = 0.9f

            caloriesChart.apply {
                data = barData
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawGridLines(false)
                    valueFormatter = IndexAxisValueFormatter(getMonths())
                    labelCount = 12
                    textSize = 12f
                }
                axisLeft.apply {
                    axisMinimum = 0f
                    setDrawGridLines(false)
                    setDrawTopYLabelEntry(true)
                    setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                    granularity = 1f
                    val highestValue = entries.maxByOrNull { it.y }?.y ?: 0f
                    axisMaximum = highestValue
                    labelCount = 1
                }
                axisRight.isEnabled = false
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(true)
                animateY(1400)
                invalidate()
            }
        } else {
            Log.e("YearlyReviewActivity", "Distance chart not initialized")
        }
    }

    private fun setupActiveDaysChart(entries: List<BarEntry>) {
        if (::activeDaysChart.isInitialized) {
            val dataSet = BarDataSet(entries, "Active Days").apply {
                valueTextColor = ContextCompat.getColor(this@YearlyReviewActivity,
                    R.color.app_theme_light
                )
                valueTextSize = 12f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "$value days"
                    }
                }
                setColors(entries.map {
                    if (it.y >= 30) ContextCompat.getColor(this@YearlyReviewActivity, R.color.green)
                    else ContextCompat.getColor(this@YearlyReviewActivity, R.color.app_theme)
                })
            }

            val barData = BarData(dataSet)
            barData.barWidth = 0.9f

            activeDaysChart.apply {
                data = barData
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawGridLines(false)
                    valueFormatter = IndexAxisValueFormatter(getMonths())
                    labelCount = 12
                    textSize = 12f
                }
                axisLeft.apply {
                    axisMinimum = 0f
                    setDrawGridLines(false)
                    setDrawTopYLabelEntry(true)
                    setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                    granularity = 1f
                    val highestValue = entries.maxByOrNull { it.y }?.y ?: 0f
                    axisMaximum = highestValue
                    labelCount = 1
                }
                axisRight.isEnabled = false
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(true)
                animateY(1400)
                invalidate()
            }
        } else {
            Log.e("YearlyReviewActivity", "Active days chart not initialized")
        }
    }

    private fun getMonths(): Array<String> {
        return arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
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
                        setupDistanceChart(parsedData.distanceEntries)
                        setupCaloriesChart(parsedData.caloriesEntries)
                        setupActiveDaysChart(parsedData.activeDaysEntries)
                    }
                }
            }
        })
    }

    private fun parseResponse(response: String): ParsedData {
        val distanceEntries = mutableListOf<BarEntry>()
        val caloriesEntries = mutableListOf<BarEntry>()
        val activeDaysEntries = mutableListOf<BarEntry>()

        val lines = response.split("\n")
        val calendar = Calendar.getInstance()

        for (line in lines) {
            if (line.isBlank()) continue

            val parts = line.split(";")
            val idPart = parts[0].split("=")[1]
            val datePart = parts[1].split("=")[1]
            val distancePart = parts[2].split("=")[1].toFloat()
            val caloriesPart = parts[3].split("=")[1].toFloat()
            val activeTimePart = parts[4].split("=")[1]

            val date = datePart.split(" ")[0]
            val month = date.split("-")[1].toInt() - 1

            distanceEntries.add(BarEntry(month.toFloat(), distancePart))
            caloriesEntries.add(BarEntry(month.toFloat(), caloriesPart))

            val activeDays = activeDaysEntries.firstOrNull { it.x == month.toFloat() }
            if (activeDays != null) {
                activeDays.y += 1
            } else {
                activeDaysEntries.add(BarEntry(month.toFloat(), 1f))
            }
        }

        return ParsedData(distanceEntries, caloriesEntries, activeDaysEntries)
    }

    data class ParsedData(
        val distanceEntries: List<BarEntry>,
        val caloriesEntries: List<BarEntry>,
        val activeDaysEntries: List<BarEntry>
    )
}
