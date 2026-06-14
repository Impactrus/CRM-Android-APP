package com.ossadkowski.crm.mobile.nawozy

import com.ossadkowski.crm.mobile.data.model.TowarListItem
import com.ossadkowski.crm.mobile.data.nawozy.dto.AddressBookDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.KontrahentNawozyDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.KoszykDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.KoszykHeaderDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.KoszykPozycjaDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.LimitStatusDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.PricingResultDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.SlownikNawozyDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.WariantDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.ZamowienieListItemDto
import com.ossadkowski.crm.mobile.data.nawozy.mapper.toDomain
import com.ossadkowski.crm.mobile.data.nawozy.mapper.toDomainOrNull
import com.ossadkowski.crm.mobile.data.nawozy.mapper.toNawozDomain
import com.ossadkowski.crm.mobile.domain.nawozy.model.StanMagazynowy
import com.ossadkowski.crm.mobile.domain.nawozy.model.ZamowienieStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NawozyMappersTest {

    @Test
    fun `ZamowienieListItem maps fields and resolves status code`() {
        val dto = ZamowienieListItemDto(
            id = 7,
            nrZamowienia = "ZN-0007",
            nrZamowieniaAx = null,
            kontrahentNazwa = "Rolnik sp. z o.o.",
            kontrahentId = "ACC-1",
            iloscTowarow = 3,
            wartoscNetto = 1234.5,
            dataUtw = "2026-06-14",
            status = "czeka_na_zatwierdzenie",
        )

        val domain = dto.toDomain()

        assertEquals(7L, domain.id)
        assertEquals("ZN-0007", domain.nrZamowienia)
        assertEquals(ZamowienieStatus.CZEKA_NA_ZATWIERDZENIE, domain.status)
        assertEquals(3, domain.iloscTowarow)
    }

    @Test
    fun `ZamowienieListItem nulls fall back to safe defaults and UNKNOWN status`() {
        val dto = ZamowienieListItemDto(
            id = 1, nrZamowienia = null, nrZamowieniaAx = null, kontrahentNazwa = null,
            kontrahentId = null, iloscTowarow = null, wartoscNetto = null, dataUtw = null,
            status = "coś-nowego",
        )

        val domain = dto.toDomain()

        assertEquals("", domain.nrZamowienia)
        assertEquals("", domain.kontrahentNazwa)
        assertEquals(0, domain.iloscTowarow)
        assertEquals(0.0, domain.wartoscNetto, 0.0)
        assertEquals(ZamowienieStatus.UNKNOWN, domain.status)
    }

    @Test
    fun `Koszyk maps nested header, accountNum, name and sumNetto`() {
        val dto = KoszykDto(
            id = 42,
            kontrahentId = "999",                 // numeric id — not used for limit-status
            kontrahentAccountNum = "ACC-9",       // accountNum — used for limit-status
            kontrahentName = "Klient",
            status = "koszyk",
            sumNetto = 2280.0,
            header = KoszykHeaderDto(paymentTerm = "PT", dlvMode = "DM", dlvTerm = "DT"),
            pozycje = listOf(
                KoszykPozycjaDto(
                    lineId = 100, itemId = "N-1", nazwa = "Saletra", qty = 24.0, magazynId = "W1",
                    cenaBazowa = 100.0, cenaSprzedazy = null, rabatProcent = 5.0,
                    transportPlnT = null, wartoscNetto = 2280.0,
                ),
            ),
        )

        val domain = dto.toDomain()

        assertEquals(42L, domain.id)
        assertEquals("ACC-9", domain.kontrahentId)        // accountNum wins
        assertEquals("Klient", domain.kontrahentNazwa)
        assertEquals("PT", domain.paymentTerm)
        assertEquals(24.0, domain.qtyTons, 0.0)           // sum of line qty
        assertEquals(1, domain.pozycje.size)
        assertEquals(100L, domain.pozycje[0].lineId)
        assertEquals(100.0, domain.pozycje[0].cenaSprzedazy!!, 0.0) // no override → base price
        assertEquals(2280.0, domain.wartoscNetto, 0.0)
    }

    @Test
    fun `Kontrahent isMyClient defaults to false when null`() {
        val dto = KontrahentNawozyDto(accountNum = "ACC-1", nazwa = "X", adres = null, nip = null, isMyClient = null)
        assertFalse(dto.toDomain().isMyClient)
    }

    @Test
    fun `LimitStatus frozen or blocked is restricted`() {
        val frozen = LimitStatusDto(limitMax = 1000.0, dostepne = 0.0, isFrozen = true, isBlocked = false, frozenReason = "X").toDomain()
        val blocked = LimitStatusDto(limitMax = null, dostepne = null, isFrozen = null, isBlocked = true, frozenReason = null).toDomain()
        val ok = LimitStatusDto(limitMax = 1000.0, dostepne = 500.0, isFrozen = null, isBlocked = null, frozenReason = null).toDomain()

        assertTrue(frozen.isRestricted)
        assertTrue(blocked.isRestricted)
        assertFalse(ok.isRestricted)
        assertFalse(ok.isFrozen)
    }

    @Test
    fun `Towar stock badge derives from available quantity`() {
        fun stan(dostepne: Double?) = TowarListItem(
            kod = "N-1", nazwa = "Nawóz", branza = "N", producent = null,
            cena = 100.0, jm = "T", dostepne = dostepne, magazyn = null, grupaNazwa = null,
        ).toNawozDomain().stan

        assertEquals(StanMagazynowy.BRAK, stan(0.0))
        assertEquals(StanMagazynowy.BRAK, stan(null))
        assertEquals(StanMagazynowy.MALO, stan(10.0))
        assertEquals(StanMagazynowy.DOSTEPNY, stan(30.0))
    }

    @Test
    fun `Slownik entry with null code maps to null`() {
        assertNull(SlownikNawozyDto(kod = null, nazwa = "X").toDomainOrNull())
        val ok = SlownikNawozyDto(kod = "PT14", nazwa = null).toDomainOrNull()
        assertEquals("PT14", ok!!.kod)
        assertEquals("PT14", ok.nazwa) // falls back to code when name missing
    }

    @Test
    fun `Pricing nulls default to zero and false`() {
        val r = PricingResultDto(null, null, null, null, null).toDomain()
        assertEquals(0.0, r.cenaSprzedazy, 0.0)
        assertFalse(r.maxRabatPrzekroczony)
    }

    @Test
    fun `Wariant null location ids become empty strings`() {
        val w = WariantDto(
            loadLocationId = null, loadLocationNazwa = null, deliveryLabel = null,
            km = 120.0, stawkaPlnT = 15.0, kosztTotal = 360.0, combiningType = null, maxRabat = 5.0,
        ).toDomain()
        assertEquals("", w.loadLocationId)
        assertEquals(120.0, w.km!!, 0.0)
    }

    @Test
    fun `AddressBook maps id and label`() {
        val a = AddressBookDto(id = 3, label = "Magazyn Wschód", adres = "ul. Polna 1", lat = 52.1, lng = 21.0).toDomain()
        assertEquals(3L, a.id)
        assertEquals("Magazyn Wschód", a.label)
    }
}
