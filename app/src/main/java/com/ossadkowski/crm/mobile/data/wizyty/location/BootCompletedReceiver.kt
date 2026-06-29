package com.ossadkowski.crm.mobile.data.wizyty.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Geofences don't survive a reboot, so re-register them on boot — but only if a work
 * session was active when the device went down (otherwise this is a no-op for users
 * who never enabled the feature).
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            return
        }
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WizytyEngineEntryPoint::class.java,
        )
        if (!entryPoint.wizytyPrefs().sessionActive) return

        val geofenceManager = entryPoint.geofenceManager()
        val source = entryPoint.contractorLocationSource()

        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                geofenceManager.register(source.all())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to re-register geofences after boot", e)
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "BootReceiver"
    }
}
