package com.appointmentmanager.app.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsDataStore by preferencesDataStore(
    name = "appointment_manager_settings"
)

class SettingsDataStore(
    private val context: Context
) {

    private object PreferenceKeys {
        val darkModeEnabled: Preferences.Key<Boolean> =
            booleanPreferencesKey("dark_mode_enabled")
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
            preferences[PreferenceKeys.darkModeEnabled]
        }

    suspend fun setDarkModeEnabled(enabled: Boolean?) {
        context.settingsDataStore.edit { preferences ->
            if (enabled == null) {
                preferences.remove(PreferenceKeys.darkModeEnabled)
            } else {
                preferences[PreferenceKeys.darkModeEnabled] = enabled
            }
        }
    }

    suspend fun toggleDarkMode(currentSystemIsDark: Boolean) {
        val currentValue = darkModeEnabledValue()
        val newValue = when (currentValue) {
            null -> !currentSystemIsDark
            else -> !currentValue
        }

        setDarkModeEnabled(newValue)
    }

    private suspend fun darkModeEnabledValue(): Boolean? {
        var value: Boolean? = null

        context.settingsDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .collect { preferences ->
                value = preferences[PreferenceKeys.darkModeEnabled]
                return@collect
            }

        return value
    }
}
