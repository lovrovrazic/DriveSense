package com.example.drivesense

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.drivesense.databinding.ActivityGpsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource


class GpsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGpsBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var mainHandler: Handler
    private lateinit var cts: CancellationTokenSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding = ActivityGpsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(findViewById(R.id.toolbar))

        // calling the action bar
        val actionBar = getSupportActionBar()

        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        mainHandler = Handler(Looper.getMainLooper())
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            getActualLocation()
        }catch (e: java.lang.Exception){
            e.printStackTrace()
        }
    }

    private fun getActualLocation() {

        //val task = fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY)

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }

        if(::cts.isInitialized)
            cts.cancel()

        cts = CancellationTokenSource()
        val task = fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, cts.token)

        task.addOnSuccessListener {
            if (it != null){
                binding.tvGpsHist.append("lati:%s long:%s\n".format(it.latitude, it.longitude))
                Log.d("latitude: ${it.latitude}", "")
                Log.d("longitude: ${it.longitude}", "")
            }
        }
    }

    private val updateLocationTask = object : Runnable {
        override fun run() {
            getActualLocation()
            mainHandler.postDelayed(this, 5000)
        }
    }

    override fun onResume() {
        super.onResume()
        mainHandler.post(updateLocationTask)
    }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(updateLocationTask)
        if(::cts.isInitialized)
            cts.cancel()
    }
}