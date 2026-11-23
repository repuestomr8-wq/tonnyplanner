package com.example.data.remote

import com.example.model.GamificationState
import com.example.model.ItemType
import com.example.model.OnboardingAnswers
import com.example.model.TaskOrHabit
import com.example.model.TonnyState
import com.example.model.UserProfileState
import com.example.model.VoiceParsePayload
import com.example.model.VoiceParseResult
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.delay
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// --- Antigravity REST API DTOs ---

@JsonClass(generateAdapter = true)
data class TaskOrHabitDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "type") val type: String,
    @Json(name = "scheduledDate") val scheduledDate: String,
    @Json(name = "startTime") val startTime: String?,
    @Json(name = "endTime") val endTime: String?,
    @Json(name = "completed") val completed: Boolean,
    @Json(name = "completedAt") val completedAt: String?,
    @Json(name = "xpAwarded") val xpAwarded: Int
)

@JsonClass(generateAdapter = true)
data class VoiceParsePayloadDto(
    @Json(name = "rawTranscript") val rawTranscript: String,
    @Json(name = "referenceDate") val referenceDate: String,
    @Json(name = "userLocalTime") val userLocalTime: String
)

@JsonClass(generateAdapter = true)
data class VoiceParseResultDto(
    @Json(name = "suggestedTitle") val suggestedTitle: String,
    @Json(name = "suggestedDate") val suggestedDate: String,
    @Json(name = "suggestedStartTime") val suggestedStartTime: String?,
    @Json(name = "suggestedEndTime") val suggestedEndTime: String?,
    @Json(name = "suggestedType") val suggestedType: String
)

@JsonClass(generateAdapter = true)
data class OnboardingAnswersDto(
    @Json(name = "discoverySource") val discoverySource: String,
    @Json(name = "mainGoal") val mainGoal: String,
    @Json(name = "dailyMinutes") val dailyMinutes: Int
)

@JsonClass(generateAdapter = true)
data class UserProfileStateDto(
    @Json(name = "isAuthenticated") val isAuthenticated: Boolean,
    @Json(name = "displayName") val displayName: String,
    @Json(name = "email") val email: String,
    @Json(name = "use24HourFormat") val use24HourFormat: Boolean,
    @Json(name = "voiceAutoListenEnabled") val voiceAutoListenEnabled: Boolean,
    @Json(name = "morningBonusTipsEnabled") val morningBonusTipsEnabled: Boolean
)

@JsonClass(generateAdapter = true)
data class GamificationStateDto(
    @Json(name = "currentLevel") val currentLevel: Int,
    @Json(name = "currentLevelXp") val currentLevelXp: Int,
    @Json(name = "nextLevelXpRequired") val nextLevelXpRequired: Int,
    @Json(name = "totalXp") val totalXp: Int,
    @Json(name = "lastMultiplierApplied") val lastMultiplierApplied: Double,
    @Json(name = "tonnyState") val tonnyState: String,
    @Json(name = "tonnyMessage") val tonnyMessage: String
)

/**
 * Retrofit contract prepared for Antigravity backend integration.
 */
interface AntigravityRetrofitApi {
    @GET("api/v1/items")
    suspend fun getItems(): List<TaskOrHabitDto>

    @POST("api/v1/items")
    suspend fun createItem(@Body item: TaskOrHabitDto): TaskOrHabitDto

    @PATCH("api/v1/items/{id}")
    suspend fun updateItem(@Path("id") id: String, @Body item: TaskOrHabitDto): TaskOrHabitDto

    @DELETE("api/v1/items/{id}")
    suspend fun deleteItem(@Path("id") id: String)

    @POST("api/v1/voice/parse")
    suspend fun parseVoice(@Body payload: VoiceParsePayloadDto): VoiceParseResultDto

    @GET("api/v1/gamification")
    suspend fun getGamificationState(): GamificationStateDto

    @PUT("api/v1/onboarding")
    suspend fun saveOnboarding(@Body answers: OnboardingAnswersDto): OnboardingAnswersDto

    @PUT("api/v1/profile")
    suspend fun syncUserProfile(@Body profile: UserProfileStateDto): UserProfileStateDto
}

interface AntigravityPlannerService {
    suspend fun parseVoiceTranscript(payload: VoiceParsePayload): VoiceParseResult
    suspend fun syncCreatedItem(item: TaskOrHabit): TaskOrHabit
    suspend fun syncUpdatedItem(item: TaskOrHabit): TaskOrHabit
    suspend fun syncDeletedItem(itemId: String)
    suspend fun syncOnboarding(answers: OnboardingAnswers): OnboardingAnswers
    suspend fun syncUserProfile(profile: UserProfileState): UserProfileState
}

class MockAntigravityPlannerService : AntigravityPlannerService {
    override suspend fun parseVoiceTranscript(payload: VoiceParsePayload): VoiceParseResult {
        delay(160)
        return VoiceScheduleParser.parse(payload)
    }

    override suspend fun syncCreatedItem(item: TaskOrHabit): TaskOrHabit {
        delay(60)
        return item
    }

    override suspend fun syncUpdatedItem(item: TaskOrHabit): TaskOrHabit {
        delay(50)
        return item
    }

    override suspend fun syncDeletedItem(itemId: String) {
        delay(40)
    }

    override suspend fun syncOnboarding(answers: OnboardingAnswers): OnboardingAnswers {
        delay(60)
        return answers
    }

    override suspend fun syncUserProfile(profile: UserProfileState): UserProfileState {
        delay(60)
        return profile
    }
}

fun TaskOrHabit.toDto(): TaskOrHabitDto = TaskOrHabitDto(
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

fun TaskOrHabitDto.toDomain(): TaskOrHabit = TaskOrHabit(
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

fun GamificationStateDto.toDomain(): GamificationState = GamificationState(
    currentLevel = currentLevel,
    currentLevelXp = currentLevelXp,
    nextLevelXpRequired = nextLevelXpRequired,
    totalXp = totalXp,
    lastMultiplierApplied = lastMultiplierApplied,
    tonnyState = TonnyState.fromApi(tonnyState),
    tonnyMessage = tonnyMessage
)
