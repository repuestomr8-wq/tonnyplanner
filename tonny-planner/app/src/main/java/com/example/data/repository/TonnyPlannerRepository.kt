package com.example.data.repository

import com.example.data.local.TaskOrHabitDao
import com.example.data.local.UserPreferencesStore
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.data.remote.AntigravityPlannerService
import com.example.model.ItemType
import com.example.model.OnboardingAnswers
import com.example.model.TaskOrHabit
import com.example.model.UserProfileState
import com.example.model.VoiceParsePayload
import com.example.model.VoiceParseResult
import com.example.utils.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class TonnyPlannerRepository(
    private val dao: TaskOrHabitDao,
    private val preferencesStore: UserPreferencesStore,
    private val remoteService: AntigravityPlannerService
) {

    val itemsFlow: Flow<List<TaskOrHabit>> = dao.observeAllItems().map { list ->
        list.map { it.toDomain() }
    }

    val onboardingFlow: Flow<OnboardingAnswers> = preferencesStore.onboardingFlow
    val userProfileFlow: Flow<UserProfileState> = preferencesStore.userProfileFlow

    suspend fun ensureSeedDataIfEmpty() {
        if (dao.countItems() > 0) return
        val today = DateTimeUtils.todayIso()
        val yesterday = DateTimeUtils.addDays(today, -1)
        val tomorrow = DateTimeUtils.addDays(today, 1)
        val inTwoDays = DateTimeUtils.addDays(today, 2)

        // Seed 85 XP total across completed items -> Level 1 (85 / 100 XP)
        val seedItems = listOf(
            TaskOrHabit(
                id = "seed-y1",
                title = "Hidratación al despertar",
                type = ItemType.HABIT,
                scheduledDate = yesterday,
                startTime = "07:30",
                endTime = "07:45",
                completed = true,
                completedAt = "${yesterday}T07:45:00",
                xpAwarded = 15
            ),
            TaskOrHabit(
                id = "seed-y2",
                title = "Planificar sprint semanal",
                type = ItemType.TASK,
                scheduledDate = yesterday,
                startTime = "09:30",
                endTime = "10:30",
                completed = true,
                completedAt = "${yesterday}T10:15:00",
                xpAwarded = 15
            ),
            TaskOrHabit(
                id = "seed-y3",
                title = "Revisar correos pendientes",
                type = ItemType.TASK,
                scheduledDate = yesterday,
                startTime = "15:00",
                endTime = "15:30",
                completed = true,
                completedAt = "${yesterday}T15:30:00",
                xpAwarded = 10
            ),
            TaskOrHabit(
                id = "seed-y4",
                title = "Caminata de desconexión 20 min",
                type = ItemType.HABIT,
                scheduledDate = yesterday,
                startTime = "18:30",
                endTime = "19:00",
                completed = true,
                completedAt = "${yesterday}T19:00:00",
                xpAwarded = 10
            ),

            // Today
            TaskOrHabit(
                id = "seed-t1",
                title = "Respiración consciente 10 min",
                type = ItemType.HABIT,
                scheduledDate = today,
                startTime = "08:00",
                endTime = "08:15",
                completed = true,
                completedAt = "${today}T08:15:00",
                xpAwarded = 15
            ),
            TaskOrHabit(
                id = "seed-t2",
                title = "Lectura técnica (15 págs)",
                type = ItemType.HABIT,
                scheduledDate = today,
                startTime = "10:00",
                endTime = "10:30",
                completed = true,
                completedAt = "${today}T10:25:00",
                xpAwarded = 10
            ),
            TaskOrHabit(
                id = "seed-t3",
                title = "Definir arquitectura Antigravity API",
                type = ItemType.TASK,
                scheduledDate = today,
                startTime = "11:00",
                endTime = "12:00",
                completed = true,
                completedAt = "${today}T16:10:00",
                xpAwarded = 10
            ),
            TaskOrHabit(
                id = "seed-t4",
                title = "Ordenar escritorio sin pantallas",
                type = ItemType.HABIT,
                scheduledDate = today,
                startTime = "20:00",
                endTime = "20:30",
                completed = false,
                completedAt = null,
                xpAwarded = 0
            ),
            TaskOrHabit(
                id = "seed-t5",
                title = "Diseñar flujo de voz con Tonny",
                type = ItemType.TASK,
                scheduledDate = today,
                startTime = "14:30",
                endTime = "15:30",
                completed = false,
                completedAt = null,
                xpAwarded = 0
            ),
            TaskOrHabit(
                id = "seed-t6",
                title = "Preparar presentación de avances",
                type = ItemType.TASK,
                scheduledDate = today,
                startTime = "17:00",
                endTime = "18:00",
                completed = false,
                completedAt = null,
                xpAwarded = 0
            ),

            // Tomorrow
            TaskOrHabit(
                id = "seed-tm1",
                title = "Meditación matutina",
                type = ItemType.HABIT,
                scheduledDate = tomorrow,
                startTime = "07:30",
                endTime = "08:00",
                completed = false,
                completedAt = null,
                xpAwarded = 0
            ),
            TaskOrHabit(
                id = "seed-tm2",
                title = "Sincronización de diseño Stitch",
                type = ItemType.TASK,
                scheduledDate = tomorrow,
                startTime = "10:30",
                endTime = "11:30",
                completed = false,
                completedAt = null,
                xpAwarded = 0
            ),
            TaskOrHabit(
                id = "seed-tm3",
                title = "Actualizar documentación técnica",
                type = ItemType.TASK,
                scheduledDate = tomorrow,
                startTime = "16:00",
                endTime = "17:00",
                completed = false,
                completedAt = null,
                xpAwarded = 0
            ),

            // +2 days
            TaskOrHabit(
                id = "seed-d2-1",
                title = "Revisión semanal con equipo",
                type = ItemType.TASK,
                scheduledDate = inTwoDays,
                startTime = "09:00",
                endTime = "10:00",
                completed = false,
                completedAt = null,
                xpAwarded = 0
            ),
            TaskOrHabit(
                id = "seed-d2-2",
                title = "Estiramiento de espalda",
                type = ItemType.HABIT,
                scheduledDate = inTwoDays,
                startTime = null,
                endTime = null,
                completed = false,
                completedAt = null,
                xpAwarded = 0
            )
        )
        dao.insertAll(seedItems.map { it.toEntity() })
    }

    suspend fun createItem(
        title: String,
        type: ItemType,
        scheduledDate: String,
        startTime: String?,
        endTime: String?
    ): TaskOrHabit {
        val newItem = TaskOrHabit(
            id = "item-${UUID.randomUUID().toString().take(8)}",
            title = title.trim(),
            type = type,
            scheduledDate = scheduledDate,
            startTime = DateTimeUtils.normalizeTimeHm(startTime),
            endTime = DateTimeUtils.normalizeTimeHm(endTime),
            completed = false,
            completedAt = null,
            xpAwarded = 0
        )
        val synced = remoteService.syncCreatedItem(newItem)
        dao.upsertItem(synced.toEntity())
        return synced
    }

    suspend fun updateItem(item: TaskOrHabit): TaskOrHabit {
        val synced = remoteService.syncUpdatedItem(item)
        dao.upsertItem(synced.toEntity())
        return synced
    }

    suspend fun toggleCompletion(item: TaskOrHabit): ToggleResult {
        return if (!item.completed) {
            val isMorning = DateTimeUtils.isCurrentlyMorning()
            val (xp, multiplier) = GamificationEngine.calculateAwardedXp(isMorning)
            val updated = item.copy(
                completed = true,
                completedAt = DateTimeUtils.nowIsoTimestamp(),
                xpAwarded = xp
            )
            val synced = remoteService.syncUpdatedItem(updated)
            dao.upsertItem(synced.toEntity())
            ToggleResult(updatedItem = synced, multiplierApplied = multiplier, wasCompleted = true)
        } else {
            val updated = item.copy(
                completed = false,
                completedAt = null,
                xpAwarded = 0
            )
            val synced = remoteService.syncUpdatedItem(updated)
            dao.upsertItem(synced.toEntity())
            ToggleResult(updatedItem = synced, multiplierApplied = DateTimeUtils.currentMultiplier(), wasCompleted = false)
        }
    }

    suspend fun completeItemFromFocus(item: TaskOrHabit): ToggleResult {
        if (item.completed) {
            return ToggleResult(
                updatedItem = item,
                multiplierApplied = if (item.xpAwarded >= 15) 1.5 else 1.0,
                wasCompleted = true
            )
        }
        return toggleCompletion(item)
    }

    suspend fun deleteItem(itemId: String) {
        remoteService.syncDeletedItem(itemId)
        dao.deleteItemById(itemId)
    }

    suspend fun parseVoiceCommand(rawTranscript: String, referenceDate: String): VoiceParseResult {
        val payload = VoiceParsePayload(
            rawTranscript = rawTranscript,
            referenceDate = referenceDate,
            userLocalTime = DateTimeUtils.nowLocalTimeHm()
        )
        return remoteService.parseVoiceTranscript(payload)
    }

    suspend fun saveOnboarding(answers: OnboardingAnswers) {
        val synced = remoteService.syncOnboarding(answers)
        preferencesStore.saveOnboarding(synced)
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        preferencesStore.setOnboardingCompleted(completed)
    }

    suspend fun saveUserProfile(profile: UserProfileState) {
        val synced = remoteService.syncUserProfile(profile)
        preferencesStore.saveUserProfile(synced)
    }

    data class ToggleResult(
        val updatedItem: TaskOrHabit,
        val multiplierApplied: Double,
        val wasCompleted: Boolean
    )
}
