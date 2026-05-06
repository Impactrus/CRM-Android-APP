package com.ossadkowski.crm.mobile.ui.serwis.screens.workcard

import com.ossadkowski.crm.mobile.ui.serwis.components.LiveTimerState
import java.time.LocalDate
import java.time.LocalTime

enum class Mode { MANUAL, STOPER }
enum class MileageMode { START_END, SUM }

data class ActivityRow(
    val title: String,
    val start: LocalTime,
    val end: LocalTime,
)

data class BreakRow(
    val minutes: Int,
    val label: String,
)

data class WorkCardFormState(
    val orderNum: String,
    val cardNum: String,
    val date: LocalDate = LocalDate.now(),
    val workMode: Mode = Mode.MANUAL,
    val travelMode: Mode = Mode.MANUAL,
    val activities: List<ActivityRow> = emptyList(),
    val travelStart: LocalTime? = null,
    val travelEnd: LocalTime? = null,
    val travelTimerState: LiveTimerState = LiveTimerState.IDLE,
    val travelTimerSeconds: Long = 0,
    val workTimerState: LiveTimerState = LiveTimerState.IDLE,
    val workTimerSeconds: Long = 0,
    val breaks: List<BreakRow> = emptyList(),
    val mileageMode: MileageMode = MileageMode.START_END,
    val mileageStart: Int? = null,
    val mileageEnd: Int? = null,
    val mileageSum: Double? = null,
    /** Last validation error, surfaces in screen as helper text. */
    val activityError: String? = null,
    val mileageError: String? = null,
)

sealed interface WorkCardUiState {
    data class Editing(val form: WorkCardFormState) : WorkCardUiState
    data object Saving : WorkCardUiState
    data object Saved : WorkCardUiState
    data class Error(val message: String) : WorkCardUiState
}
