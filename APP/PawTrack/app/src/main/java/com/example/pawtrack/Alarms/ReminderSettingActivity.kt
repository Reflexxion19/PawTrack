package com.example.pawtrack.Alarms

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import com.example.pawtrack.HomePageActivity
import com.example.pawtrack.Map.MapActivity
import com.example.pawtrack.R
import com.example.pawtrack.Tracking.StatisticsActivity
import com.example.pawtrack.Tracking.TrackingActivity
import com.example.pawtrack.User.SubscriptionActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

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
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_setting)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        scheduler = AndroidAlarmScheduler(this)
        adapter = ReminderAdapter(emptyList()) { alarm ->
            deleteAlarm(alarm)
        }

        recyclerView = findViewById(R.id.recyclerViewReminders)
        fabAddReminder = findViewById(R.id.fabAddReminder)
        bottomSheet = findViewById(R.id.bottomSheet)
        btnSaveReminder = findViewById(R.id.btnSaveReminder)
        etReminderName = findViewById(R.id.etReminderName)
        cbRepeat = findViewById(R.id.cbRepeat)
        timePicker = findViewById(R.id.timePicker)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    val intent = Intent(applicationContext, HomePageActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.map -> {
                    val intent = Intent(applicationContext, MapActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.tracking -> {
                    val intent = Intent(applicationContext, TrackingActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.statistics -> {
                    val intent = Intent(applicationContext, StatisticsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.subscription -> {
                    val intent = Intent(applicationContext, SubscriptionActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        fabAddReminder.setOnClickListener {
            if (bottomSheet.visibility == View.VISIBLE) {
                bottomSheet.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            } else {
                bottomSheet.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
        }

        btnSaveReminder.setOnClickListener {
            val message = etReminderName.text.toString()
            val repeat = cbRepeat.isChecked

            val hour = timePicker.hour
            val minute = timePicker.minute
            val time = String.format("%02d:%02d", hour, minute)

            val newAlarm = AlarmItem(
                id = System.currentTimeMillis().toInt(),
                message = message,
                time = time,
                repeat = repeat
            )

            scheduler.schedule(newAlarm)
            saveAlarm(newAlarm)
            updateAlarms()
            clearInputs()

            bottomSheet.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }

        // Request notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }

        loadAlarms()
    }

    private fun loadAlarms() {
        val sharedPreferences = getSharedPreferences("alarms", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("alarms_list", null)
        val type: Type = object : TypeToken<List<AlarmItem>>() {}.type
        val alarms: List<AlarmItem> = gson.fromJson(json, type) ?: emptyList()
        adapter.updateData(alarms)
    }

    private fun saveAlarm(alarm: AlarmItem) {
        val sharedPreferences = getSharedPreferences("alarms", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("alarms_list", null)
        val type: Type = object : TypeToken<List<AlarmItem>>() {}.type
        val alarms: MutableList<AlarmItem> = gson.fromJson(json, type) ?: mutableListOf()
        alarms.add(alarm)
        val editor = sharedPreferences.edit()
        editor.putString("alarms_list", gson.toJson(alarms))
        editor.apply()
    }

    private fun deleteAlarm(alarm: AlarmItem) {
        val sharedPreferences = getSharedPreferences("alarms", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("alarms_list", null)
        val type: Type = object : TypeToken<List<AlarmItem>>() {}.type
        val alarms: MutableList<AlarmItem> = gson.fromJson(json, type) ?: mutableListOf()
        alarms.remove(alarm)
        val editor = sharedPreferences.edit()
        editor.putString("alarms_list", gson.toJson(alarms))
        editor.apply()

        scheduler.cancel(alarm.id)
        updateAlarms()
    }

    private fun updateAlarms() {
        val sharedPreferences = getSharedPreferences("alarms", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("alarms_list", null)
        val type: Type = object : TypeToken<List<AlarmItem>>() {}.type
        val alarms: List<AlarmItem> = gson.fromJson(json, type) ?: emptyList()
        adapter.updateData(alarms)
    }

    private fun clearInputs() {
        etReminderName.text.clear()
        cbRepeat.isChecked = false
        timePicker.hour = 0
        timePicker.minute = 0
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted
            } else {
                // Permission denied
            }
        }
    }
}
