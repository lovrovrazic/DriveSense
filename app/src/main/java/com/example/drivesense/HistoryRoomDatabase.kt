package com.example.drivesense

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

abstract class HistoryRoomDatabase : RoomDatabase() {

    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: HistoryRoomDatabase? = null

        fun getDatabase(context: Context): HistoryRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HistoryRoomDatabase::class.java,
                    "history"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}