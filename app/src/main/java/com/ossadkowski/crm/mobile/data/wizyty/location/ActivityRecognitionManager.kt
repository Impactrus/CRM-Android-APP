package com.ossadkowski.crm.mobile.data.wizyty.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Activity Recognition gate. Transition events are nearly free energetically; we use
 * them to suppress visit detection while the rep is driving (IN_VEHICLE) so a drive-by
 * never becomes a visit. The latest activity is cached in [WizytyPrefs] and read by
 * [GeofenceBroadcastReceiver].
 */
@Singleton
class ActivityRecognitionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: WizytyPrefs,
) {
    private val client = ActivityRecognition.getClient(context)

    private val pendingIntent: PendingIntent by lazy {
        val intent = Intent(context, ActivityTransitionReceiver::class.java)
            .setAction(ACTION_ACTIVITY_TRANSITION)
        PendingIntent.getBroadcast(context, REQUEST_CODE, intent, LocationPermissions.broadcastFlags())
    }

    @SuppressLint("MissingPermission") // runtime-checked below
    fun start() {
        if (!LocationPermissions.hasActivityRecognition(context)) {
            Log.w(TAG, "Activity recognition skipped: no permission")
            return
        }
        val transitions = listOf(
            transition(DetectedActivity.STILL, ActivityTransition.ACTIVITY_TRANSITION_ENTER),
            transition(DetectedActivity.STILL, ActivityTransition.ACTIVITY_TRANSITION_EXIT),
            transition(DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_ENTER),
            transition(DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_EXIT),
        )
        client.requestActivityTransitionUpdates(ActivityTransitionRequest(transitions), pendingIntent)
            .addOnFailureListener { Log.e(TAG, "requestActivityTransitionUpdates failed", it) }
    }

    fun stop() {
        client.removeActivityTransitionUpdates(pendingIntent)
            .addOnFailureListener { Log.e(TAG, "removeActivityTransitionUpdates failed", it) }
    }

    /** Called by [ActivityTransitionReceiver] with the most recent transition's type. */
    fun onActivityTransition(activityType: Int) {
        prefs.lastActivityType = activityType
    }

    /** Suppress detection only while actively driving; allow otherwise (incl. unknown). */
    fun allowsDetection(): Boolean = prefs.lastActivityType != DetectedActivity.IN_VEHICLE

    private fun transition(activity: Int, type: Int): ActivityTransition =
        ActivityTransition.Builder()
            .setActivityType(activity)
            .setActivityTransition(type)
            .build()

    companion object {
        const val ACTION_ACTIVITY_TRANSITION = "com.ossadkowski.crm.mobile.wizyty.ACTIVITY_TRANSITION"
        private const val REQUEST_CODE = 1002
        private const val TAG = "ActivityRecognitionMgr"
    }
}
