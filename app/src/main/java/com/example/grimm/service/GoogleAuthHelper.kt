package com.example.grimm.service

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

sealed class GoogleAuthResult {
    data class Success(
        val displayName: String,
        val email: String,
        val avatarUrl: String?,
        val idToken: String? = null
    ) : GoogleAuthResult()

    data class Error(val message: String, val isCancelled: Boolean = false) : GoogleAuthResult()
}

object GoogleAuthHelper {
    private const val TAG = "GoogleAuthHelper"

    suspend fun signInWithGoogle(context: Context, webClientId: String? = null): GoogleAuthResult {
        return try {
            val credentialManager = CredentialManager.create(context)

            // Use configured client ID or standard Google identity options
            val clientId = webClientId?.takeIf { it.isNotBlank() }
                ?: "grimm-ai-app.apps.googleusercontent.com"

            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                GoogleAuthResult.Success(
                    displayName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.givenName ?: "Google User",
                    email = googleIdTokenCredential.id,
                    avatarUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                    idToken = googleIdTokenCredential.idToken
                )
            } else {
                GoogleAuthResult.Error("Unsupported credential type: ${credential.type}")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "Google Sign-In was cancelled by user")
            GoogleAuthResult.Error("Sign in cancelled", isCancelled = true)
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Google Credential Manager reported: ${e.message}", e)
            GoogleAuthResult.Error(e.message ?: "Google Sign-In failed")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google Sign-In", e)
            GoogleAuthResult.Error(e.message ?: "Authentication error")
        }
    }
}
