package com.example.drivesense

import android.util.Log
import kotlin.math.pow
import kotlin.math.sqrt

class Efficiency {
    private val breaking = EventData()
    private val steering = EventData()
    private val acceleration = EventData()


    fun add(x:FloatArray, y:FloatArray, z:FloatArray, classification:Int){
        // if event 0-breaking, 1-steering, 2-acceleration, 3-null
        when (classification) {
            // breaking
            0 -> {
                // add data
                breaking.addData(x,y,z)
                // update magnitudes of old events
                steering.updateMag()
                acceleration.updateMag()
            }
            // steering
            1 -> {
                // add data
                steering.addData(x,y,z)
                // update magnitudes of old events
                breaking.updateMag()
                acceleration.updateMag()
            }
            // acceleration
            2 -> {
                // add data
                acceleration.addData(x,y,z)
                // update magnitudes of old events
                breaking.updateMag()
                steering.updateMag()
            }
            // null
            3 -> {
                // update magnitudes of old events
                acceleration.updateMag()
                breaking.updateMag()
                steering.updateMag()
            }
            else -> { // Note the block
                Log.d("error:","Efficiency add classification not found")
            }
        }

    }

}