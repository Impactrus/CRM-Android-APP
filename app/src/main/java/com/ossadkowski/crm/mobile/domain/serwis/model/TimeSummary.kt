package com.ossadkowski.crm.mobile.domain.serwis.model

import java.time.LocalDate

/**
 * Aggregated time / travel / kilometre report for one technician across a date range.
 * Backed by the `/service-orders/time-summary` endpoint.
 */
data class TimeSummary(
    val technicianId: String?,
    val totalHours: Double,
    val totalTravelHours: Double,
    val totalKilometers: Double,
    val entries: List<TimeSummaryDay>
)

data class TimeSummaryDay(
    val date: LocalDate,
    val hours: Double,
    val travelHours: Double,
    val kilometers: Double
)
