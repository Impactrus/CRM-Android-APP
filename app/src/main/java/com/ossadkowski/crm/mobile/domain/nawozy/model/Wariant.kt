package com.ossadkowski.crm.mobile.domain.nawozy.model

/**
 * One logistics option from the calculator
 * (`POST /zamowienia-nawozy/logistyka/warianty`).
 *
 * [stawkaPlnT] (PLN/t) and [kosztTotal] may be null when geocoding of the
 * warehouse or delivery target failed. [stawkaPlnT] is **read-only** for the
 * salesperson — it originates from the Transport cost engine, never edited here.
 * Variants are returned ascending by total cost (cheapest first).
 */
data class WariantLogistyczny(
    val loadLocationId: String,
    val loadLocationNazwa: String,
    val deliveryLabel: String,
    val km: Double?,
    val stawkaPlnT: Double?,
    val kosztTotal: Double?,
    val combiningType: String?,
    val maxRabat: Double?,
)
