package com.example.pawtrack

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

class UserPreferencesActivity : AppCompatActivity() {

    private val PREFERENCES_FILE = "com.example.pawtrack.preferences"
    private val PREFERENCE_LANGUAGE_KEY = "language"
    private val PREFERENCE_THEME_KEY = "theme"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_preferences_layout)

        val username = intent.getStringExtra("USERNAME")
        val backButton = findViewById<Button>(R.id.button2)
        val aboutButton = findViewById<Button>(R.id.buttonAbout)
        val languageSpinner = findViewById<Spinner>(R.id.spinnerLanguage)
        val themeSwitch = findViewById<Switch>(R.id.switch1)

        // Back button listener
        backButton.setOnClickListener {
            val intent = Intent(applicationContext, UserProfileActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
            finish()
        }

        // About button listener
        aboutButton.setOnClickListener {
            val intent = Intent(applicationContext, AboutActivity::class.java)
            startActivity(intent)
        }

        // Setup language spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.languages,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            languageSpinner.adapter = adapter
        }

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLanguage = parent.getItemAtPosition(position).toString()
                Log.d("UserPreferencesActivity", "Language selected: $selectedLanguage")
                setLocale(selectedLanguage)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Setup theme switch
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            Log.d("UserPreferencesActivity", "Theme switched: $isChecked")
            setThemeMode(isChecked)
        }

        // Load stored preferences
        loadPreferences()
    }

    private fun setLocale(language: String) {
        val newLocale = when (language) {
            "lietuvių" -> "lt"
            else -> "en"
        }

        val sharedPreferences = getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        val currentLanguage = sharedPreferences.getString(PREFERENCE_LANGUAGE_KEY, Locale.getDefault().language)

        if (newLocale != currentLanguage) {
            val locale = Locale(newLocale)
            Locale.setDefault(locale)
            val config = Configuration()
            config.setLocale(locale)
            baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

            sharedPreferences.edit().putString(PREFERENCE_LANGUAGE_KEY, newLocale).apply()
            Log.d("UserPreferencesActivity", "Locale changed to: $newLocale")
            recreate()
        }
    }

    private fun setThemeMode(isNightMode: Boolean) {
        val themeMode = if (isNightMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(themeMode)
        saveThemePreference(isNightMode)
    }

    private fun saveThemePreference(isChecked: Boolean) {
        val sharedPreferences = getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(PREFERENCE_THEME_KEY, isChecked).apply()
    }

    private fun loadPreferences() {
        val sharedPreferences = getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        val currentLanguage = sharedPreferences.getString(PREFERENCE_LANGUAGE_KEY, Locale.getDefault().language)
        val isNightMode = sharedPreferences.getBoolean(PREFERENCE_THEME_KEY, false)

        val languageSpinner = findViewById<Spinner>(R.id.spinnerLanguage)
        val languageArray = resources.getStringArray(R.array.languages)
        val selectedPosition = when (currentLanguage) {
            "es" -> languageArray.indexOf("Spanish")
            else -> languageArray.indexOf("English")
        }

        if (selectedPosition >= 0) {
            languageSpinner.setSelection(selectedPosition)
        }

        val themeSwitch = findViewById<Switch>(R.id.switch1)
        themeSwitch.isChecked = isNightMode
        AppCompatDelegate.setDefaultNightMode(
            if (isNightMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
