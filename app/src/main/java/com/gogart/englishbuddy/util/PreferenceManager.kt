package com.gogart.englishbuddy.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class PreferenceManager(private val context: Context) {
    private val LAST_SESSION_ID = longPreferencesKey("last_session_id")

    val lastSessionId: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[LAST_SESSION_ID]
    }

    suspend fun saveLastSessionId(id: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SESSION_ID] = id
        }
    }
}
