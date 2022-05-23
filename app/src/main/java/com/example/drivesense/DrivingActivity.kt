package com.example.drivesense

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import com.aware.Aware
import com.aware.Aware_Preferences
import com.aware.LinearAccelerometer
import com.aware.providers.Linear_Accelerometer_Provider
import com.example.drivesense.databinding.ActivityDrivingBinding
import androidx.activity.viewModels

private lateinit var binding: ActivityDrivingBinding

class DrivingActivity : AppCompatActivity() {

    lateinit var machine_learning: Model
    lateinit var speeding: Speeding
    var recording: Boolean = false
    var breakingScore: Int = 0
    var accelerationScore: Int = 0
    var steeringScore: Int = 0
    var speedingScore: Int = 0
    var startTime: Long = 0
    var elapsedTime: Long = 0

    lateinit var mainHandler: Handler
    var counts = intArrayOf(0,0,0,0)

    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory((application as MainApplication).repository)
    }

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
        //binding.breakingScoreTextView.text = "%d".format(counts[0])
        //binding.steeringScoreTextView.text = "%d".format(counts[1])
        //binding.accelerationScoreTextView.text = "%d".format(counts[2])
        //binding.speedScoreTextView.text = "%d".format(counts[3])
    }

    fun updateScores() {
        breakingScore = machine_learning.getScoreBreaking()
        accelerationScore = machine_learning.getScoreAcceleration()
        steeringScore = machine_learning.getScoreSteering()
        speedingScore = speeding.getCurrentScore()

        // update breaking, steering, acceleration scores
        Log.d("breaking score", "%d".format(breakingScore))
        Log.d("steering score", "%d".format(steeringScore))
        Log.d("acceleration score", "%d".format(accelerationScore))
        binding.breakingScoreTextView.text = "%d".format(breakingScore)
        binding.breakingScoreTextView.setTextColor(colorInterpolation(breakingScore))
        binding.steeringScoreTextView.text = "%d".format(steeringScore)
        binding.steeringScoreTextView.setTextColor(colorInterpolation(steeringScore))
        binding.accelerationScoreTextView.text = "%d".format(accelerationScore)
        binding.accelerationScoreTextView.setTextColor(colorInterpolation(accelerationScore))
        binding.speedScoreTextView.text = "%d".format(speedingScore)
        binding.speedScoreTextView.setTextColor(colorInterpolation(speedingScore))

        val overAll = (breakingScore+accelerationScore+steeringScore+speedingScore)/4
        setOverallScore(overAll)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        speeding = Speeding(this)
        machine_learning = Model(isHorizontal())
        mainHandler = Handler(Looper.getMainLooper())

        initView()

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
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        initView()
        setStartButton()

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

        val startBtn = findViewById<Button>(R.id.start_button)
        startBtn.setOnClickListener{
            if (!recording) {
                startScoring()
            } else {
                stopScoring()
                showSummary()
                saveRecord()
            }
        }

        updateScores()
    }

    fun startScoring() {
        recording = true
        machine_learning.startModel(this)
        Aware.startLinearAccelerometer(this)
        mainHandler.post(updateTextTask)
        mainHandler.post(updateScoresTask)
        speeding.startLocationUpdates(this)
        setStartButton()
        startTime = System.currentTimeMillis()
    }

    fun stopScoring() {
        if (!recording)
            return
        resetScores()
        recording = false
        machine_learning.closeModel()
        Aware.stopLinearAccelerometer(this)
        mainHandler.removeCallbacks(updateTextTask)
        mainHandler.removeCallbacks(updateScoresTask)
        speeding.stopLocationUpdates()
        setStartButton()
        elapsedTime = System.currentTimeMillis()-startTime
    }

    fun setStartButton() {
        val startBtn = findViewById<Button>(R.id.start_button)
        if (recording) {
            startBtn.text = getString(R.string.start_button_active)
            startBtn.setBackgroundColor(getColor(R.color.red))
        } else {
            startBtn.text = getString(R.string.start_button)
            startBtn.setBackgroundColor(getColor(R.color.green_200))
        }
    }

    fun setOverallScore(score: Int) {
        findViewById<ImageView>(R.id.needle_imageView).animate().setDuration(1000).rotation((score-50)*2.69f).start()
        binding.scoreTextView.text = "%d".format(score)
    }

    fun resetScores() {
        breakingScore = 0
        accelerationScore = 0
        steeringScore = 0
        speedingScore = 0
    }

    fun showSummary() {
        val minutes = elapsedTime / 1000 / 60
        val seconds = elapsedTime / 1000 % 60
        val overallScore = (breakingScore+accelerationScore+steeringScore+speedingScore)/4
        val dialogBuilder = AlertDialog.Builder(this)
        val summary =
            "${getString(R.string.acceleration_score)} $accelerationScore\n"+
            "${getString(R.string.breaking_score)} $breakingScore\n"+
            "${getString(R.string.steering_score)} $steeringScore\n"+
            "${getString(R.string.speeding_score)} $speedingScore\n"+
            "${getString(R.string.overall_score)} $overallScore\n"+
            "${getString(R.string.elapsed_time)} $minutes min $seconds sec"

        dialogBuilder.setMessage(summary)
            .setCancelable(false)
//            .setPositiveButton("Proceed", DialogInterface.OnClickListener {
//                    dialog, id -> finish()
//            })
            .setNegativeButton("Close", DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
            })

        val alert = dialogBuilder.create()
        alert.setTitle("Summary")
        alert.show()
    }

    fun saveRecord() {
        val record: Record = Record(
            0,
            accelerationScore,
            breakingScore,
            steeringScore,
            speedingScore,
            (breakingScore+accelerationScore+steeringScore+speedingScore)/4,
            elapsedTime,
            System.currentTimeMillis()
        )

        historyViewModel.insert(record)
    }

    override fun onPause() {
        super.onPause()
        stopScoring()
    }

    override fun onResume() {
        super.onResume()
    }

//   fun colorInterpolation(percent:Int): Int {
//       val startColor = ContextCompat.getColor(this, R.color.red)
//       val endColor = ContextCompat.getColor(this, R.color.green_200)
//       val midColor = Color.rgb(255, 255, 102)

//       return when{
//           percent < 50 -> ColorUtils.blendARGB(startColor, midColor, percent/50f)
//           else -> ColorUtils.blendARGB(midColor, endColor, (percent-50)/50f)
//       }
//   }

    fun colorInterpolation(percent:Int): Int {
        val h = (percent/100f) * 0.3f * 360 // Hue (note 0.4 = Green, see huge chart below)
        val s = 0.74f + ((percent/100f) * 0.26f)
        val b = 0.90f - ((percent/100f) * 0.23f)

        return Color.HSVToColor(floatArrayOf(h,s,b))
    }


}