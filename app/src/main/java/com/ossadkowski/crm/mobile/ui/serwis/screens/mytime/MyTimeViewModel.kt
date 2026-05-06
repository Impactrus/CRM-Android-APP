package com.ossadkowski.crm.mobile.ui.serwis.screens.mytime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.usecase.GetMyTimeSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MyTimeViewModel @Inject constructor(
    private val getMyTimeSummary: GetMyTimeSummaryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyTimeUiState>(MyTimeUiState.Loading)
    val uiState: StateFlow<MyTimeUiState> = _uiState.asStateFlow()

    private var currentWeek: WeekRange = WeekRange.forDate(LocalDate.now())

    init {
        loadWeek(currentWeek.start)
    }

    /** Reloads the currently selected week. */
    fun refresh() {
        loadWeek(currentWeek.start)
    }

    /** Loads the Monday → Sunday week starting at [weekStartMonday]. */
    fun loadWeek(weekStartMonday: LocalDate) {
        val week = WeekRange.forDate(weekStartMonday)
        currentWeek = week
        viewModelScope.launch {
            _uiState.value = MyTimeUiState.Loading
            when (val r = getMyTimeSummary(week.start, week.end)) {
                is Result.Success -> _uiState.value = MyTimeUiState.Success(week, r.data)
                is Result.Error -> _uiState.value = MyTimeUiState.Error(r.message)
                Result.Loading -> Unit
            }
        }
    }

    fun nextWeek() {
        loadWeek(currentWeek.start.plusDays(7))
    }

    fun prevWeek() {
        loadWeek(currentWeek.start.minusDays(7))
    }
}
