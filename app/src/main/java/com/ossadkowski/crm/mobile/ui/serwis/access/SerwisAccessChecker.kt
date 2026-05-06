package com.ossadkowski.crm.mobile.ui.serwis.access

import com.ossadkowski.crm.mobile.data.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gates the Serwis (field-service) module behind the user's role / claims.
 *
 * Access rules (matching the desktop CRM-OC):
 *  - any user with role containing "admin" → always allowed
 *  - users in the Serwis department (`dzial == "Serwis"`) → allowed
 *  - users granted the explicit `nav.serwis` claim (or fallback alias `nav_serwis`) → allowed
 *
 * The hamburger entry on `DashboardActivity` (and any other launcher surface)
 * MUST consult this checker before rendering its tile.
 */
@Singleton
class SerwisAccessChecker @Inject constructor(
    private val session: SessionManager,
) {
    fun hasAccess(): Boolean {
        val role = session.role.lowercase()
        if (role.contains("admin")) return true
        if (session.dzial.equals("Serwis", ignoreCase = true)) return true
        // Both spellings are tolerated — the backend documents `nav.serwis`,
        // older fixtures used `nav_serwis`.
        if (session.hasClaim("nav.serwis")) return true
        if (session.hasClaim("nav_serwis")) return true
        return false
    }
}
