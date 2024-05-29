package com.example.pawtrack

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel>()
    private val PREFERENCES_FILE = "com.example.pawtrack.preferences"
    private val PREFERENCE_THEME_KEY = "theme"
    private val PREFERENCE_LANGUAGE_KEY = "language"

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition{
                !viewModel.isReady.value
            }
            setOnExitAnimationListener{
                    screen -> val zoomX = ObjectAnimator.ofFloat(
                screen.iconView,
                View.SCALE_X,
                0.4f,
                0.0f
            )
                zoomX.interpolator = OvershootInterpolator()
                zoomX.duration = 500L
                zoomX.doOnEnd { screen.remove() }
                val zoomY = ObjectAnimator.ofFloat(
                    screen.iconView,
                    View.SCALE_X,
                    0.4f,
                    0.0f
                )
                zoomY.interpolator = OvershootInterpolator()
                zoomY.duration = 500L
                zoomY.doOnEnd { screen.remove() }

                zoomX.start()
                zoomY.start()
            }
        }
        super.onCreate(savedInstanceState)
        loadUserPreferences()

        setLanguage()

        val (token, username) = getToken(applicationContext)
        if (token.isNullOrEmpty() || username.isNullOrEmpty()) {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
            finish()
        } else {
            val homeIntent = Intent(this, HomePageActivity::class.java).apply {
                putExtra("USERNAME", username)
            }
            startActivity(homeIntent)
            finish()
        }
        // Initialize osmdroid configuration
        //Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE))
    }
    private fun loadUserPreferences() {
        val sharedPreferences = getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        val isNightMode = sharedPreferences.getBoolean(PREFERENCE_THEME_KEY, false)

        // Set the appropriate theme based on the retrieved preference
        if (isNightMode) {
            setTheme(R.style.Theme_PawTrack2_Dark) // Use the dark theme
        } else {
            setTheme(R.style.Theme_PawTrack2) // Use the light theme
        }
    }
    fun openMap(view: View) {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }

    fun onSetReminderButtonClick(view: View) {
        // Start the ReminderSettingActivity
        val intent = Intent(this, ReminderSettingActivity::class.java)
        startActivity(intent)
    }

    fun getToken(context: Context): Triple<String?, String?, String?> {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            "user_preferences",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val token = sharedPreferences.getString("user_token", null)
        val username = sharedPreferences.getString("USERNAME", null)
        val pet_id = sharedPreferences.getString("PET_ID", null)
        return Triple(token, username, pet_id)
    }

    fun setLanguage(){
        val sharedPreferences = getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        val newLocale = sharedPreferences.getString(PREFERENCE_LANGUAGE_KEY, Locale.getDefault().language);

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
