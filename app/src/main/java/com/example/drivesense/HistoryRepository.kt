package com.example.drivesense

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {
    val allRecords: Flow<List<Record>> = historyDao.getAllRecords()

    @Suppress("RedundantModifier")
    @WorkerThread
    suspend fun insert(record: Record) {
        historyDao.insert(record)
    }

    @Suppress("RedundantModifier")
    @WorkerThread
    suspend fun deleteAllRecords() {
        historyDao.deleteAllRecords()
    }
}