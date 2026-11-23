package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.OnboardingAnswers
import com.example.model.UserPreferences
import com.example.model.UserProfileState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.tonnyPrefsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "tonny_user_preferences"
)

class UserPreferencesStore(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val DISCOVERY_SOURCE = stringPreferencesKey("discovery_source")
        val MAIN_GOAL = stringPreferencesKey("main_goal")
        val DAILY_MINUTES = intPreferencesKey("daily_minutes")

        val IS_AUTHENTICATED = booleanPreferencesKey("is_authenticated")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val EMAIL = stringPreferencesKey("email")

        val USE_24_HOUR = booleanPreferencesKey("use_24_hour")
        val VOICE_AUTO_LISTEN = booleanPreferencesKey("voice_auto_listen")
        val MORNING_BONUS_TIPS = booleanPreferencesKey("morning_bonus_tips")
    }

    val onboardingFlow: Flow<OnboardingAnswers> = context.tonnyPrefsDataStore.data.map { prefs ->
        OnboardingAnswers(
            discoverySource = prefs[Keys.DISCOVERY_SOURCE].orEmpty(),
            mainGoal = prefs[Keys.MAIN_GOAL].orEmpty(),
            dailyMinutes = prefs[Keys.DAILY_MINUTES] ?: 15,
            completed = prefs[Keys.ONBOARDING_COMPLETED] ?: false
        )
    }

    val userProfileFlow: Flow<UserProfileState> = context.tonnyPrefsDataStore.data.map { prefs ->
        UserProfileState(
            isAuthenticated = prefs[Keys.IS_AUTHENTICATED] ?: false,
            displayName = prefs[Keys.DISPLAY_NAME] ?: "Explorador Tonny",
            email = prefs[Keys.EMAIL].orEmpty(),
            preferences = UserPreferences(
                use24HourFormat = prefs[Keys.USE_24_HOUR] ?: true,
                voiceAutoListenEnabled = prefs[Keys.VOICE_AUTO_LISTEN] ?: true,
                morningBonusTipsEnabled = prefs[Keys.MORNING_BONUS_TIPS] ?: true
            )
        )
    }

    suspend fun saveOnboarding(answers: OnboardingAnswers) {
        context.tonnyPrefsDataStore.edit { prefs ->
            prefs[Keys.DISCOVERY_SOURCE] = answers.discoverySource
            prefs[Keys.MAIN_GOAL] = answers.mainGoal
            prefs[Keys.DAILY_MINUTES] = answers.dailyMinutes
            prefs[Keys.ONBOARDING_COMPLETED] = answers.completed
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.tonnyPrefsDataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun saveUserProfile(profile: UserProfileState) {
        context.tonnyPrefsDataStore.edit { prefs ->
            prefs[Keys.IS_AUTHENTICATED] = profile.isAuthenticated
            prefs[Keys.DISPLAY_NAME] = profile.displayName
            prefs[Keys.EMAIL] = profile.email
            prefs[Keys.USE_24_HOUR] = profile.preferences.use24HourFormat
            prefs[Keys.VOICE_AUTO_LISTEN] = profile.preferences.voiceAutoListenEnabled
            prefs[Keys.MORNING_BONUS_TIPS] = profile.preferences.morningBonusTipsEnabled
        }
    }
}
