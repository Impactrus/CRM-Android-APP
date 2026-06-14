package com.ossadkowski.crm.mobile.data.nawozy.dto

import com.google.gson.annotations.SerializedName

/**
 * Wire models for the fertiliser-order feature. All fields are camelCase per the
 * backend contract (branch `feat/zamowienia-nawozy`). Where the exact JSON shape
 * was not pinned down in the spec, `alternate` names are supplied so a minor
 * server naming difference does not break deserialization — these are flagged for
 * confirmation during E2E against the real API.
 */

// ── /zamowienia-nawozy/start ──
data class StartKoszykRequest(
    val kontrahentId: String,
)

data class StartKoszykResponse(
    @SerializedName("koszykId") val koszykId: Long,
)

// ── GET /zamowienia-nawozy (list) ──
data class ZamowieniaPageResponse(
    @SerializedName(value = "items", alternate = ["data"]) val items: List<ZamowienieListItemDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val pageSize: Int = 30,
)

data class ZamowienieListItemDto(
    val id: Long,
    val nrZamowienia: String?,
    val nrZamowieniaAx: String?,
    val kontrahentNazwa: String?,
    val kontrahentId: String?,
    val iloscTowarow: Int?,
    val wartoscNetto: Double?,
    val dataUtw: String?,
    val status: String?,
)

// ── POST /zamowienia-nawozy/logistyka/warianty ──
data class WariantyRequest(
    val itemId: String,
    val qtyTons: Double,
    val deliveryAddress: String? = null,
    val deliveryLat: Double? = null,
    val deliveryLng: Double? = null,
    val addressBookId: Long? = null,
)

data class WariantyResponse(
    val warianty: List<WariantDto> = emptyList(),
)

data class WariantDto(
    val loadLocationId: String?,
    val loadLocationNazwa: String?,
    val deliveryLabel: String?,
    val km: Double?,
    val stawkaPlnT: Double?,
    val kosztTotal: Double?,
    val combiningType: String?,
    val maxRabat: Double?,
)

// ── /zamowienia/koszyk/{id} ──
// Real shape (verified against live API): header fields are nested under `header`,
// totals are `sumNetto`, the customer name is `kontrahentName`, and `kontrahentAccountNum`
// is the value used for the limit-status lookup.
data class KoszykDto(
    val id: Long,
    val kontrahentId: String?,
    val kontrahentAccountNum: String?,
    @SerializedName(value = "kontrahentName", alternate = ["kontrahentNazwa"]) val kontrahentName: String?,
    val status: String?,
    @SerializedName(value = "sumNetto", alternate = ["sumaNetto", "wartoscNetto", "totalNetto"]) val sumNetto: Double?,
    val header: KoszykHeaderDto? = null,
    @SerializedName(value = "pozycje", alternate = ["lines", "items"]) val pozycje: List<KoszykPozycjaDto> = emptyList(),
)

data class KoszykHeaderDto(
    val magazynId: String? = null,
    val dlvMode: String? = null,
    val dlvTerm: String? = null,
    val paymentTerm: String? = null,
    val dataDostawy: String? = null,
    val customerRef: String? = null,
    val notes: String? = null,
)

data class KoszykPozycjaDto(
    @SerializedName(value = "lineId", alternate = ["id"]) val lineId: Long,
    val itemId: String?,
    @SerializedName(value = "name", alternate = ["nazwa", "itemName"]) val nazwa: String?,
    val qty: Double?,
    val magazynId: String?,
    @SerializedName(value = "cena", alternate = ["cenaBazowa"]) val cenaBazowa: Double?,
    @SerializedName(value = "cenaOverride", alternate = ["cenaSprzedazy"]) val cenaSprzedazy: Double?,
    @SerializedName(value = "linePercent", alternate = ["rabatProcent", "rabatProcentowy"]) val rabatProcent: Double?,
    @SerializedName(value = "transportPlnT", alternate = ["stawkaPlnT"]) val transportPlnT: Double?,
    @SerializedName(value = "lineNetto", alternate = ["wartoscNetto"]) val wartoscNetto: Double?,
)

// ── Cart commands ──
data class AddPozycjaRequest(
    val itemId: String,
    val qty: Double,
    val magazynId: String?,
    val cenaOverride: Double? = null,
)

