package com.ossadkowski.crm.mobile.domain.nawozy.model

/** A single line in the fertiliser cart. */
data class KoszykPozycja(
    val lineId: Long,
    val itemId: String,
    val nazwa: String,
    val qty: Double,
    val magazynId: String?,
    val cenaBazowa: Double?,
    val cenaSprzedazy: Double?,
    val rabatProcent: Double?,
    /** Transport cost in PLN/t — read-only, pinned from the logistics calculator. */
    val transportPlnT: Double?,
    val wartoscNetto: Double?,
)

/**
 * The full fertiliser cart (`GET /zamowienia/koszyk/{id}`): header + lines + totals.
 *
 * The cart endpoints are shared with the web "Koszyk" — only the listing and
 * logistics endpoints are fertiliser-specific.
 */
data class Koszyk(
    val id: Long,
    val kontrahentId: String,
    val kontrahentNazwa: String?,
    val status: ZamowienieStatus,
    val qtyTons: Double,
    val dlvMode: String?,
    val dlvTerm: String?,
    val paymentTerm: String?,
    val dataDostawy: String?,
    val adresDostawy: String?,
    val addressBookId: Long?,
    val customerRef: String?,
    val notes: String?,
    val pozycje: List<KoszykPozycja>,
    val wartoscNetto: Double,
)
