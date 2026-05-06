package com.ossadkowski.crm.mobile.data.serwis.mapper

import com.ossadkowski.crm.mobile.data.serwis.dto.ActivityDto
import com.ossadkowski.crm.mobile.data.serwis.dto.CreateActivityRequest
import com.ossadkowski.crm.mobile.data.serwis.dto.CreateTimeEntryRequest
import com.ossadkowski.crm.mobile.data.serwis.dto.JobCardDto
import com.ossadkowski.crm.mobile.data.serwis.dto.JobCardLiteDto
import com.ossadkowski.crm.mobile.data.serwis.dto.MachineDto
import com.ossadkowski.crm.mobile.data.serwis.dto.MachineHistoryEntryDto
import com.ossadkowski.crm.mobile.data.serwis.dto.MyOrderDto
import com.ossadkowski.crm.mobile.data.serwis.dto.OrderLogEntryDto
import com.ossadkowski.crm.mobile.data.serwis.dto.ScheduleDto
import com.ossadkowski.crm.mobile.data.serwis.dto.ServiceOrderDto
import com.ossadkowski.crm.mobile.data.serwis.dto.TechnicianDto
import com.ossadkowski.crm.mobile.data.serwis.dto.TimeEntryDto
import com.ossadkowski.crm.mobile.data.serwis.dto.TimeSummaryDayDto
import com.ossadkowski.crm.mobile.data.serwis.dto.TimeSummaryDto
import com.ossadkowski.crm.mobile.data.serwis.dto.WarrantyCheckDto
import com.ossadkowski.crm.mobile.domain.serwis.model.Activity
import com.ossadkowski.crm.mobile.domain.serwis.model.FuelLevels
import com.ossadkowski.crm.mobile.domain.serwis.model.JobCard
import com.ossadkowski.crm.mobile.domain.serwis.model.JobCardRef
import com.ossadkowski.crm.mobile.domain.serwis.model.Machine
import com.ossadkowski.crm.mobile.domain.serwis.model.MachineHistoryEntry
import com.ossadkowski.crm.mobile.domain.serwis.model.MyOrder
import com.ossadkowski.crm.mobile.domain.serwis.model.OrderLogEntry
import com.ossadkowski.crm.mobile.domain.serwis.model.OrderStatus
import com.ossadkowski.crm.mobile.domain.serwis.model.Schedule
import com.ossadkowski.crm.mobile.domain.serwis.model.ServiceOrderSummary
import com.ossadkowski.crm.mobile.domain.serwis.model.Technician
import com.ossadkowski.crm.mobile.domain.serwis.model.TimeEntry
import com.ossadkowski.crm.mobile.domain.serwis.model.TimeSummary
import com.ossadkowski.crm.mobile.domain.serwis.model.TimeSummaryDay
import com.ossadkowski.crm.mobile.domain.serwis.model.WarrantyCheck
import com.ossadkowski.crm.mobile.domain.serwis.model.WarrantyStatus
import com.ossadkowski.crm.mobile.domain.serwis.repository.NewActivity
import com.ossadkowski.crm.mobile.domain.serwis.repository.NewTimeEntry
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId

// =====================================================================
// Date / time parsing helpers
// =====================================================================

private fun String?.toLocalDateOrNull(): LocalDate? =
    this?.takeIf { it.isNotBlank() }?.let {
        runCatching { LocalDate.parse(it) }.getOrNull()
    }

private fun String?.toInstantOrNull(): Instant? =
    this?.takeIf { it.isNotBlank() }?.let {
        runCatching { OffsetDateTime.parse(it).toInstant() }.getOrNull()
            ?: runCatching {
                LocalDateTime.parse(it).atZone(ZoneId.systemDefault()).toInstant()
            }.getOrNull()
    }

private fun String?.toLocalTimeOrNull(): LocalTime? =
    this?.takeIf { it.isNotBlank() }?.let {
        runCatching { LocalTime.parse(it) }.getOrNull()
    }

// =====================================================================
// Service order
// =====================================================================

fun MyOrderDto.toDomain(): MyOrder = MyOrder(
    orderRegNum = orderRegNum,
    custAccount = custAccount,
    custName = custName,
    orderDate = orderDate.toLocalDateOrNull(),
    orderType = orderType,
    status = OrderStatus.fromCode(mpeOrderStatus),
    jobCards = jobCards.orEmpty().map { it.toDomain() }
)

fun JobCardLiteDto.toDomain(): JobCardRef = JobCardRef(
    mpeOrderJobCardNum = mpeOrderJobCardNum,
    technican = technican
)

fun ServiceOrderDto.toDomain(): ServiceOrderSummary = ServiceOrderSummary(
    orderRegNum = orderRegNum,
    custAccount = custAccount,
    custName = custName,
    orderDate = orderDate.toLocalDateOrNull(),
    orderType = orderType,
    status = OrderStatus.fromCode(mpeOrderStatus),
    estimatedHours = estimatedHours,
    deadline = deadline.toLocalDateOrNull(),
    machineId = machineId,
    numerSeryjny = numerSeryjny,
    isWarranty = isWarranty,
    scheduledStart = scheduledStart.toInstantOrNull(),
    scheduledEnd = scheduledEnd.toInstantOrNull()
)

fun JobCardDto.toDomain(): JobCard = JobCard(
    mpeOrderJobCardNum = mpeOrderJobCardNum,
    orderRegNum = orderRegNum,
    cardNo = cardNo,
    technican = technican,
    machineType = machineType,
    isClosed = closed == 1,
    serviceType = serviceType,
    reportedSymptoms = reportedSymptoms,
    arrangements = arrangements,
    fixLocation = fixLocation,
    fuel = FuelLevels(
        zero = fuel0,
        q14 = fuel14,
        q12 = fuel12,
        q34 = fuel34,
        full = fuel44
    ),
    remarks = remarks
)

