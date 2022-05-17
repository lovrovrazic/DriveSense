package com.example.drivesense

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
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

class Speeding(activity: Activity) {

    private var fusedLocationProviderClient: FusedLocationProviderClient
    var mainHandler: Handler
    private var responseHandler: ResponseHandler
    private var locationRequest: LocationRequest
    private var locationCallback: LocationCallback
    private var currentScore: Double
    private var validReadings: Int
    private var speedingReadings: Int
    private val DETECTION_THRESHOLD = 10 //kmph
    private val SPEEDING_TOLERANCE = 5 //kmph

    init {
        mainHandler = Handler(Looper.getMainLooper())
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
        responseHandler = ResponseHandler()
        currentScore = 100.0
        validReadings = 0
        speedingReadings = 0

        locationRequest = LocationRequest.create().apply {
            interval = 30000
            fastestInterval = 30000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0 ?: return
                for (location in p0.locations){
                    getSpeedLimit(location)
                }
            }
        }
    }

    fun getCurrentScore(): Double {
        return currentScore
    }

    private fun updateScore(location: Location, maxSpeed: Int) {
        val speed = location.speed*3.6f //kmph
        if (speed > DETECTION_THRESHOLD) {
            ++validReadings
            if (speed > maxSpeed+SPEEDING_TOLERANCE) {
                ++speedingReadings
            }
            currentScore = (1.0 - (speedingReadings.toDouble()/validReadings)) * 100.0
        }
    }

    private fun getSpeedLimit(location: Location) {
        val connection = OsmConnection("https://overpass-api.de/api/", System.getProperty("http.agent"))
        val overpass = OverpassMapDataApi(connection)

        GlobalScope.launch(Dispatchers.IO) {
            val result = overpass.query(
                "[out:json];\n" +
                        "way(around:20, ${location.latitude}, ${location.longitude})[maxspeed];\n" +
                        "out;",
                responseHandler
            )
            val maxSpeed = parseJson(result)
            Log.d("GPS", "lati:%s long:%s speed_lim: %d speed: %.2f".format(location.latitude, location.longitude, maxSpeed, location.speed*3.6f))
            updateScore(location, maxSpeed)
        }
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

    fun startLocationUpdates(context: Context) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(context as Activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }

    fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
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