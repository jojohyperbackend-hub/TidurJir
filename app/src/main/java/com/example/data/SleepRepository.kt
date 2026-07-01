package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SleepRepository(private val sleepDao: SleepDao) {

    val allLogs: Flow<List<SleepLog>> = sleepDao.getAllLogs()

    suspend fun getLogById(id: Long): SleepLog? = withContext(Dispatchers.IO) {
        sleepDao.getLogById(id)
    }

    suspend fun insertLog(log: SleepLog): Long = withContext(Dispatchers.IO) {
        // Enforce validations
        validate(log)
        
        // Prepare clean model for insertion to guarantee fresh timestamps
        val cleanLog = log.copy(
            id = 0, // ensure room auto-generates
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        sleepDao.insert(cleanLog)
    }

    suspend fun updateLog(log: SleepLog) = withContext(Dispatchers.IO) {
        // Fetch existing to preserve its original createdAt timestamp
        val existing = sleepDao.getLogById(log.id) ?: throw IllegalArgumentException("Log dengan ID ${log.id} tidak ditemukan.")
        
        // Form a validated log containing original createdAt and updated updatedAt
        val validatedLog = log.copy(
            createdAt = existing.createdAt,
            updatedAt = System.currentTimeMillis()
        )
        
        validate(validatedLog)
        sleepDao.update(validatedLog)
    }

    suspend fun deleteLog(log: SleepLog) = withContext(Dispatchers.IO) {
        sleepDao.delete(log)
    }

    fun validate(log: SleepLog) {
        require(log.sleepQuality in 1..5) { "Kualitas tidur harus berkisar antara 1 hingga 5." }
        require(log.mood in 1..5) { "Mood harus berkisar antara 1 hingga 5." }
        require(log.wakeTime > log.sleepTime) { "Waktu bangun tidur harus setelah waktu tidur." }
        require((log.note?.length ?: 0) <= 280) { "Catatan tidak boleh melebihi 280 karakter." }
    }
}
