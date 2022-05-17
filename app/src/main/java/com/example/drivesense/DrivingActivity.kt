package com.example.drivesense

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import com.aware.Aware
import com.aware.Aware_Preferences
import com.aware.LinearAccelerometer
import com.aware.providers.Linear_Accelerometer_Provider
import com.example.drivesense.databinding.ActivityDrivingBinding

private lateinit var binding: ActivityDrivingBinding

class DrivingActivity : AppCompatActivity() {

    lateinit var machine_learning: Model
    lateinit var speeding: Speeding

    lateinit var mainHandler: Handler
    var counts = intArrayOf(0,0,0,0)

    private val updateTextTask = object : Runnable {
        override fun run() {
            average_samples()
            mainHandler.postDelayed(this, 100)
        }
    }

    // average acc readings every 100ms (10Hz)
    fun average_samples() {
        // if average equal true, means that enough samples in buffer to classify
        if (machine_learning.average()){
            // classify: 0-breaking, 1-steering, 2-acceleration, 3-null
            val classification = machine_learning.model_classification(this)
            counts[classification]++
            // print counts
            Log.d("counts", counts.map { it.toString() }.toTypedArray().contentToString())
        }
        binding.breakingScoreTextView.text = "%d".format(counts[0])
        binding.steeringScoreTextView.text = "%d".format(counts[1])
        binding.accelerationScoreTextView.text = "%d".format(counts[2])
        binding.speedScoreTextView.text = "%d".format(counts[3])
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()

        //speeding = Speeding(this)
        machine_learning = Model(isHorizontal())
        mainHandler = Handler(Looper.getMainLooper())

        Aware.startAWARE(this) //initialise core AWARE service
        //sampling frequency in microseconds
        //Aware.setSetting(this, Aware_Preferences.FREQUENCY_LINEAR_ACCELEROMETER, 200000)
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_LINEAR_ACCELEROMETER, 10000)
        // intensity threshold to report the reading
        // Aware.setSetting(this, Aware_Preferences.THRESHOLD_LINEAR_ACCELEROMETER, 0.02f)
        Aware.setSetting(this, Aware_Preferences.THRESHOLD_LINEAR_ACCELEROMETER, 0.001f)

        LinearAccelerometer.setSensorObserver { data ->
            try {
                runOnUiThread {
                    val x = data.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_0)
                    val y = data.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_1)
                    val z = data.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_2)

                    // add readings list
                    machine_learning.add(x,y,z)
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        initView()

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            machine_learning.update_orientation(true)
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            machine_learning.update_orientation(false)
        }
    }

    private fun isHorizontal(): Boolean {
        val config: Configuration = resources.configuration
        return config.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    private fun initView() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_driving)
        binding = ActivityDrivingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(findViewById(R.id.toolbar))

        // calling the action bar
        val actionBar = getSupportActionBar()

        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onPause() {
        super.onPause()
        Aware.stopLinearAccelerometer(this)
        mainHandler.removeCallbacks(updateTextTask)
        speeding.stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        Aware.startLinearAccelerometer(this)
        mainHandler.post(updateTextTask)
        //speeding.startLocationUpdates(this)
    }
}