package com.appointmentmanager.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsDataStore by preferencesDataStore(
    name = "appointment_manager_settings"
)

class SettingsDataStore(
    private val context: Context
) {
    private companion object {
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
    }

    val darkModeEnabled: Flow<Boolean?> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[DARK_MODE_ENABLED]
        }

    suspend fun setDarkModeEnabled(enabled: Boolean?) {
        context.settingsDataStore.edit { preferences ->
            if (enabled == null) {
                preferences.remove(DARK_MODE_ENABLED)
            } else {
                preferences[DARK_MODE_ENABLED] = enabled
            }
        }
    }

    suspend fun toggleDarkMode(currentSystemIsDark: Boolean) {
        val currentPreference = context.settingsDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[DARK_MODE_ENABLED]
            }
            .first()

        val currentEffectiveValue = currentPreference ?: currentSystemIsDark
        setDarkModeEnabled(!currentEffectiveValue)
    }
}
