package com.ossadkowski.crm.mobile.domain.nawozy.model

/**
 * Lifecycle status of a fertiliser order ("Zamówienie nawozy").
 *
 * Codes mirror the backend `status` string verbatim — see the web module on
 * branch `feat/zamowienia-nawozy`. [UNKNOWN] is a defensive fallback so an
 * unexpected server value never crashes the list.
 */
enum class ZamowienieStatus(val code: String, val label: String) {
    KOSZYK("koszyk", "Koszyk"),
    DRAFT("draft", "Szkic"),
    CZEKA_NA_ZATWIERDZENIE("czeka_na_zatwierdzenie", "Czeka na zatwierdzenie"),
    WYSLANY_OCZEKUJE_AX("wyslany_oczekuje_ax", "Wysłany — oczekuje AX"),
    WYSLANY("wyslany", "Wysłany"),
    ANULOWANY("anulowany", "Anulowany"),
    ODRZUCONE_KIEROWNIK("odrzucone_kierownik", "Odrzucone przez kierownika"),
    UNKNOWN("", "—");

    companion object {
        fun fromCode(code: String?): ZamowienieStatus =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: UNKNOWN
    }
}
