package com.screen.autolocker.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.settingsStore by preferencesDataStore("settings_prefs")

data class SavedThemePreset(
    val name: String,
    val value: String
)

data class SettingsState(
    val theme: String = "Purple",
    val amoledPolish: Boolean = false,
    val overlayEnabled: Boolean = false,
    val lastMinutes: Int = 15,
    val savedThemes: List<SavedThemePreset> = emptyList(),
    val privacyAccepted: Boolean = false
)

private object SettingsKeys {
    val THEME = stringPreferencesKey("theme")
    val AMOLED = booleanPreferencesKey("amoled_polish")
    val OVERLAY = booleanPreferencesKey("overlay_enabled")
    val LAST_MINUTES = intPreferencesKey("last_minutes")
    val SAVED_THEMES = stringPreferencesKey("saved_themes")
    val PRIVACY_ACCEPTED = booleanPreferencesKey("privacy_accepted")
}

class SettingsRepository(private val context: Context) {

    val state: Flow<SettingsState> = context.settingsStore.data.map { prefs ->
        SettingsState(
            theme = prefs[SettingsKeys.THEME] ?: "Purple",
            amoledPolish = prefs[SettingsKeys.AMOLED] ?: false,
            overlayEnabled = prefs[SettingsKeys.OVERLAY] ?: false,
            lastMinutes = prefs[SettingsKeys.LAST_MINUTES] ?: 15,
            savedThemes = decodeThemes(prefs[SettingsKeys.SAVED_THEMES].orEmpty()),
            privacyAccepted = prefs[SettingsKeys.PRIVACY_ACCEPTED] ?: false
        )
    }

    suspend fun setTheme(theme: String) {
        context.settingsStore.edit { it[SettingsKeys.THEME] = theme }
    }

    suspend fun setAmoledPolish(enabled: Boolean) {
        context.settingsStore.edit { it[SettingsKeys.AMOLED] = enabled }
    }

    suspend fun setOverlayEnabled(enabled: Boolean) {
        context.settingsStore.edit { it[SettingsKeys.OVERLAY] = enabled }
    }

    suspend fun setLastMinutes(minutes: Int) {
        context.settingsStore.edit { it[SettingsKeys.LAST_MINUTES] = minutes }
    }

    suspend fun setPrivacyAccepted(accepted: Boolean) {
        context.settingsStore.edit { it[SettingsKeys.PRIVACY_ACCEPTED] = accepted }
    }

    suspend fun saveThemePreset(name: String, value: String) {
        context.settingsStore.edit { prefs ->
            val updated = decodeThemes(prefs[SettingsKeys.SAVED_THEMES].orEmpty())
                .filterNot { it.name.equals(name, true) }
                .plus(SavedThemePreset(name, value))
                .takeLast(15)
            prefs[SettingsKeys.SAVED_THEMES] = encodeThemes(updated)
        }
    }

    suspend fun deleteThemePreset(name: String) {
        context.settingsStore.edit { prefs ->
            val updated = decodeThemes(prefs[SettingsKeys.SAVED_THEMES].orEmpty())
                .filterNot { it.name.equals(name, true) }
            prefs[SettingsKeys.SAVED_THEMES] = encodeThemes(updated)
        }
    }

    suspend fun restoreThemeState(
        theme: String,
        amoled: Boolean,
        savedThemes: List<SavedThemePreset>
    ) {
        context.settingsStore.edit { prefs ->
            prefs[SettingsKeys.THEME] = theme
            prefs[SettingsKeys.AMOLED] = amoled
            prefs[SettingsKeys.SAVED_THEMES] = encodeThemes(savedThemes)
        }
    }

    fun decodeThemes(raw: String): List<SavedThemePreset> {
        return raw.split(";;")
            .mapNotNull { entry ->
                val parts = entry.split("|")
                val name = parts.getOrNull(0)?.trim().orEmpty()
                val value = parts.getOrNull(1)?.trim().orEmpty()
                if (name.isBlank() || value.isBlank()) null else SavedThemePreset(name, value)
            }
    }

    fun encodeThemes(themes: List<SavedThemePreset>): String {
        return themes.joinToString(";;") { "${it.name}|${it.value}" }
    }

    fun snapshot(): SettingsState = runBlocking { state.first() }
}
