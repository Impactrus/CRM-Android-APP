package com.ossadkowski.crm.mobile.domain.nawozy.repository

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.model.AdresDostawy
import com.ossadkowski.crm.mobile.domain.nawozy.model.Koszyk
import com.ossadkowski.crm.mobile.domain.nawozy.model.Kontrahent
import com.ossadkowski.crm.mobile.domain.nawozy.model.LimitStatus
import com.ossadkowski.crm.mobile.domain.nawozy.model.MagazynStan
import com.ossadkowski.crm.mobile.domain.nawozy.model.OstatniaCena
import com.ossadkowski.crm.mobile.domain.nawozy.model.PricingResult
import com.ossadkowski.crm.mobile.domain.nawozy.model.SlownikPozycja
import com.ossadkowski.crm.mobile.domain.nawozy.model.TowarNawoz
import com.ossadkowski.crm.mobile.domain.nawozy.model.WariantLogistyczny
import com.ossadkowski.crm.mobile.domain.nawozy.model.ZamowienieNawozy
import com.ossadkowski.crm.mobile.domain.nawozy.model.ZamowienieStatus

// ── Query / command params ──────────────────────────────────────────────────

data class ZamowieniaFilters(
    val search: String? = null,
    val status: ZamowienieStatus? = null,
    val page: Int = 1,
    val pageSize: Int = 30,
)

data class PagedZamowienia(
    val items: List<ZamowienieNawozy>,
    val total: Int,
)

data class NowaPozycja(
    val itemId: String,
    val qty: Double,
    val magazynId: String?,
    val cenaOverride: Double? = null,
)

data class EdycjaPozycji(
    val qty: Double? = null,
    val cenaOverride: Double? = null,
    val linePercent: Double? = null,
)

data class KoszykHeader(
    val magazynId: String? = null,
    val dlvMode: String? = null,
    val dlvTerm: String? = null,
    val paymentTerm: String? = null,
    val dataDostawy: String? = null,
    val customerRef: String? = null,
    val notes: String? = null,
)

data class WariantyZapytanie(
    val itemId: String,
    val qtyTons: Double,
    val deliveryAddress: String? = null,
    val deliveryLat: Double? = null,
    val deliveryLng: Double? = null,
    val addressBookId: Long? = null,
)

data class PricingZapytanie(
    val itemId: String,
    val cennik: String,
    val paymTermId: String,
    val rabatKwotowy: Double,
)

data class PricingReverseZapytanie(
    val itemId: String,
    val cennik: String,
    val paymTermId: String,
    val cenaSprzedazy: Double,
)

/** Dictionary categories accepted by `GET /zamowienia/slowniki/{kategoria}`. */
object SlownikKategoria {
    const val DLV_MODE = "dlvmode"
    const val DLV_TERM = "dlvterm"
    const val PAYM_TERM = "paymterm"
    const val INVENT_LOCATION = "inventlocation"
}

/** Price lists accepted by the pricing / product-price endpoints. */
object Cennik {
    const val BAZOWY = "BAZOWY"
    const val BB = "BB"
}

/**
 * Single gateway for the fertiliser-order feature. Fertiliser-specific endpoints
 * (`/zamowienia-nawozy/…`) and the shared cart / dictionary / pricing endpoints
 * (`/zamowienia/koszyk/…`, `/zamowienia/towary`, ...) are funnelled through here so
 * the UI never reaches into Retrofit directly. All calls return [Result].
 */
interface NawozyRepository {

    // ── Fertiliser-specific ──
    suspend fun startKoszyk(kontrahentId: String): Result<Long>
    suspend fun listZamowienia(filters: ZamowieniaFilters = ZamowieniaFilters()): Result<PagedZamowienia>
    suspend fun getWarianty(req: WariantyZapytanie): Result<List<WariantLogistyczny>>

    // ── Shared cart operations ──
    suspend fun getKoszyk(koszykId: Long): Result<Koszyk>
    suspend fun addPozycja(koszykId: Long, pozycja: NowaPozycja): Result<Koszyk>
    suspend fun updatePozycja(koszykId: Long, lineId: Long, zmiana: EdycjaPozycji): Result<Koszyk>
    suspend fun deletePozycja(koszykId: Long, lineId: Long): Result<Koszyk>
    suspend fun updateHeader(koszykId: Long, header: KoszykHeader): Result<Koszyk>
    suspend fun submitKoszyk(koszykId: Long, warningsAcknowledged: Boolean): Result<Koszyk>
    suspend fun abandonKoszyk(koszykId: Long): Result<Unit>

    // ── Customers & limit ──
    suspend fun searchKontrahenci(search: String?, myOnly: Boolean): Result<List<Kontrahent>>
    suspend fun getLimitStatus(accountNum: String): Result<LimitStatus>

    // ── Products & stock ──
    suspend fun searchTowaryNawozy(search: String?): Result<List<TowarNawoz>>
    suspend fun getTowarMagazyny(itemId: String): Result<List<MagazynStan>>
    suspend fun getTowarCena(itemId: String, cennik: String = Cennik.BAZOWY): Result<Double?>
    suspend fun getOstatnieCeny(accountNum: String, itemIds: List<String>): Result<List<OstatniaCena>>

    // ── Dictionaries & addresses ──
    suspend fun getSlownik(kategoria: String): Result<List<SlownikPozycja>>
    suspend fun getAddressBook(search: String?): Result<List<AdresDostawy>>

    // ── Pricing ──
    suspend fun calcPricing(req: PricingZapytanie): Result<PricingResult>
    suspend fun calcPricingReverse(req: PricingReverseZapytanie): Result<PricingResult>
}
