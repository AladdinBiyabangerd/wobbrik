package com.aladdin.wobbrik

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider

/**
 * Obtains a Google ID token for linking the current anonymous Firebase user in place.
 * Never signs in over the guest; collision must not adopt another wallet UID.
 */
internal object GoogleGuestLink {
    fun serverClientId(context: Context): String {
        BuildConfig.GOOGLE_WEB_CLIENT_ID.trim().takeIf { it.isNotEmpty() }?.let { return it }
        val id = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        return if (id != 0) context.getString(id).trim() else ""
    }

    suspend fun googleCredential(activity: Activity, serverClientId: String): AuthCredential {
        require(serverClientId.isNotBlank()) { "Google web client ID konfiqurasiya edilməyib" }
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false)
            .build()
        val response = CredentialManager.create(activity)
            .getCredential(activity, GetCredentialRequest.Builder().addCredentialOption(option).build())
        val credential = response.credential
        require(credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            "Google ID token credential gözlənilirdi"
        }
        val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
        require(token.isNotBlank()) { "Google boş ID token qaytardı" }
        return GoogleAuthProvider.getCredential(token, null)
    }

    fun userMessage(failure: Throwable): String? = when (failure) {
        is GetCredentialCancellationException -> null
        is NoCredentialException -> "Cihazda Google hesabı tapılmadı"
        is FirebaseAuthUserCollisionException ->
            "Bu Google hesabı artıq başqa oyun hesabına bağlıdır"
        else -> failure.message?.takeIf { it.isNotBlank() } ?: "Google bağlantısı alınmadı"
    }
}
