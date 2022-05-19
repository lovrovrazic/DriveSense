package com.example.drivesense

import android.util.Log
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class Efficiency() {
    private val breaking = EventData(0.025f, "breaking")
    private val steering = EventData(0.025f, "steering")
    private val acceleration = EventData(0.025f, "acceleration")
    // thresholds
    private val normal = 0.1f
    private val aggressive = 0.6f



    fun add(x:FloatArray, y:FloatArray, z:FloatArray, classification:Int){
        // if event 0-breaking, 1-steering, 2-acceleration, 3-null
        when (classification) {
            // breaking
            0 -> {
                //Log.d("efficiency:","breaking")
                // add data
                breaking.addData(x,y,z)
                // update magnitudes of old events
                steering.updateMag()
                acceleration.updateMag()
            }
            // steering
            1 -> {
                //Log.d("efficiency:","steering")
                // add data
                steering.addData(x,y,z)
                // update magnitudes of old events
                breaking.updateMag()
                acceleration.updateMag()
            }
            // acceleration
            2 -> {
                //Log.d("efficiency:","acceleration")
                // add data
                acceleration.addData(x,y,z)
                // update magnitudes of old events
                breaking.updateMag()
                steering.updateMag()
            }
            // null
            3 -> {
                //Log.d("efficiency:","null")
                // update magnitudes of old events
                acceleration.updateMag()
                breaking.updateMag()
                steering.updateMag()
            }
            else -> { // default
                Log.d("efficiency:","classification not found")
            }
        }

    }

    fun getScoreBreaking(): Int {
        return getPercentages(breaking.percentile(90), normal, aggressive)
    }
    fun getScoreSteering(): Int {
        return getPercentages(steering.percentile(90), normal, aggressive)
    }

    fun getScoreAcceleration(): Int {
        return getPercentages(acceleration.percentile(90), normal, aggressive)
    }

    private fun getPercentages(percentile:Float, lowerBound:Float, upperBound:Float):Int{
        return when{
            // normal driving, 100% score
            percentile < lowerBound -> 100
            // aggressive driving 0% score
            percentile > upperBound -> 0
            // in between calculate percentages
            else -> {
                ((1 - ((percentile - lowerBound)/(upperBound - lowerBound))) * 100).roundToInt()
            }
        }
    }


}