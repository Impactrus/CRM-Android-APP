package com.ossadkowski.crm.mobile.ui.nawozy.access

import com.ossadkowski.crm.mobile.data.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gates the fertiliser-order module ("Zamówienia nawozy").
 *
 * Access rules (mirroring the desktop CRM-OC sales section):
 *  - any user whose role contains "admin", or who holds the `action.admin` claim → allowed
 *  - users granted the explicit `nav.sprzedaz.zamowienia_nawozy` claim → allowed
 *  - INTERIM: until that dedicated claim is rolled out to users, anyone holding the
 *    general sales-orders nav (`nav.sprzedaz.zamowienia`) → allowed. Verified on-device:
 *    real tokens (claims v51) carry `nav.sprzedaz.zamowienia` but not yet the `_nawozy`
 *    one, so without this fallback the feature would be unreachable for everyone.
 *
 * Both the drawer entry in `BaseDrawerActivity` and the dashboard launch surface
 * MUST consult this checker before exposing the feature.
 */
@Singleton
class NawozyAccessChecker @Inject constructor(
    private val session: SessionManager,
) {
    fun hasAccess(): Boolean {
        if (session.role.lowercase().contains("admin")) return true
        if (session.hasClaim(CLAIM)) return true
        if (session.hasClaim("action.admin")) return true
        if (session.hasClaim(FALLBACK_CLAIM)) return true
        return false
    }

    companion object {
        const val CLAIM = "nav.sprzedaz.zamowienia_nawozy"
        const val FALLBACK_CLAIM = "nav.sprzedaz.zamowienia"
    }
}
