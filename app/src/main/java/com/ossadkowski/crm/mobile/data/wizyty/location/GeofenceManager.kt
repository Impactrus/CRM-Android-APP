package com.ossadkowski.crm.mobile.data.wizyty.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.ossadkowski.crm.mobile.domain.wizyty.model.ContractorLocation
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registers/clears geofences for contractor locations. Battery-conscious per the spec:
 * ENTER/DWELL/EXIT with a ~5-min loitering delay and relaxed responsiveness, capped at
 * [DetectionTuning.MAX_GEOFENCES] (platform limit is 100). Triggers are delivered to
 * [GeofenceBroadcastReceiver] via a single PendingIntent.
 */
@Singleton
class GeofenceManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val client = LocationServices.getGeofencingClient(context)

    private val pendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
            .setAction(ACTION_GEOFENCE_EVENT)
        PendingIntent.getBroadcast(context, REQUEST_CODE, intent, LocationPermissions.broadcastFlags())
    }

    @SuppressLint("MissingPermission") // runtime-checked below
    fun register(contractors: List<ContractorLocation>) {
        if (contractors.isEmpty()) {
            clear()
            return
        }
        if (!LocationPermissions.hasForegroundLocation(context)) {
            Log.w(TAG, "Geofence register skipped: no location permission")
            return
        }
        val geofences = contractors.take(DetectionTuning.MAX_GEOFENCES).map { c ->
            Geofence.Builder()
                .setRequestId(c.key)
                .setCircularRegion(c.lat, c.lng, DetectionTuning.GEOFENCE_RADIUS_M)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(
                    Geofence.GEOFENCE_TRANSITION_ENTER or
                        Geofence.GEOFENCE_TRANSITION_DWELL or
                        Geofence.GEOFENCE_TRANSITION_EXIT,
                )
                .setLoiteringDelay(DetectionTuning.LOITERING_DELAY_MS)
                .setNotificationResponsiveness(DetectionTuning.NOTIFICATION_RESPONSIVENESS_MS)
                .build()
        }
        val request = GeofencingRequest.Builder()
            // DWELL initial trigger: don't fire ENTER for places we're already inside.
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
            .addGeofences(geofences)
            .build()
        // Clear the previously-registered set first so the live geofences exactly match
        // `contractors`. Otherwise geofences for contractors that were removed (e.g. a
        // deleted test location) linger and keep firing. addGeofences only adds/updates
        // the requestIds we pass — it never prunes stale ones.
        client.removeGeofences(pendingIntent)
            .addOnCompleteListener {
                client.addGeofences(request, pendingIntent)
                    .addOnFailureListener { Log.e(TAG, "addGeofences failed", it) }
            }
    }

    fun clear() {
        client.removeGeofences(pendingIntent)
            .addOnFailureListener { Log.e(TAG, "removeGeofences failed", it) }
    }

    /** Remove specific geofences by their requestId (= contractor key). */
    fun remove(keys: List<String>) {
        if (keys.isEmpty()) return
        client.removeGeofences(keys)
            .addOnFailureListener { Log.e(TAG, "removeGeofences(keys) failed", it) }
    }

    companion object {
        const val ACTION_GEOFENCE_EVENT = "com.ossadkowski.crm.mobile.wizyty.GEOFENCE_EVENT"
        private const val REQUEST_CODE = 1001
        private const val TAG = "GeofenceManager"
    }
}
