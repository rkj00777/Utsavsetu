package com.example.data.repository

import android.content.Context
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.example.base.FirestoreEmulatorTestBase
import com.example.data.local.SamagriProgressDao
import com.example.data.local.SamagriProgressEntity
import com.example.data.preferences.AppLanguage
import com.example.data.preferences.CustomReminderEntry
import com.example.data.preferences.UserPreferences
import com.example.panchang.City
import com.example.panchang.RegionalTradition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.Shadows.shadowOf

class FakeSamagriProgressDao : SamagriProgressDao {
    val items = mutableMapOf<String, SamagriProgressEntity>()

    override fun getProgress(festivalId: String, year: Int): Flow<SamagriProgressEntity?> {
        return flowOf(items["${festivalId}_$year"])
    }

    override suspend fun getProgressDirect(festivalId: String, year: Int): SamagriProgressEntity? {
        return items["${festivalId}_$year"]
    }

    override fun getAllProgress(): Flow<List<SamagriProgressEntity>> {
        return flowOf(items.values.toList())
    }

    override suspend fun getAllProgressList(): List<SamagriProgressEntity> {
        return items.values.toList()
    }

    override suspend fun upsert(progress: SamagriProgressEntity) {
        items["${progress.festivalId}_${progress.year}"] = progress
    }

    override suspend fun deleteAll() {
        items.clear()
    }
}

class CloudSyncRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private suspend fun awaitWithIdle(block: suspend () -> Unit) {
        var error: Throwable? = null
        val job = CoroutineScope(Dispatchers.Unconfined).launch {
            try {
                block()
            } catch (t: Throwable) {
                error = t
            }
        }
        val deadline = System.currentTimeMillis() + 10_000L
        while (job.isActive) {
            if (System.currentTimeMillis() > deadline) {
                job.cancel()
                throw RuntimeException("Timed out waiting for repository suspend call")
            }
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(15)
        }
        error?.let { throw it }
    }

    @Test
    fun syncAndPullUserPreferencesAndSamagriSucceedsForAuthenticatedOwner() = runTestWithLooper {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val uid = signInTestUser("aarav@utsavsetu.com", "Password123!")
        assertTrue(uid.isNotBlank())

        val fakeDao = FakeSamagriProgressDao()
        fakeDao.upsert(
            SamagriProgressEntity(
                festivalId = "diwali_lakshmi_puja",
                year = 2026,
                checkedItemIdsJson = "[\"diwali_s1\",\"diwali_s2\"]",
                notes = "Cow ghee diyas ready",
                updatedAt = System.currentTimeMillis(),
                isSynced = false
            )
        )

        val repo = CloudSyncRepository(
            context = context,
            samagriProgressDao = fakeDao,
            userPreferencesRepository = null,
            firestoreOverride = firestore
        )

        val prefs = UserPreferences(
            selectedTraditions = setOf(RegionalTradition.NORTH_INDIAN, RegionalTradition.MAHARASHTRIAN),
            hostCity = requireNotNull(City.findById("bengaluru")),
            preferredLanguage = AppLanguage.HINDI,
            dayBeforeReminder = true,
            morningOfReminder = true,
            muhuratStartReminder = true,
            themeMode = "DARK",
            textScaleMode = "LARGE",
            hasCompletedSetup = true,
            dpdpConsentGiven = true,
            savedFestivalIds = setOf("diwali_lakshmi_puja", "ganesh_chaturthi"),
            customReminders = listOf(
                CustomReminderEntry(
                    festivalId = "diwali_lakshmi_puja",
                    festivalName = "Diwali",
                    dateFormatted = "Sun, 8 Nov 2026",
                    triggerType = "ONE_DAY_BEFORE",
                    timeLabel = "08:00 PM"
                )
            ),
            pujaStepProgress = mapOf("diwali_lakshmi_puja" to 2),
            appMode = "CONSUMER",
            panditVoiceGender = "FEMALE"
        )

        awaitWithIdle {
            repo.syncUserPreferences(prefs, rethrowOnError = true)
            repo.syncSamagriProgress(rethrowOnError = true)
        }

        val remoteMap = repo.let {
            var res: Map<String, Any>? = null
            awaitWithIdle {
                res = it.pullUserPreferences(rethrowOnError = true)
            }
            res
        }
        assertNotNull(remoteMap)
        assertEquals("bengaluru", remoteMap?.get("hostCity"))
        assertEquals("hi", remoteMap?.get("preferredLanguage"))
        assertEquals("LARGE", remoteMap?.get("textScaleMode"))
        assertEquals("FEMALE", remoteMap?.get("panditVoiceGender"))

        // Clear local DAO and verify pullRemoteProgress restores it
        fakeDao.deleteAll()
        awaitWithIdle {
            repo.pullRemoteProgress(rethrowOnError = true)
        }
        val restored = fakeDao.getProgressDirect("diwali_lakshmi_puja", 2026)
        assertNotNull(restored)
        assertEquals("Cow ghee diyas ready", restored?.notes)

        // Verify cross-user isolation: Bob cannot read Aarav's profile document
        val bobUid = signInTestUser("bob@utsavsetu.com", "Password123!")
        assertTrue(bobUid != uid)
        var crossUserDenied = false
        try {
            awaitTask(firestore.collection("users").document(uid).get())
        } catch (e: Exception) {
            crossUserDenied = true
        }
        assertTrue("Cross-user access to another user's profile must be denied", crossUserDenied)
    }
}
