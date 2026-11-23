package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.ItemType
import com.example.model.TaskOrHabit

@Entity(tableName = "planner_items")
data class TaskOrHabitEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String,
    val scheduledDate: String,
    val startTime: String?,
    val endTime: String?,
    val completed: Boolean,
    val completedAt: String?,
    val xpAwarded: Int
)

fun TaskOrHabitEntity.toDomain(): TaskOrHabit = TaskOrHabit(
    id = id,
    title = title,
    type = ItemType.fromApi(type),
    scheduledDate = scheduledDate,
    startTime = startTime,
    endTime = endTime,
    completed = completed,
    completedAt = completedAt,
    xpAwarded = xpAwarded
)

fun TaskOrHabit.toEntity(): TaskOrHabitEntity = TaskOrHabitEntity(
    id = id,
    title = title,
    type = type.apiValue,
    scheduledDate = scheduledDate,
    startTime = startTime,
    endTime = endTime,
    completed = completed,
    completedAt = completedAt,
    xpAwarded = xpAwarded
)
