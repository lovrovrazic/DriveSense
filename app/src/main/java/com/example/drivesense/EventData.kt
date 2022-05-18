package com.example.drivesense

import android.util.Log
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sqrt

class EventData(binSize:Float, eventName:String) {
    // name could be breaking, steering, acceleration
    private var name = eventName

    private var lastMagPeak:Float = 0f
    private var oldEvent = false


    private var binSize = binSize
    private var peaks = mutableListOf<Int>()
    private var numberOfEvents = 0


//   // calculate magnitude of x,y,z axis
//   fun addData(x:FloatArray, y:FloatArray, z:FloatArray){
//       // calculate magnitude
//       val magnitude = FloatArray(20)
//       for (i in 0..19){
//           magnitude[i] = sqrt(x[i].pow(2) + y[i].pow(2) + z[i].pow(2))
//       }
//       // find peak/max magnitude
//       var maxMag = magnitude.maxOrNull() ?: 0f


//       if ((lastMagPeak < maxMag && oldEvent) || (!oldEvent)){
//           // if old event is still active, check if new max magnitude greater than old max magnitude
//           // if new event update magnitude
//           lastMagPeak = maxMag
//       }

//       // update oldEvent flag
//       oldEvent = true
//   }

    // calculate peak of the event according to its name and orientation
    fun addData(x:FloatArray, y:FloatArray, z:FloatArray, horizontalOrientation:Boolean){
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
            //peaks.add(lastMagPeak)
            addPeak(lastMagPeak)
            // clear old event flag
            oldEvent = false
            // set magnitude to 0
            lastMagPeak = 0f
            Log.d("peaks", peaks.map { it.toString() }.toTypedArray().contentToString())
        }
    }

    // calculates percentile
    fun percentile(p:Int):Float{
        var threshold: Float = 0f
        // calculate number of events inside percentile
        var eventsBelowPercentile: Int = (p/100f * numberOfEvents).roundToInt()
        // iterate through list and find percentile
        for ((index, value) in peaks.withIndex()){
            // subtract number of peaks in the bin from number of events below the percentile
            eventsBelowPercentile -= value
            // calculate threshold
            threshold = (index * binSize).toFloat()
            if (eventsBelowPercentile <= 0){
                // when number under 0, we hit the percentile threshold
                break
            }
        }
        return threshold
    }

    // adds new peak into the list,
    private fun addPeak(peak:Float){
        // increase number of events
        ++numberOfEvents
        // get number of a bin, floor round the result
        val binNumber: Int = (peak / binSize).toInt()
        // check if mutable list big enough
        if (peaks.size > binNumber){
            peaks[binNumber]++
        }else{
            // add bins to mutable list so that you can store result
            resizeList(binNumber)
            peaks[binNumber]++
        }

    }

    // adds new bins too the peaks list
    private fun resizeList(newSize:Int){
        while(peaks.size <= newSize){
            peaks.add(0)
        }
    }

    // get name of this class
    fun name():String{
        return name
    }
}