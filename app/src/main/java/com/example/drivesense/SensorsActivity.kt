package com.example.drivesense

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.CircularArray
import com.aware.Aware
import com.aware.Aware_Preferences
import com.aware.LinearAccelerometer
import com.aware.providers.Linear_Accelerometer_Provider
import com.example.drivesense.databinding.ActivitySensorsBinding
import com.example.drivesense.ml.Behaviour
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import kotlin.math.pow
import com.example.drivesense.MovingAverageBuffer

private lateinit var binding: ActivitySensorsBinding

const val g = 9.80665

class SensorsActivity : AppCompatActivity() {

    var x_list = mutableListOf<Double>()
    var y_list = mutableListOf<Double>()
    var z_list = mutableListOf<Double>()

    val x_buffer = MovingAverageBuffer(20,4)
    val y_buffer = MovingAverageBuffer(20,4)
    val z_buffer = MovingAverageBuffer(20,4)





    lateinit var mainHandler: Handler
    var counts = intArrayOf(0,0,0,0)

    private val updateTextTask = object : Runnable {
        override fun run() {
            updateText()
            mainHandler.postDelayed(this, 100)
        }
    }

    fun model_classification(){
//      var data = ArrayList<Array<Array<Float>>>()
//      for (i in 0..29) {
//          data.add(arrayOf( arrayOf(x_buffer.get(i).toFloat()),arrayOf(y_buffer.get(i).toFloat()) ,arrayOf(z_buffer.get(i).toFloat())))
//      }

        var model = Behaviour.newInstance(this)

        var x_sample = x_buffer.get()
        var y_sample = y_buffer.get()
        var z_sample = z_buffer.get()
        var data = FloatArray(60)

        Log.d("x_sample", x_sample.size.toString())
        Log.d("y_sample", x_sample.size.toString())
        Log.d("z_sample", x_sample.size.toString())

        for (i in 0..19){
            data[i*3] = x_sample[i]
            data[i*3+1] = y_sample[i]
            data[i*3+2] = z_sample[i]
        }


        val inputFeature0 = TensorBuffer.createFixedSize( intArrayOf(1,20,3,1), DataType.FLOAT32 )

        inputFeature0.loadArray(data)

        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

        val maxIdx = outputFeature0.asList().indexOf(outputFeature0.maxOrNull())
        counts[maxIdx]++

        Log.d("rezultat", "lanch: %.2f, acc: %.2f, steer: %.2f".format(outputFeature0[0],outputFeature0[1],outputFeature0[2], outputFeature0[3]))

        binding.tvDetectedAccValue.text = "%.2f  %d".format(outputFeature0[2], counts[2])
        binding.tvDetectedSteeringValue.text = "%.2f  %d".format(outputFeature0[1], counts[1])
        binding.tvDetectedBreakingValue.text = "%.2f  %d".format(outputFeature0[0], counts[0])
        binding.tvDetectedLaneValue.text = "%.2f  %d".format(outputFeature0[3], counts[3])

        model.close()
    }

    fun updateText() {

        var x = (x_list.sum()/x_list.size) / g
        binding.tvAccXValue.text = "%.4f".format(x)
        x_buffer.add(x)
        x_list.clear()

        var y = (y_list.sum()/y_list.size) / g
        binding.tvAccYValue.text = "%.4f".format(y)
        y_buffer.add(y)
        y_list.clear()

        var z = (z_list.sum()/z_list.size) / g
        binding.tvAccZValue.text = "%.4f".format(z)
        z_buffer.add(z)
        z_list.clear()

        Log.d("", doubleArrayOf(x,y,z).toString())
        if (x_buffer.new_samples() >= 10 && x_buffer.buff_size() == 20) {
            model_classification()

        }
        //Log.d(x.toString(), "")


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_sensors)
        setSupportActionBar(findViewById(R.id.toolbar))

        // calling the action bar
        val actionBar = getSupportActionBar()

        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

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
                    //Log.d("ACC DATA:", data.toString())

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