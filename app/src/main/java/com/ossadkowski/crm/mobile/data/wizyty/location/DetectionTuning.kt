package com.ossadkowski.crm.mobile.data.wizyty.location

import com.google.android.gms.location.Priority

/**
 * Central, tunable parameters for GPS visit detection. Every value the spec flagged
 * for post-pilot battery/accuracy tuning lives here so it can be adjusted in one place.
 */
object DetectionTuning {
    /** Geofence radius (m): covers GPS drift + a parking lot without overlapping neighbours. */
    const val GEOFENCE_RADIUS_M = 150f

    /** Platform cap is 100; keep a safety buffer and register only the nearest N. */
    const val MAX_GEOFENCES = 80

    /** DWELL / loitering delay (ms): only count a visit after staying ~5 min — filters drive-bys. */
    const val LOITERING_DELAY_MS = 300_000

    /** Geofence notification responsiveness (ms): relaxed → fewer wake-ups, acceptable lag. */
    const val NOTIFICATION_RESPONSIVENESS_MS = 300_000

    /** Active-session location request interval (ms). */
    const val SESSION_LOCATION_INTERVAL_MS = 60_000L

    /** Batch window (ms): deliver location in batches instead of one-by-one. */
    const val SESSION_LOCATION_MAX_DELAY_MS = 120_000L

    /** Balanced power/accuracy (~100 m, Wi-Fi/cell) instead of pure-GPS high accuracy. */
    const val SESSION_LOCATION_PRIORITY = Priority.PRIORITY_BALANCED_POWER_ACCURACY
}
