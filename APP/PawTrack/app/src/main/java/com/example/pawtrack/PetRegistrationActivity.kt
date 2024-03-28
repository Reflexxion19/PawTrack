package com.example.pawtrack

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.math.log

class PetRegistrationActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_pet_layout)
        val username = intent.getStringExtra("USERNAME")


        val petNameText = findViewById<EditText>(R.id.editTextText)
        val petCategoryText = findViewById<Spinner>(R.id.spinner)
        var selectedItemId = 0;
        if(petCategoryText.selectedItem.toString() == "Low Activity") {
            selectedItemId = 1;
        }
        else if (petCategoryText.selectedItem.toString() == "Medium Activity"){
            selectedItemId = 3;
        }
        if(petCategoryText.selectedItem.toString() == "High Activity") {
            selectedItemId = 5;
        }
        val petTrackerID = findViewById<EditText>(R.id.editTextText2)
        val petRegistrationButton = findViewById<Button>(R.id.RegisterButton)
        petRegistrationButton.setOnClickListener(){
            performPostRequest(username, petNameText.text.toString(), selectedItemId, petTrackerID.text.toString())
        }

        val backButton = findViewById<Button>(R.id.button)
        backButton.setOnClickListener(){
            val intent = Intent(applicationContext, PetProfileActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
            finish()
        }
    }
    private fun performPostRequest(username: String?, petName: String, category: Int, trackerID: String) {
        if (username.isNullOrEmpty()) {
            runOnUiThread {
                Toast.makeText(applicationContext, "Username is required", Toast.LENGTH_SHORT).show()
            }
            return
        }
        if(petName.isNotEmpty() && trackerID.all { it.isDigit() })
        {
            val jsonMediaType = "application/json; charset=utf-8".toMediaType()
            var json = """
            {
                "type": "p_r",
                "u_n": "$username",
                "p_n": "$petName",
                "p_p": "",
                "t_i": "",
                "t_s": "0",
                "a_c": "$category"
            }
            """.trimIndent()

            if(trackerID.isNotEmpty())
            {
                json = """
            {
                "type": "p_r",
                "u_n": "$username",
                "p_n": "$petName",
                "p_p": "",
                "t_i": "$trackerID",
                "t_s": "1",
                "a_c": "$category"
            }
            """.trimIndent()
            }
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
                        runOnUiThread {
                            Toast.makeText(
                                applicationContext,
                                "Pet registered successfully",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                applicationContext,
                                "Error registering",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            })
        }
        else
        {
            runOnUiThread {
                Toast.makeText(
                    applicationContext,
                    "Error putting in data",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}