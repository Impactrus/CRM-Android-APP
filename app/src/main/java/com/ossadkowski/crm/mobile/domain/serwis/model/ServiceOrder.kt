package com.ossadkowski.crm.mobile.domain.serwis.model

import java.time.Instant
import java.time.LocalDate

/** Full service order summary — used both for list rows and for detail-without-cards. */
data class ServiceOrderSummary(
    val orderRegNum: String,
    val custAccount: String?,
    val custName: String?,
    val orderDate: LocalDate?,
    val orderType: Int?,
    val status: OrderStatus,
    val estimatedHours: Double?,
    val deadline: LocalDate?,
    val machineId: Long?,
    val numerSeryjny: String?,
    val isWarranty: Boolean?,
    val scheduledStart: Instant?,
    val scheduledEnd: Instant?
)

/** Lighter shape returned by `/service-orders/my` (no deadline / machine fields). */
data class MyOrder(
    val orderRegNum: String,
    val custAccount: String?,
    val custName: String?,
    val orderDate: LocalDate?,
    val orderType: Int?,
    val status: OrderStatus,
    val jobCards: List<JobCardRef>
)

data class JobCardRef(
    val mpeOrderJobCardNum: String,
    val technican: String?
)

data class FuelLevels(
    val zero: Int?,
    val q14: Int?,
    val q12: Int?,
    val q34: Int?,
    val full: Int?
)

data class JobCard(
    val mpeOrderJobCardNum: String,
    val orderRegNum: String?,
    val cardNo: Int?,
    val technican: String?,
    val machineType: String?,
    val isClosed: Boolean,
    val serviceType: String?,
    val reportedSymptoms: String?,
    val arrangements: String?,
    val fixLocation: String?,
    val fuel: FuelLevels,
    val remarks: String?
)
