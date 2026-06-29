package com.ossadkowski.crm.mobile.data.wizyty.location

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Small SharedPreferences store for the Wizyty engine: whether a work session is
 * active (so the boot receiver knows to re-register geofences) and the last detected
 * activity type used to gate detection (suppress while driving).
 */
@Singleton
class WizytyPrefs @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("wizyty_prefs", Context.MODE_PRIVATE)

    var sessionActive: Boolean
        get() = prefs.getBoolean(KEY_SESSION_ACTIVE, false)
        set(value) = prefs.edit().putBoolean(KEY_SESSION_ACTIVE, value).apply()

    /** Last detected activity type (DetectedActivity.* int), or -1 if unknown. */
    var lastActivityType: Int
        get() = prefs.getInt(KEY_LAST_ACTIVITY, -1)
        set(value) = prefs.edit().putInt(KEY_LAST_ACTIVITY, value).apply()

    private companion object {
        const val KEY_SESSION_ACTIVE = "session_active"
        const val KEY_LAST_ACTIVITY = "last_activity_type"
    }
}
