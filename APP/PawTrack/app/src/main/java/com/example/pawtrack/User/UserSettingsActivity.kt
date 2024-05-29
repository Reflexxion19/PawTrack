package com.example.pawtrack.User

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.pawtrack.R

class UserSettingsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_settings_layout)
        
        val changeEmailButton = findViewById<Button>(R.id.button3)
        changeEmailButton.setOnClickListener() {
            val intent = Intent(applicationContext, ChangeEmailActivity::class.java)
            startActivity(intent)
            finish()
        }
        val changePasswordButton = findViewById<Button>(R.id.button4)
        changePasswordButton.setOnClickListener() {
            val intent = Intent(applicationContext, ChangePasswordActivity::class.java)
            startActivity(intent)
            finish()
        }
        val backButton = findViewById<Button>(R.id.button)
        backButton.setOnClickListener() {
            val intent = Intent(applicationContext, UserProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}