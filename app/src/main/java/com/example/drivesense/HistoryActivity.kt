package com.example.drivesense

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryActivity : AppCompatActivity() {

    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory((application as MainApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        setSupportActionBar(findViewById(R.id.toolbar))

        // calling the action bar
        val actionBar = getSupportActionBar()

        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.history_recyclerview)
        val adapter = HistoryListAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        historyViewModel.allRecords.observe(this) { records ->
            records.let { adapter.submitList(it) }
        }
    }
}