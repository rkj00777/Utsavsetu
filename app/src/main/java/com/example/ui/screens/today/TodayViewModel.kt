package com.example.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.CustomReminderEntry
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.FestivalRepository
import com.example.domain.model.FestivalOccurrence
import com.example.panchang.City
import com.example.panchang.PanchangDay
import com.example.panchang.PanchangEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class TodayUiState(
    val isLoading: Boolean = true,
    val panchang: PanchangDay? = null,
    val userPreferences: UserPreferences? = null,
    val todaysFestivals: List<FestivalOccurrence> = emptyList(),
    val nextFestival: FestivalOccurrence? = null,
    val daysUntilNextFestival: Long = 0,
    val nextSevenDaysOccurrences: List<FestivalOccurrence> = emptyList(),
    val upcomingOccurrences: List<FestivalOccurrence> = emptyList(),
    val errorMessage: String? = null
)

class TodayViewModel(
    private val festivalRepository: FestivalRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TodayUiState(
            isLoading = false,
            panchang = PanchangEngine.computeDayPanchang(LocalDate.now(), City.DELHI),
            userPreferences = UserPreferences(hostCity = City.DELHI)
        )
    )
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        loadToday()
    }

    fun loadToday() {
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            try {
                festivalRepository.ensureCatalogLoaded()

                combine(
                    userPreferencesRepository.userPreferencesFlow,
                    festivalRepository.getAllFestivals()
                ) { prefs, festivals ->
                    val today = LocalDate.now()
                    val city = prefs.hostCity
                    val panchang = PanchangEngine.computeDayPanchang(today, city)

                    val filteredFestivals = festivals.filter { fest ->
                        fest.traditions.any { prefs.selectedTraditions.contains(it) }
                    }.ifEmpty { festivals }

                    val currentYearOccurrences = festivalRepository.computeOccurrencesForYear(
                        festivals = filteredFestivals,
                        year = today.year,
                        city = city
                    )
                    val nextYearOccurrences = festivalRepository.computeOccurrencesForYear(
                        festivals = filteredFestivals,
                        year = today.year + 1,
                        city = city
                    )
                    val occurrences = currentYearOccurrences + nextYearOccurrences

                    val todaysList = currentYearOccurrences.filter { it.date == today }
                    val strictlyUpcoming = occurrences.filter { it.date.isAfter(today) }
                    val upcoming = occurrences.filter { !it.date.isBefore(today) }
                    val nextFest = strictlyUpcoming.firstOrNull() ?: upcoming.firstOrNull()
                    val daysUntil = nextFest?.let { ChronoUnit.DAYS.between(today, it.date) } ?: 0

                    val sevenDaysWindow = strictlyUpcoming.filter {
                        it.date.isBefore(today.plusDays(15))
                    }

                    TodayUiState(
                        isLoading = false,
                        panchang = panchang,
                        userPreferences = prefs,
                        todaysFestivals = todaysList,
                        nextFestival = nextFest,
                        daysUntilNextFestival = daysUntil,
                        nextSevenDaysOccurrences = sevenDaysWindow,
                        upcomingOccurrences = strictlyUpcoming.take(12).ifEmpty { upcoming.take(12) }
                    )
                }.catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load panchang") }
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Error loading today") }
            }
        }
    }

    fun selectCity(city: City) {
        viewModelScope.launch {
            userPreferencesRepository.updateHostCityObject(city)
        }
    }

    fun toggleSavedFestival(festivalId: String) {
        viewModelScope.launch {
            userPreferencesRepository.toggleSavedFestival(festivalId)
        }
    }

    fun deleteCustomReminder(entry: CustomReminderEntry) {
        viewModelScope.launch {
            userPreferencesRepository.deleteCustomReminder(entry.festivalId, entry.triggerType)
        }
    }

    fun updateGlobalReminderRules(dayBefore: Boolean, morningOf: Boolean, muhuratStart: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateReminders(dayBefore, morningOf, muhuratStart)
        }
    }
}
