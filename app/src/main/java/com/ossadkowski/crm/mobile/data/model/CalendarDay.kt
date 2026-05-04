package com.ossadkowski.crm.mobile.data.model

import java.util.Date

data class CalendarDay(
    val date: Date,
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean = false,
    val hasTasks: Boolean = false,
    val hasZamrozenia: Boolean = false,
    val taskTitle: String? = null,
    val taskSummary: String? = null
)
