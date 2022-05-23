package com.example.drivesense

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val getscoreBtn = findViewById<Button>(R.id.getscore_button)
        getscoreBtn.setOnClickListener{
            val Intent = Intent(this, DrivingActivity::class.java)
            startActivity(Intent)
        }

        val historyBtn = findViewById<Button>(R.id.history_button)
        historyBtn.setOnClickListener{
            val Intent = Intent(this, HistoryActivity::class.java)
            startActivity(Intent)
        }

        val tipsBtn = findViewById<Button>(R.id.tips_button)
        tipsBtn.setOnClickListener{
            val Intent = Intent(this, TipsActivity::class.java)
            startActivity(Intent)
        }
    }
}