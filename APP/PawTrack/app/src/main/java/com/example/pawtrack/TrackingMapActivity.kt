package com.example.pawtrack

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Timer
import kotlin.concurrent.timerTask


class CustomLocationOverlay(provider: IMyLocationProvider, map: MapView, private val updatePathCallback: (GeoPoint) -> Unit) : MyLocationNewOverlay(provider, map) {
    override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
        super.onLocationChanged(location, source)
        location?.let {
            val geoPoint = GeoPoint(location.latitude, location.longitude)
            updatePathCallback(geoPoint)
        }
    }
}
class TrackingMapActivity : AppCompatActivity() {

    private var map: MapView? = null
    private var myLocationOverlay: MyLocationNewOverlay? = null
    private var pathOverlay: Polyline? = null
    private var isTracking = false
    private var startGeoPoint: GeoPoint? = null
    private var endGeoPoint: GeoPoint? = null
    private var timer: Timer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tracking_map_layout)
        val username = intent.getStringExtra("USERNAME")
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.tracking
        setupMap()

        val petprofileButton = findViewById<FloatingActionButton>(R.id.pet_profile)
        petprofileButton.setOnClickListener(){
            val intent = Intent(applicationContext, PetProfileActivity::class.java)
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

        val startButton = findViewById<Button>(R.id.btnPetStart)
        startButton.setOnClickListener(){
            toggleTracking(startButton)
        }

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
    }
    private fun setupMap() {
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        map = findViewById(R.id.map)
        map?.setTileSource(TileSourceFactory.MAPNIK)
        map?.setBuiltInZoomControls(false)
        map?.setMultiTouchControls(true)

        val mapController = map?.controller
        mapController?.setZoom(18.0)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            enableLocationTracking()
            setupPathOverlay()
        }
    }

    private fun enableLocationTracking() {
        val locationOverlay =
            map?.let {
                CustomLocationOverlay(GpsMyLocationProvider(this), it) { location ->
                    updatePath(location)
                }
            }
        map?.overlays?.add(locationOverlay)
        locationOverlay?.enableMyLocation()
        locationOverlay?.enableFollowLocation()
        /*myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        myLocationOverlay?.enableMyLocation()
        myLocationOverlay?.runOnFirstFix {
            runOnUiThread {
                map?.controller?.setCenter(myLocationOverlay?.myLocation)
            }
        }
        myLocationOverlay?.enableFollowLocation()
        map?.overlays?.add(myLocationOverlay)
        setupPathOverlay()*/
    }

    private fun setupPathOverlay() {
        pathOverlay = Polyline()
        pathOverlay?.color = ContextCompat.getColor(this, R.color.app_theme)
        map?.overlays?.add(pathOverlay)
    }

    private fun updatePath(location: GeoPoint) {
        if (isTracking) {
            pathOverlay?.addPoint(location)
            map?.invalidate()
        }
    }

    private fun toggleTracking(startButton: Button) {
        isTracking = !isTracking

        if (isTracking) {
            startGeoPoint = myLocationOverlay?.myLocation
            pathOverlay?.points?.clear()
            myLocationOverlay?.enableFollowLocation()
            startLocationPolling()
        } else {
            endGeoPoint = myLocationOverlay?.myLocation
            saveTrip(startGeoPoint, endGeoPoint)
            myLocationOverlay?.disableFollowLocation()
            stopLocationPolling()
        }

        updateButtonAndIcon(startButton)
    }
    private fun startLocationPolling() {
        val timer = Timer()
        timer.schedule(timerTask {
            runOnUiThread {
                myLocationOverlay?.myLocation?.let {
                    updatePath(it)
                }
            }
        }, 0, 1000)
    }
    private fun stopLocationPolling() {
        timer?.cancel()
        timer = null
    }
    private fun updateButtonAndIcon(startButton: Button) {
        if (isTracking) {
            startGeoPoint = myLocationOverlay?.myLocation
            val stopIcon: Drawable? = ContextCompat.getDrawable(this, R.drawable.stop_icon)
            startButton.setCompoundDrawablesWithIntrinsicBounds(stopIcon, null, null, null)
            startButton.text = getString(R.string.map_stop)
            myLocationOverlay?.disableFollowLocation()
        } else {
            startGeoPoint = myLocationOverlay?.myLocation
            val startIcon: Drawable? = ContextCompat.getDrawable(this, R.drawable.start_icon)
            startButton.setCompoundDrawablesWithIntrinsicBounds(startIcon, null, null, null)
            startButton.text = getString(R.string.map_start)
            myLocationOverlay?.enableFollowLocation()
        }
    }
    private fun saveTrip(start: GeoPoint?, end: GeoPoint?) {
        Toast.makeText(this, "Trip completed", Toast.LENGTH_SHORT).show()
    }
}