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
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseInitializer.ensureInitialized(context)
            }
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            false
        }

    /**
     * Inicia sesión con Correo Electrónico y Contraseña usando Firebase Auth.
     * Funciona en cualquier dispositivo o emulador sin depender de Google Play Services ni SHA-1.
     */
    suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Result<FirebaseAccountInfo> = withContext(Dispatchers.IO) {
        if (!isFirebaseReady) {
            return@withContext Result.failure(
                IllegalStateException("Firebase no está disponible en este momento.")
            )
        }
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || password.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("Por favor ingresa un correo y contraseña válidos.")
            )
        }

        try {
            val auth = FirebaseAuth.getInstance()
            val authResult = auth.signInWithEmailAndPassword(cleanEmail, password).await()
            val user = authResult.user
            if (user != null) {
                val accountInfo = FirebaseAccountInfo(
                    uid = user.uid,
                    email = user.email ?: cleanEmail,
                    displayName = user.displayName ?: cleanEmail.substringBefore("@"),
                    photoUrl = user.photoUrl?.toString(),
                    isAnonymous = false
                )
                Result.success(accountInfo)
            } else {
                Result.failure(Exception("No se pudo obtener información de la cuenta."))
            }
        } catch (e: com.google.firebase.auth.FirebaseAuthInvalidUserException) {
            Result.failure(Exception("No existe ninguna cuenta registrada con este correo."))
        } catch (e: com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Contraseña incorrecta o correo inválido."))
        } catch (e: Exception) {
            Log.e("GoogleAuthManager", "Error en signInWithEmailAndPassword", e)
            Result.failure(Exception(e.localizedMessage ?: "Error al iniciar sesión."))
        }
    }

    /**
     * Registra una nueva cuenta con Correo Electrónico y Contraseña usando Firebase Auth.
     */
    suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String,
        displayName: String
    ): Result<FirebaseAccountInfo> = withContext(Dispatchers.IO) {
        if (!isFirebaseReady) {
            return@withContext Result.failure(
                IllegalStateException("Firebase no está disponible en este momento.")
            )
        }
        val cleanEmail = email.trim()
        val cleanName = displayName.trim()
        if (cleanEmail.isBlank() || password.length < 6) {
            return@withContext Result.failure(
                IllegalArgumentException("La contraseña debe tener al menos 6 caracteres y el correo ser válido.")
            )
        }

        try {
            val auth = FirebaseAuth.getInstance()
            val authResult = auth.createUserWithEmailAndPassword(cleanEmail, password).await()
            val user = authResult.user
            if (user != null) {
                // Actualizar nombre de visualización si se proporcionó
                if (cleanName.isNotBlank()) {
                    try {
                        val profileUpdate = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(cleanName)
                            .build()
                        user.updateProfile(profileUpdate).await()
                    } catch (e: Exception) {
                        Log.w("GoogleAuthManager", "No se pudo actualizar el nombre de perfil", e)
                    }
                }
                val accountInfo = FirebaseAccountInfo(
                    uid = user.uid,
                    email = user.email ?: cleanEmail,
                    displayName = if (cleanName.isNotBlank()) cleanName else (user.displayName ?: cleanEmail.substringBefore("@")),
                    photoUrl = user.photoUrl?.toString(),
                    isAnonymous = false
                )
                Result.success(accountInfo)
            } else {
                Result.failure(Exception("No se pudo crear la cuenta de usuario."))
            }
        } catch (e: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
            Result.failure(Exception("Ya existe una cuenta con este correo. Prueba iniciando sesión."))
        } catch (e: com.google.firebase.auth.FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("La contraseña es muy débil. Debe tener al menos 6 caracteres."))
        } catch (e: Exception) {
            Log.e("GoogleAuthManager", "Error en signUpWithEmailAndPassword", e)
            Result.failure(Exception(e.localizedMessage ?: "Error al registrar la cuenta."))
        }
    }

    /**
     * Envía correo de recuperación de contraseña.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isFirebaseReady) {
            return@withContext Result.failure(
                IllegalStateException("Firebase no está disponible en este momento.")
            )
        }
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Ingresa un correo electrónico válido."))
        }
        try {
            FirebaseAuth.getInstance().sendPasswordResetEmail(cleanEmail).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Error al enviar correo de recuperación."))
        }
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
