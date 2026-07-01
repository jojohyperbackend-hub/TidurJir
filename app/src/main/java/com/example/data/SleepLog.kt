package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_logs")
data class SleepLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sleepTime: Long,        // epoch millis
    val wakeTime: Long,         // epoch millis
    val sleepQuality: Int,      // 1..5
    val mood: Int,              // 1..5
    val note: String? = null,   // max 280 char
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
