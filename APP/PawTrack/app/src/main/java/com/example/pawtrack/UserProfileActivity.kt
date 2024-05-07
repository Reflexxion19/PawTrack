package com.example.pawtrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class UserProfileActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_profile_layout)
        val username = intent.getStringExtra("USERNAME")

        val usernameText = findViewById<TextView>(R.id.textView)
        usernameText.text = username

        val preferencesButton = findViewById<Button>(R.id.button4)
        preferencesButton.setOnClickListener() {
            val intent = Intent(applicationContext, UserPreferencesActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
            finish()
        }
        val backButton = findViewById<Button>(R.id.button)
        backButton.setOnClickListener() {
            val intent = Intent(applicationContext, HomePageActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
            finish()
        }
        val userSettingsButton = findViewById<Button>(R.id.button3)
        userSettingsButton.setOnClickListener() {
            val intent = Intent(applicationContext, UserSettingsActivity::class.java)
            intent.putExtra("USERNAME", username)
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
    }
    fun clearAllPreferences(context: Context) {
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            "user_preferences",
            masterKey,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        with(sharedPreferences.edit()){
            clear()
            apply()
        }
    }
}