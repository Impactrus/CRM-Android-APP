package com.ossadkowski.crm.mobile.data.wizyty.location

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.ossadkowski.crm.mobile.domain.wizyty.repository.ContractorLocationSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coordinates a work session: registers geofences for the rep's contractor locations,
 * starts activity recognition + the foreground service on start, and tears everything
 * down on stop. The active flag is persisted so the boot receiver can restore geofences.
 */
@Singleton
class WizytySessionController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geofenceManager: GeofenceManager,
    private val activityRecognitionManager: ActivityRecognitionManager,
    private val locationSource: ContractorLocationSource,
    private val prefs: WizytyPrefs,
) {
    private val _active = MutableStateFlow(prefs.sessionActive)
    val active: StateFlow<Boolean> = _active.asStateFlow()

    suspend fun start() {
        prefs.sessionActive = true
        _active.value = true
        geofenceManager.register(locationSource.all())
        activityRecognitionManager.start()
        ContextCompat.startForegroundService(
            context,
            Intent(context, VisitDetectionForegroundService::class.java),
        )
    }

    fun stop() {
        prefs.sessionActive = false
        _active.value = false
        geofenceManager.clear()
        activityRecognitionManager.stop()
        context.stopService(Intent(context, VisitDetectionForegroundService::class.java))
    }

    /** Stop watching a single location (e.g. after deleting a test location). */
    fun removeGeofence(key: String) {
        geofenceManager.remove(listOf(key))
    }
}
