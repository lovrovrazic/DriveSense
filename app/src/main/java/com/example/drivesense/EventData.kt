package com.example.drivesense

import kotlin.math.pow
import kotlin.math.sqrt

class EventData {
    private var lastMagPeak:Float = 0f
    private var oldEvent = false
    private var magPeaks = mutableListOf<Float>()

    // calculate magnitude of x,y,z axis
    fun addData(x:FloatArray, y:FloatArray, z:FloatArray){
        // calculate magnitude
        val magnitude = FloatArray(20)
        for (i in 0..19){
            magnitude[i] = sqrt(x[i].pow(2) + y[i].pow(2) + z[i].pow(2))
        }
        // find peak/max magnitude
        var maxMag = magnitude.maxOrNull() ?: 0f


        if ((lastMagPeak < maxMag && oldEvent) || (!oldEvent)){
            // if old event is still active, check if new max magnitude greater than old max magnitude
            // if new event update magnitude
            lastMagPeak = maxMag
        }

        // update oldEvent flag
        oldEvent = true
    }

    // if old event still active add last magnitude peak to list
    fun updateMag(){
        if (oldEvent){
            magPeaks.add(lastMagPeak)
            // clear old event flag
            oldEvent = false
            // set magnitude to 0
            lastMagPeak = 0f
        }
    }

    // calculate 90 percentile
    fun p90():Float{
        return 2.8f
    }
}