package com.connectlens.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension property creates a single DataStore instance per process
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cl_user_prefs")

/**
 * Stores lightweight user preferences using Jetpack DataStore.
 *
 * Currently stores:
 * - Whether the user has completed onboarding (skip check on subsequent launches).
 *
 * No sensitive personal data is stored here.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    /** Flow of onboarding completion state. Emits false until explicitly set. */
    val isOnboardingCompleted: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] ?: false
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    /** Clears all app preferences (called from Settings → Clear Data). */
    suspend fun clearAllPreferences() {
        context.dataStore.edit { it.clear() }
    }
}
