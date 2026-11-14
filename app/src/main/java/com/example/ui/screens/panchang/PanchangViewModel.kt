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
    val selectedDate: LocalDate = PanchangEngine.localDateForCity(City.DELHI),
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

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
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
            ) { prefs, explicitDate ->
                val city = prefs.hostCity
                val date = explicitDate ?: PanchangEngine.localDateForCity(city)
                val panchang = festivalRepository?.getAndCacheDayPanchang(date, city)
                    ?: PanchangEngine.computeDayPanchang(date, city)
                PanchangUiState(
                    selectedDate = date,
                    hostCity = city,
                    panchangDay = panchang,
                    brahmaMuhurat = panchang.brahmaMuhurat,
                    userPreferences = prefs,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun previousDay() {
        val current = _selectedDate.value ?: _uiState.value.selectedDate
        _selectedDate.value = current.minusDays(1)
    }

    fun nextDay() {
        val current = _selectedDate.value ?: _uiState.value.selectedDate
        _selectedDate.value = current.plusDays(1)
    }

    fun jumpToToday() {
        _selectedDate.value = PanchangEngine.localDateForCity(_uiState.value.hostCity)
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
