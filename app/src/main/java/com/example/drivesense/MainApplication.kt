package com.example.drivesense

import android.app.Application

class MainApplication : Application() {
    val database by lazy { HistoryRoomDatabase.getDatabase(this) }
    val repository by lazy { HistoryRepository(database.historyDao())}
}