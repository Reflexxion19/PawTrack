package com.example.pawtrack

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

object AlarmManager {

    private const val ALARM_PREFS_KEY = "ALARM_PREFS_KEY"

    fun saveAlarms(context: Context, alarms: List<AlarmItem>) {
        val prefs = context.getSharedPreferences(ALARM_PREFS_KEY, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val gson = Gson()
        val json = gson.toJson(alarms)
        editor.putString(ALARM_PREFS_KEY, json)
        editor.apply()
    }

    fun getAlarms(context: Context): List<AlarmItem> {
        val prefs = context.getSharedPreferences(ALARM_PREFS_KEY, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString(ALARM_PREFS_KEY, null)
        val type: Type = object : TypeToken<List<AlarmItem>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}
