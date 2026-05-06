package com.ossadkowski.crm.mobile.domain.serwis.model

import java.time.LocalDate

data class Activity(
    val id: Long?,
    val jobCardNum: String,
    val technican: String?,
    val transDate: LocalDate,
    val activity: String,
    val qtyPlan: Double?,
    val qtyReal: Double?
)
