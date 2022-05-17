package com.example.drivesense

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.drivesense.databinding.ActivityGpsBinding

class GpsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGpsBinding
    private lateinit var speeding: Speeding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding = ActivityGpsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(findViewById(R.id.toolbar))

        // calling the action bar
        val actionBar = getSupportActionBar()

        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        binding.tvGpsHist.setMovementMethod(ScrollingMovementMethod())

        speeding = Speeding(this)
    }

    override fun onResume() {
        super.onResume()
        speeding.startLocationUpdates(this)
    }

    override fun onPause() {
        super.onPause()
        speeding.stopLocationUpdates()
    }
}