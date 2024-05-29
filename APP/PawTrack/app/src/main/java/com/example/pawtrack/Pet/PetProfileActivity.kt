package com.example.pawtrack.Pet

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.pawtrack.HomePageActivity
import com.example.pawtrack.R
import com.example.pawtrack.User.UserProfileActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class PetProfileActivity: AppCompatActivity() {
    private lateinit var adapter: PetFragmentPageAdapter
    private lateinit var sharedPreferences: SharedPreferences
    interface OnDataFetched {
        fun onDataFetched(parsedList: List<Map<String, String?>>)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pet_profile_layout)
        sharedPreferences = getSharedPreferences("PawTrackPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("USERNAME", null)
        val lastSelectedPetId = sharedPreferences.getString("LastSelectedPetName", null)
        val petRegistrationButton = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        petRegistrationButton.setOnClickListener(){
            val intent = Intent(applicationContext, PetRegistrationActivity::class.java)
            startActivity(intent)
            finish()
        }

        val petEditButton = findViewById<FloatingActionButton>(R.id.floatingEditButton)
        petEditButton.setOnClickListener(){
            val intent = Intent(applicationContext, PetEditActivity::class.java)
            startActivity(intent)
            finish()
        }


        petRegistrationButton.setOnClickListener(){
            val intent = Intent(applicationContext, PetRegistrationActivity::class.java)
            startActivity(intent)
            finish()
        }

        val profileButton = findViewById<FloatingActionButton>(R.id.floatingActionButton2)
        profileButton.setOnClickListener(){
            val intent = Intent(applicationContext, UserProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        val backButton = findViewById<Button>(R.id.button)
        backButton.setOnClickListener(){
            val intent = Intent(applicationContext, HomePageActivity::class.java)
            startActivity(intent)
            finish()
        }

        val removeButton = findViewById<FloatingActionButton>(R.id.removePet)
        removeButton.setOnClickListener()
        {

            performRemovePostRequest(username, lastSelectedPetId)

        }

        performGetRequest(username, object : OnDataFetched {
            override fun onDataFetched(parsedList: List<Map<String, String?>>) {
            }
        })
    }

    private fun performRemovePostRequest(username: String?, pet_id: String?) {
        Log.d("POST",username + " " + pet_id)
        if(!username.isNullOrEmpty() && !pet_id.isNullOrEmpty())
        {
            val jsonMediaType = "application/json; charset=utf-8".toMediaType()
            val json = """
                {
                    "type": "p_rm",
                    "p_n": "$pet_id",
                    "u_n": "$username"
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
                    e.printStackTrace()
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    runOnUiThread {
                        val intent = Intent(applicationContext, PetProfileActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            })
        }
        else
        {
            Toast.makeText(applicationContext,"All fields are required", Toast.LENGTH_SHORT).show()
        }

    }

    private fun performGetRequest(username: String?, onDataFetched: OnDataFetched) {
        if (username.isNullOrEmpty()) {
            runOnUiThread {
                Toast.makeText(applicationContext, "Username is required", Toast.LENGTH_SHORT).show()
            }
            return
        }


        val httpUrl = HttpUrl.Builder()
            .scheme("https")
            .host("pvp.seriouss.am")
            .addQueryParameter("type", "l_p")
            .addQueryParameter("u", username)
            .build()

        val request = Request.Builder()
            .url(httpUrl)
            .get()
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseBodyString = response.body?.string() ?: ""
                    val parsedList = parseResponseToList(responseBodyString)
                    runOnUiThread {
                        onDataFetched.onDataFetched(parsedList)
                        setupViewPager(parsedList)
                    }
                }
            }
        })
    }
    private fun parseResponseToList(response: String): List<Map<String, String?>> {
        return response.split("\n").mapNotNull { petInfo ->
            if (petInfo.isNotBlank()) {
                petInfo.split(";").mapNotNull {
                    val parts = it.split("=")
                    if (parts.size == 2) {
                        parts[0] to parts[1].ifEmpty { null }
                    } else {
                        null
                    }
                }.toMap().takeIf { it.isNotEmpty() }
            } else {
                null
            }
        }
    }
    private fun setupViewPager(parsedList: List<Map<String, String?>>) {
        val viewPager = findViewById<ViewPager2>(R.id.viewPagerPetProfiles)
        val tabLayout = findViewById<TabLayout>(R.id.tabDots)
        adapter = PetFragmentPageAdapter(supportFragmentManager, lifecycle, parsedList);
        parsedList.forEach { pet ->
            tabLayout.addTab(tabLayout.newTab())
        }
        val lastSelectedPetId = sharedPreferences.getString("LastSelectedPetId", null)
        val initialPosition = parsedList.indexOfFirst { it["i"] == lastSelectedPetId }.takeIf { it >= 0 } ?: 0
        adapter.setArguments(1)
        viewPager.adapter = adapter
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab != null)
                {
                    viewPager.currentItem = tab.position
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout.selectTab(tabLayout.getTabAt(position))

                val selectedPetId = parsedList[position]["i"]
                val selectedPetName = parsedList[position]["n"]
                val selectedTrackerId = parsedList[position]["t_i"]
                val selectedPetPhoto = parsedList[position]["p_p"]
                sharedPreferences.edit().apply {
                    putString("LastSelectedPetProfile", selectedPetPhoto)
                    putString("LastSelectedTrackerId", selectedTrackerId)
                    putString("LastSelectedPetName", selectedPetName)
                    putString("LastSelectedPetId", selectedPetId)
                    apply()
                }
            }
        })
    }
}
