package com.ossadkowski.crm.mobile.data.wizyty.location

import com.google.android.gms.location.Priority

/**
 * Central, tunable parameters for GPS visit detection. Every value the spec flagged
 * for post-pilot battery/accuracy tuning lives here so it can be adjusted in one place.
 */
object DetectionTuning {
    /** Geofence radius (m): set to 1000m (1km) temporarily for user testing. */
    const val GEOFENCE_RADIUS_M = 1000f

    /** Platform cap is 100; keep a safety buffer and register only the nearest N. */
    const val MAX_GEOFENCES = 80

    /** DWELL / loitering delay (ms): set to 30 seconds for responsive testing (5 min in prod). */
    const val LOITERING_DELAY_MS = 30_000

    /**
     * Dedup window (ms): suppress a new auto-detected visit for the same contractor if one
     * was already recorded within this window. GMS can re-fire DWELL and a re-started
     * session re-registers geofences, so without this the same stay produces duplicate rows.
     */
    const val DETECTION_DEDUP_WINDOW_MS = 1_800_000L // 30 min

    /** Geofence notification responsiveness (ms): set to 0 for instant testing. */
    const val NOTIFICATION_RESPONSIVENESS_MS = 0

    /** Active-session location request interval (ms). */
    const val SESSION_LOCATION_INTERVAL_MS = 60_000L

    /** Batch window (ms): deliver location in batches instead of one-by-one. */
    const val SESSION_LOCATION_MAX_DELAY_MS = 120_000L

    /** Balanced power/accuracy (~100 m, Wi-Fi/cell) instead of pure-GPS high accuracy. */
    const val SESSION_LOCATION_PRIORITY = Priority.PRIORITY_BALANCED_POWER_ACCURACY
}
