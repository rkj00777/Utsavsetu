package com.example.ui.screens.setup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.AppLanguage
import com.example.data.preferences.UserPreferencesRepository
import com.example.panchang.City
import com.example.panchang.RegionalTradition
import com.example.platform.location.LocationCityResolver
import com.example.platform.location.LocationCityResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class SetupUiState(
    val currentStep: Int = 0, // 0: Welcome, 1: Language, 2: City, 3: Tradition, 4: Reminders & Privacy
    val selectedTraditions: Set<RegionalTradition> = setOf(RegionalTradition.NORTH_INDIAN),
    val selectedCity: City = City.DELHI,
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    val citySearchQuery: String = "",
    val isDetectingLocation: Boolean = false,
    val locationStatusMessage: String? = null,
    val dayBeforeReminder: Boolean = true,
    val morningOfReminder: Boolean = true,
    val muhuratStartReminder: Boolean = true,
    val dpdpConsent: Boolean = true,
    val isSigningIn: Boolean = false,
    val authError: String? = null,
    val isSetupComplete: Boolean = false
)

class SetupViewModel(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = preferencesRepository.userPreferencesFlow.firstOrNull()
            if (prefs != null) {
                Locale.setDefault(Locale.forLanguageTag(prefs.preferredLanguage.code))
                _uiState.update {
                    it.copy(
                        selectedLanguage = prefs.preferredLanguage,
                        selectedCity = prefs.hostCity,
                        selectedTraditions = prefs.selectedTraditions,
                        dayBeforeReminder = prefs.dayBeforeReminder,
                        morningOfReminder = prefs.morningOfReminder,
                        muhuratStartReminder = prefs.muhuratStartReminder
                    )
                }
            }
        }
    }

    fun setStep(step: Int) {
        _uiState.update { it.copy(currentStep = step.coerceIn(0, 4)) }
    }

    fun nextStep() {
        _uiState.update { it.copy(currentStep = (it.currentStep + 1).coerceAtMost(4)) }
    }

    fun previousStep() {
        _uiState.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0)) }
    }

    fun toggleTradition(tradition: RegionalTradition) {
        _uiState.update { current ->
            val updated = current.selectedTraditions.toMutableSet()
            if (updated.contains(tradition)) {
                if (updated.size > 1) updated.remove(tradition)
            } else {
                updated.add(tradition)
            }
            current.copy(selectedTraditions = updated)
        }
    }

    fun selectCity(city: City) {
        _uiState.update {
            it.copy(
                selectedCity = city,
                locationStatusMessage = if (city.isLocationDetected) {
                    "GPS Location Active: ${city.name}, ${city.state}"
                } else null
            )
        }
        viewModelScope.launch {
            preferencesRepository.updateHostCityObject(city)
        }
    }

    fun detectCityFromLocation(context: Context) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDetectingLocation = true,
                    locationStatusMessage = null
                )
            }
            when (val result = LocationCityResolver.detectUserCity(context)) {
                is LocationCityResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isDetectingLocation = false,
                            selectedCity = result.city,
                            locationStatusMessage = "Detected ${result.city.name} (${result.city.state}) via ${result.sourceLabel}"
                        )
                    }
                    preferencesRepository.updateHostCityObject(result.city)
                }
                is LocationCityResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isDetectingLocation = false,
                            locationStatusMessage = result.message
                        )
                    }
                }
            }
        }
    }

    /**
     * Immediately updates the selected language in UI state, system Locale, and persistent DataStore
     * so the entire Welcome screen and all subsequent screens switch language on the spot.
     */
    fun selectLanguage(language: AppLanguage) {
        Locale.setDefault(Locale.forLanguageTag(language.code))
        val matchingTradition = when (language) {
            AppLanguage.HINDI -> RegionalTradition.NORTH_INDIAN
            AppLanguage.MARATHI -> RegionalTradition.MAHARASHTRIAN
            AppLanguage.GUJARATI -> RegionalTradition.GUJARATI
            AppLanguage.TAMIL -> RegionalTradition.TAMIL
            AppLanguage.TELUGU, AppLanguage.KANNADA -> RegionalTradition.KANNADA_TELUGU
            AppLanguage.BENGALI -> RegionalTradition.BENGALI
            AppLanguage.ENGLISH -> null
        }
        _uiState.update { current ->
            val updatedTraditions = if (matchingTradition != null) {
                current.selectedTraditions + matchingTradition
            } else {
                current.selectedTraditions
            }
            current.copy(
                selectedLanguage = language,
                selectedTraditions = updatedTraditions
            )
        }
        viewModelScope.launch {
            preferencesRepository.updateLanguage(language.code)
        }
    }

    fun onCitySearchQueryChanged(query: String) {
        _uiState.update { it.copy(citySearchQuery = query) }
    }

    fun updateReminderPrefs(dayBefore: Boolean, morningOf: Boolean, muhuratStart: Boolean) {
        _uiState.update {
            it.copy(
                dayBeforeReminder = dayBefore,
                morningOfReminder = morningOf,
                muhuratStartReminder = muhuratStart
            )
        }
    }

    fun setConsent(consent: Boolean) {
        _uiState.update { it.copy(dpdpConsent = consent) }
    }

    fun setSigningIn(loading: Boolean) {
        _uiState.update { it.copy(isSigningIn = loading, authError = null) }
    }

    fun setAuthError(error: String?) {
        _uiState.update { it.copy(isSigningIn = false, authError = error) }
    }

    fun completeSetup(onFinished: () -> Unit) {
        val s = _uiState.value
        _uiState.update { it.copy(isSetupComplete = true) }
        onFinished()
        viewModelScope.launch {
            preferencesRepository.updateReminders(
                dayBefore = s.dayBeforeReminder,
                morningOf = s.morningOfReminder,
                muhuratStart = s.muhuratStartReminder
            )
            preferencesRepository.completeSetup(
                traditions = s.selectedTraditions,
                cityId = s.selectedCity.id,
                dpdpConsent = s.dpdpConsent,
                languageCode = s.selectedLanguage.code,
                city = s.selectedCity
            )
        }
    }
}
