package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.SamagriProgressDao
import com.example.data.local.SamagriProgressEntity
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType {
    CREATE, UPDATE, DELETE, LIST, GET, WRITE
}

class CloudSyncRepository(
    private val context: Context,
    private val samagriProgressDao: SamagriProgressDao,
    private val userPreferencesRepository: UserPreferencesRepository? = null,
    private val firestoreOverride: FirebaseFirestore? = null
) {
    private val auth
        get() = runCatching { Firebase.auth }.getOrNull()
    private val db: FirebaseFirestore by lazy {
        firestoreOverride ?: FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
    }

    val currentUserId: String?
        get() = auth?.currentUser?.uid

    val isUserSignedIn: Boolean
        get() = auth?.currentUser != null

    fun handleFirestoreError(
        exception: Exception,
        operationType: OperationType,
        path: String?
    ) {
        val currentUser = auth?.currentUser
        val providerArray = JSONArray()
        currentUser?.providerData?.forEach { profile ->
            val providerObj = JSONObject().apply {
                put("providerId", profile.providerId)
                if (com.example.BuildConfig.DEBUG) {
                    put("displayName", profile.displayName)
                    put("email", profile.email)
                    put("photoUrl", profile.photoUrl?.toString())
                }
            }
            providerArray.put(providerObj)
        }
        val authInfo = JSONObject().apply {
            put("userId", if (com.example.BuildConfig.DEBUG) currentUser?.uid else "[REDACTED]")
            if (com.example.BuildConfig.DEBUG) {
                put("email", currentUser?.email)
            }
            put("emailVerified", currentUser?.isEmailVerified)
            put("isAnonymous", currentUser?.isAnonymous)
            put("tenantId", currentUser?.tenantId)
            put("providerInfo", providerArray)
        }
        val errInfo = JSONObject().apply {
            put("error", exception.message ?: exception.toString())
            put("operationType", operationType.name.lowercase())
            put("path", if (com.example.BuildConfig.DEBUG) path else path?.substringBefore("/"))
            put("authInfo", authInfo)
        }
        val jsonString = errInfo.toString()
        Log.e("FirestoreError", jsonString, exception)
    }

    suspend fun syncUserPreferences(preferences: UserPreferences, rethrowOnError: Boolean = false) {
        val uid = auth?.currentUser?.uid ?: return
        val path = "users/$uid"
        val userDoc = db.collection("users").document(uid)

        val payload = hashMapOf<String, Any>(
            "displayName" to (auth?.currentUser?.displayName ?: "Devotee").take(100),
            "traditions" to preferences.selectedTraditions.map { it.id }.take(10),
            "hostCity" to preferences.hostCity.id.take(50),
            "preferredLanguage" to preferences.preferredLanguage.code.take(10),
            "reminderPrefs" to mapOf(
                "dayBefore" to preferences.dayBeforeReminder,
                "morningOf" to preferences.morningOfReminder,
                "muhuratStart" to preferences.muhuratStartReminder
            ),
            "themeMode" to preferences.themeMode.take(20),
            "textScaleMode" to preferences.textScaleMode.take(20),
            "savedFestivalIds" to preferences.savedFestivalIds.toList().take(100),
            "customReminders" to preferences.customReminders.map { it.serialize() }.take(100),
            "pujaStepProgress" to preferences.pujaStepProgress,
            "appMode" to preferences.appMode.take(20),
            "panditVoiceGender" to if (preferences.panditVoiceGender.equals("FEMALE", ignoreCase = true)) "FEMALE" else "MALE",
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            userDoc.set(payload, SetOptions.merge()).await()
            Log.d("CloudSync", "User profile synced successfully for $uid")
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            if (rethrowOnError) throw e
        }
    }

    suspend fun pullUserPreferences(rethrowOnError: Boolean = false): Map<String, Any>? {
        val uid = auth?.currentUser?.uid ?: return null
        val path = "users/$uid"
        return try {
            val doc = db.collection("users").document(uid).get().await()
            if (!doc.exists()) return null
            val data = doc.data ?: return null

            val traditionIds = (data["traditions"] as? List<*>)?.filterIsInstance<String>()
            val hostCityId = data["hostCity"] as? String
            val preferredLanguageCode = data["preferredLanguage"] as? String
            val reminderPrefs = data["reminderPrefs"] as? Map<*, *>
            val dayBefore = reminderPrefs?.get("dayBefore") as? Boolean
            val morningOf = reminderPrefs?.get("morningOf") as? Boolean
            val muhuratStart = reminderPrefs?.get("muhuratStart") as? Boolean
            val themeMode = data["themeMode"] as? String
            val textScaleMode = data["textScaleMode"] as? String
            val savedFestivalIds = (data["savedFestivalIds"] as? List<*>)?.filterIsInstance<String>()
            val customReminders = (data["customReminders"] as? List<*>)?.filterIsInstance<String>()
            val rawStepMap = data["pujaStepProgress"] as? Map<*, *>
            val pujaStepProgressMap = rawStepMap?.mapNotNull { (k, v) ->
                val key = k as? String ?: return@mapNotNull null
                val step = (v as? Number)?.toInt() ?: return@mapNotNull null
                key to step
            }?.toMap()
            val appMode = data["appMode"] as? String
            val panditVoiceGender = data["panditVoiceGender"] as? String

            userPreferencesRepository?.restoreFromCloud(
                traditionIds = traditionIds,
                hostCityId = hostCityId,
                preferredLanguageCode = preferredLanguageCode,
                dayBeforeReminder = dayBefore,
                morningOfReminder = morningOf,
                muhuratStartReminder = muhuratStart,
                themeMode = themeMode,
                textScaleMode = textScaleMode,
                savedFestivalIds = savedFestivalIds,
                customRemindersRaw = customReminders,
                pujaStepProgressMap = pujaStepProgressMap,
                appMode = appMode,
                panditVoiceGender = panditVoiceGender
            )
            data
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            if (rethrowOnError) throw e
            null
        }
    }

    suspend fun syncSamagriProgress(rethrowOnError: Boolean = false) {
        val uid = auth?.currentUser?.uid ?: return
        val progressList = samagriProgressDao.getAllProgressList()
        val path = "users/$uid/samagri_progress"

        val batch = db.batch()
        for (item in progressList) {
            val docId = "${item.festivalId}_${item.year}"
            val docRef = db.collection("users").document(uid).collection("samagri_progress").document(docId)

            val checkedItems = try {
                val array = JSONArray(item.checkedItemIdsJson)
                val list = mutableListOf<String>()
                for (i in 0 until array.length()) list.add(array.getString(i))
                list
            } catch (e: Exception) {
                emptyList<String>()
            }

            val payload = hashMapOf<String, Any>(
                "festivalId" to item.festivalId,
                "year" to item.year,
                "checkedItems" to checkedItems.take(100),
                "notes" to item.notes.take(1000),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            batch.set(docRef, payload, SetOptions.merge())
        }

        try {
            batch.commit().await()
            Log.d("CloudSync", "Samagri progress batch synced successfully for $uid")
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            if (rethrowOnError) throw e
        }
    }

    suspend fun pullRemoteProgress(rethrowOnError: Boolean = false) {
        val uid = auth?.currentUser?.uid ?: return
        pullUserPreferences(rethrowOnError = rethrowOnError)
        val path = "users/$uid/samagri_progress"
        try {
            val snapshot = db.collection("users").document(uid).collection("samagri_progress").get().await()
            for (doc in snapshot.documents) {
                val festivalId = doc.getString("festivalId") ?: continue
                val year = doc.getLong("year")?.toInt() ?: continue
                val checkedList = (doc.get("checkedItems") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val notes = doc.getString("notes") ?: ""

                val entity = SamagriProgressEntity(
                    festivalId = festivalId,
                    year = year,
                    checkedItemIdsJson = JSONArray(checkedList).toString(),
                    notes = notes,
                    updatedAt = System.currentTimeMillis(),
                    isSynced = true
                )
                samagriProgressDao.upsert(entity)
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            if (rethrowOnError) throw e
        }
    }

    suspend fun deleteUserData(rethrowOnError: Boolean = false) {
        val uid = auth?.currentUser?.uid ?: return
        val path = "users/$uid"
        try {
            val progressCollection = db.collection("users").document(uid).collection("samagri_progress").get().await()
            val batch = db.batch()
            for (doc in progressCollection.documents) {
                batch.delete(doc.reference)
            }
            batch.delete(db.collection("users").document(uid))
            batch.commit().await()
            Log.d("CloudSync", "Remote user data wiped for $uid")
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            if (rethrowOnError) throw e
        }
    }
}
