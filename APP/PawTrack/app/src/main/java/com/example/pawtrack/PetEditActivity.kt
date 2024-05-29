package com.example.pawtrack

import CircleTransform
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class PetEditActivity: AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_pet_layout)
        sharedPreferences = getSharedPreferences("PawTrackPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("USERNAME", null)
        val pet_name = sharedPreferences.getString("LastSelectedPetName", null)
        val tracker_id = sharedPreferences.getString("LastSelectedTrackerId", null)
        val pet_profile_picture = sharedPreferences.getString("LastSelectedPetProfile", null)

        val petNameText = findViewById<EditText>(R.id.editTextText)
        val petTrackerID = findViewById<EditText>(R.id.editTextText2)
        val petPictureView = findViewById<ImageView>(R.id.imageView2)

        Picasso.get()
            .load(pet_profile_picture)
            .placeholder(R.drawable.default_pet_picture)
            .transform(CircleTransform())
            .into(petPictureView)
        petNameText.setText(pet_name)
        petTrackerID.setText(tracker_id)

        val petCategoryText = findViewById<Spinner>(R.id.spinner)
        var selectedItemId = 0;
        if (petCategoryText.selectedItem.toString() == "Low Activity") {
            selectedItemId = 1;
        } else if (petCategoryText.selectedItem.toString() == "Medium Activity") {
            selectedItemId = 3;
        }
        if (petCategoryText.selectedItem.toString() == "High Activity") {
            selectedItemId = 5;
        }
        val petRegistrationButton = findViewById<Button>(R.id.RegisterButton)
        petRegistrationButton.setOnClickListener() {
            performPostRequest(
                username,
                pet_name,
                petNameText.text.toString(),
                pet_profile_picture,
                selectedItemId,
                petTrackerID.text.toString()
            )
        }

        val backButton = findViewById<Button>(R.id.button)
        backButton.setOnClickListener() {
            val intent = Intent(applicationContext, PetProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun performPostRequest(username: String?, oldPetName: String?, petName: String, pet_profile_picture: String?, category: Int, trackerID: String) {
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
                "type":"p_u",
                "p_n":"$oldPetName",
                "n_p_n":"$petName",
                "p_p":"$pet_profile_picture",
                "t_i":0,
                "t_s":0,
                "a_c":$category,
                "u_n":"$username"
            }
            """.trimIndent()

            if(trackerID.isNotEmpty())
            {
                json = """
            {
                "type":"p_u",
                "p_n":"$oldPetName",
                "n_p_n":"$petName",
                "p_p":"$pet_profile_picture",
                "t_i":"$trackerID",
                "t_s":1,
                "a_c":$category,
                "u_n":"$username"
            }
            """.trimIndent()
            }
            Log.d("UpdatePetInfo", "$json")
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
                                "Pet information edited successfully",
                                Toast.LENGTH_LONG
                            ).show()
                            val intent = Intent(applicationContext, PetProfileActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                applicationContext,
                                "Error editing pet information",
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