package com.example.drivesense

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.drivesense.databinding.ActivityGpsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import de.westnordost.osmapi.ApiResponseReader
import de.westnordost.osmapi.OsmConnection
import de.westnordost.osmapi.overpass.OverpassMapDataApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.json.JSONTokener
import java.io.BufferedReader
import java.io.InputStream


class GpsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGpsBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var mainHandler: Handler
    private lateinit var cts: CancellationTokenSource
    private lateinit var responseHandler: ResponseHandler

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

        binding.tvGpsHist.setMovementMethod(ScrollingMovementMethod())

        mainHandler = Handler(Looper.getMainLooper())
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        responseHandler = ResponseHandler()

        try {
            getActualLocation()
        }catch (e: java.lang.Exception){
            e.printStackTrace()
        }
    }

    private fun getActualLocation() {
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
                getSpeedLimit(it)
            }
        }
    }

    private val updateLocationTask = object : Runnable {
        override fun run() {
            getActualLocation()
            mainHandler.postDelayed(this, 30000)
        }
    }

    private fun getSpeedLimit(location: Location) {
        val connection = OsmConnection("https://overpass-api.de/api/", System.getProperty("http.agent"))
        val overpass = OverpassMapDataApi(connection)

        GlobalScope.launch(Dispatchers.IO) {
            val result = overpass.query(
                "[out:json];\n" +
                        "way(around:10, ${location.latitude}, ${location.longitude})[maxspeed];\n" +
                        "out;",
                responseHandler
            )
            val maxSpeed = parseJson(result)
            displayResult(location.latitude, location.longitude, maxSpeed, location.speed*3.6f)
        }
    }

    private fun displayResult(lati: Double, long: Double, maxSpeed: Int, speed: Float) {
        binding.tvGpsHist.append("lati:%s long:%s speed_limit: %d speed: %.2f\n".format(lati, long, maxSpeed, speed))
    }

    private fun parseJson(s: String): Int {
        val jsonObject = JSONTokener(s).nextValue() as JSONObject
        val jsonArray = jsonObject.getJSONArray("elements")

        var maxSpeed = 0
        for (i in 0 until jsonArray.length()) {
            val currentSpeed = jsonArray.getJSONObject(i).getJSONObject("tags").getInt("maxspeed")
            if (maxSpeed < currentSpeed)
                maxSpeed = currentSpeed
        }
        return maxSpeed
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

class ResponseHandler : ApiResponseReader<String> {
    override fun parse(`in`: InputStream?): String {
        val reader = BufferedReader(`in`?.reader())
        val content = StringBuilder()
        try {
            var line = reader.readLine()
            while (line != null) {
                content.append(line)
                line = reader.readLine()
            }
        } finally {
            reader.close()
        }
        return content.toString()
    }
}