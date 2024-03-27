package com.example.pawtrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class PetProfileActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pet_profile_layout)
        val username = intent.getStringExtra("USERNAME")

        val petRegistrationButton = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        petRegistrationButton.setOnClickListener(){
            val intent = Intent(applicationContext, PetRegistrationActivity::class.java)
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

        val backButton = findViewById<Button>(R.id.button)
        backButton.setOnClickListener(){
            val intent = Intent(applicationContext, HomePageActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
            finish()
        }

        //performPostRequest(username)
    }
    private fun performPostRequest(username: String?) {
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val json = """
        {
            "type": "l_p",
            "u": "$username"
        }
        """.trimIndent()
        val body = json.toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("https://pvp.seriouss.am")
            .post(body)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Failed to fetch data", Toast.LENGTH_SHORT)
                        .show()
                }
                e.printStackTrace()
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseBodyString = response.body?.string()
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            "Data fetched successfully: $responseBodyString",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            "Error fetching data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }
}