package com.example.pawtrack.AppInterface

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.pawtrack.R
import com.example.pawtrack.User.UserProfileActivity

class AboutActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_layout)


        val backButton = findViewById<Button>(R.id.button2)
        backButton.setOnClickListener {
            val intent = Intent(applicationContext, UserProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}