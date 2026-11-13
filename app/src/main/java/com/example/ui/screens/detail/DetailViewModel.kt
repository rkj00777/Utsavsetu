package com.example.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.CustomReminderEntry
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.CloudSyncRepository
import com.example.data.repository.FestivalRepository
import com.example.domain.model.Festival
import com.example.domain.model.SamagriProgress
import com.example.panchang.City
import com.example.panchang.PanchangDay
import com.example.panchang.PanchangEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DetailUiState(
    val isLoading: Boolean = true,
    val selectedYear: Int = LocalDate.now().year,
    val festival: Festival? = null,
    val resolvedDate: LocalDate? = null,
    val muhuratWindow: String = "",
    val muhuratBreakdown: String = "",
    val tithiSpanFormatted: String? = null,
    val godhuliMuhuratFormatted: String = "",
    val panchang: PanchangDay? = null,
    val hostCity: City = City.DELHI,
    val samagriProgress: SamagriProgress? = null,
    val isSaved: Boolean = false,
    val isReminderSet: Boolean = false,
    val activeReminders: List<CustomReminderEntry> = emptyList(),
    val savedPujaStepIndex: Int = 0,
    val panditVoiceGender: String = "MALE",
    val errorMessage: String? = null
)

class DetailViewModel(
    private val festivalId: String,
    private val festivalRepository: FestivalRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val cloudSyncRepository: CloudSyncRepository
) : ViewModel() {

    private val _selectedYear = MutableStateFlow(LocalDate.now().year)
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadFestivalDetail()
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun loadFestivalDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                combine(
                    festivalRepository.getFestivalById(festivalId),
                    userPreferencesRepository.userPreferencesFlow,
                    _selectedYear,
                    _selectedYear.flatMapLatest { yr -> festivalRepository.getSamagriProgress(festivalId, yr) }
                ) { fest, prefs, year, progress ->
                    if (fest == null) {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Festival not found") }
                        return@combine
                    }

                    val city = prefs.hostCity
                    val occurrences = festivalRepository.computeOccurrencesForYear(listOf(fest), year, city)
                    val occurrence = occurrences.firstOrNull()
                    val resolvedDate = occurrence?.date ?: LocalDate.of(year, 1, 1)
                    val muhuratWindow = occurrence?.muhuratWindowFormatted ?: ""
                    val muhuratBreakdown = festivalRepository.formatMuhuratBreakdownForCity(fest, resolvedDate, city)
                    val panchang = PanchangEngine.computeDayPanchang(resolvedDate, city)
                    val tithiSpan = PanchangEngine.formatTithiStartAndEnd(resolvedDate, city, fest.dateRule)
                    val godhuliFormatted = PanchangEngine.computeGodhuliWindow(panchang).format()
                    val festReminders = prefs.customReminders.filter { it.festivalId == festivalId }
                    val savedStep = prefs.pujaStepProgress[festivalId] ?: 0

                    _uiState.value = DetailUiState(
                        isLoading = false,
                        selectedYear = year,
                        festival = fest,
                        resolvedDate = resolvedDate,
                        muhuratWindow = muhuratWindow,
                        muhuratBreakdown = muhuratBreakdown,
                        tithiSpanFormatted = tithiSpan,
                        godhuliMuhuratFormatted = godhuliFormatted,
                        panchang = panchang,
                        hostCity = city,
                        samagriProgress = progress,
                        isSaved = prefs.savedFestivalIds.contains(festivalId),
                        isReminderSet = festReminders.isNotEmpty(),
                        activeReminders = festReminders,
                        savedPujaStepIndex = savedStep,
                        panditVoiceGender = prefs.panditVoiceGender
                    )
                }.collect()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Error loading details") }
            }
        }
    }

    fun selectYear(year: Int) {
        _selectedYear.value = year
    }

    fun selectCity(city: City) {
        viewModelScope.launch {
            userPreferencesRepository.updateHostCityObject(city)
            syncPrefsIfSignedIn()
        }
    }

    fun toggleSamagriItem(itemId: String) {
        viewModelScope.launch {
            val currentYear = _selectedYear.value
            festivalRepository.toggleSamagriItem(festivalId, currentYear, itemId)
            if (cloudSyncRepository.isUserSignedIn) {
                cloudSyncRepository.syncSamagriProgress()
            }
        }
    }

    fun resetSamagriChecklist() {
        viewModelScope.launch {
            val currentYear = _selectedYear.value
            festivalRepository.resetSamagriChecklist(festivalId, currentYear)
            if (cloudSyncRepository.isUserSignedIn) {
                cloudSyncRepository.syncSamagriProgress()
            }
        }
    }

    fun updateNotes(notes: String) {
        viewModelScope.launch {
            val currentYear = _selectedYear.value
            festivalRepository.updateNotes(festivalId, currentYear, notes)
            if (cloudSyncRepository.isUserSignedIn) {
                cloudSyncRepository.syncSamagriProgress()
            }
        }
    }

    fun toggleSavedFestival() {
        viewModelScope.launch {
            userPreferencesRepository.toggleSavedFestival(festivalId)
            syncPrefsIfSignedIn()
        }
    }

    fun saveCustomReminder(entry: CustomReminderEntry) {
        viewModelScope.launch {
            userPreferencesRepository.upsertCustomReminder(entry)
            syncPrefsIfSignedIn()
        }
    }

    fun savePujaStepProgress(stepIndex: Int) {
        viewModelScope.launch {
            userPreferencesRepository.savePujaStepProgress(festivalId, stepIndex)
            syncPrefsIfSignedIn()
        }
    }

    private suspend fun syncPrefsIfSignedIn() {
        if (cloudSyncRepository.isUserSignedIn) {
            val prefs = userPreferencesRepository.userPreferencesFlow.first()
            cloudSyncRepository.syncUserPreferences(prefs)
        }
    }
}