// =====================================================================
// Time entries
// =====================================================================

fun TimeEntryDto.toDomain(): TimeEntry = TimeEntry(
    id = id,
    jobCardNum = mpeOrderJobCardNum,
    technican = technican,
    transDate = transDate.toLocalDateOrNull() ?: LocalDate.MIN,
    timeBegin = timeBegin.toLocalTimeOrNull() ?: LocalTime.MIDNIGHT,
    timeEnd = timeEnd.toLocalTimeOrNull() ?: LocalTime.MIDNIGHT,
    kilometers = kilometers,
    travelToMinutes = travelToMinutes,
    travelFromMinutes = travelFromMinutes
)

fun NewTimeEntry.toRequest(): CreateTimeEntryRequest = CreateTimeEntryRequest(
    transDate = transDate.toString(),
    timeBegin = timeBegin.toString().take(5),  // HH:mm
    timeEnd = timeEnd.toString().take(5),
    kilometers = kilometers,
    travelToMinutes = travelToMinutes,
    travelFromMinutes = travelFromMinutes
)

// =====================================================================
// Activities
// =====================================================================

fun ActivityDto.toDomain(): Activity = Activity(
    id = id,
    jobCardNum = mpeOrderJobCardNum,
    technican = technican,
    transDate = transDate.toLocalDateOrNull() ?: LocalDate.MIN,
    activity = activity,
    qtyPlan = qtyPlan,
    qtyReal = qtyReal
)

fun NewActivity.toRequest(): CreateActivityRequest = CreateActivityRequest(
    transDate = transDate.toString(),
    activity = activity,
    qtyPlan = qtyPlan,
    qtyReal = qtyReal
)

// =====================================================================
// Technicians, log
// =====================================================================

fun TechnicianDto.toDomain(): Technician = Technician(
    technicianId = technicianId,
    isLead = isLead
)

fun OrderLogEntryDto.toDomain(): OrderLogEntry = OrderLogEntry(
    orderRegNum = orderRegNum,
    description = description,
    createdBy = createdBy,
    createdAt = createdDateTime.toInstantOrNull()
)

// =====================================================================
// Machines
// =====================================================================

fun MachineHistoryEntryDto.toDomain(): MachineHistoryEntry = MachineHistoryEntry(
    orderRegNum = orderRegNum,
    orderDate = orderDate.toLocalDateOrNull(),
    orderType = orderType,
    status = OrderStatus.fromCode(mpeOrderStatus),
    reportedSymptoms = reportedSymptoms,
    serviceType = serviceType,
    isWarranty = isWarranty
)

fun MachineDto.toDomain(): Machine = Machine(
    id = id,
    accountNum = accountNum,
    marka = marka,
    model = model,
    numerSeryjny = numerSeryjny,
    typMaszyny = typMaszyny,
    rokProdukcji = rokProdukcji,
    gwarancjaOd = gwarancjaOd.toLocalDateOrNull(),
    gwarancjaDo = gwarancjaDo.toLocalDateOrNull(),
    dataSprzedazy = dataSprzedazy.toLocalDateOrNull(),
    nrRejestracyjny = nrRejestracyjny,
    itemId = itemId,
    itemName = itemName,
    zrodlo = zrodlo,
    uwagi = uwagi,
    warrantyStatus = WarrantyStatus.fromString(warrantyStatus),
    totalOrders = totalOrders,
    openOrders = openOrders,
    history = serviceOrders.orEmpty().map { it.toDomain() }
)

fun WarrantyCheckDto.toDomain(): WarrantyCheck = WarrantyCheck(
    machineId = machineId,
    gwarancjaOd = gwarancjaOd.toLocalDateOrNull(),
    gwarancjaDo = gwarancjaDo.toLocalDateOrNull(),
    warrantyStatus = WarrantyStatus.fromString(warrantyStatus),
    isActive = isActive
)

// =====================================================================
// Time summary
// =====================================================================

fun TimeSummaryDayDto.toDomain(): TimeSummaryDay? {
    val parsedDate = date.toLocalDateOrNull() ?: return null
    return TimeSummaryDay(
        date = parsedDate,
        hours = hours ?: 0.0,
        travelHours = travelHours ?: 0.0,
        kilometers = kilometers ?: 0.0
    )
}

fun TimeSummaryDto.toDomain(): TimeSummary = TimeSummary(
    technicianId = technicianId,
    totalHours = totalHours ?: 0.0,
    totalTravelHours = totalTravelHours ?: 0.0,
    totalKilometers = totalKilometers ?: 0.0,
    entries = entries.orEmpty().mapNotNull { it.toDomain() }
)

// =====================================================================
// Schedules
// =====================================================================

fun ScheduleDto.toDomain(): Schedule = Schedule(
    id = id,
    machineId = machineId,
    marka = marka,
    model = model,
    numerSeryjny = numerSeryjny,
    accountNum = accountNum,
    scheduleType = scheduleType,
    intervalMonths = intervalMonths,
    lastServiceDate = lastServiceDate.toLocalDateOrNull(),
    nextServiceDate = nextServiceDate.toLocalDateOrNull(),
    orderRegNum = orderRegNum,
    scheduleStatus = scheduleStatus,
    notes = notes,
    createdBy = createdBy
)
