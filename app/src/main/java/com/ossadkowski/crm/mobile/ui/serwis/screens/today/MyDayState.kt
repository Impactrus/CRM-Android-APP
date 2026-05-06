package com.ossadkowski.crm.mobile.ui.serwis.screens.today

import com.ossadkowski.crm.mobile.domain.serwis.model.MyOrder

/** Filter chips on the "Plan dnia" segmented row. */
enum class TaskFilter { ALL, URGENT, SLA, RECALL }

/**
 * UI state for [MyDayScreen]. The `Loading → Success / Error` triad is the
 * pattern used across all 5 flagship screens.
 */
sealed interface MyDayUiState {
    data object Loading : MyDayUiState
    data class Success(
        val orders: List<MyOrder>,
        val filter: TaskFilter = TaskFilter.ALL,
    ) : MyDayUiState
    data class Error(val message: String) : MyDayUiState
}
