package com.example.pawtrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class UserProfileActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_profile_layout)
        val username = intent.getStringExtra("USERNAME")

        val usernameText = findViewById<TextView>(R.id.textView)
        usernameText.text = username;

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
    }
}