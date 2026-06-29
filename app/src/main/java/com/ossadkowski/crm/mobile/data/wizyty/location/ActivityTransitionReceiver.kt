package com.ossadkowski.crm.mobile.data.wizyty.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityTransitionResult
import dagger.hilt.android.EntryPointAccessors

/**
 * Caches the latest detected activity transition so [GeofenceBroadcastReceiver] can
 * gate detection (suppress while driving).
 */
class ActivityTransitionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (!ActivityTransitionResult.hasResult(intent)) return
        val result = ActivityTransitionResult.extractResult(intent) ?: return
        val latest = result.transitionEvents.lastOrNull() ?: return

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WizytyEngineEntryPoint::class.java,
        )
        entryPoint.activityRecognitionManager().onActivityTransition(latest.activityType)
    }
}
