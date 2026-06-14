package com.ossadkowski.crm.mobile.domain.nawozy.model

/**
 * Result of the server-side price calculation
 * (`POST /zamowienia/koszyk/pricing-calc` and `.../pricing-calc-reverse`).
 *
 * The backend is the single source of truth for the merchant-credit surcharge
 * ([kredytKupiecki]) and the discount ⇄ price relationship — nothing here is
 * recomputed on the client.
 */
data class PricingResult(
    val cenaBazowa: Double,
    val kredytKupiecki: Double,
    val cenaSprzedazy: Double,
    val rabatProcentowy: Double,
    val maxRabatPrzekroczony: Boolean,
)

/**
 * Last price a customer actually paid for a given article
 * (`GET /zamowienia/kontrahenci/{accountNum}/ostatnie-ceny`).
 */
data class OstatniaCena(
    val itemId: String,
    val cena: Double?,
    val data: String?,
)
