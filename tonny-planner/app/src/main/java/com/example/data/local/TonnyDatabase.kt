package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TaskOrHabitEntity::class], version = 2, exportSchema = false)
abstract class TonnyDatabase : RoomDatabase() {
    abstract fun taskOrHabitDao(): TaskOrHabitDao

    companion object {
        @Volatile
        private var INSTANCE: TonnyDatabase? = null

        fun getInstance(context: Context): TonnyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TonnyDatabase::class.java,
                    "tonny_planner_db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
