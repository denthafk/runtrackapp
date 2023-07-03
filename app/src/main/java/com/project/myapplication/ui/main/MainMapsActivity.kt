package com.project.myapplication.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.project.myapplication.databinding.ActivityMainMapsBinding
import java.util.*
import com.project.myapplication.R
import com.project.myapplication.data.model.TrackEntity
import com.project.myapplication.ui.history.HistoryActivity
import com.project.myapplication.ui.viewmodels.MainViewModel
import com.project.myapplication.ui.viewmodels.factory.MainViewModelFactory
import com.project.myapplication.ui.widget.RuntrackAppWidget
import kotlin.math.roundToInt

class MainMapsActivity : AppCompatActivity(), OnMapReadyCallback, SensorEventListener {

    private val mainViewModel by viewModels<MainViewModel> {
        MainViewModelFactory(this.application)
    }

    private var initialStepCount = -1
    private var currentNumberOfStepCount = 0

    private var mMap: GoogleMap? = null
    private lateinit var binding: ActivityMainMapsBinding
    private var appWidgetManager: AppWidgetManager? = null

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var polylineOptions = PolylineOptions()
    private var timer = Timer()

    private var isBtnStarted = false
    private var distanceTravelled = 0f
    private var timeCount = 0

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            if (locationResult.locations.size > 0) {
                addToLocationRoute(locationResult.locations)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Runtrack"

        appWidgetManager = AppWidgetManager.getInstance(
            this)
        if (!hasPermissions(this, *PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL)
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        setListeners()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        showUserLocation()
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient!!.lastLocation.addOnSuccessListener { location: Location ->
                val startLocation = LatLng(location.latitude, location.longitude)
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 17f))
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setListeners() {
        if (!isBtnStarted) {
            binding.tvNumberStep.text = "0 Steps"
            binding.tvAveragePace.text = "0 Seconds"
            showContent(false)
        }
        binding.btnAction.setOnClickListener {
            if (!isBtnStarted) {
                isBtnStarted = true
                polylineOptions = PolylineOptions()
                latLngList.clear()
                mMap!!.clear()
                sendDataToWidget(0,
                    0, 0f)
                binding.tvNumberStep.text = "0 Steps"
                binding.tvAveragePace.text = "0 Seconds"
                showContent(false)
                setupStepCounterListener()
                setupLocationChangeListener()
                timeCount = 0
                timer = Timer()
                timer.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        runOnUiThread {
                            timeCount++
                            binding.tvAveragePace.text = StringBuilder("$timeCount Seconds")
                            sendDataToWidget(currentNumberOfStepCount,
                                timeCount, 0f)
                        }
                    }
                }, 1000, 1000)
                binding.btnAction.text = "end"
            } else if (isBtnStarted) {
                isBtnStarted = false
                timer.cancel()
                for (i in latLngList.indices) {
                    if (i > 0) {
                        val locationA =
                            Location("Previous Location")
                        locationA.latitude = latLngList[i - 1].latitude
                        locationA.longitude = latLngList[i - 1].longitude
                        val locationB =
                            Location("New Location")
                        locationB.latitude = latLngList[i].latitude
                        locationB.longitude = latLngList[i].longitude
                        val meter = locationA.distanceTo(locationB)
                        distanceTravelled += meter
                    }
                }
                showContent(true)

                val trackEntity = TrackEntity()
                trackEntity.totalDistance = distanceTravelled.toString()
                trackEntity.totalSteps = currentNumberOfStepCount.toString()
                trackEntity.totalDuration = timeCount.toString()

                binding.tvTotalDistance.text = StringBuilder("$distanceTravelled meter")
                binding.tvAveragePace.text = StringBuilder("$timeCount Seconds")
                binding.btnAction.text = "start"
                sendDataToWidget(currentNumberOfStepCount,
                    timeCount, distanceTravelled)


                mainViewModel.insertTrack(trackEntity)
                stopTracking()
            }
        }
    }

    private fun showUserLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap!!.isMyLocationEnabled = true
        }
    }

    private fun sendDataToWidget(
        currentNumberSteps: Int,
        timerCounter: Int,
        distanceTravelled: Float,
    ) {
        val intent = Intent(this, RuntrackAppWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra("current_steps", currentNumberSteps)
        intent.putExtra("total_distance", distanceTravelled)
        intent.putExtra("timer_counter", timerCounter)
        val ids = AppWidgetManager.getInstance(this).getAppWidgetIds(ComponentName(this,
            RuntrackAppWidget::class.java))
        if (ids != null && ids.isNotEmpty()) {
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            sendBroadcast(intent)
        }
    }

    private fun hasPermissions(context: Context?, vararg permissions: String?): Boolean {
        if (context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context,
                        permission!!) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    private fun addToLocationRoute(locations: List<Location>) {
        mMap!!.clear()
        val originalLatLngList = polylineOptions.points
        for (location in locations) {
            latLngList.add(LatLng(location.latitude, location.longitude))
        }
        originalLatLngList.addAll(latLngList)
        mMap!!.addPolyline(polylineOptions)
    }

    private fun setupLocationChangeListener() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationRequest = LocationRequest()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 5000
            fusedLocationProviderClient!!.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper())
        }
    }

    private fun setupStepCounterListener() {
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            ?: return
        sensorManager.registerListener(this@MainMapsActivity,
            stepCounterSensor,
            SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val arr = event.values
        if (arr.isNotEmpty()) {
            if (initialStepCount == -1) {
                initialStepCount = arr[0].roundToInt()
            }
            currentNumberOfStepCount = arr[0].roundToInt() - initialStepCount
            Log.d("TAG", "Steps count: $currentNumberOfStepCount")
            binding.tvNumberStep.text = StringBuilder("$currentNumberOfStepCount Steps")
            sendDataToWidget(currentNumberOfStepCount,
                timeCount, distanceTravelled)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Log.d("TAG", "onAccuracyChanged: Sensor: $sensor; accuracy: $accuracy")
    }

    private fun showContent(isShow: Boolean) {
        if (isShow) {
            binding.tvTotalDistance.visibility = View.VISIBLE
            binding.dumbPoint1.visibility = View.VISIBLE
            binding.dumbDistance.visibility = View.VISIBLE
        } else {
            binding.tvTotalDistance.visibility = View.GONE
            binding.dumbPoint1.visibility = View.GONE
            binding.dumbDistance.visibility = View.GONE
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ALL) {
            for (i in permissions.indices) {
                val permission = permissions[i]
                val grantResult = grantResults[i]
                if (permission == Manifest.permission.ACCESS_FINE_LOCATION) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        finish()
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun stopTracking() {
        fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        initialStepCount = -1
        currentNumberOfStepCount = 0
        val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        sensorManager.unregisterListener(this, stepCounterSensor)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_history -> {
                val iFav = Intent(this, HistoryActivity::class.java)
                startActivity(iFav)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        var latLngList = ArrayList<LatLng>()

        var PERMISSION_ALL = 1

        var PERMISSIONS = arrayOf(
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}