data class UpdatePozycjaRequest(
    val qty: Double? = null,
    val cenaOverride: Double? = null,
    val linePercent: Double? = null,
)

data class UpdateHeaderRequest(
    val magazynId: String? = null,
    val dlvMode: String? = null,
    val dlvTerm: String? = null,
    val paymentTerm: String? = null,
    val dataDostawy: String? = null,
    val customerRef: String? = null,
    val notes: String? = null,
)

data class SubmitKoszykRequest(
    val warningsAcknowledged: Boolean = false,
)

// ── /zamowienia/koszyk/kontrahenci ──
data class KontrahentNawozyDto(
    @SerializedName(value = "accountNum", alternate = ["id"]) val accountNum: String?,
    @SerializedName(value = "nazwa", alternate = ["name"]) val nazwa: String?,
    @SerializedName(value = "adres", alternate = ["address"]) val adres: String?,
    val nip: String?,
    @SerializedName(value = "isMyKontrahent", alternate = ["isMyClient", "myClient", "mojKlient"]) val isMyClient: Boolean? = null,
)

// ── /kontrahenci/{accountNum}/limit-status ──
data class LimitStatusDto(
    val limitMax: Double?,
    val dostepne: Double?,
    val isFrozen: Boolean? = null,
    val isBlocked: Boolean? = null,
    val frozenReason: String? = null,
)

// ── /zamowienia/towary/{itemId}/magazyny ──
// Live API returns the warehouse code in `magazyn`; the addPozycja body expects it as magazynId.
data class MagazynStanDto(
    @SerializedName(value = "magazyn", alternate = ["magazynId", "kod", "inventLocationId"]) val magazynId: String?,
    @SerializedName(value = "magazynNazwa", alternate = ["nazwa", "name"]) val magazynNazwa: String?,
    val dostepne: Double?,
    val dataWaznosci: String?,
    val numerPartii: String?,
    val przeterminowany: Boolean? = null,
)

// ── /zamowienia/towary/{itemId}/cena ──
data class CenaDto(
    @SerializedName(value = "cena", alternate = ["cenaBazowa", "price"]) val cena: Double?,
)

// ── /zamowienia/kontrahenci/{accountNum}/ostatnie-ceny ──
data class OstatniaCenaDto(
    val itemId: String?,
    @SerializedName(value = "cena", alternate = ["cenaNetto", "price"]) val cena: Double?,
    @SerializedName(value = "data", alternate = ["dataSprzedazy"]) val data: String?,
)

// ── /zamowienia/slowniki/{kategoria} ──
// Live API wraps the entries in an object: { "kategoria": "...", "items": [...] }.
data class SlownikResponse(
    val kategoria: String? = null,
    val items: List<SlownikNawozyDto> = emptyList(),
)

data class SlownikNawozyDto(
    @SerializedName(value = "kod", alternate = ["id", "value"]) val kod: String?,
    @SerializedName(value = "nazwa", alternate = ["name", "label"]) val nazwa: String?,
)

// ── pricing-calc / pricing-calc-reverse ──
data class PricingCalcRequest(
    val itemId: String,
    val cennik: String,
    val paymTermId: String,
    val rabatKwotowy: Double,
)

data class PricingCalcReverseRequest(
    val itemId: String,
    val cennik: String,
    val paymTermId: String,
    val cenaSprzedazy: Double,
)

data class PricingResultDto(
    val cenaBazowa: Double?,
    val kredytKupiecki: Double?,
    val cenaSprzedazy: Double?,
    val rabatProcentowy: Double?,
    @SerializedName(value = "maxRabatPrzekroczony", alternate = ["maxRabatExceeded"]) val maxRabatPrzekroczony: Boolean? = null,
)

// ── /address-book ──
// Live API wraps the entries in an object: { "data": [...] }.
data class AddressBookResponse(
    @SerializedName(value = "data", alternate = ["items"]) val data: List<AddressBookDto> = emptyList(),
)

data class AddressBookDto(
    val id: Long,
    @SerializedName(value = "nazwa", alternate = ["label", "name"]) val label: String?,
    @SerializedName(value = "adres", alternate = ["address"]) val adres: String?,
    @SerializedName(value = "lat", alternate = ["geoLat", "deliveryLat"]) val lat: Double? = null,
    @SerializedName(value = "lng", alternate = ["geoLon", "geoLng", "deliveryLng"]) val lng: Double? = null,
)
