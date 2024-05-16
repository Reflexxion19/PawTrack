package com.example.pawtrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class UserSettingsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_settings_layout)
        val username = intent.getStringExtra("USERNAME")
        
        val changeEmailButton = findViewById<Button>(R.id.button3)
        changeEmailButton.setOnClickListener() {
            val intent = Intent(applicationContext, ChangeEmailActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
            finish()
        }
        val changePasswordButton = findViewById<Button>(R.id.button4)
        changePasswordButton.setOnClickListener() {
            val intent = Intent(applicationContext, ChangePasswordActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
            finish()
        }
        val backButton = findViewById<Button>(R.id.button)
        backButton.setOnClickListener() {
            val intent = Intent(applicationContext, UserProfileActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
            finish()
        }
    }
}