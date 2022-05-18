package com.example.drivesense

import android.util.Log
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sqrt

class EventData {
    private var lastMagPeak:Float = 0f
    private var oldEvent = false
    private var magPeaks = mutableListOf<Float>()

    // calculate magnitude of x,y,z axis
    fun addData(x:FloatArray, y:FloatArray, z:FloatArray){
        Log.d("event:","calculate magnitude")
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
            Log.d("event:","mag update %f".format(lastMagPeak))
            magPeaks.add(lastMagPeak)
            // clear old event flag
            oldEvent = false
            // set magnitude to 0
            lastMagPeak = 0f
        }
    }

    // calculate 90 percentile
    fun percentile(p:Int):Float{
        // sort mutable list
        magPeaks.sort()
        var ind = (p/100f * (magPeaks.size - 1)).roundToInt()
        return magPeaks[ind]
    }
}