package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.TonnyDatabase
import com.example.data.local.UserPreferencesStore
import com.example.data.remote.MockAntigravityPlannerService
import com.example.data.repository.GamificationEngine
import com.example.data.repository.TonnyPlannerRepository
import com.example.model.GamificationState
import com.example.model.ItemType
import com.example.model.OnboardingAnswers
import com.example.model.ProgressMetrics
import com.example.model.TaskOrHabit
import com.example.model.TonnyState
import com.example.model.UserProfileState
import com.example.utils.DateTimeUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VoiceParseDraft(
    val title: String,
    val type: ItemType,
    val scheduledDate: String,
    val startTime: String,
    val endTime: String
)

data class FocusSessionState(
    val task: TaskOrHabit,
    val presetMinutes: Int = 25,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false
)

data class TonnyPlannerUiState(
    val selectedDate: String = DateTimeUtils.todayIso(),
    val calendarWeekAnchor: String = DateTimeUtils.todayIso(),
    val allItems: List<TaskOrHabit> = emptyList(),
    val gamification: GamificationState = GamificationState(
        currentLevel = 1,
        currentLevelXp = 0,
        nextLevelXpRequired = 100,
        totalXp = 0,
        lastMultiplierApplied = DateTimeUtils.currentMultiplier(),
        tonnyState = TonnyState.IDLE,
        tonnyMessage = "Cargando tus tareas..."
    ),
    val metrics: ProgressMetrics = ProgressMetrics(),
    val onboarding: OnboardingAnswers = OnboardingAnswers(),
    val isOnboardingVisible: Boolean = false,
    val onboardingLoaded: Boolean = false,
    val userProfile: UserProfileState = UserProfileState(),
    val focusSession: FocusSessionState? = null,
    val rawTranscript: String = "",
    val isListening: Boolean = false,
    val isParsingVoice: Boolean = false,
    val voiceError: String? = null,
    val voiceDraft: VoiceParseDraft? = null,
    val showCreateSheet: Boolean = false,
    val editingItem: TaskOrHabit? = null,
    val createSheetDate: String = DateTimeUtils.todayIso(),
    val createSheetStartTime: String? = null,
    val createSheetEndTime: String? = null,
    val feedbackBanner: String? = null
) {
    val selectedDateItems: List<TaskOrHabit>
        get() = allItems.filter { it.scheduledDate == selectedDate }

    val selectedDateHabits: List<TaskOrHabit>
        get() = selectedDateItems.filter { it.type == ItemType.HABIT }

    val selectedDateTasks: List<TaskOrHabit>
        get() = selectedDateItems.filter { it.type == ItemType.TASK }
}

class TonnyPlannerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TonnyPlannerRepository = TonnyPlannerRepository(
        dao = TonnyDatabase.getInstance(application).taskOrHabitDao(),
        preferencesStore = UserPreferencesStore(application),
        remoteService = MockAntigravityPlannerService()
    )

    private val _uiState = MutableStateFlow(TonnyPlannerUiState())
    val uiState: StateFlow<TonnyPlannerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var parseJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureSeedDataIfEmpty()
            combine(
                repository.itemsFlow,
                repository.onboardingFlow,
                repository.userProfileFlow
            ) { items, onboarding, profile ->
                Triple(items, onboarding, profile)
            }.collect { (items, onboarding, profile) ->
                _uiState.update { current ->
                    val metrics = GamificationEngine.computeMetrics(items)
                    val refreshedGamification = GamificationEngine.buildGamificationState(
                        items = items,
                        lastMultiplierApplied = current.gamification.lastMultiplierApplied,
                        isFocusActive = current.focusSession != null
                    ).let { computed ->
                        if (current.gamification.tonnyState == TonnyState.LEVEL_UP &&
                            computed.currentLevel == current.gamification.currentLevel &&
                            current.focusSession == null
                        ) {
                            computed.copy(
                                tonnyState = TonnyState.LEVEL_UP,
                                tonnyMessage = current.gamification.tonnyMessage
                            )
                        } else if (current.gamification.tonnyState == TonnyState.HAPPY &&
                            current.focusSession == null &&
                            current.gamification.totalXp == computed.totalXp
                        ) {
                            computed.copy(
                                tonnyState = TonnyState.HAPPY,
                                tonnyMessage = current.gamification.tonnyMessage
                            )
                        } else {
                            computed
                        }
                    }

                    val updatedFocus = current.focusSession?.let { session ->
                        val latestTask = items.firstOrNull { it.id == session.task.id }
                        if (latestTask != null) session.copy(task = latestTask) else session
                    }

                    val shouldShowOnboarding = if (!current.onboardingLoaded) {
                        !onboarding.completed
                    } else {
                        current.isOnboardingVisible
                    }

                    current.copy(
                        allItems = items,
                        metrics = metrics,
                        gamification = refreshedGamification,
                        onboarding = onboarding,
                        isOnboardingVisible = shouldShowOnboarding,
                        onboardingLoaded = true,
                        userProfile = profile,
                        focusSession = updatedFocus
                    )
                }
            }
        }
    }

    // --- Onboarding ---

    fun completeOnboarding(
        discoverySource: String,
        mainGoal: String,
        dailyMinutes: Int
    ) {
        viewModelScope.launch {
            val answers = OnboardingAnswers(
                discoverySource = discoverySource,
                mainGoal = mainGoal,
                dailyMinutes = dailyMinutes,
                completed = true
            )
            repository.saveOnboarding(answers)
            _uiState.update {
                it.copy(
                    onboarding = answers,
                    isOnboardingVisible = false,
                    feedbackBanner = "¡Bienvenido! Plan inicial ajustado a $dailyMinutes min/día."
                )
            }
        }
    }

    fun skipOnboarding() {
        viewModelScope.launch {
            repository.setOnboardingCompleted(true)
            _uiState.update {
                it.copy(
                    onboarding = it.onboarding.copy(completed = true),
                    isOnboardingVisible = false
                )
            }
        }
    }

    fun reopenOnboarding() {
        _uiState.update { it.copy(isOnboardingVisible = true) }
    }

    // --- User Profile & Settings ---

    fun signInOrCreateAccount(displayName: String, email: String) {
        val cleanName = displayName.trim().ifEmpty { "Explorador Tonny" }
        val cleanEmail = email.trim()
        viewModelScope.launch {
            val updated = _uiState.value.userProfile.copy(
                isAuthenticated = true,
                displayName = cleanName,
                email = cleanEmail
            )
            repository.saveUserProfile(updated)
            _uiState.update {
                it.copy(
                    userProfile = updated,
                    feedbackBanner = "Sesión iniciada como $cleanName"
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            val updated = _uiState.value.userProfile.copy(
                isAuthenticated = false,
                displayName = "Explorador Tonny",
                email = ""
            )
            repository.saveUserProfile(updated)
            _uiState.update {
                it.copy(
                    userProfile = updated,
                    feedbackBanner = "Sesión cerrada"
                )
            }
        }
    }

    fun updateUserPreferences(
        use24HourFormat: Boolean? = null,
        voiceAutoListenEnabled: Boolean? = null,
        morningBonusTipsEnabled: Boolean? = null
    ) {
        viewModelScope.launch {
            val current = _uiState.value.userProfile
            val updatedPrefs = current.preferences.copy(
                use24HourFormat = use24HourFormat ?: current.preferences.use24HourFormat,
                voiceAutoListenEnabled = voiceAutoListenEnabled ?: current.preferences.voiceAutoListenEnabled,
                morningBonusTipsEnabled = morningBonusTipsEnabled ?: current.preferences.morningBonusTipsEnabled
            )
            val updated = current.copy(preferences = updatedPrefs)
            repository.saveUserProfile(updated)
            _uiState.update { it.copy(userProfile = updated) }
        }
    }

    // --- Date & Calendar Navigation ---

    fun selectDate(isoDate: String) {
        _uiState.update {
            it.copy(
                selectedDate = isoDate,
                calendarWeekAnchor = isoDate
            )
        }
    }

    fun shiftCalendarWeek(deltaWeeks: Int) {
        _uiState.update { current ->
            val newAnchor = DateTimeUtils.addDays(current.calendarWeekAnchor, deltaWeeks * 7)
            current.copy(calendarWeekAnchor = newAnchor)
        }
    }

    fun jumpToToday() {
        val today = DateTimeUtils.todayIso()
        _uiState.update {
            it.copy(
                selectedDate = today,
                calendarWeekAnchor = today
            )
        }
    }

    // --- Create / Edit Sheet with Start & End Time ---

    fun openCreateSheet(
        date: String = _uiState.value.selectedDate,
        startTime: String? = null
    ) {
        val normalizedStart = DateTimeUtils.normalizeTimeHm(startTime)
        val suggestedEnd = DateTimeUtils.addMinutesToTimeHm(normalizedStart, 60)
        _uiState.update {
            it.copy(
                showCreateSheet = true,
                editingItem = null,
                createSheetDate = date,
                createSheetStartTime = normalizedStart,
                createSheetEndTime = suggestedEnd
            )
        }
    }

    fun openEditSheet(item: TaskOrHabit) {
        _uiState.update {
            it.copy(
                showCreateSheet = true,
                editingItem = item,
                createSheetDate = item.scheduledDate,
                createSheetStartTime = item.startTime,
                createSheetEndTime = item.endTime
            )
        }
    }

    fun dismissCreateSheet() {
        _uiState.update { it.copy(showCreateSheet = false, editingItem = null) }
    }

    fun saveManualItem(
        title: String,
        type: ItemType,
        scheduledDate: String,
        startTime: String?,
        endTime: String?
    ) {
        if (title.isBlank()) return
        val existing = _uiState.value.editingItem
        viewModelScope.launch {
            if (existing != null) {
                val updated = existing.copy(
                    title = title.trim(),
                    type = type,
                    scheduledDate = scheduledDate,
                    startTime = DateTimeUtils.normalizeTimeHm(startTime),
                    endTime = DateTimeUtils.normalizeTimeHm(endTime)
                )
                repository.updateItem(updated)
                _uiState.update {
                    it.copy(
                        showCreateSheet = false,
                        editingItem = null,
                        selectedDate = scheduledDate,
                        calendarWeekAnchor = scheduledDate,
                        feedbackBanner = "${type.displayLabel} actualizada"
                    )
                }
            } else {
                repository.createItem(
                    title = title,
                    type = type,
                    scheduledDate = scheduledDate,
                    startTime = startTime,
                    endTime = endTime
                )
                _uiState.update {
                    it.copy(
                        showCreateSheet = false,
                        editingItem = null,
                        selectedDate = scheduledDate,
                        calendarWeekAnchor = scheduledDate,
                        feedbackBanner = "${type.displayLabel} agregada para ${DateTimeUtils.formatReadableDate(scheduledDate)}"
                    )
                }
            }
        }
    }

    fun toggleItemCompletion(item: TaskOrHabit) {
        val previousLevel = _uiState.value.gamification.currentLevel
        viewModelScope.launch {
            val result = repository.toggleCompletion(item)
            val updatedItems = _uiState.value.allItems.map {
                if (it.id == item.id) result.updatedItem else it
            }
            val nextGamification = GamificationEngine.buildGamificationState(
                items = updatedItems,
                lastMultiplierApplied = result.multiplierApplied,
                isFocusActive = _uiState.value.focusSession != null,
                justCompletedItem = if (result.wasCompleted) result.updatedItem else null,
                justUncheckedItem = !result.wasCompleted,
                previousLevel = if (result.wasCompleted) previousLevel else null
            )
            val banner = when {
                nextGamification.tonnyState == TonnyState.LEVEL_UP ->
                    "¡Subiste al Nivel ${nextGamification.currentLevel}! (+${result.updatedItem.xpAwarded} XP)"
                result.wasCompleted && result.multiplierApplied > 1.0 ->
                    "+${result.updatedItem.xpAwarded} XP (Bonus matutino x1.5)"
                result.wasCompleted ->
                    "+${result.updatedItem.xpAwarded} XP ganados"
                else ->
                    "Completado revertido (-${item.xpAwarded} XP)"
            }
            _uiState.update {
                it.copy(
                    gamification = nextGamification,
                    feedbackBanner = banner
                )
            }
        }
    }

    fun deleteItem(item: TaskOrHabit) {
        viewModelScope.launch {
            repository.deleteItem(item.id)
            _uiState.update {
                it.copy(feedbackBanner = "Elemento eliminado")
            }
        }
    }

    // --- Focus Mode ---

    fun openFocusMode(task: TaskOrHabit) {
        timerJob?.cancel()
        _uiState.update { current ->
            val updatedGamification = current.gamification.copy(
                tonnyState = TonnyState.FOCUSED,
                tonnyMessage = "Modo Foco activo en \"${task.title}\". ¡Vamos!"
            )
            current.copy(
                focusSession = FocusSessionState(task = task, presetMinutes = 25, remainingSeconds = 25 * 60, isRunning = false),
                gamification = updatedGamification
            )
        }
    }

    fun closeFocusMode() {
        timerJob?.cancel()
        _uiState.update { current ->
            val restored = GamificationEngine.buildGamificationState(
                items = current.allItems,
                lastMultiplierApplied = current.gamification.lastMultiplierApplied,
                isFocusActive = false
            )
            current.copy(
                focusSession = null,
                gamification = restored
            )
        }
    }

    fun selectFocusPreset(minutes: Int) {
        timerJob?.cancel()
        _uiState.update { current ->
            val session = current.focusSession ?: return@update current
            current.copy(
                focusSession = session.copy(
                    presetMinutes = minutes,
                    remainingSeconds = minutes * 60,
                    isRunning = false
                )
            )
        }
    }

    fun toggleFocusTimer() {
        val session = _uiState.value.focusSession ?: return
        if (session.isRunning) {
            timerJob?.cancel()
            _uiState.update { current ->
                current.copy(focusSession = current.focusSession?.copy(isRunning = false))
            }
        } else {
            val startSeconds = if (session.remainingSeconds <= 0) session.presetMinutes * 60 else session.remainingSeconds
            _uiState.update { current ->
                current.copy(
                    focusSession = current.focusSession?.copy(
                        remainingSeconds = startSeconds,
                        isRunning = true
                    )
                )
            }
            timerJob?.cancel()
            timerJob = viewModelScope.launch {
                while (true) {
                    delay(1000L)
                    val latest = _uiState.value.focusSession
                    if (latest == null || !latest.isRunning) break
                    if (latest.remainingSeconds <= 1) {
                        _uiState.update { state ->
                            state.copy(
                                focusSession = state.focusSession?.copy(
                                    remainingSeconds = 0,
                                    isRunning = false
                                ),
                                feedbackBanner = "¡Bloque de foco terminado! Completa la tarea para reclamar tu XP."
                            )
                        }
                        break
                    } else {
                        _uiState.update { state ->
                            state.copy(
                                focusSession = state.focusSession?.copy(
                                    remainingSeconds = latest.remainingSeconds - 1
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun resetFocusTimer() {
        timerJob?.cancel()
        _uiState.update { current ->
            val session = current.focusSession ?: return@update current
            current.copy(
                focusSession = session.copy(
                    remainingSeconds = session.presetMinutes * 60,
                    isRunning = false
                )
            )
        }
    }

    fun completeActiveFocusTask() {
        val session = _uiState.value.focusSession ?: return
        val previousLevel = _uiState.value.gamification.currentLevel
        timerJob?.cancel()
        viewModelScope.launch {
            val result = repository.completeItemFromFocus(session.task)
            val updatedItems = _uiState.value.allItems.map {
                if (it.id == session.task.id) result.updatedItem else it
            }
            val nextGamification = GamificationEngine.buildGamificationState(
                items = updatedItems,
                lastMultiplierApplied = result.multiplierApplied,
                isFocusActive = false,
                justCompletedItem = result.updatedItem,
                previousLevel = previousLevel
            )
            val banner = if (nextGamification.tonnyState == TonnyState.LEVEL_UP) {
                "¡Nivel ${nextGamification.currentLevel} desbloqueado desde Modo Foco! (+${result.updatedItem.xpAwarded} XP)"
            } else {
                "¡Tarea completada en Foco! +${result.updatedItem.xpAwarded} XP"
            }
            _uiState.update {
                it.copy(
                    focusSession = null,
                    gamification = nextGamification,
                    feedbackBanner = banner
                )
            }
        }
    }

    // --- Voice Scheduling ---

    fun setListening(listening: Boolean) {
        _uiState.update {
            it.copy(
                isListening = listening,
                voiceError = if (listening) null else it.voiceError
            )
        }
    }

    fun setVoiceError(message: String?) {
        _uiState.update { it.copy(voiceError = message, isListening = false) }
    }

    fun updateRawTranscript(transcript: String, triggerParse: Boolean = true) {
        _uiState.update { it.copy(rawTranscript = transcript, voiceError = null) }
        if (transcript.isBlank()) {
            parseJob?.cancel()
            _uiState.update { it.copy(voiceDraft = null, isParsingVoice = false) }
            return
        }
        if (triggerParse) {
            parseTranscriptDebounced(transcript)
        }
    }

    fun parseCurrentTranscriptNow() {
        val text = _uiState.value.rawTranscript
        if (text.isBlank()) return
        parseJob?.cancel()
        parseJob = viewModelScope.launch {
            performParse(text)
        }
    }

    private fun parseTranscriptDebounced(transcript: String) {
        parseJob?.cancel()
        parseJob = viewModelScope.launch {
            delay(200)
            performParse(transcript)
        }
    }

    private suspend fun performParse(transcript: String) {
        _uiState.update { it.copy(isParsingVoice = true) }
        val result = repository.parseVoiceCommand(
            rawTranscript = transcript,
            referenceDate = _uiState.value.selectedDate
        )
        _uiState.update {
            it.copy(
                isParsingVoice = false,
                voiceDraft = VoiceParseDraft(
                    title = result.suggestedTitle,
                    type = result.suggestedType,
                    scheduledDate = result.suggestedDate,
                    startTime = result.suggestedStartTime.orEmpty(),
                    endTime = result.suggestedEndTime.orEmpty()
                )
            )
        }
    }

    fun updateVoiceDraft(
        title: String? = null,
        type: ItemType? = null,
        scheduledDate: String? = null,
        startTime: String? = null,
        endTime: String? = null
    ) {
        _uiState.update { current ->
            val draft = current.voiceDraft ?: VoiceParseDraft(
                title = "",
                type = ItemType.TASK,
                scheduledDate = current.selectedDate,
                startTime = "",
                endTime = ""
            )
            val newStart = startTime ?: draft.startTime
            val autoSuggestedEnd = if (startTime != null && endTime == null) {
                val normalizedStart = DateTimeUtils.normalizeTimeHm(startTime)
                if (normalizedStart != null) DateTimeUtils.addMinutesToTimeHm(normalizedStart, 60).orEmpty()
                else ""
            } else {
                endTime ?: draft.endTime
            }

            current.copy(
                voiceDraft = draft.copy(
                    title = title ?: draft.title,
                    type = type ?: draft.type,
                    scheduledDate = scheduledDate ?: draft.scheduledDate,
                    startTime = newStart,
                    endTime = autoSuggestedEnd
                )
            )
        }
    }

    fun confirmVoiceDraft() {
        val draft = _uiState.value.voiceDraft ?: return
        if (draft.title.isBlank()) return
        val normalizedStart = DateTimeUtils.normalizeTimeHm(draft.startTime)
        val normalizedEnd = DateTimeUtils.normalizeTimeHm(draft.endTime)
        viewModelScope.launch {
            repository.createItem(
                title = draft.title,
                type = draft.type,
                scheduledDate = draft.scheduledDate,
                startTime = normalizedStart,
                endTime = normalizedEnd
            )
            _uiState.update { current ->
                current.copy(
                    rawTranscript = "",
                    voiceDraft = null,
                    selectedDate = draft.scheduledDate,
                    calendarWeekAnchor = draft.scheduledDate,
                    gamification = current.gamification.copy(
                        tonnyState = TonnyState.HAPPY,
                        tonnyMessage = "¡Agendado! \"${draft.title}\" listo para ${DateTimeUtils.formatReadableDate(draft.scheduledDate)}."
                    ),
                    feedbackBanner = "${draft.type.displayLabel} guardada por voz en ${DateTimeUtils.formatReadableDate(draft.scheduledDate)}"
                )
            }
        }
    }

    fun clearVoiceDraft() {
        parseJob?.cancel()
        _uiState.update {
            it.copy(
                rawTranscript = "",
                voiceDraft = null,
                voiceError = null,
                isParsingVoice = false
            )
        }
    }

    fun clearFeedbackBanner() {
        _uiState.update { it.copy(feedbackBanner = null) }
    }

    override fun onCleared() {
        timerJob?.cancel()
        parseJob?.cancel()
        super.onCleared()
    }
}
