package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskOrHabitDao {
    @Query("SELECT * FROM planner_items ORDER BY scheduledDate ASC, CASE WHEN startTime IS NULL THEN 1 ELSE 0 END, startTime ASC, id ASC")
    fun observeAllItems(): Flow<List<TaskOrHabitEntity>>

    @Query("SELECT COUNT(*) FROM planner_items")
    suspend fun countItems(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TaskOrHabitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: TaskOrHabitEntity)

    @Query("DELETE FROM planner_items WHERE id = :id")
    suspend fun deleteItemById(id: String)
}
