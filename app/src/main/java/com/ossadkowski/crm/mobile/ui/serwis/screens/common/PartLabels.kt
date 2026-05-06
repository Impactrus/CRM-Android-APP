package com.ossadkowski.crm.mobile.ui.serwis.screens.common

import com.ossadkowski.crm.mobile.domain.serwis.parts.model.PartStatus
import com.ossadkowski.crm.mobile.ui.serwis.theme.StatusToken

/** Polish display label for a [PartStatus]. */
fun PartStatus.label(): String = when (this) {
    PartStatus.REQUESTED  -> "Zapotrzebowanie"
    PartStatus.ORDERED    -> "Zamówione"
    PartStatus.IN_TRANSIT -> "W drodze"
    PartStatus.RECEIVED   -> "Otrzymane"
    PartStatus.CANCELLED  -> "Anulowane"
}

/** Map a [PartStatus] to its [StatusToken] for pill colors. */
fun PartStatus.statusToken(): StatusToken = when (this) {
    PartStatus.REQUESTED  -> StatusToken.INFO
    PartStatus.ORDERED    -> StatusToken.WARN
    PartStatus.IN_TRANSIT -> StatusToken.WARN
    PartStatus.RECEIVED   -> StatusToken.OK
    PartStatus.CANCELLED  -> StatusToken.BRAND
}
