package com.ossadkowski.crm.mobile.ui.serwis.screens.mytime

import com.ossadkowski.crm.mobile.domain.serwis.model.TimeSummary
import java.time.LocalDate

/**
 * Inclusive Monday → Sunday window used as the unit of navigation on
 * [MyTimeScreen].
 *
 * @property label uppercase Polish abbreviation pair, e.g. `"29 KWI – 5 MAJ"`.
 */
data class WeekRange(
    val start: LocalDate,
    val end: LocalDate,
) {
    val label: String
        get() = buildLabel(start, end)

    companion object {
        /**
         * Returns the Monday → Sunday week containing [reference].
         * Monday is day-of-week 1 in [java.time.DayOfWeek].
         */
        fun forDate(reference: LocalDate): WeekRange {
            val monday = reference.minusDays((reference.dayOfWeek.value - 1).toLong())
            return WeekRange(monday, monday.plusDays(6))
        }

        private val PL_MONTHS_SHORT = arrayOf(
            "STY", "LUT", "MAR", "KWI", "MAJ", "CZE",
            "LIP", "SIE", "WRZ", "PAŹ", "LIS", "GRU"
        )

        private fun buildLabel(start: LocalDate, end: LocalDate): String {
            val s = "${start.dayOfMonth} ${PL_MONTHS_SHORT[start.monthValue - 1]}"
            val e = "${end.dayOfMonth} ${PL_MONTHS_SHORT[end.monthValue - 1]}"
            return "$s – $e"
        }
    }
}

/**
 * UI state for [MyTimeScreen]. Same `Loading → Success / Error` triad as
 * Mój dzień (see `MyDayUiState`).
 */
sealed interface MyTimeUiState {
    data object Loading : MyTimeUiState
    data class Success(
        val week: WeekRange,
        val summary: TimeSummary,
    ) : MyTimeUiState
    data class Error(val message: String) : MyTimeUiState
}
