package com.example.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.FestivalRepository
import com.example.domain.model.FestivalOccurrence
import com.example.panchang.City
import com.example.panchang.PanchangDay
import com.example.panchang.PanchangEngine
import com.example.panchang.RegionalTradition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth

enum class CalendarCategoryFilter(val label: String) {
    ALL("All festivals"),
    MAJOR("Major festivals"),
    REGIONAL("Regional festivals"),
    FASTS("Fasts & observances")
}

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.from(PanchangEngine.localDateForCity(City.DELHI)),
    val selectedDate: LocalDate = PanchangEngine.localDateForCity(City.DELHI),
    val selectedDayPanchang: PanchangDay? = null,
    val isListView: Boolean = false,
    val activeCategoryFilter: CalendarCategoryFilter = CalendarCategoryFilter.ALL,
    val activeTraditionFilter: RegionalTradition? = null,
    val monthOccurrences: List<FestivalOccurrence> = emptyList(),
    val allYearOccurrences: List<FestivalOccurrence> = emptyList(),
    val scheduledReminderFestivalIds: Set<String> = emptySet(),
    val hostCity: City = City.DELHI,
    val isLoading: Boolean = false
)

class CalendarViewModel(
    private val festivalRepository: FestivalRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private var cachedYearKey: String? = null
    private var cachedYearOccurrences: List<FestivalOccurrence> = emptyList()
    private var hasUserChangedDate = false

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            val initialCity = withContext(Dispatchers.IO) {
                festivalRepository.ensureCatalogLoaded()
                userPreferencesRepository.userPreferencesFlow.first().hostCity
            }
            if (!hasUserChangedDate) {
                val cityToday = PanchangEngine.localDateForCity(initialCity)
                _uiState.update {
                    it.copy(
                        currentMonth = YearMonth.from(cityToday),
                        selectedDate = cityToday,
                        hostCity = initialCity
                    )
                }
            }
            combine(
                userPreferencesRepository.userPreferencesFlow,
                festivalRepository.getAllFestivals(),
                _uiState.map { it.currentMonth }.distinctUntilChanged(),
                _uiState.map { it.activeTraditionFilter }.distinctUntilChanged(),
                _uiState.map { it.activeCategoryFilter }.distinctUntilChanged()
            ) { prefs, festivals, month, filter, categoryFilter ->
                val city = prefs.hostCity
                val reminderIds = prefs.customReminders.map { it.festivalId }.toSet()

                val yearKey = "${month.year}_${city.id}_${filter?.name}_${categoryFilter.name}_${festivals.size}_${prefs.selectedTraditions.hashCode()}"

                val (yearOccurrences, inMonth, selectedPanchang) = withContext(Dispatchers.Default) {
                    val computedYear = if (yearKey == cachedYearKey && cachedYearOccurrences.isNotEmpty()) {
                        cachedYearOccurrences
                    } else {
                        val traditionFiltered = if (filter != null) {
                            festivals.filter { it.traditions.contains(filter) }
                        } else {
                            festivals.filter { fest -> fest.traditions.any { prefs.selectedTraditions.contains(it) } }
                                .ifEmpty { festivals }
                        }

                        val categoryFiltered = when (categoryFilter) {
                            CalendarCategoryFilter.ALL -> traditionFiltered
                            CalendarCategoryFilter.MAJOR -> traditionFiltered.filter {
                                it.id in setOf(
                                    "diwali_lakshmi_puja",
                                    "govardhan_puja",
                                    "bhai_dooj",
                                    "dhanteras",
                                    "naraka_chaturdashi_choti_diwali",
                                    "labh_panchami",
                                    "chhath_puja",
                                    "tulsi_vivah_dev_uthani_ekadashi",
                                    "kartik_purnima",
                                    "guru_nanak_jayanti",
                                    "ganesh_chaturthi",
                                    "hartalika_teej",
                                    "makar_sankranti",
                                    "lohri_bhogi",
                                    "vasant_panchami",
                                    "maha_shivaratri",
                                    "holi_dhulandi",
                                    "gudi_padwa_ugadi",
                                    "cheti_chand_jhulelal",
                                    "rama_navami",
                                    "hanuman_jayanti",
                                    "mesha_sankranti_new_year",
                                    "akshaya_tritiya",
                                    "ratha_yatra",
                                    "guru_purnima",
                                    "nag_panchami",
                                    "raksha_bandhan",
                                    "krishna_janmashtami",
                                    "onam_thiruvonam",
                                    "navratri_ghatasthapana",
                                    "durga_ashtami",
                                    "maha_navami_ayudha_puja",
                                    "vijayadashami_dussehra",
                                    "karwa_chauth"
                                )
                            }
                            CalendarCategoryFilter.REGIONAL -> traditionFiltered.filter {
                                it.traditions.size <= 4 || it.id in setOf(
                                    "gudi_padwa_ugadi",
                                    "cheti_chand_jhulelal",
                                    "chhath_puja",
                                    "varalakshmi_vratam",
                                    "ganesh_chaturthi",
                                    "durga_ashtami",
                                    "maha_navami_ayudha_puja",
                                    "labh_panchami",
                                    "bathukamma",
                                    "bonalu",
                                    "nuakhai_rishi_panchami",
                                    "raja_parba",
                                    "gangaur",
                                    "thaipusam",
                                    "onam_thiruvonam",
                                    "mesha_sankranti_new_year"
                                )
                            }
                            CalendarCategoryFilter.FASTS -> traditionFiltered.filter {
                                it.id in setOf(
                                    "karwa_chauth",
                                    "maha_shivaratri",
                                    "krishna_janmashtami",
                                    "varalakshmi_vratam",
                                    "chhath_puja",
                                    "rama_navami",
                                    "jitiya_jivitputrika",
                                    "sankashti_chaturthi_sakat",
                                    "hartalika_teej",
                                    "kajari_teej",
                                    "hariyali_teej",
                                    "amalaki_ekadashi_rangbhari",
                                    "putrada_ekadashi",
                                    "mauni_amavasya",
                                    "hariyali_amavasya",
                                    "akshaya_navami_amla_navami",
                                    "kalabhairava_jayanti"
                                ) || it.aliases.any { a ->
                                    a.contains("vrat", ignoreCase = true) ||
                                        a.contains("upvas", ignoreCase = true) ||
                                        a.contains("nombu", ignoreCase = true) ||
                                        a.contains("ekadashi", ignoreCase = true) ||
                                        a.contains("chauth", ignoreCase = true) ||
                                        a.contains("teej", ignoreCase = true) ||
                                        a.contains("amavasya", ignoreCase = true)
                                }
                            }
                        }

                        festivalRepository.computeOccurrencesForYear(
                            festivals = categoryFiltered,
                            year = month.year,
                            city = city
                        ).also {
                            cachedYearKey = yearKey
                            cachedYearOccurrences = it
                        }
                    }

                    val filteredInMonth = computedYear.filter {
                        it.date.year == month.year && it.date.monthValue == month.monthValue
                    }

                    val panchang = PanchangEngine.computeDayPanchang(_uiState.value.selectedDate, city)
                    Triple(computedYear, filteredInMonth, panchang)
                }

                _uiState.update { current ->
                    current.copy(
                        hostCity = city,
                        monthOccurrences = inMonth,
                        allYearOccurrences = yearOccurrences,
                        scheduledReminderFestivalIds = reminderIds,
                        selectedDayPanchang = selectedPanchang
                    )
                }
            }.collect()
        }
    }

    fun selectDate(date: LocalDate) {
        hasUserChangedDate = true
        val city = _uiState.value.hostCity
        val targetMonth = YearMonth.from(date)
        _uiState.update {
            it.copy(
                currentMonth = targetMonth,
                selectedDate = date
            )
        }
        viewModelScope.launch(Dispatchers.Default) {
            val panchang = PanchangEngine.computeDayPanchang(date, city)
            _uiState.update { it.copy(selectedDayPanchang = panchang) }
        }
    }

    fun jumpToToday() {
        hasUserChangedDate = true
        val today = PanchangEngine.localDateForCity(_uiState.value.hostCity)
        selectYearMonth(YearMonth.from(today), preferredDay = today.dayOfMonth)
    }

    fun nextMonth() {
        val next = _uiState.value.currentMonth.plusMonths(1)
        val clampedYear = next.year.coerceIn(
            com.example.panchang.FestivalDateEvaluator.SUPPORTED_YEARS.first,
            com.example.panchang.FestivalDateEvaluator.SUPPORTED_YEARS.last
        )
        selectYearMonth(YearMonth.of(clampedYear, next.monthValue))
    }

    fun prevMonth() {
        val prev = _uiState.value.currentMonth.minusMonths(1)
        val clampedYear = prev.year.coerceIn(
            com.example.panchang.FestivalDateEvaluator.SUPPORTED_YEARS.first,
            com.example.panchang.FestivalDateEvaluator.SUPPORTED_YEARS.last
        )
        selectYearMonth(YearMonth.of(clampedYear, prev.monthValue))
    }

    fun selectMonth(monthValue: Int) {
        val safeMonth = monthValue.coerceIn(1, 12)
        val currentYear = _uiState.value.currentMonth.year
        selectYearMonth(YearMonth.of(currentYear, safeMonth))
    }

    fun selectYear(year: Int) {
        val clampedYear = year.coerceIn(
            com.example.panchang.FestivalDateEvaluator.SUPPORTED_YEARS.first,
            com.example.panchang.FestivalDateEvaluator.SUPPORTED_YEARS.last
        )
        val currentMonthValue = _uiState.value.currentMonth.monthValue
        selectYearMonth(YearMonth.of(clampedYear, currentMonthValue))
    }

    fun selectYearMonth(targetMonth: YearMonth, preferredDay: Int? = null) {
        hasUserChangedDate = true
        val current = _uiState.value
        val sameYearOccurrences = if (current.currentMonth.year == targetMonth.year) {
            current.allYearOccurrences.filter {
                it.date.year == targetMonth.year && it.date.monthValue == targetMonth.monthValue
            }
        } else {
            emptyList()
        }

        val targetDate = when {
            preferredDay != null -> targetMonth.atDay(preferredDay.coerceIn(1, targetMonth.lengthOfMonth()))
            sameYearOccurrences.isNotEmpty() -> sameYearOccurrences.first().date
            else -> {
                val safeDay = current.selectedDate.dayOfMonth.coerceIn(1, targetMonth.lengthOfMonth())
                targetMonth.atDay(safeDay)
            }
        }

        _uiState.update { state ->
            state.copy(
                currentMonth = targetMonth,
                selectedDate = targetDate,
                monthOccurrences = if (state.currentMonth.year == targetMonth.year) sameYearOccurrences else state.monthOccurrences
            )
        }

        viewModelScope.launch(Dispatchers.Default) {
            val panchang = PanchangEngine.computeDayPanchang(targetDate, _uiState.value.hostCity)
            _uiState.update { it.copy(selectedDayPanchang = panchang) }
        }
    }

    fun toggleViewMode() {
        _uiState.update { it.copy(isListView = !it.isListView) }
    }

    fun setListView(isAgenda: Boolean) {
        _uiState.update { it.copy(isListView = isAgenda) }
    }

    fun setCategoryFilter(category: CalendarCategoryFilter) {
        _uiState.update { it.copy(activeCategoryFilter = category) }
    }

    fun setTraditionFilter(tradition: RegionalTradition?) {
        _uiState.update { it.copy(activeTraditionFilter = tradition) }
    }
}
