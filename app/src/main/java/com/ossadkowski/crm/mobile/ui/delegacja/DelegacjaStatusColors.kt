package com.ossadkowski.crm.mobile.ui.delegacja

import androidx.compose.ui.graphics.Color

object DelegacjaStatusColors {
    data class StatusStyle(val bg: Color, val text: Color)

    private val statusMap = mapOf(
        "Robocza" to StatusStyle(Color(0xFFf3f4f6), Color(0xFF374151)),
        "Potwierdzona" to StatusStyle(Color(0xFFdbeafe), Color(0xFF1e40af)),
        "Oczekuje Finansów" to StatusStyle(Color(0xFFfef3c7), Color(0xFF92400e)),
        "Do wyjaśnienia" to StatusStyle(Color(0xFFFFF7ED), Color(0xFFC2410C)),
        "Rozliczona" to StatusStyle(Color(0xFFd1fae5), Color(0xFF065f46)),
        "Odrzucona" to StatusStyle(Color(0xFFfee2e2), Color(0xFF991b1b))
    )

    private val defaultStyle = StatusStyle(Color(0xFFf3f4f6), Color(0xFF374151))

    fun forStatus(status: String?): StatusStyle = statusMap[status] ?: defaultStyle
}
