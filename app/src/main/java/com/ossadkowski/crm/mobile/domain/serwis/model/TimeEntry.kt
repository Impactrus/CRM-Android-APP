package com.ossadkowski.crm.mobile.domain.serwis.model

import java.time.LocalDate
import java.time.LocalTime

data class TimeEntry(
    val id: Long?,
    val jobCardNum: String,
    val technican: String?,
    val transDate: LocalDate,
    val timeBegin: LocalTime,
    val timeEnd: LocalTime,
    val kilometers: Double?,
    val travelToMinutes: Int?,
    val travelFromMinutes: Int?
)
