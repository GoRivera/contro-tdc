package com.example.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.BuildConfig
import com.example.data.sync.FirebaseAccountInfo
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Administrador de Autenticación con Google Sign-In usando el moderno Android Credential Manager y Firebase Auth.
 */
class GoogleAuthManager(private val context: Context) {

    companion object {
        // Web Client ID generado automáticamente por Firebase para tu proyecto control-tdc
        const val DEFAULT_WEB_CLIENT_ID = "233930565744-j4f4e3nipr30e4i29m1pcelf6vim4bvl.apps.googleusercontent.com"
    }

    private val credentialManager = CredentialManager.create(context)

    val isFirebaseReady: Boolean
        get() = try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            false
        }

    /**
     * Inicia el flujo nativo de Google Sign-In mediante Credential Manager.
     * @param serverClientId El Web Client ID generado en Firebase Console (si está vacío, usa el predeterminado).
     */
    suspend fun signInWithGoogle(serverClientId: String = DEFAULT_WEB_CLIENT_ID): Result<FirebaseAccountInfo> = withContext(Dispatchers.IO) {
        if (!isFirebaseReady) {
            return@withContext Result.failure(
                IllegalStateException("Firebase aún no está configurado con google-services.json.")
            )
        }

        val effectiveClientId = serverClientId.ifBlank { DEFAULT_WEB_CLIENT_ID }
        if (effectiveClientId.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("Debes proporcionar el Web Client ID de tu proyecto de Firebase.")
            )
        }

        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(effectiveClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                // Autenticar en Firebase con el token recibido
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = FirebaseAuth.getInstance().signInWithCredential(authCredential).await()
                val user = authResult.user

                if (user != null) {
                    val accountInfo = FirebaseAccountInfo(
                        uid = user.uid,
                        email = user.email ?: "",
                        displayName = user.displayName ?: "Usuario",
                        photoUrl = user.photoUrl?.toString(),
                        isAnonymous = false
                    )
                    Result.success(accountInfo)
                } else {
                    Result.failure(Exception("No se obtuvo información del usuario de Firebase."))
                }
            } else {
                Result.failure(Exception("Credencial no reconocida o no soportada."))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d("GoogleAuthManager", "Inicio de sesión cancelado por el usuario.")
            Result.failure(Exception("Inicio de sesión cancelado."))
        } catch (e: Exception) {
            Log.e("GoogleAuthManager", "Error en inicio de sesión con Google", e)
            Result.failure(e)
        }
    }

    /**
     * Cierra la sesión en Firebase y limpia el estado de credenciales de Google en el dispositivo.
     */
    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (isFirebaseReady) {
                FirebaseAuth.getInstance().signOut()
            }
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("GoogleAuthManager", "Error al cerrar sesión", e)
            Result.failure(e)
        }
    }
}
