package com.ossadkowski.crm.mobile.domain.nawozy.model

/**
 * A single row on the fertiliser-orders list (`GET /zamowienia-nawozy`).
 */
data class ZamowienieNawozy(
    val id: Long,
    val nrZamowienia: String,
    val nrZamowieniaAx: String?,
    val kontrahentNazwa: String,
    val kontrahentId: String,
    val iloscTowarow: Int,
    val wartoscNetto: Double,
    val dataUtw: String,
    val status: ZamowienieStatus,
)
