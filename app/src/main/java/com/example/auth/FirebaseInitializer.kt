package com.example.auth

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

/**
 * Inicializador seguro y tolerante a fallos para Firebase.
 * Garantiza que FirebaseApp esté disponible en cualquier entorno (emulador, dispositivo real, etc.)
 * utilizando los parámetros configurados en google-services.json para el proyecto 'control-tdc'.
 */
object FirebaseInitializer {
    private const val TAG = "FirebaseInitializer"

    // Parámetros del proyecto control-tdc
    const val DEFAULT_WEB_CLIENT_ID = "233930565744-j4f4e3nipr30e4i29m1pcelf6vim4bvl.apps.googleusercontent.com"
    const val PROJECT_ID = "control-tdc"
    const val APPLICATION_ID = "1:233930565744:android:e37e422b249d8583abacb5"
    const val API_KEY = "AIzaSyAvT3qhbpZEXnHOcwMMQV0G2hY_ZJub30U"
    const val STORAGE_BUCKET = "control-tdc.firebasestorage.app"
    const val GCM_SENDER_ID = "233930565744"
    const val PACKAGE_NAME = "com.aistudio.creditcards.qwvpkz"
    const val DEBUG_SHA1 = "88:6A:7E:94:23:8B:07:89:71:ED:3A:D9:D1:D5:23:DD:75:DC:1C:9B"

    fun ensureInitialized(context: Context): Boolean {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val app = FirebaseApp.initializeApp(context)
                if (app == null) {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId(APPLICATION_ID)
                        .setApiKey(API_KEY)
                        .setProjectId(PROJECT_ID)
                        .setStorageBucket(STORAGE_BUCKET)
                        .setGcmSenderId(GCM_SENDER_ID)
                        .build()
                    FirebaseApp.initializeApp(context, options)
                }
            }
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            try {
                val options = FirebaseOptions.Builder()
                    .setApplicationId(APPLICATION_ID)
                    .setApiKey(API_KEY)
                    .setProjectId(PROJECT_ID)
                    .setStorageBucket(STORAGE_BUCKET)
                    .setGcmSenderId(GCM_SENDER_ID)
                    .build()
                FirebaseApp.initializeApp(context, options)
                true
            } catch (e2: Exception) {
                Log.e(TAG, "Error initializing Firebase: ${e2.message}", e2)
                false
            }
        }
    }
}
