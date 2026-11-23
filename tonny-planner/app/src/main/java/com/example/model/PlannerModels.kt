package com.example.model

enum class ItemType(val apiValue: String, val displayLabel: String) {
    TASK("task", "Tarea"),
    HABIT("habit", "Hábito");

    companion object {
        fun fromApi(value: String): ItemType =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: TASK
    }
}

enum class TonnyState(val apiValue: String, val badgeLabel: String) {
    IDLE("idle", "Tranquilo"),
    HAPPY("happy", "¡Feliz!"),
    FOCUSED("focused", "En Foco"),
    LEVEL_UP("level_up", "¡Subiste de Nivel!");

    companion object {
        fun fromApi(value: String): TonnyState =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: IDLE
    }
}

data class TaskOrHabit(
    val id: String,
    val title: String,
    val type: ItemType,
    val scheduledDate: String, // YYYY-MM-DD
    val startTime: String?,    // HH:mm or null
    val endTime: String?,      // HH:mm or null
    val completed: Boolean,
    val completedAt: String?,  // ISO local timestamp or null
    val xpAwarded: Int
)

data class VoiceParsePayload(
    val rawTranscript: String,
    val referenceDate: String,
    val userLocalTime: String
)

data class VoiceParseResult(
    val suggestedTitle: String,
    val suggestedDate: String,
    val suggestedStartTime: String?,
    val suggestedEndTime: String?,
    val suggestedType: ItemType
)

data class OnboardingAnswers(
    val discoverySource: String = "",
    val mainGoal: String = "",
    val dailyMinutes: Int = 15,
    val completed: Boolean = false
)

data class UserPreferences(
    val use24HourFormat: Boolean = true,
    val voiceAutoListenEnabled: Boolean = true,
    val morningBonusTipsEnabled: Boolean = true
)

data class UserProfileState(
    val isAuthenticated: Boolean = false,
    val displayName: String = "Invitado",
    val email: String = "",
    val preferences: UserPreferences = UserPreferences()
)

data class GamificationState(
    val currentLevel: Int,
    val currentLevelXp: Int,
    val nextLevelXpRequired: Int,
    val totalXp: Int,
    val lastMultiplierApplied: Double, // 1.0 or 1.5
    val tonnyState: TonnyState,
    val tonnyMessage: String
) {
    val progressFraction: Float
        get() = if (nextLevelXpRequired <= 0) 0f
        else (currentLevelXp.toFloat() / nextLevelXpRequired.toFloat()).coerceIn(0f, 1f)
}

data class ProgressMetrics(
    val totalItems: Int = 0,
    val completedItems: Int = 0,
    val completedTasks: Int = 0,
    val totalTasks: Int = 0,
    val completedHabits: Int = 0,
    val totalHabits: Int = 0,
    val morningCompletionsCount: Int = 0,
    val baseCompletionsCount: Int = 0,
    val morningXpTotal: Int = 0,
    val baseXpTotal: Int = 0
)
