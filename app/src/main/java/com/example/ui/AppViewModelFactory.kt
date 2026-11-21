package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.di.AppContainer
import com.example.ui.screens.calendar.CalendarViewModel
import com.example.ui.screens.detail.DetailViewModel
import com.example.ui.screens.festivals.FestivalsViewModel
import com.example.ui.screens.panchang.PanchangViewModel
import com.example.ui.screens.profile.ProfileViewModel
import com.example.ui.screens.setup.SetupViewModel
import com.example.ui.screens.today.TodayViewModel

class AppViewModelFactory(
    private val appContainer: AppContainer,
    private val festivalId: String? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SetupViewModel::class.java) -> {
                SetupViewModel(appContainer.userPreferencesRepository) as T
            }
            modelClass.isAssignableFrom(TodayViewModel::class.java) -> {
                TodayViewModel(
                    festivalRepository = appContainer.festivalRepository,
                    userPreferencesRepository = appContainer.userPreferencesRepository,
                    appContext = appContainer.context.applicationContext
                ) as T
            }
            modelClass.isAssignableFrom(CalendarViewModel::class.java) -> {
                CalendarViewModel(appContainer.festivalRepository, appContainer.userPreferencesRepository) as T
            }
            modelClass.isAssignableFrom(FestivalsViewModel::class.java) -> {
                FestivalsViewModel(appContainer.festivalRepository, appContainer.userPreferencesRepository) as T
            }
            modelClass.isAssignableFrom(PanchangViewModel::class.java) -> {
                PanchangViewModel(
                    userPreferencesRepository = appContainer.userPreferencesRepository,
                    festivalRepository = appContainer.festivalRepository,
                    appContext = appContainer.context.applicationContext
                ) as T
            }
            modelClass.isAssignableFrom(DetailViewModel::class.java) -> {
                DetailViewModel(
                    festivalId = festivalId ?: "",
                    festivalRepository = appContainer.festivalRepository,
                    userPreferencesRepository = appContainer.userPreferencesRepository,
                    cloudSyncRepository = appContainer.cloudSyncRepository
                ) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(
                    userPreferencesRepository = appContainer.userPreferencesRepository,
                    festivalRepository = appContainer.festivalRepository,
                    cloudSyncRepository = appContainer.cloudSyncRepository,
                    appContext = appContainer.context.applicationContext
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
