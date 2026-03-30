package com.ossadkowski.app.data.model

import org.junit.Assert.*
import org.junit.Test

class ModelsTest {

    // ── LoginRequest ──

    @Test
    fun `LoginRequest stores username and password`() {
        val req = LoginRequest("admin", "secret")
        assertEquals("admin", req.username)
        assertEquals("secret", req.password)
    }

    @Test
    fun `LoginRequest equality`() {
        val a = LoginRequest("user", "pass")
        val b = LoginRequest("user", "pass")
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `LoginRequest inequality`() {
        val a = LoginRequest("user", "pass1")
        val b = LoginRequest("user", "pass2")
        assertNotEquals(a, b)
    }

    @Test
    fun `LoginRequest copy with new values`() {
        val original = LoginRequest("user", "pass")
        val copied = original.copy(password = "newPass")
        assertEquals("user", copied.username)
        assertEquals("newPass", copied.password)
    }

    // ── LoginResponse ──

    @Test
    fun `LoginResponse nullable fields default to null`() {
        val resp = LoginResponse(
            token = null, userId = null, role = null, username = null,
            success = null, message = null, dzial = null, employeeCacheId = null,
            claims = null, claimsVersion = null
        )
        assertNull(resp.token)
        assertNull(resp.userId)
        assertNull(resp.claims)
    }

    @Test
    fun `LoginResponse with all fields populated`() {
        val resp = LoginResponse(
            token = "jwt123", userId = 1, role = "Admin", username = "admin",
            success = true, message = "OK", dzial = "IT", employeeCacheId = 42,
            claims = arrayOf("read", "write"), claimsVersion = 3
        )
        assertEquals("jwt123", resp.token)
        assertEquals(1, resp.userId)
        assertEquals("Admin", resp.role)
        assertEquals(true, resp.success)
        assertEquals(2, resp.claims!!.size)
        assertEquals(3, resp.claimsVersion)
    }

    // ── PaginatedRequest ──

    @Test
    fun `PaginatedRequest default search is null`() {
        val req = PaginatedRequest(1, 10)
        assertNull(req.search)
    }

    @Test
    fun `PaginatedRequest with search`() {
        val req = PaginatedRequest(2, 20, "test")
        assertEquals(2, req.page)
        assertEquals(20, req.pageSize)
        assertEquals("test", req.search)
    }

    // ── PaginatedResponse ──

    @Test
    fun `PaginatedResponse holds items and counts`() {
        val resp = PaginatedResponse(
            items = listOf("a", "b"), totalCount = 50, totalPages = 5
        )
        assertEquals(2, resp.items.size)
        assertEquals(50, resp.totalCount)
        assertEquals(5, resp.totalPages)
    }

    // ── GenericPageResponse ──

    @Test
    fun `GenericPageResponse holds page info`() {
        val resp = GenericPageResponse(
            data = listOf(1, 2, 3), total = 100, page = 1, pageSize = 20
        )
        assertEquals(3, resp.data.size)
        assertEquals(100, resp.total)
        assertEquals(1, resp.page)
    }

    // ── TransportPriceRequestDto ──

    @Test
    fun `TransportPriceRequestDto all fields`() {
        val dto = TransportPriceRequestDto(
            id = 1, user_id = 10, username = "handlowiec",
            ax_vend_contract_id = "V001", ax_cust_contract_id = "C001",
            kontrahent_nazwa = "Firma", towar = "Pszenica",
            ilosc_ton = 25.5, adres_zaladunku = "Warszawa",
            odbiorca = "Odbiorca", adres_odbioru = "Krakow",
            szacowany_koszt_transportu = 1500.0, zatwierdzony_koszt = 1400.0,
            sklad = "Glowny", status = "PENDING",
            reviewed_by = 5, reviewed_by_username = "logistyk",
            reviewed_at = "2026-01-01", komentarz_logistyka = "OK",
            komentarz_handlowiec = "Pilne", created_at = "2026-01-01"
        )
        assertEquals(1, dto.id)
        assertEquals(25.5, dto.ilosc_ton)
        assertEquals("PENDING", dto.status)
        assertEquals("Glowny", dto.sklad)
    }

    @Test
    fun `TransportPriceRequestDto nullable fields`() {
        val dto = TransportPriceRequestDto(
            id = 1, user_id = null, username = null,
            ax_vend_contract_id = null, ax_cust_contract_id = null,
            kontrahent_nazwa = null, towar = null,
            ilosc_ton = null, adres_zaladunku = null,
            odbiorca = null, adres_odbioru = null,
            szacowany_koszt_transportu = null, zatwierdzony_koszt = null,
            sklad = null, status = null,
            reviewed_by = null, reviewed_by_username = null,
            reviewed_at = null, komentarz_logistyka = null,
            komentarz_handlowiec = null, created_at = null
        )
        assertNull(dto.username)
        assertNull(dto.ilosc_ton)
    }

    // ── CreateTransportPriceRequest ──

    @Test
    fun `CreateTransportPriceRequest required vs optional`() {
        val req = CreateTransportPriceRequest(
            kontrahentNazwa = "Firma",
            szacowanyKosztTransportu = 1000.0
        )
        assertEquals("Firma", req.kontrahentNazwa)
        assertEquals(1000.0, req.szacowanyKosztTransportu, 0.001)
        assertNull(req.axVendContractId)
        assertNull(req.towar)
        assertNull(req.iloscTon)
        assertEquals("Główny", req.sklad)  // default value
    }

    @Test
    fun `CreateTransportPriceRequest custom sklad`() {
        val req = CreateTransportPriceRequest(
            kontrahentNazwa = "F", szacowanyKosztTransportu = 100.0,
            sklad = "Zamiejscowy"
        )
        assertEquals("Zamiejscowy", req.sklad)
    }

    // ── ReviewTransportPriceRequest ──

    @Test
    fun `ReviewTransportPriceRequest approve with cost`() {
        val req = ReviewTransportPriceRequest(
            approved = true, zatwierdzonyKoszt = 950.0, komentarz = "OK"
        )
        assertTrue(req.approved)
        assertEquals(950.0, req.zatwierdzonyKoszt!!, 0.001)
    }

    @Test
    fun `ReviewTransportPriceRequest reject without cost`() {
        val req = ReviewTransportPriceRequest(approved = false)
        assertFalse(req.approved)
        assertNull(req.zatwierdzonyKoszt)
        assertNull(req.komentarz)
    }

    // ── CreateLimitKredytowyRequest ──

    @Test
    fun `CreateLimitKredytowyRequest defaults`() {
        val req = CreateLimitKredytowyRequest(
            userId = 1, kontrahentAccountNum = "ACC001", wnioskowanyLimit = 50000.0
        )
        assertNull(req.terminZabezpieczen)
        assertNull(req.uwagi)
        assertFalse(req.potwierdzonePrzeterminowane)
        assertFalse(req.rozliczeniePlonami)
    }

    // ── TaskItem ──

    @Test
    fun `TaskItem equality`() {
        val a = TaskItem(1, "T1", "D1", "Nowe", "user", "2026-01-01", "2026-02-01")
        val b = TaskItem(1, "T1", "D1", "Nowe", "user", "2026-01-01", "2026-02-01")
        assertEquals(a, b)
    }

    // ── WniosekItem ──

    @Test
    fun `WniosekItem fields`() {
        val w = WniosekItem(
            id = 1, username = "user", typ = "Urlop", odDo = "01-05",
            godziny = "8", powod = "Wypoczynek", iloscDni = "5", status = "W trakcie"
        )
        assertEquals("Urlop", w.typ)
        assertEquals("W trakcie", w.status)
    }

    // ── LogoutRequest ──

    @Test
    fun `LogoutRequest default placeholder is null`() {
        val req = LogoutRequest()
        assertNull(req.placeholder)
    }

    // ── LimitKredytowyListItem ──

    @Test
    fun `LimitKredytowyListItem fields`() {
        val item = LimitKredytowyListItem(
            id = 1, user_id = 10, kontrahent_account_num = "ACC",
            kontrahent_nazwa = "Firma", obecny_limit = 10000.0,
            wnioskowany_limit = 20000.0, status = "PENDING",
            ax_sync = false, created_at = "2026-01-01", created_by = "user"
        )
        assertEquals(10000.0, item.obecny_limit!!, 0.001)
        assertEquals(false, item.ax_sync)
    }
}
