package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SleepLog::class], version = 1, exportSchema = false)
abstract class SleepDatabase : RoomDatabase() {
    abstract fun sleepDao(): SleepDao

    companion object {
        @Volatile
        private var INSTANCE: SleepDatabase? = null

        fun getDatabase(context: Context): SleepDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SleepDatabase::class.java,
                    "sleepsanity_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
