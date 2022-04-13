package com.example.drivesense

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.aware.LinearAccelerometer
import com.aware.Aware
import com.aware.Aware_Preferences
import com.aware.providers.Linear_Accelerometer_Provider
import com.example.drivesense.databinding.ActivitySensorsBinding

private lateinit var binding: ActivitySensorsBinding

class SensorsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensors)

        Aware.startAWARE(this) //initialise core AWARE service
        //sampling frequency in microseconds
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_LINEAR_ACCELEROMETER, 200000)
        // intensity threshold to report the reading
        Aware.setSetting(this, Aware_Preferences.THRESHOLD_LINEAR_ACCELEROMETER, 0.02f)

        binding = ActivitySensorsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        LinearAccelerometer.setSensorObserver { data ->
            try {
                runOnUiThread {
                    Log.d("ACC DATA:", data.toString())
                    val x = data.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_0).toString()
                    val y = data.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_1).toString()
                    val z = data.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_2).toString()

                    binding.tvAccXValue.text = x
                    binding.tvAccYValue.text = y
                    binding.tvAccZValue.text = z
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Aware.stopLinearAccelerometer(this)
    }

    override fun onResume() {
        super.onResume()
        Aware.startLinearAccelerometer(this)
    }
}