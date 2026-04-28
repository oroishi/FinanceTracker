package com.example.financetracker.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    val currentUserId: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_ID]?.takeIf { it > 0 }
    }

    val darkTheme: Flow<Boolean?> = context.dataStore.data.map { prefs ->
        prefs[KEY_DARK_THEME]
    }

    suspend fun setUserId(userId: Long?) {
        context.dataStore.edit { prefs ->
            if (userId == null) prefs.remove(KEY_USER_ID)
            else prefs[KEY_USER_ID] = userId
        }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DARK_THEME] = enabled
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_USER_ID)
        }
    }

    companion object {
        private val KEY_USER_ID = longPreferencesKey("current_user_id")
        private val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
    }
}
