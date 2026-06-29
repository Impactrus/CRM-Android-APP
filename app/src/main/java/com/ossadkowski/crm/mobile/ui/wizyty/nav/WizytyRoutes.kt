package com.ossadkowski.crm.mobile.ui.wizyty.nav

/**
 * Compose-Navigation route templates for the "Wizyty" (GPS visit-detection) module.
 *
 * Flow: [DASHBOARD] (work-session toggle + status) → [LIST] (visits list: auto-detected
 * rows awaiting manual confirm + manual rows) and [ADD] (manual visit + address search).
 * [PERMISSIONS] hosts the staggered location-permission gate, opened from the dashboard
 * when the required permissions are missing.
 */
object WizytyRoutes {
    const val DASHBOARD = "wizyty/dashboard"
    const val LIST = "wizyty/list"
    const val ADD = "wizyty/add"
    const val TEST_LOCATION = "wizyty/test-location"
    const val TEST_LOCATIONS = "wizyty/test-locations"
    const val PERMISSIONS = "wizyty/permissions"
}
