package com.ossadkowski.crm.mobile.ui.nawozy.access

import com.ossadkowski.crm.mobile.data.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gates the fertiliser-order module ("Zamówienia nawozy").
 *
 * Access rules (mirroring the desktop CRM-OC sales section):
 *  - any user whose role contains "admin" → always allowed
 *  - users granted the explicit `nav.sprzedaz.zamowienia_nawozy` claim → allowed
 *
 * NOTE: the feature is hidden until the backend assigns `nav.sprzedaz.zamowienia_nawozy`
 * to the user (real tokens did not carry it yet at build time).
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
        return session.hasClaim(CLAIM)
    }

    companion object {
        const val CLAIM = "nav.sprzedaz.zamowienia_nawozy"
    }
}
