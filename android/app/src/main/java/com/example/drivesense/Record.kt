package com.example.drivesense

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class Record(
    @PrimaryKey(autoGenerate = true) val id: Int,

    val acceleration_score: Int,
    val breaking_score: Int,
    val steering_score: Int,
    val speeding_score: Int,
    val overall_score: Int,

    val elapsed_time: Long,
    val timestamp: Long,
)