package com.example.drivesense

import android.util.Log
import kotlin.math.pow
import kotlin.math.sqrt

class Efficiency {
    private val breaking = EventData(0.025f)
    private val steering = EventData(0.025f)
    private val acceleration = EventData(0.025f)


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

}