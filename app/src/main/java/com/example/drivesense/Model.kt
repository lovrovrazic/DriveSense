package com.example.drivesense

import android.content.Context
import android.util.Log
import com.example.drivesense.ml.Behaviour
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class Model(var orientation:Boolean) {
    private var x_list = mutableListOf<Double>()
    private var y_list = mutableListOf<Double>()
    private var z_list = mutableListOf<Double>()

    private val x_buffer = MovingAverageBuffer(20,4)
    private val y_buffer = MovingAverageBuffer(20,4)
    private val z_buffer = MovingAverageBuffer(20,4)

    private val efficiency = Efficiency(orientation)

    private var horizontalOrientation = orientation

    private lateinit var model: Behaviour

    private val g = 9.80665


    // fill x,y,z lists with more than 10Hz
    fun add(x:Double, y:Double, z:Double){
        x_list.add(x)
        y_list.add(y)
        z_list.add(z)
    }
    // average samples in mutable list and add it to moving average buffer
    // called every 100ms (10Hz)
    // return: True if enough samples in buffer to make classification
    fun average(): Boolean {
        val x = (x_list.sum()/x_list.size) / g
        x_buffer.add(x)
        x_list.clear()

        val y = (y_list.sum()/y_list.size) / g
        y_buffer.add(y)
        y_list.clear()

        val z = (z_list.sum()/z_list.size) / g
        z_buffer.add(z)
        z_list.clear()

        // if enough samples in buffer return True
        return (x_buffer.new_samples() >= 10 && x_buffer.buff_size() == 20)
    }

    // update orientation of the phone
    // True - horizontal, False - vertical
    fun update_orientation(orientation: Boolean){
        horizontalOrientation = orientation
        efficiency.changeOrientation(orientation)

        // terminate previous sample
        x_buffer.reset_samples()
        y_buffer.reset_samples()
        z_buffer.reset_samples()
    }

    fun startModel(contex:Context) {
        model = Behaviour.newInstance(contex)
    }

    fun closeModel(){
        model.close()
    }
    //
    fun model_classification(contex:Context): Int {
        // init behaviour model
        //startModel(contex)

        // get samples
        val x_sample = x_buffer.get()
        val y_sample = y_buffer.get()
        val z_sample = z_buffer.get()
        val data = FloatArray(60)

        // fill up data array
        // consider phone orientation
        if(horizontalOrientation){
            //Log.d("horizontal","classification")
            for (i in 0..19){
                data[i*3] = x_sample[i]
                data[i*3+1] = y_sample[i]
                data[i*3+2] = z_sample[i]
            }
        }else{
            //Log.d("vertical","classification")
            for (i in 0..19){
                data[i*3] = y_sample[i]
                data[i*3+1] = x_sample[i] * -1
                data[i*3+2] = z_sample[i]
            }
        }


        // reshape and prepare data for classification
        val inputFeature0 = TensorBuffer.createFixedSize( intArrayOf(1,20,3,1), DataType.FLOAT32 )
        inputFeature0.loadArray(data)

        // run model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

        // get output of a model
        val classification = outputFeature0.asList().indexOf(outputFeature0.maxOrNull())

        //Log.d("orientation", horizontalOrientation.toString())

        // close model
        //closeModel()

        efficiency.add(x_sample,y_sample,z_sample, classification)


        return classification
    }
}