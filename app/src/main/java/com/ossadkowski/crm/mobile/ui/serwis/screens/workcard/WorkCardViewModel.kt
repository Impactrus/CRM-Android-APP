package com.ossadkowski.crm.mobile.ui.serwis.screens.workcard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.repository.NewActivity
import com.ossadkowski.crm.mobile.domain.serwis.repository.NewTimeEntry
import com.ossadkowski.crm.mobile.domain.serwis.usecase.AddActivityUseCase
import com.ossadkowski.crm.mobile.domain.serwis.usecase.AddTimeEntryUseCase
import com.ossadkowski.crm.mobile.ui.serwis.components.LiveTimerState
import com.ossadkowski.crm.mobile.ui.serwis.nav.SerwisRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class WorkCardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val addTimeEntry: AddTimeEntryUseCase,
    private val addActivity: AddActivityUseCase,
) : ViewModel() {

    private val orderNum: String =
        savedStateHandle.get<String>(SerwisRoutes.ARG_ORDER_NUM).orEmpty()
    private val cardNum: String =
        savedStateHandle.get<String>(SerwisRoutes.ARG_CARD_NUM).orEmpty()

    private val _uiState = MutableStateFlow<WorkCardUiState>(
        WorkCardUiState.Editing(
            WorkCardFormState(orderNum = orderNum, cardNum = cardNum),
        ),
    )
    val uiState: StateFlow<WorkCardUiState> = _uiState.asStateFlow()

    /** True when there is something meaningful to send to the API. */
    fun canSave(): Boolean {
        val form = (_uiState.value as? WorkCardUiState.Editing)?.form ?: return false
        if (form.activityError != null || form.mileageError != null) return false
        val hasWork = form.activities.isNotEmpty() || form.workTimerSeconds > 0
        val hasTravel = form.travelStart != null || form.travelTimerSeconds > 0
        val hasKm = totalKm(form) != null
        return hasWork || hasTravel || hasKm
    }

    /* ----------------------------- editors ----------------------------- */

    private inline fun updateForm(block: (WorkCardFormState) -> WorkCardFormState) {
        val current = _uiState.value
        if (current is WorkCardUiState.Editing) {
            _uiState.value = WorkCardUiState.Editing(block(current.form))
        }
    }

    fun setWorkMode(mode: Mode) = updateForm { it.copy(workMode = mode) }
    fun setTravelMode(mode: Mode) = updateForm { it.copy(travelMode = mode) }
    fun setMileageMode(mode: MileageMode) = updateForm { it.copy(mileageMode = mode) }
    fun setMileageStart(value: Int?) = updateForm { revalidateMileage(it.copy(mileageStart = value)) }
    fun setMileageEnd(value: Int?) = updateForm { revalidateMileage(it.copy(mileageEnd = value)) }
    fun setMileageSum(value: Double?) = updateForm { revalidateMileage(it.copy(mileageSum = value)) }
    fun setTravelStart(value: LocalTime?) = updateForm { it.copy(travelStart = value) }
    fun setTravelEnd(value: LocalTime?) = updateForm { it.copy(travelEnd = value) }

    fun addActivity(row: ActivityRow) = updateForm { state ->
        val err = validateActivity(row)
        if (err != null) {
            state.copy(activityError = err)
        } else {
            state.copy(
                activities = state.activities + row,
                activityError = null,
            )
        }
    }

    fun removeActivity(index: Int) = updateForm { state ->
        if (index !in state.activities.indices) state
        else state.copy(activities = state.activities.toMutableList().apply { removeAt(index) })
    }

    fun addBreak(row: BreakRow) = updateForm { it.copy(breaks = it.breaks + row) }
    fun removeBreak(index: Int) = updateForm { state ->
        if (index !in state.breaks.indices) state
        else state.copy(breaks = state.breaks.toMutableList().apply { removeAt(index) })
    }

    /* --------------------------- timer FSM --------------------------- */

    /** Per spec: starting one timer auto-pauses the other. */
    fun startWorkTimer() = updateForm { state ->
        val pausedTravel = state.travelTimerState
            .takeIf { it == LiveTimerState.RUNNING }
            ?.let { LiveTimerState.PAUSED }
            ?: state.travelTimerState
        state.copy(
            workTimerState = LiveTimerState.RUNNING,
            travelTimerState = pausedTravel,
        )
    }

    fun pauseWorkTimer() = updateForm {
        if (it.workTimerState == LiveTimerState.RUNNING) {
            it.copy(workTimerState = LiveTimerState.PAUSED)
        } else if (it.workTimerState == LiveTimerState.PAUSED) {
            it.copy(workTimerState = LiveTimerState.RUNNING)
        } else it
    }

    fun stopWorkTimer() = updateForm { it.copy(workTimerState = LiveTimerState.DONE) }

    fun tickWorkTimer(deltaSeconds: Long) = updateForm {
        if (it.workTimerState == LiveTimerState.RUNNING) {
            it.copy(workTimerSeconds = it.workTimerSeconds + deltaSeconds)
        } else it
    }

    fun startTravelTimer() = updateForm { state ->
        val pausedWork = state.workTimerState
            .takeIf { it == LiveTimerState.RUNNING }
            ?.let { LiveTimerState.PAUSED }
            ?: state.workTimerState
        state.copy(
            travelTimerState = LiveTimerState.RUNNING,
            workTimerState = pausedWork,
        )
    }

    fun pauseTravelTimer() = updateForm {
        if (it.travelTimerState == LiveTimerState.RUNNING) {
            it.copy(travelTimerState = LiveTimerState.PAUSED)
        } else if (it.travelTimerState == LiveTimerState.PAUSED) {
            it.copy(travelTimerState = LiveTimerState.RUNNING)
        } else it
    }

    fun stopTravelTimer() = updateForm { it.copy(travelTimerState = LiveTimerState.DONE) }

    fun tickTravelTimer(deltaSeconds: Long) = updateForm {
        if (it.travelTimerState == LiveTimerState.RUNNING) {
            it.copy(travelTimerSeconds = it.travelTimerSeconds + deltaSeconds)
        } else it
    }

    /* ----------------------------- save ----------------------------- */

    fun save() {
        val form = (_uiState.value as? WorkCardUiState.Editing)?.form ?: return
        if (!canSave()) return
        viewModelScope.launch {
            _uiState.value = WorkCardUiState.Saving

            val workNetSec = computeWorkNetSeconds(form)
            val timeBegin = form.activities.minByOrNull { it.start }?.start
                ?: form.travelStart
                ?: LocalTime.of(0, 0)
            val timeEnd = form.activities.maxByOrNull { it.end }?.end
                ?: form.travelEnd
                ?: timeBegin.plusSeconds(workNetSec)

            val travelToMinutes = form.travelStart?.let { start ->
                form.travelEnd?.let { end ->
                    abs(ChronoUnit.MINUTES.between(start, end)).toInt()
                }
            } ?: (form.travelTimerSeconds / 60).toInt().takeIf { it > 0 }

            val req = NewTimeEntry(
                transDate = form.date,
                timeBegin = timeBegin,
                timeEnd = timeEnd,
                kilometers = totalKm(form),
                travelToMinutes = travelToMinutes,
                travelFromMinutes = null,
            )

            when (val r = addTimeEntry(form.cardNum, req)) {
                is Result.Success -> {
                    var failureMessage: String? = null
                    for (a in form.activities) {
                        val resp = addActivity(
                            form.cardNum,
                            NewActivity(
                                transDate = form.date,
                                activity = a.title,
                                qtyPlan = null,
                                qtyReal = null,
                            ),
                        )
                        if (resp is Result.Error) {
                            failureMessage = resp.message
                            break
                        }
                    }
                    _uiState.value = if (failureMessage != null) {
                        WorkCardUiState.Error(failureMessage)
                    } else WorkCardUiState.Saved
                }
                is Result.Error -> _uiState.value = WorkCardUiState.Error(r.message)
                Result.Loading -> Unit
            }
        }
    }

    /* --------------------------- validation --------------------------- */

    private fun validateActivity(row: ActivityRow): String? {
        if (!row.end.isAfter(row.start)) return "Koniec musi być po starcie"
        val mins = ChronoUnit.MINUTES.between(row.start, row.end)
        if (mins > 16 * 60) return "Maks. 16 godzin"
        return null
    }

    private fun revalidateMileage(state: WorkCardFormState): WorkCardFormState {
        val err = when (state.mileageMode) {
            MileageMode.START_END -> {
                val s = state.mileageStart
                val e = state.mileageEnd
                if (s != null && e != null) {
                    if (e < s) "Koniec musi być >= start"
                    else if (e - s > 2000) "Maks. 2000 km"
                    else null
                } else null
            }
            MileageMode.SUM -> null
        }
        return state.copy(mileageError = err)
    }
}

/** Net work time (seconds) used by the summary card. */
fun computeWorkNetSeconds(form: WorkCardFormState): Long {
    val activitiesSec = form.activities.sumOf {
        ChronoUnit.SECONDS.between(it.start, it.end)
    }
    val timer = form.workTimerSeconds
    val gross = if (form.workMode == Mode.MANUAL) activitiesSec else timer
    val breaksSec = form.breaks.sumOf { it.minutes.toLong() } * 60
    return (gross - breaksSec).coerceAtLeast(0L)
}

fun totalKm(form: WorkCardFormState): Double? = when (form.mileageMode) {
    MileageMode.START_END -> {
        val s = form.mileageStart
        val e = form.mileageEnd
        if (s != null && e != null && e >= s) (e - s).toDouble() else null
    }
    MileageMode.SUM -> form.mileageSum
}

fun formatHhMmSs(totalSeconds: Long): String {
    val s = totalSeconds.coerceAtLeast(0)
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return "%02d:%02d:%02d".format(h, m, sec)
}

fun formatHhMm(totalSeconds: Long): String {
    val s = totalSeconds.coerceAtLeast(0)
    val h = s / 3600
    val m = (s % 3600) / 60
    return "%02d:%02d".format(h, m)
}
