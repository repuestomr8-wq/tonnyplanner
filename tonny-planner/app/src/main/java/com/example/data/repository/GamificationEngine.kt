package com.example.data.repository

import com.example.model.GamificationState
import com.example.model.ItemType
import com.example.model.ProgressMetrics
import com.example.model.TaskOrHabit
import com.example.model.TonnyState
import com.example.utils.DateTimeUtils
import kotlin.math.roundToInt

/**
 * Pure gamification rules (no streaks, no streak multipliers):
 * - Base XP = 10
 * - Morning multiplier (< 12:00 local) = 1.5x -> 15 XP
 * - Afternoon/Evening (>= 12:00 local) = 1.0x -> 10 XP
 * - Level N requires N * 100 XP within level N to reach level N + 1
 */
object GamificationEngine {
    const val BASE_XP = 10
    const val MORNING_MULTIPLIER = 1.5
    const val STANDARD_MULTIPLIER = 1.0

    data class LevelProgress(
        val level: Int,
        val currentLevelXp: Int,
        val nextLevelXpRequired: Int
    )

    fun calculateAwardedXp(isMorning: Boolean): Pair<Int, Double> {
        val multiplier = if (isMorning) MORNING_MULTIPLIER else STANDARD_MULTIPLIER
        val xp = (BASE_XP * multiplier).roundToInt()
        return xp to multiplier
    }

    fun computeLevelProgress(totalXp: Int): LevelProgress {
        var remaining = totalXp.coerceAtLeast(0)
        var level = 1
        while (remaining >= level * 100) {
            remaining -= level * 100
            level += 1
        }
        return LevelProgress(
            level = level,
            currentLevelXp = remaining,
            nextLevelXpRequired = level * 100
        )
    }

    fun buildGamificationState(
        items: List<TaskOrHabit>,
        lastMultiplierApplied: Double = DateTimeUtils.currentMultiplier(),
        isFocusActive: Boolean = false,
        justCompletedItem: TaskOrHabit? = null,
        justUncheckedItem: Boolean = false,
        previousLevel: Int? = null
    ): GamificationState {
        val totalXp = items.filter { it.completed }.sumOf { it.xpAwarded }.coerceAtLeast(0)
        val progress = computeLevelProgress(totalXp)

        val leveledUp = previousLevel != null && progress.level > previousLevel
        val tonnyState = when {
            isFocusActive -> TonnyState.FOCUSED
            leveledUp -> TonnyState.LEVEL_UP
            justCompletedItem != null -> TonnyState.HAPPY
            else -> TonnyState.IDLE
        }

        val tonnyMessage = when (tonnyState) {
            TonnyState.FOCUSED -> "Modo Foco activo. ¡Tú puedes con esto, estoy contigo!"
            TonnyState.LEVEL_UP -> "¡Subiste al Nivel ${progress.level}! Tonny celebra tu progreso."
            TonnyState.HAPPY -> {
                val xp = justCompletedItem?.xpAwarded ?: BASE_XP
                if (xp > BASE_XP) "¡+$xp XP con multiplicador matutino x1.5! Gran ritmo."
                else "¡+$xp XP ganados! Sigue avanzando paso a paso."
            }
            TonnyState.IDLE -> when {
                justUncheckedItem -> "XP ajustada correctamente. ¡Cuando quieras lo retomamos!"
                DateTimeUtils.isCurrentlyMorning() -> "Buenos días. ¡Completa antes de las 12:00 para ganar x1.5 XP!"
                else -> "Todo listo para organizar tu día con calma y enfoque."
            }
        }

        return GamificationState(
            currentLevel = progress.level,
            currentLevelXp = progress.currentLevelXp,
            nextLevelXpRequired = progress.nextLevelXpRequired,
            totalXp = totalXp,
            lastMultiplierApplied = lastMultiplierApplied,
            tonnyState = tonnyState,
            tonnyMessage = tonnyMessage
        )
    }

    fun computeMetrics(items: List<TaskOrHabit>): ProgressMetrics {
        val completed = items.filter { it.completed }
        val tasks = items.filter { it.type == ItemType.TASK }
        val habits = items.filter { it.type == ItemType.HABIT }

        val morningCompleted = completed.filter {
            it.xpAwarded >= 15 || DateTimeUtils.isMorningTimestamp(it.completedAt)
        }
        val baseCompleted = completed.filterNot {
            it.xpAwarded >= 15 || DateTimeUtils.isMorningTimestamp(it.completedAt)
        }

        return ProgressMetrics(
            totalItems = items.size,
            completedItems = completed.size,
            completedTasks = tasks.count { it.completed },
            totalTasks = tasks.size,
            completedHabits = habits.count { it.completed },
            totalHabits = habits.size,
            morningCompletionsCount = morningCompleted.size,
            baseCompletionsCount = baseCompleted.size,
            morningXpTotal = morningCompleted.sumOf { it.xpAwarded },
            baseXpTotal = baseCompleted.sumOf { it.xpAwarded }
        )
    }
}
