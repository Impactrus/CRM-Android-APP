package com.ossadkowski.crm.mobile.domain.nawozy.model

/** Coarse stock signal shown as a badge on the product picker. */
enum class StanMagazynowy { DOSTEPNY, MALO, BRAK }

/**
 * A fertiliser article (branża `N` — Nawozy, or `D` — Nawozy dolistne).
 *
 * [dostepne] is the total available quantity across warehouses; [stan] derives
 * the badge from it (full truck = 24 T is the reference threshold for "mało").
 */
data class TowarNawoz(
    val itemId: String,
    val nazwa: String,
    val branza: String?,
    val producent: String?,
    val grupa: String?,
    val jm: String?,
    val cenaBazowa: Double?,
    val dostepne: Double?,
) {
    val stan: StanMagazynowy
        get() = when {
            (dostepne ?: 0.0) <= 0.0 -> StanMagazynowy.BRAK
            (dostepne ?: 0.0) < FULL_TRUCK_TONS -> StanMagazynowy.MALO
            else -> StanMagazynowy.DOSTEPNY
        }

    companion object {
        const val FULL_TRUCK_TONS = 24.0
    }
}

/**
 * Per-warehouse stock for a product
 * (`GET /zamowienia/towary/{itemId}/magazyny`).
 */
data class MagazynStan(
    val magazynId: String,
    val magazynNazwa: String?,
    val dostepne: Double?,
    val dataWaznosci: String?,
    val numerPartii: String?,
    val przeterminowany: Boolean,
)
