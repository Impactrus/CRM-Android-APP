package com.ossadkowski.crm.mobile.ui.nawozy.common

import java.text.NumberFormat
import java.util.Locale

private val PL = Locale.forLanguageTag("pl-PL")

private val plnFormat: NumberFormat = NumberFormat.getNumberInstance(PL).apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
}

private val tonsFormat: NumberFormat = NumberFormat.getNumberInstance(PL).apply {
    minimumFractionDigits = 0
    maximumFractionDigits = 1
}

private val kmFormat: NumberFormat = NumberFormat.getNumberInstance(PL).apply {
    minimumFractionDigits = 0
    maximumFractionDigits = 0
}

/** "1 234,50 zł" — Polish grouping + 2 decimals. Null renders as an em dash. */
fun formatPln(value: Double?): String =
    if (value == null) "—" else "${plnFormat.format(value)} zł"

/** PLN per tonne, read-only transport rate. */
fun formatPlnPerTon(value: Double?): String =
    if (value == null) "—" else "${plnFormat.format(value)} zł/t"

/** "24 T" / "23,5 T". */
fun formatTons(value: Double?): String =
    if (value == null) "—" else "${tonsFormat.format(value)} T"

/** "128 km" (rounded to whole kilometres). Null renders as an em dash. */
fun formatKm(value: Double?): String =
    if (value == null) "—" else "${kmFormat.format(value)} km"
