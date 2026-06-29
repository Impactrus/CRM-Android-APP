package com.ossadkowski.crm.mobile.data.wizyty.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.ossadkowski.crm.mobile.domain.wizyty.model.NewVisitEvent
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitEventType
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * Receives geofence transitions. A visit candidate is recorded only on DWELL (staying
 * ~5 min) — ENTER alone is noise and EXIT is left for backend pairing. Detection is
 * suppressed while the rep is driving (Activity Recognition gate). The resulting row is
 * DETECTED and awaits manual confirmation.
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) {
            Log.e(TAG, "Geofence event error: ${event.errorCode}")
            return
        }
        if (event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_DWELL) return
        val triggering = event.triggeringGeofences ?: return

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WizytyEngineEntryPoint::class.java,
        )
        if (!entryPoint.activityRecognitionManager().allowsDetection()) {
            Log.d(TAG, "Detection suppressed (driving)")
            return
        }
        val repo = entryPoint.visitRepository()
        val source = entryPoint.contractorLocationSource()
        val notifier = entryPoint.wizytyNotifier()

        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                for (geofence in triggering) {
                    val loc = source.forKey(geofence.requestId)
                    val name = loc?.name ?: geofence.requestId
                    val result = repo.recordDetectedEvent(
                        NewVisitEvent(
                            contractorAccountNum = loc?.key,
                            contractorName = name,
                            addressLabel = loc?.label,
                            lat = loc?.lat,
                            lng = loc?.lng,
                            eventType = VisitEventType.DWELL,
                            occurredAt = Instant.now(),
                        ),
                    )
                    // Local notification about the detected visit (best-effort).
                    if (result is com.ossadkowski.crm.mobile.domain.common.Result.Success) {
                        notifier.notifyDetectedVisit(name)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to record detected visit", e)
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "GeofenceReceiver"
    }
}
