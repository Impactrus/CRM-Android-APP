package com.ossadkowski.crm.mobile.ui.nawozy.nav

import java.net.URLEncoder

/**
 * Compose-Navigation route templates for the fertiliser-order module.
 *
 * Flow: [LISTA] → [KONTRAHENT] (customer picker) → [KOSZYK] (cart). From the cart
 * the salesperson opens [TOWAR_PICKER] to add products; the logistics calculator
 * lives inside the product picker's bottom sheet. `koszykId` is a Long.
 */
object NawozyRoutes {
    const val LISTA = "nawozy/lista"
    const val KONTRAHENT = "nawozy/kontrahent"
    const val KOSZYK = "nawozy/koszyk/{koszykId}"
    const val TOWAR_PICKER = "nawozy/koszyk/{koszykId}/towar"

    const val ARG_KOSZYK_ID = "koszykId"

    fun koszyk(koszykId: Long) = "nawozy/koszyk/$koszykId"
    fun towarPicker(koszykId: Long) = "nawozy/koszyk/$koszykId/towar"
}
