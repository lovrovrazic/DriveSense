package com.example.drivesense

import android.util.Log
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class Efficiency() {
    // thresholds
    private val binSize = 0.025f

    private val breaking = EventData(binSize, "breaking")
    private val steering = EventData(binSize, "steering")
    private val acceleration = EventData(binSize, "acceleration")

    class Thresholds {
        companion object {
            const val breakingNormal = 0.15f
            const val breakingAggressive = 0.35f
            const val steeringNormal = 0.2f
            const val steeringAggressive = 0.4f
            const val accelerationNormal = 0.15f
            const val accelerationAggressive = 0.35f
        }
    }

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
        return getPercentages(breaking.percentile(90), Thresholds.breakingNormal, Thresholds.breakingAggressive)
    }
    fun getScoreSteering(): Int {
        return getPercentages(steering.percentile(90), Thresholds.steeringNormal, Thresholds.steeringAggressive)
    }

    fun getScoreAcceleration(): Int {
        return getPercentages(acceleration.percentile(90), Thresholds.accelerationNormal, Thresholds.accelerationAggressive)
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


    // new scoring approach
    fun getNewScoreBreaking(): Int {
        return getNewPercentages(breaking.getListOfPeaks(), Thresholds.breakingNormal, Thresholds.breakingAggressive)
    }
    fun getNewScoreSteering(): Int {
        return getNewPercentages(steering.getListOfPeaks(), Thresholds.steeringNormal, Thresholds.steeringAggressive)
    }

    fun getNewScoreAcceleration(): Int {
        return getNewPercentages(acceleration.getListOfPeaks(), Thresholds.accelerationNormal, Thresholds.accelerationAggressive)
    }

    private fun getNewPercentages(peaks:MutableList<Int>, lowerBound:Float, upperBound:Float):Int{
        // get number of events
        val numberOfEvents:Int = peaks.sum()
        // if 0 events return score 100
        if (numberOfEvents == 0){return 0}

        // calculate score
        val peaksScores = peaks.foldIndexed(0) { index, sum, element -> when{
            // normal driving, 100% score
            index * binSize < lowerBound -> sum + (element * 100)
            // aggressive driving 0% score
            index * binSize > upperBound -> sum
            // in between calculate percentages
            else -> {
                sum + ((1 - ((index * binSize - lowerBound)/(upperBound - lowerBound))) * 100 * element).roundToInt()
            }
        }}

        return (peaksScores / numberOfEvents)
    }


}