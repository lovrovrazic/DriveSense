package com.example.drivesense

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import com.aware.Aware
import com.aware.Aware_Preferences
import com.aware.LinearAccelerometer
import com.aware.providers.Linear_Accelerometer_Provider
import com.example.drivesense.databinding.ActivityDrivingBinding

private lateinit var binding: ActivityDrivingBinding

class DrivingActivity : AppCompatActivity() {

    lateinit var machine_learning: Model
    lateinit var speeding: Speeding
    var recording: Boolean = false

    lateinit var mainHandler: Handler
    var counts = intArrayOf(0,0,0,0)

    private val updateTextTask = object : Runnable {
        override fun run() {
            averageSamples()
            mainHandler.postDelayed(this, 100)
        }
    }

    private val updateScoresTask = object : Runnable {
        override fun run() {
            updateScores()
            mainHandler.postDelayed(this, 10000)
        }
    }

    // average acc readings every 100ms (10Hz)
    fun averageSamples() {
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
        //binding.speedScoreTextView.text = "%d".format(counts[3])
    }

    fun updateScores() {
        binding.speedScoreTextView.text = "%d".format(speeding.getCurrentScore().toInt())


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()

        speeding = Speeding(this)
        machine_learning = Model(isHorizontal())
        mainHandler = Handler(Looper.getMainLooper())

        Aware.startAWARE(this) //initialise core AWARE service
        //sampling frequency in microseconds - default: 200000
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_LINEAR_ACCELEROMETER, 10000)
        // intensity threshold to report the reading - default: 0.02f
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

        val startBtn = findViewById<Button>(R.id.start_button)
        startBtn.setOnClickListener{
            if (!recording) {
                startScoring()
                startBtn.text = getString(R.string.start_button_active)
                recording = true
            } else {
                stopScoring()
                startBtn.text = getString(R.string.start_button)
                recording = false
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

    fun startScoring() {
        Aware.startLinearAccelerometer(this)
        mainHandler.post(updateTextTask)
        mainHandler.post(updateScoresTask)
        speeding.startLocationUpdates(this)
    }

    fun stopScoring() {
        Aware.stopLinearAccelerometer(this)
        mainHandler.removeCallbacks(updateTextTask)
        mainHandler.removeCallbacks(updateScoresTask)
        speeding.stopLocationUpdates()
    }

    fun resetScores() {
        binding.speedScoreTextView.text = "0"
    }

    override fun onPause() {
        super.onPause()
        stopScoring()
    }

    override fun onResume() {
        super.onResume()
    }
}