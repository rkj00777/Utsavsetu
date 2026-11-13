package com.example.ui.screens.festivals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.remote.OnlineDiscoveredFestival
import com.example.data.remote.OnlineFestivalDiscoveryService
import com.example.data.repository.FestivalRepository
import com.example.domain.SearchEngine
import com.example.domain.model.FestivalOccurrence
import com.example.panchang.City
import com.example.panchang.RegionalTradition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

enum class DiscoveryCategory(val label: String) {
    ALL("All"),
    COMING_SOON("Coming soon"),
    MAJOR("Major festivals"),
    FASTS("Fasts & observances"),
    SAVED("Saved festivals")
}

data class MonthFestivalGroup(
    val monthName: String,
    val year: Int,
    val occurrences: List<FestivalOccurrence>
)

data class FestivalsUiState(
    val searchQuery: String = "",
    val activeCategory: DiscoveryCategory = DiscoveryCategory.ALL,
    val activeTraditionFilter: RegionalTradition? = null,
    val featuredOccurrence: FestivalOccurrence? = null,
    val groupedFestivals: List<MonthFestivalGroup> = emptyList(),
    val searchResults: List<FestivalOccurrence> = emptyList(),
    val matchedAliases: Map<String, String> = emptyMap(),
    val savedFestivalIds: Set<String> = emptySet(),
    val isSearching: Boolean = false,
    val isSearchingOnline: Boolean = false,
    val onlineDiscoveredResults: List<OnlineDiscoveredFestival> = emptyList(),
    val onlineStatusMessage: String? = null,
    val hostCity: City = City.DELHI,
    val isLoading: Boolean = true
)

