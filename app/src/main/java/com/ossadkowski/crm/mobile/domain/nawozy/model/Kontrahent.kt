package com.ossadkowski.crm.mobile.domain.nawozy.model

/**
 * A customer eligible to receive a fertiliser order, as returned by the
 * cart customer picker (`GET /zamowienia/koszyk/kontrahenci`).
 */
data class Kontrahent(
    val accountNum: String,
    val nazwa: String,
    val adres: String? = null,
    val nip: String? = null,
    val isMyClient: Boolean = false,
)

/**
 * Credit-limit health for a customer (`GET /kontrahenci/{accountNum}/limit-status`).
 *
 * When [isRestricted] is true the salesperson must explicitly acknowledge the
 * risk (checkbox) before the cart can be submitted.
 */
data class LimitStatus(
    val limitMax: Double?,
    val dostepne: Double?,
    val isFrozen: Boolean,
    val isBlocked: Boolean,
    val frozenReason: String? = null,
) {
    val isRestricted: Boolean get() = isFrozen || isBlocked
}
