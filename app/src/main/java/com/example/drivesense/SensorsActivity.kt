package com.example.drivesense

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.aware.LinearAccelerometer
import com.aware.Aware
import com.aware.Aware_Preferences
import com.aware.providers.Linear_Accelerometer_Provider
import com.example.drivesense.databinding.ActivitySensorsBinding
import androidx.collection.CircularArray
import kotlin.math.pow


private lateinit var binding: ActivitySensorsBinding

const val g = 9.80665

class SensorsActivity : AppCompatActivity() {

    var x_list = mutableListOf<Double>()
    var y_list = mutableListOf<Double>()
    var z_list = mutableListOf<Double>()

    val x_buffer = CircularArray<Double>(30)
    val y_buffer = CircularArray<Double>(30)
    val z_buffer = CircularArray<Double>(30)

    lateinit var mainHandler: Handler
    var t = true

    private val updateTextTask = object : Runnable {
        override fun run() {
            updateText()
            mainHandler.postDelayed(this, 100)
        }
    }

    fun model_classification(){

        Log.d("threshold reached", "")
        t = true
    }

    fun updateText() {
        var x = (x_list.sum()/x_list.size) / g
        x_buffer.addLast(x)
        binding.tvAccXValue.text = "%.4f".format(x)
        x_list.clear()

        var y = (y_list.sum()/y_list.size) / g
        y_buffer.addLast(y)
        binding.tvAccYValue.text = "%.4f".format(y)
        y_list.clear()

        var z = (z_list.sum()/z_list.size) / g
        z_buffer.addLast(z)
        binding.tvAccZValue.text = "%.4f".format(z)
        z_list.clear()

        var yz_mag = Math.sqrt(y.pow(2) + z.pow(2))

        if (yz_mag > 0.1 && t) {
            t = false
            Handler().postDelayed({
                model_classification()
            }, 2000)
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensors)

        mainHandler = Handler(Looper.getMainLooper())

        Aware.startAWARE(this) //initialise core AWARE service
        //sampling frequency in microseconds
        //Aware.setSetting(this, Aware_Preferences.FREQUENCY_LINEAR_ACCELEROMETER, 200000)
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_LINEAR_ACCELEROMETER, 10000)
        // intensity threshold to report the reading
        // Aware.setSetting(this, Aware_Preferences.THRESHOLD_LINEAR_ACCELEROMETER, 0.02f)
        Aware.setSetting(this, Aware_Preferences.THRESHOLD_LINEAR_ACCELEROMETER, 0.001f)

        binding = ActivitySensorsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        LinearAccelerometer.setSensorObserver { data ->
            try {
                runOnUiThread {
                    Log.d("ACC DATA:", data.toString())

//                    val x = data.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_0).toString()
//                    val y = data.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_1).toString()
//                    val z = data.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_2).toString()
//
//                    binding.tvAccXValue.text = x
//                    binding.tvAccYValue.text = y
//                    binding.tvAccZValue.text = z

                    val x = data.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_0)
                    val y = data.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_1)
                    val z = data.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_2)

                    x_list.add(x)
                    y_list.add(y)
                    z_list.add(z)
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Aware.stopLinearAccelerometer(this)
        mainHandler.removeCallbacks(updateTextTask)
    }

    override fun onResume() {
        super.onResume()
        Aware.startLinearAccelerometer(this)
        mainHandler.post(updateTextTask)
    }
}