package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.Coordinates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "porter_settings")

data class UserSettings(
    val homeCoordinates: Coordinates?,
    val homeLabel: String,
    val radiusKm: Double,
    val bufferKm: Double,
    val overlayEnabled: Boolean,
    val isMonitoringActive: Boolean
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val HOME_LAT = doublePreferencesKey("home_lat")
        val HOME_LNG = doublePreferencesKey("home_lng")
        val HOME_LABEL = stringPreferencesKey("home_label")
        val RADIUS_KM = doublePreferencesKey("radius_km")
        val BUFFER_KM = doublePreferencesKey("buffer_km")
        val OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
        val MONITORING_ACTIVE = booleanPreferencesKey("monitoring_active")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        val lat = prefs[Keys.HOME_LAT]
        val lng = prefs[Keys.HOME_LNG]
        val coords = if (lat != null && lng != null && Coordinates.isValid(lat, lng)) {
            Coordinates(lat, lng)
        } else {
            null
        }

        UserSettings(
            homeCoordinates = coords,
            homeLabel = prefs[Keys.HOME_LABEL] ?: "Home Base",
            radiusKm = prefs[Keys.RADIUS_KM] ?: 20.0,
            bufferKm = prefs[Keys.BUFFER_KM] ?: 1.0,
            overlayEnabled = prefs[Keys.OVERLAY_ENABLED] ?: true,
            isMonitoringActive = prefs[Keys.MONITORING_ACTIVE] ?: false
        )
    }

    suspend fun saveHomeCoordinates(lat: Double, lng: Double, label: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HOME_LAT] = lat
            prefs[Keys.HOME_LNG] = lng
            prefs[Keys.HOME_LABEL] = label
        }
    }

    suspend fun clearHomeLocation() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.HOME_LAT)
            prefs.remove(Keys.HOME_LNG)
        }
    }

    suspend fun saveRadiusAndBuffer(radiusKm: Double, bufferKm: Double) {
        context.dataStore.edit { prefs ->
            prefs[Keys.RADIUS_KM] = radiusKm
            prefs[Keys.BUFFER_KM] = bufferKm
        }
    }

    suspend fun setOverlayEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.OVERLAY_ENABLED] = enabled
        }
    }

    suspend fun setMonitoringActive(active: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.MONITORING_ACTIVE] = active
        }
    }

    suspend fun resetDefaults() {
        context.dataStore.edit { prefs ->
            prefs[Keys.RADIUS_KM] = 20.0
            prefs[Keys.BUFFER_KM] = 1.0
            prefs[Keys.OVERLAY_ENABLED] = true
            prefs[Keys.MONITORING_ACTIVE] = false
        }
    }
}