class FestivalsViewModel(
    private val festivalRepository: FestivalRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FestivalsUiState())
    val uiState: StateFlow<FestivalsUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")
    private val categoryFlow = MutableStateFlow(DiscoveryCategory.ALL)
    private val filterFlow = MutableStateFlow<RegionalTradition?>(null)
    private var lastAutoOnlineQuery: String = ""

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                festivalRepository.ensureCatalogLoaded()
            }
            combine(
                userPreferencesRepository.userPreferencesFlow,
                festivalRepository.getAllFestivals(),
                queryFlow,
                categoryFlow,
                filterFlow
            ) { prefs, allFestivals, query, category, filter ->
                val today = LocalDate.now()
                val currentYear = today.year
                val city = prefs.hostCity
                val savedIds = prefs.savedFestivalIds

                val traditionFiltered = if (filter != null) {
                    allFestivals.filter { it.traditions.contains(filter) }
                } else {
                    allFestivals
                }

                val allOccurrences = festivalRepository.computeOccurrencesForYear(
                    festivals = traditionFiltered,
                    year = currentYear,
                    city = city
                )
                val nextYearOccurrences = festivalRepository.computeOccurrencesForYear(
                    festivals = traditionFiltered,
                    year = currentYear + 1,
                    city = city
                )

                val featured = (allOccurrences + nextYearOccurrences).firstOrNull { !it.date.isBefore(today) }
                    ?: allOccurrences.firstOrNull()

                val categoryFiltered = when (category) {
                    DiscoveryCategory.ALL -> allOccurrences
                    DiscoveryCategory.COMING_SOON -> (allOccurrences + nextYearOccurrences).filter { !it.date.isBefore(today) }.take(20)
                    DiscoveryCategory.MAJOR -> allOccurrences.filter {
                        it.festival.id in setOf(
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
                    DiscoveryCategory.FASTS -> allOccurrences.filter {
                        it.festival.id in setOf(
                            "karwa_chauth",
                            "maha_shivaratri",
                            "krishna_janmashtami",
                            "varalakshmi_vratam",
                            "chhath_puja",
                            "rama_navami",
                            "vat_savitri_vrat",
                            "nag_panchami",
                            "vaikunta_ekadashi",
                            "hartalika_teej",
                            "kajari_teej",
                            "hariyali_teej",
                            "amalaki_ekadashi_rangbhari",
                            "putrada_ekadashi",
                            "mauni_amavasya",
                            "hariyali_amavasya",
                            "akshaya_navami_amla_navami",
                            "kalabhairava_jayanti"
                        ) ||
                            it.festival.aliases.any { a ->
                                a.contains("vrat", ignoreCase = true) ||
                                    a.contains("upvas", ignoreCase = true) ||
                                    a.contains("nombu", ignoreCase = true) ||
                                    a.contains("ekadashi", ignoreCase = true) ||
                                    a.contains("teej", ignoreCase = true) ||
                                    a.contains("amavasya", ignoreCase = true)
                            }
                    }
                    DiscoveryCategory.SAVED -> allOccurrences.filter { savedIds.contains(it.festival.id) }
                }

                if (query.isNotBlank()) {
                    val matchingFestivals = SearchEngine.search(query, traditionFiltered)
                    val occById = allOccurrences.associateBy { it.festival.id }
                    val matchingOccurrences = matchingFestivals.mapNotNull { occById[it.id] }
                    val aliasMap = matchingOccurrences.mapNotNull { occ ->
                        val matchedAlias = SearchEngine.findMatchedRegionalName(query, occ.festival)
                        if (matchedAlias != null) occ.festival.id to matchedAlias else null
                    }.toMap()

                    val prevState = _uiState.value
                    FestivalsUiState(
                        searchQuery = query,
                        activeCategory = category,
                        activeTraditionFilter = filter,
                        featuredOccurrence = featured,
                        searchResults = matchingOccurrences,
                        matchedAliases = aliasMap,
                        savedFestivalIds = savedIds,
                        isSearching = true,
                        isSearchingOnline = prevState.isSearchingOnline,
                        onlineDiscoveredResults = prevState.onlineDiscoveredResults,
                        onlineStatusMessage = prevState.onlineStatusMessage,
                        hostCity = city,
                        isLoading = false
                    )
                } else {
                    val currentMonthValue = today.monthValue
                    val groups = categoryFiltered.groupBy { Pair(it.date.year, it.date.month) }
                        .entries
                        .sortedWith(
                            compareBy<Map.Entry<Pair<Int, java.time.Month>, List<FestivalOccurrence>>> { (yearMonth, _) ->
                                val (yr, m) = yearMonth
                                when {
                                    yr == currentYear && m.value >= currentMonthValue -> m.value - currentMonthValue
                                    yr > currentYear -> 12 + m.value
                                    else -> 24 + m.value
                                }
                            }
                        )
                        .map { (yearMonth, occs) ->
                            val (yr, month) = yearMonth
                            val orderedOccs = if (yr == currentYear && month.value == currentMonthValue) {
                                val upcomingInMonth = occs.filter { !it.date.isBefore(today) }
                                val earlierInMonth = occs.filter { it.date.isBefore(today) }
                                upcomingInMonth + earlierInMonth
                            } else {
                                occs
                            }
                            MonthFestivalGroup(
                                monthName = month.name.lowercase().replaceFirstChar { it.uppercase() },
                                year = yr,
                                occurrences = orderedOccs
                            )
                        }

                    FestivalsUiState(
                        searchQuery = query,
                        activeCategory = category,
                        activeTraditionFilter = filter,
                        featuredOccurrence = featured,
                        groupedFestivals = groups,
                        savedFestivalIds = savedIds,
                        isSearching = false,
                        isSearchingOnline = false,
                        onlineDiscoveredResults = emptyList(),
                        onlineStatusMessage = null,
                        hostCity = city,
                        isLoading = false
                    )
                }
            }.flowOn(Dispatchers.Default).collect { state ->
                _uiState.value = state
                // Auto-trigger online festival search & catalog update if a query >= 3 chars yields 0 local hits
                val trimmed = state.searchQuery.trim()
                if (state.isSearching && state.searchResults.isEmpty() && trimmed.length >= 3 && trimmed != lastAutoOnlineQuery) {
                    lastAutoOnlineQuery = trimmed
                    searchOnlineAndUpdateApp(trimmed)
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        if (query.isBlank()) {
            lastAutoOnlineQuery = ""
        }
        queryFlow.value = query
        _uiState.update {
            it.copy(
                searchQuery = query,
                onlineStatusMessage = if (query.isBlank()) null else it.onlineStatusMessage
            )
        }
    }

    fun searchOnlineAndUpdateApp(query: String = _uiState.value.searchQuery) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSearchingOnline = true,
                    onlineStatusMessage = null
                )
            }
            val discovered = OnlineFestivalDiscoveryService.searchOnlineForFestival(cleanQuery)
            if (discovered.isNotEmpty()) {
                // Immediately insert discovered festivals into the app's local Room database so they appear in search & calendar!
                discovered.forEach { item ->
                    festivalRepository.addOrUpdateFestival(item.entity)
                }
                _uiState.update {
                    it.copy(
                        isSearchingOnline = false,
                        onlineDiscoveredResults = discovered,
                        onlineStatusMessage = "Added ${discovered.first().entity.nameEnglish} to UtsavSetu catalog with computed Panchang timings!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSearchingOnline = false,
                        onlineStatusMessage = "No additional online festival entries found for '$cleanQuery'."
                    )
                }
            }
        }
    }

    fun setCategory(category: DiscoveryCategory) {
        categoryFlow.value = category
        _uiState.update { it.copy(activeCategory = category) }
    }

    fun setTraditionFilter(tradition: RegionalTradition?) {
        filterFlow.value = tradition
        _uiState.update { it.copy(activeTraditionFilter = tradition) }
    }

    fun toggleSavedFestival(festivalId: String) {
        viewModelScope.launch {
            userPreferencesRepository.toggleSavedFestival(festivalId)
        }
    }
}

