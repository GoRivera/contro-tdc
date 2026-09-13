package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * DataStore de preferencias de la app (tema, privacidad, hápticos, listas personalizables). Migra
 * automáticamente, la primera vez que se usa, cualquier valor que ya existiera en el
 * SharedPreferences "app_user_prefs" (el que usaba la app antes de este cambio), para que nada de
 * lo que el usuario ya configuró se pierda.
 */
val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_settings",
    produceMigrations = { context -> listOf(SharedPreferencesMigration(context, "app_user_prefs")) }
)

object PrefsKeys {
    val PRIVACY_MODE = booleanPreferencesKey("privacy_mode_enabled")
    val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val QUICK_CONCEPTS = stringPreferencesKey("quick_concepts")
    val PEOPLE_LIST = stringPreferencesKey("people_list")
    val PAYMENT_CONCEPTS = stringPreferencesKey("payment_concepts")
    val PAYERS_LIST = stringPreferencesKey("payers_list")
}
