package com.ossadkowski.crm.mobile.util

import org.junit.Assert.assertEquals
import org.junit.Test

class StatusHelperTest {

    // ── Uppercase statuses (transport ceny / generic) ──

    @Test
    fun `PENDING returns amber colors`() {
        val colors = StatusHelper.getColors("PENDING")
        assertEquals("#FEF3C7", colors.bg)
        assertEquals("#92400E", colors.text)
    }

    @Test
    fun `APPROVED returns green colors`() {
        val colors = StatusHelper.getColors("APPROVED")
        assertEquals("#DEF7EC", colors.bg)
        assertEquals("#03543F", colors.text)
    }

    @Test
    fun `REJECTED returns red colors`() {
        val colors = StatusHelper.getColors("REJECTED")
        assertEquals("#FDE8E8", colors.bg)
        assertEquals("#9B1C1C", colors.text)
    }

    @Test
    fun `REVIEW returns blue colors`() {
        val colors = StatusHelper.getColors("REVIEW")
        assertEquals("#E1EFFE", colors.bg)
        assertEquals("#1E429F", colors.text)
    }

    @Test
    fun `COMPLETED returns green colors`() {
        val colors = StatusHelper.getColors("COMPLETED")
        assertEquals("#DEF7EC", colors.bg)
        assertEquals("#03543F", colors.text)
    }

    // ── Case insensitivity for uppercase statuses ──

    @Test
    fun `pending lowercase matches PENDING`() {
        val colors = StatusHelper.getColors("pending")
        assertEquals("#FEF3C7", colors.bg)
    }

    @Test
    fun `Approved mixed case matches APPROVED`() {
        val colors = StatusHelper.getColors("Approved")
        assertEquals("#DEF7EC", colors.bg)
    }

    @Test
    fun `rejected lowercase matches REJECTED`() {
        val colors = StatusHelper.getColors("rejected")
        assertEquals("#FDE8E8", colors.bg)
    }

    // ── Polish statuses (case-sensitive, matched in else branch) ──

    @Test
    fun `W trakcie returns orange`() {
        val colors = StatusHelper.getColors("W trakcie")
        assertEquals("#FEECDC", colors.bg)
        assertEquals("#8A2C0D", colors.text)
    }

    @Test
    fun `Szkic returns blue`() {
        val colors = StatusHelper.getColors("Szkic")
        assertEquals("#E1EFFE", colors.bg)
        assertEquals("#1E429F", colors.text)
    }

    @Test
    fun `Do korekty returns blue`() {
        val colors = StatusHelper.getColors("Do korekty")
        assertEquals("#E1EFFE", colors.bg)
    }

    @Test
    fun `Odrzucony returns red`() {
        val colors = StatusHelper.getColors("Odrzucony")
        assertEquals("#FDE8E8", colors.bg)
        assertEquals("#9B1C1C", colors.text)
    }

    @Test
    fun `Cofniety returns red`() {
        val colors = StatusHelper.getColors("Cofnięty")
        assertEquals("#FDE8E8", colors.bg)
    }

    @Test
    fun `Zaakceptowany przez kierownika returns amber`() {
        val colors = StatusHelper.getColors("Zaakceptowany przez kierownika")
        assertEquals("#FEF3C7", colors.bg)
        assertEquals("#92400E", colors.text)
    }

    @Test
    fun `Zaakceptowany returns green`() {
        val colors = StatusHelper.getColors("Zaakceptowany")
        assertEquals("#DEF7EC", colors.bg)
        assertEquals("#03543F", colors.text)
    }

    @Test
    fun `Zatwierdzony returns green`() {
        val colors = StatusHelper.getColors("Zatwierdzony")
        assertEquals("#DEF7EC", colors.bg)
    }

    @Test
    fun `Do poprawy returns amber`() {
        val colors = StatusHelper.getColors("Do poprawy")
        assertEquals("#FEF3C7", colors.bg)
    }

    @Test
    fun `Do poprawy HR returns amber`() {
        val colors = StatusHelper.getColors("Do poprawy (HR)")
        assertEquals("#FEF3C7", colors.bg)
    }

    @Test
    fun `Wyslany returns blue`() {
        val colors = StatusHelper.getColors("Wysłany")
        assertEquals("#E1EFFE", colors.bg)
    }

    // ── Task statuses ──

    @Test
    fun `Nowe returns blue`() {
        val colors = StatusHelper.getColors("Nowe")
        assertEquals("#E1EFFE", colors.bg)
    }

    @Test
    fun `Do wyjasnienia returns amber`() {
        val colors = StatusHelper.getColors("Do wyjaśnienia")
        assertEquals("#FEF3C7", colors.bg)
    }

    @Test
    fun `Przeterminowane returns red`() {
        val colors = StatusHelper.getColors("Przeterminowane")
        assertEquals("#FDE8E8", colors.bg)
    }

    @Test
    fun `Zakonczone returns green`() {
        val colors = StatusHelper.getColors("Zakończone")
        assertEquals("#DEF7EC", colors.bg)
    }

    @Test
    fun `Anulowane returns gray`() {
        val colors = StatusHelper.getColors("Anulowane")
        assertEquals("#F3F4F6", colors.bg)
        assertEquals("#6B7280", colors.text)
    }

    // ── Default / unknown ──

    @Test
    fun `unknown status returns gray default`() {
        val colors = StatusHelper.getColors("UNKNOWN_STATUS")
        assertEquals("#F3F4F6", colors.bg)
        assertEquals("#6B7280", colors.text)
    }

    @Test
    fun `null status returns gray default`() {
        val colors = StatusHelper.getColors(null)
        assertEquals("#F3F4F6", colors.bg)
        assertEquals("#6B7280", colors.text)
    }

    @Test
    fun `empty string returns gray default`() {
        val colors = StatusHelper.getColors("")
        assertEquals("#F3F4F6", colors.bg)
        assertEquals("#6B7280", colors.text)
    }

    // ── Edge case: Polish statuses are case-sensitive in else branch ──
    // "W trakcie" uppercased becomes "W TRAKCIE" which doesn't match any uppercase branch,
    // then falls to else where original "W trakcie" is checked. So uppercase "W TRAKCIE" -> default.
    @Test
    fun `W TRAKCIE uppercase does NOT match W trakcie`() {
        val colors = StatusHelper.getColors("W TRAKCIE")
        // "W TRAKCIE" uppercase doesn't match any case in the first when,
        // falls to else, but original status was "W TRAKCIE" not "W trakcie" -> default gray
        assertEquals("#F3F4F6", colors.bg)
    }
}
