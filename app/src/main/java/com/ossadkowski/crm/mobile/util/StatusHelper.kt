package com.ossadkowski.crm.mobile.util

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.TextView

object StatusHelper {
    data class StatusColors(val bg: String, val text: String)

    fun getColors(status: String?): StatusColors {
        return when (status?.uppercase()) {
            // Transport ceny / generic
            "PENDING" -> StatusColors("#FEF3C7", "#92400E")
            "APPROVED" -> StatusColors("#DEF7EC", "#03543F")
            "REJECTED" -> StatusColors("#FDE8E8", "#9B1C1C")
            "REVIEW" -> StatusColors("#E1EFFE", "#1E429F")
            "COMPLETED" -> StatusColors("#DEF7EC", "#03543F")
            else -> when (status) {
                // Wnioski
                "W trakcie" -> StatusColors("#FEECDC", "#8A2C0D")
                "Do korekty", "Szkic" -> StatusColors("#E1EFFE", "#1E429F")
                "Odrzucony", "Cofnięty" -> StatusColors("#FDE8E8", "#9B1C1C")
                "Zaakceptowany przez kierownika" -> StatusColors("#FEF3C7", "#92400E")
                "Zaakceptowany" -> StatusColors("#DEF7EC", "#03543F")
                "Zatwierdzony" -> StatusColors("#DEF7EC", "#03543F")
                "Do poprawy", "Do poprawy (HR)" -> StatusColors("#FEF3C7", "#92400E")
                "Wysłany" -> StatusColors("#E1EFFE", "#1E429F")
                // Tasks
                "Nowe" -> StatusColors("#E1EFFE", "#1E429F")
                "Do wyjaśnienia" -> StatusColors("#FEF3C7", "#92400E")
                "Przeterminowane" -> StatusColors("#FDE8E8", "#9B1C1C")
                "Zakończone" -> StatusColors("#DEF7EC", "#03543F")
                "Anulowane" -> StatusColors("#F3F4F6", "#6B7280")
                else -> StatusColors("#F3F4F6", "#6B7280")
            }
        }
    }

    fun applyStatusStyle(textView: TextView, status: String?) {
        val colors = getColors(status)
        val bg = GradientDrawable().apply {
            setColor(Color.parseColor(colors.bg))
            cornerRadius = 999f
        }
        textView.background = bg
        textView.setTextColor(Color.parseColor(colors.text))
        textView.setPadding(24, 4, 24, 4)
    }
}
