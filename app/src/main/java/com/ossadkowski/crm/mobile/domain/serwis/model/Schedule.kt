package com.ossadkowski.crm.mobile.domain.serwis.model

import java.time.LocalDate

data class Schedule(
    val id: Long,
    val machineId: Long?,
    val marka: String?,
    val model: String?,
    val numerSeryjny: String?,
    val accountNum: String?,
    val scheduleType: String?,
    val intervalMonths: Int?,
    val lastServiceDate: LocalDate?,
    val nextServiceDate: LocalDate?,
    val orderRegNum: String?,
    val scheduleStatus: String?,
    val notes: String?,
    val createdBy: String?
)
