package com.example.drivesense

import android.util.Log
import kotlin.math.*

class EventData(binSize:Float, eventName:String) {
    // name could be breaking, steering, acceleration
    private var name = eventName

    private var lastPeak:Float = 0f
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

    private fun getPeak(sample:FloatArray): Float {
        return sample.map { abs(it) }.maxOrNull() ?: 0f
    }

    // calculate peak of the event
    fun addData(x:FloatArray, y:FloatArray, z:FloatArray){
        var curPeak = 0f
        // calculate peak
        when(name){
            "breaking" -> {
                // we look at z axis
                curPeak = getPeak(z)
            }
            "steering" -> {
                // we look at y axis
                curPeak = getPeak(y)
            }
            "acceleration" -> {
                // we look at z axis
                curPeak = getPeak(z)
            }
            else -> {
                Log.d("eventData:","name not valid")
            }
        }

        Log.d("curPeak", "%f".format(curPeak))

        if ((lastPeak < curPeak && oldEvent) || (!oldEvent)){
            // if old event is still active, check if new peak greater than old peak
            // if new event update peak
            lastPeak = curPeak
        }

        // update oldEvent flag
        oldEvent = true
    }

    // if old event still active add last magnitude peak to list
    fun updateMag(){
        if (oldEvent){
            //peaks.add(lastMagPeak)
            addPeak(lastPeak)
            // clear old event flag
            oldEvent = false
            // set magnitude to 0
            lastPeak = 0f
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