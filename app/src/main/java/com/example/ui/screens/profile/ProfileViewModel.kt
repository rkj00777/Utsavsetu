package com.example.ui.screens.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.AppLanguage
import com.example.data.preferences.CustomReminderEntry
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.CloudSyncRepository
import com.example.data.repository.FestivalRepository
import com.example.panchang.City
import com.example.panchang.RegionalTradition
import com.example.platform.notifications.ReminderScheduler
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userPreferences: UserPreferences? = null,
    val isUserSignedIn: Boolean = false,
    val userDisplayName: String? = null,
    val userEmail: String? = null,
    val isSyncing: Boolean = false,
    val syncStatusMessage: String? = null,
    val showDeleteConfirmation: Boolean = false
)

class ProfileViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val festivalRepository: FestivalRepository,
    private val cloudSyncRepository: CloudSyncRepository,
    private val appContext: Context? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeProfile()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect { prefs ->
                val authUser = runCatching { Firebase.auth.currentUser }.getOrNull()
                _uiState.update { current ->
                    current.copy(
                        userPreferences = prefs,
                        isUserSignedIn = authUser != null,
                        userDisplayName = authUser?.displayName ?: "Devotee",
                        userEmail = authUser?.email
                    )
                }
            }
        }
    }

    fun toggleTradition(tradition: RegionalTradition) {
        val current = _uiState.value.userPreferences ?: return
        val updated = current.selectedTraditions.toMutableSet()
        if (updated.contains(tradition)) {
            if (updated.size > 1) updated.remove(tradition)
        } else {
            updated.add(tradition)
        }
        viewModelScope.launch {
            userPreferencesRepository.updateTraditions(updated)
            rescheduleNotificationsIfContextAvailable()
            syncIfSignedIn()
        }
    }

    fun updateHostCity(cityId: String) {
        viewModelScope.launch {
            val updated = userPreferencesRepository.updateHostCity(cityId)
            if (updated) {
                rescheduleNotificationsIfContextAvailable()
                syncIfSignedIn()
            } else {
                _uiState.update { it.copy(syncStatusMessage = "Unknown city ID: $cityId") }
            }
        }
    }

    fun updateHostCityObject(city: City) {
        viewModelScope.launch {
            userPreferencesRepository.updateHostCityObject(city)
            rescheduleNotificationsIfContextAvailable()
            syncIfSignedIn()
        }
    }

    fun updateLanguage(language: AppLanguage) {
        viewModelScope.launch {
            userPreferencesRepository.updateLanguage(language.code)
            syncIfSignedIn()
        }
    }

    fun updateReminders(dayBefore: Boolean, morningOf: Boolean, muhuratStart: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateReminders(dayBefore, morningOf, muhuratStart)
            rescheduleNotificationsIfContextAvailable()
            syncIfSignedIn()
        }
    }

    fun updateDailyMorningSettings(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updateDailyMorningSettings(enabled, hour, minute)
            rescheduleNotificationsIfContextAvailable()
            syncIfSignedIn()
        }
    }

    fun markNotificationRationaleShown() {
        viewModelScope.launch {
            userPreferencesRepository.markNotificationRationaleShown()
        }
    }

    fun deleteCustomReminder(entry: CustomReminderEntry) {
        viewModelScope.launch {
            userPreferencesRepository.deleteCustomReminder(entry.festivalId, entry.triggerType)
            rescheduleNotificationsIfContextAvailable()
            syncIfSignedIn()
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateThemeMode(mode)
            syncIfSignedIn()
        }
    }

    fun updateTextScaleMode(scaleMode: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateTextScaleMode(scaleMode)
            syncIfSignedIn()
        }
    }

    fun updatePanditVoiceGender(gender: String) {
        viewModelScope.launch {
            userPreferencesRepository.updatePanditVoiceGender(gender)
            syncIfSignedIn()
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncStatusMessage = "Syncing with cloud...") }
            try {
                val prefs = _uiState.value.userPreferences ?: return@launch
                cloudSyncRepository.syncUserPreferences(prefs)
                cloudSyncRepository.syncSamagriProgress()
                cloudSyncRepository.pullRemoteProgress()
                _uiState.update { it.copy(isSyncing = false, syncStatusMessage = "Cloud sync up to date.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSyncing = false, syncStatusMessage = "Sync failed: ${e.message}") }
            }
        }
    }

    private fun syncIfSignedIn() {
        if (cloudSyncRepository.isUserSignedIn) {
            triggerSync()
        }
    }

    private suspend fun rescheduleNotificationsIfContextAvailable() {
        val ctx = appContext ?: return
        runCatching { ReminderScheduler.rescheduleFromCurrentPreferences(ctx) }
    }

    fun showDeleteDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteConfirmation = show) }
    }

    fun deleteMyData(onCompleted: () -> Unit) {
        viewModelScope.launch {
            cloudSyncRepository.deleteUserData()
            festivalRepository.clearAllLocalData()
            userPreferencesRepository.clearAll()
            onCompleted()
        }
    }
}
