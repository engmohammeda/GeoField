package com.agon.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "geofield_settings")

/**
 * Persists user preferences (theme, units, language, precision, project name).
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val UNITS = stringPreferencesKey("units")
        val LANGUAGE = stringPreferencesKey("language")
        val DECIMALS = intPreferencesKey("decimals")
        val PROJECT = stringPreferencesKey("project_name")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[Keys.THEME]?.let { name ->
                ThemeMode.entries.firstOrNull { it.name == name }
            } ?: ThemeMode.SYSTEM,
            units = prefs[Keys.UNITS]?.let { name ->
                UnitSystem.entries.firstOrNull { it.name == name }
            } ?: UnitSystem.METRIC,
            language = prefs[Keys.LANGUAGE]?.let { name ->
                AppLanguage.entries.firstOrNull { it.name == name }
            } ?: AppLanguage.ARABIC,
            decimals = prefs[Keys.DECIMALS]?.coerceIn(0, 4) ?: 2,
            projectName = prefs[Keys.PROJECT] ?: "مشروع المسح الميداني",
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[Keys.THEME] = mode.name }
    }

    suspend fun setUnits(units: UnitSystem) {
        context.settingsDataStore.edit { it[Keys.UNITS] = units.name }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.settingsDataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun setDecimals(decimals: Int) {
        context.settingsDataStore.edit { it[Keys.DECIMALS] = decimals.coerceIn(0, 4) }
    }

    suspend fun setProjectName(name: String) {
        context.settingsDataStore.edit { it[Keys.PROJECT] = name }
    }
}
