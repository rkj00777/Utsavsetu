package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.CloudSyncRepository
import com.example.data.repository.FestivalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(val context: Context) {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(context, applicationScope)
    }

    val festivalRepository: FestivalRepository by lazy {
        FestivalRepository(
            festivalDao = database.festivalDao(),
            samagriProgressDao = database.samagriProgressDao(),
            dailyPanchangDao = database.dailyPanchangDao(),
            muhuratDao = database.muhuratDao()
        )
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context)
    }

    val cloudSyncRepository: CloudSyncRepository by lazy {
        CloudSyncRepository(
            context = context,
            samagriProgressDao = database.samagriProgressDao(),
            userPreferencesRepository = userPreferencesRepository
        )
    }
}
