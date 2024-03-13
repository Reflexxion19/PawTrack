package com.example.pawtrack

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarItemView
import com.vishnusivadas.advanced_httpurlconnection.PutData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

class HomePageActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_layout)

        val CurrentTime = findViewById<TextView>(R.id.textView)
        CurrentTime.text = getCurrentTime()

        val mapsButton = findViewById<Button>(R.id.button)

        mapsButton.setOnClickListener(){
            val intent = Intent(applicationContext, MapActivity::class.java)
            startActivity(intent)
            finish()
        }

        val reminderButton = findViewById<Button>(R.id.button5)

        reminderButton.setOnClickListener(){
            val intent = Intent(applicationContext, ReminderSettingActivity::class.java)
            startActivity(intent)
            finish()
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    val intent = Intent(applicationContext, HomePageActivity::class.java)
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
}