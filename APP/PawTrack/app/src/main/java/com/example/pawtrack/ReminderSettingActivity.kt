package com.example.pawtrack

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.time.LocalTime
import android.Manifest
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

class ReminderSettingActivity : AppCompatActivity() {

    private lateinit var scheduler: AndroidAlarmScheduler
    private lateinit var adapter: ReminderAdapter
    private lateinit var bottomSheet: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddReminder: FloatingActionButton
    private lateinit var btnSaveReminder: Button
    private lateinit var etReminderName: EditText
    private lateinit var cbRepeat: CheckBox
    private lateinit var timePicker: TimePicker
    private var selectedTime: LocalTime = LocalTime.now() // Initialize with current time
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_setting)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val username = intent.getStringExtra("USERNAME")




        scheduler = AndroidAlarmScheduler(this)
        adapter = ReminderAdapter(emptyList())

        // Find views
        recyclerView = findViewById(R.id.recyclerViewReminders)
        fabAddReminder = findViewById(R.id.fabAddReminder)
        bottomSheet = findViewById(R.id.bottomSheet)
        btnSaveReminder = findViewById(R.id.btnSaveReminder)
        etReminderName = findViewById(R.id.etReminderName)
        cbRepeat = findViewById(R.id.cbRepeat)
        timePicker = findViewById(R.id.timePicker)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    val intent = Intent(applicationContext, HomePageActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    startActivity(intent)
                    true
                }
                R.id.map -> {
                    val intent = Intent(applicationContext, MapActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    startActivity(intent)
                    true
                }
                R.id.tracking -> {
                    val intent = Intent(applicationContext, TrackingActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    startActivity(intent)
                    true
                }
                R.id.statistics -> {
                    val intent = Intent(applicationContext, StatisticsActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    startActivity(intent)
                    true
                }
                R.id.subscription -> {
                    val intent = Intent(applicationContext, SubscriptionActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Set up FAB click listener
        fabAddReminder.setOnClickListener {
            if (bottomSheet.visibility == View.VISIBLE) {
                bottomSheet.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE // Show the RecyclerView
            } else {
                bottomSheet.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE // Hide the RecyclerView
            }
        }

        // Set up Save Button click listener
        btnSaveReminder.setOnClickListener {
            val message = etReminderName.text.toString()
            val repeat = cbRepeat.isChecked

            // Schedule alarm with selected time
            val alarmItem = AlarmItem(
                time = selectedTime,
                message = message,
                repeat = repeat
            )
            scheduler.schedule(alarmItem)
            // Save the alarm
            saveAlarm(alarmItem)

            // Clear input fields
            etReminderName.setText("")
            cbRepeat.isChecked = false

            // Fetch and show list of already set alarms
            showAlreadySetAlarms()
        }

        // Set up TimePicker listener
        timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            selectedTime = LocalTime.of(hourOfDay, minute)
        }
    }

    override fun onResume() {
        super.onResume()
        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request notification permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun saveAlarm(alarmItem: AlarmItem) {
        val alarms = fetchAlreadySetAlarms().toMutableList()
        val toSave = SavedAlarm(
                alarmItem.time.toString(),
                 alarmItem.message,
                alarmItem.repeat

        )
        alarms.add(toSave)

        for(alarm in alarms)
        {
            Log.d("Alarm time:", alarm.time.toString())
        }

        val sharedPreferences = getSharedPreferences("alarms", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = gson.toJson(alarms)
        sharedPreferences.edit().putString("alarms", json).apply()
        adapter.updateData(alarms)
    }

    private fun fetchAlreadySetAlarms(): List<SavedAlarm> {
        val sharedPreferences = getSharedPreferences("alarms", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("alarms", null)
        val type: Type = object : TypeToken<List<SavedAlarm>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private fun showAlreadySetAlarms() {
        // Fetch the list of already set alarms
        val alarms = fetchAlreadySetAlarms()

        // Update the adapter with the list of alarms
        adapter.updateData(alarms)
    }
}
