package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_logs ORDER BY createdAt DESC")
    fun getAllLogs(): Flow<List<SleepLog>>

    @Query("SELECT * FROM sleep_logs WHERE id = :id LIMIT 1")
    suspend fun getLogById(id: Long): SleepLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: SleepLog): Long

    @Update
    suspend fun update(log: SleepLog)

    @Delete
    suspend fun delete(log: SleepLog)
}
