package com.ossadkowski.crm.mobile.domain.nawozy.model

/**
 * A dictionary entry (`GET /zamowienia/slowniki/{kategoria}`) — delivery mode,
 * delivery term, payment term or warehouse. `kod` is the stable identifier sent
 * back on the cart header; `nazwa` is the human label.
 */
data class SlownikPozycja(
    val kod: String,
    val nazwa: String,
)

/**
 * A delivery address (`GET /address-book`). [id] is passed back to the logistics
 * calculator as `addressBookId`.
 */
data class AdresDostawy(
    val id: Long,
    val label: String,
    val adres: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
)
