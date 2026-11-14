package com.example.platform.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object AuthHelper {

    fun signInWithGoogle(
        context: Context,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit
    ) {
        // google-services.json is intentionally not committed. The Google Services
        // Gradle plugin creates this resource only when a real Firebase config is supplied.
        // Resolve it dynamically so the app still compiles for offline/local builds.
        val clientIdResource = context.resources.getIdentifier(
            "default_web_client_id",
            "string",
            context.packageName
        )
        if (clientIdResource == 0) {
            onError("Google Sign-In is not configured for this build. Add the Firebase Android configuration to enable it.")
            return
        }
        val clientId = context.getString(clientIdResource).takeIf { it.isNotBlank() }
        if (clientId == null) {
            onError("Google Sign-In client ID is empty. Check the Firebase Android configuration.")
            return
        }

        val credentialManager = CredentialManager.create(context)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        scope.launch {
            try {
                val activity = context as? Activity ?: run {
                    onError("Context is not an Activity")
                    return@launch
                }
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    Firebase.auth.signInWithCredential(authCredential).await()
                    Log.d("AuthHelper", "Google Sign-In succeeded for ${Firebase.auth.currentUser?.email}")
                    onSuccess()
                } else {
                    onError("Unexpected credential type returned")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("AuthHelper", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
                onCancelled()
            } catch (e: Exception) {
                Log.e("AuthHelper", "Google Sign-In failed", e)
                onError(e.localizedMessage ?: "Sign in failed")
            }
        }
    }

    fun signOut(context: Context, scope: CoroutineScope, onComplete: () -> Unit) {
        Firebase.auth.signOut()
        val credentialManager = CredentialManager.create(context)
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("AuthHelper", "Failed to clear credential state", e)
            } finally {
                onComplete()
            }
        }
    }
}
