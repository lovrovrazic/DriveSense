package com.example.drivesense
import android.util.Log
import androidx.collection.CircularArray

class MovingAverageBuffer(var bufSize: Int, var period: Int) {
    private val buffer = CircularArray<Double>()
    private val window = mutableListOf<Double>()
    private var new_samples:Int = 0

    // add number
    fun add(e:Double) {
        if (window.size < period){
            window.add(e)
        }else{
            window.removeFirst()
            window.add(e)
            var window_average = window.sum() / window.size
            buffer.addLast(window_average)
            new_samples ++

            if(buffer.size() > bufSize) buffer.popFirst()
        }
    }
    fun new_samples(): Int {
        return new_samples
    }

    fun buff_size(): Int {
        return buffer.size()
    }

    // get sample
    fun get(): FloatArray {
        reset_samples()

        var data = FloatArray(buffer.size())
        for (i in 0..buffer.size()-1) {
            data[i] = buffer.get(i).toFloat()
        }
        return data
    }


    fun reset_samples(){
        new_samples = 0
    }
}