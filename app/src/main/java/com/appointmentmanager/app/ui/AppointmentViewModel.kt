package com.appointmentmanager.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.appointmentmanager.app.data.AppointmentEntity
import com.appointmentmanager.app.data.AppointmentRepository
import com.appointmentmanager.app.data.SettingsDataStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalCoroutinesApi::class)
class AppointmentViewModel(
    private val repository: AppointmentRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val mutableSearchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = mutableSearchQuery.asStateFlow()

    val appointments: StateFlow<List<AppointmentEntity>> =
        mutableSearchQuery
            .flatMapLatest { query ->
                repository.observeAppointments(query)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val darkModeEnabled: StateFlow<Boolean?> =
        settingsDataStore.darkModeEnabled.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun updateSearchQuery(query: String) {
        mutableSearchQuery.value = query
    }

    fun observeAppointment(id: Long): Flow<AppointmentEntity?> {
        return repository.observeAppointmentById(id)
    }

    fun saveAppointment(
        appointment: AppointmentEntity,
        onSaved: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (appointment.id == 0L) {
                    repository.insert(appointment)
                } else {
                    repository.update(appointment)
                }
                onSaved()
            } catch (exception: Exception) {
                onError(exception)
            }
        }
    }

    fun deleteAppointment(
        id: Long,
        onDeleted: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.deleteById(id)
                onDeleted()
            } catch (exception: Exception) {
                onError(exception)
            }
        }
    }

    fun toggleDarkMode(currentSystemIsDark: Boolean) {
        viewModelScope.launch {
            settingsDataStore.toggleDarkMode(currentSystemIsDark)
        }
    }

    fun useSystemTheme() {
        viewModelScope.launch {
            settingsDataStore.setDarkModeEnabled(null)
        }
    }
}

class AppointmentViewModelFactory(
    private val repository: AppointmentRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppointmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppointmentViewModel(
                repository = repository,
                settingsDataStore = settingsDataStore
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
