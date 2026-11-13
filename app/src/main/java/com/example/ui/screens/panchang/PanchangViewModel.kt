package com.example.ui.screens.panchang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.FestivalRepository
import com.example.panchang.City
import com.example.panchang.PanchangDay
import com.example.panchang.PanchangEngine
import com.example.panchang.TimeWindow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class PanchangUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val hostCity: City = City.DELHI,
    val panchangDay: PanchangDay? = null,
    val brahmaMuhurat: TimeWindow? = null,
    val userPreferences: UserPreferences? = null,
    val isLoading: Boolean = false
)

class PanchangViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val festivalRepository: FestivalRepository? = null
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _uiState = MutableStateFlow(PanchangUiState())
    val uiState: StateFlow<PanchangUiState> = _uiState.asStateFlow()

    init {
        observePanchang()
    }

    private fun observePanchang() {
        viewModelScope.launch {
            combine(
                userPreferencesRepository.userPreferencesFlow,
                _selectedDate
            ) { prefs, date ->
                val city = prefs.hostCity
                val panchang = festivalRepository?.getAndCacheDayPanchang(date, city)
                    ?: PanchangEngine.computeDayPanchang(date, city)
                val sunriseMins = panchang.sunrise.hour * 60 + panchang.sunrise.minute
                val brahma = TimeWindow(
                    startMinutes = (sunriseMins - 96).coerceAtLeast(0),
                    endMinutes = (sunriseMins - 48).coerceAtLeast(0)
                )
                PanchangUiState(
                    selectedDate = date,
                    hostCity = city,
                    panchangDay = panchang,
                    brahmaMuhurat = brahma,
                    userPreferences = prefs,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun previousDay() {
        _selectedDate.update { it.minusDays(1) }
    }

    fun nextDay() {
        _selectedDate.update { it.plusDays(1) }
    }

    fun jumpToToday() {
        _selectedDate.value = LocalDate.now()
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    /**
     * Blueprint Section 11.2: Changing the city recalculates Panchang for the currently
     * selected date without silently switching dates.
     */
    fun selectCity(city: City) {
        viewModelScope.launch {
            userPreferencesRepository.updateHostCityObject(city)
        }
    }
}
