package com.example.drivesense

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.MenuItem
import android.view.View
import android.widget.TextView

class TipsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tips)
        setSupportActionBar(findViewById(R.id.toolbar))

        // calling the action bar
        val actionBar = getSupportActionBar()

        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        val tips_textView: TextView = findViewById(R.id.tips_textView)
        tips_textView.text = Html.fromHtml(resources.getString(R.string.tips_text), Html.FROM_HTML_MODE_COMPACT)
    }
}