package com.example.pawtrack

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class PetProfileActivity: AppCompatActivity() {
    private lateinit var adapter: PetFragmentPageAdapter
    interface OnDataFetched {
        fun onDataFetched(parsedList: List<Map<String, String?>>)
    }
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

        performGetRequest(username, object : OnDataFetched {
            override fun onDataFetched(parsedList: List<Map<String, String?>>) {
                val petName = parsedList.joinToString(", ") { it["n"] ?: "Unknown" }
                val petNameTextView = findViewById<TextView>(R.id.PetNameText)
                //petNameTextView.text = petName
            }
        })
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
                        Toast.makeText(
                            applicationContext,
                            "Data fetched successfully",
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
            }
        })
    }
}
/*class PetPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val petsList: List<Map<String, String?>>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = petsList.size

    override fun createFragment(position: Int): Fragment {
        val petName = petsList[position]["n"] ?: "Unknown"
        val petActivity = petsList[position]["a_c"] ?: "No Activity Set"
        val petTrackerID = petsList[position]["t_i"] ?: "No Tracker Set"
        val petTrackerStatus = petsList[position]["t_s"] ?: "No Tracker Set"
        val petPhotoBitmapURL = petsList[position]["p_p"] ?: ""
        return PetDetailFragment.newInstance(petName, petActivity, petTrackerID, petTrackerStatus, petPhotoBitmapURL)
    }
}*/
