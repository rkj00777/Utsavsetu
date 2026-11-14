package com.example.base

import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.example.R
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
abstract class FirestoreEmulatorTestBase {

    lateinit var firestore: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var databaseId: String

    @Before
    open fun setUp() {
        val firestoreHost = System.getProperty("firebase.emulator.firestore.host") ?: "127.0.0.1"
        val firestorePort = System.getProperty("firebase.emulator.firestore.port")?.toIntOrNull() ?: 8085
        val authHost = System.getProperty("firebase.emulator.auth.host") ?: "127.0.0.1"
        val authPort = System.getProperty("firebase.emulator.auth.port")?.toIntOrNull() ?: 9099

        org.junit.Assume.assumeTrue(
            "Firebase Auth Emulator not running on $authHost:$authPort (run via firebase emulators:exec)",
            isPortOpen(authHost, authPort)
        )

        val context = ApplicationProvider.getApplicationContext<Context>()
        databaseId = context.getString(R.string.firestore_database_id)

        if (FirebaseApp.getApps(context).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setProjectId("demo-no-project")
                .setApplicationId("1:1234567890:android:abcdef")
                .setApiKey("fake-api-key-for-emulator")
                .build()
            FirebaseApp.initializeApp(context, options)
        }

        firestore = FirebaseFirestore.getInstance(databaseId)
        try {
            firestore.useEmulator(firestoreHost, firestorePort)
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                .build()
            firestore.firestoreSettings = settings
        } catch (e: IllegalStateException) {
            // Already configured in this JVM
        }

        auth = FirebaseAuth.getInstance()
        try {
            auth.useEmulator(authHost, authPort)
        } catch (e: IllegalStateException) {
            // Already configured
        }

        clearFirestoreEmulatorData(firestoreHost, firestorePort)
    }

    @After
    open fun tearDown() {
        try {
            auth.signOut()
            shadowOf(Looper.getMainLooper()).idle()
        } catch (_: Exception) {
        }
    }

    protected fun clearFirestoreEmulatorData(host: String = "127.0.0.1", port: Int = 8085) {
        val projectId = FirebaseApp.getInstance().options.projectId ?: "demo-no-project"
        val url = URL("http://$host:$port/emulator/v1/projects/$projectId/databases/$databaseId/documents")
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "DELETE"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val code = conn.responseCode
            if (code !in 200..299) {
                Log.w("FirestoreTestBase", "Failed to clear Firestore emulator data (HTTP $code)")
            }
        } catch (e: Exception) {
            Log.w("FirestoreTestBase", "Error clearing Firestore emulator data: ${e.message}")
        } finally {
            conn.disconnect()
        }
    }

    protected fun <T> awaitTask(task: Task<T>, timeoutSeconds: Long = 10): T {
        val deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutSeconds)
        while (!task.isComplete) {
            if (System.currentTimeMillis() > deadline) {
                throw TimeoutException("Timed out waiting for Firebase Task after ${timeoutSeconds}s")
            }
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(10)
        }
        if (task.isSuccessful) {
            return task.result
        } else {
            throw task.exception ?: RuntimeException("Task failed without exception")
        }
    }

    protected fun signInTestUser(
        email: String = "testuser@utsavsetu.com",
        password: String = "Password123!"
    ): String {
        return try {
            awaitTask(auth.createUserWithEmailAndPassword(email, password)).user!!.uid
        } catch (e: Exception) {
            awaitTask(auth.signInWithEmailAndPassword(email, password)).user!!.uid
        }
    }

    protected fun isPortOpen(host: String, port: Int): Boolean {
        return try {
            java.net.Socket().use { socket ->
                socket.connect(java.net.InetSocketAddress(host, port), 500)
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    protected fun runTestWithLooper(block: suspend () -> Unit) = runBlocking {
        block()
    }
}

