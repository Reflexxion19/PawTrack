package com.example.pawtrack

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class UserProfileActivity: AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_profile_layout)
        sharedPreferences = getSharedPreferences("PawTrackPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("USERNAME", null)

        val usernameText = findViewById<TextView>(R.id.textView)
        usernameText.text = username

        val preferencesButton = findViewById<Button>(R.id.button4)
        preferencesButton.setOnClickListener() {
            val intent = Intent(applicationContext, UserPreferencesActivity::class.java)
            startActivity(intent)
            finish()
        }
        val backButton = findViewById<Button>(R.id.button)
        backButton.setOnClickListener() {
            val intent = Intent(applicationContext, HomePageActivity::class.java)
            startActivity(intent)
            finish()
        }
        val userSettingsButton = findViewById<Button>(R.id.button3)
        userSettingsButton.setOnClickListener() {
            val intent = Intent(applicationContext, UserSettingsActivity::class.java)
            startActivity(intent)
            finish()
        }
        val logOutButton = findViewById<Button>(R.id.button5)
        logOutButton.setOnClickListener{
            clearAllPreferences(applicationContext)
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        val reminderButton = findViewById<Button>(R.id.reminder)
        reminderButton.setOnClickListener(){
            val intent = Intent(applicationContext, ReminderSettingActivity::class.java)
            startActivity(intent)
            finish()
        }
        val aboutButton = findViewById<Button>(R.id.button6)
        aboutButton.setOnClickListener {
            val intent = Intent(applicationContext, AboutActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    fun clearAllPreferences(context: Context) {
        val sharedPreferences = context.getSharedPreferences("PawTrackPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }
}