package com.ossadkowski.crm.mobile.domain.serwis.repository

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.model.Activity
import com.ossadkowski.crm.mobile.domain.serwis.model.JobCard
import com.ossadkowski.crm.mobile.domain.serwis.model.Machine
import com.ossadkowski.crm.mobile.domain.serwis.model.MyOrder
import com.ossadkowski.crm.mobile.domain.serwis.model.OrderLogEntry
import com.ossadkowski.crm.mobile.domain.serwis.model.OrderStatus
import com.ossadkowski.crm.mobile.domain.serwis.model.Schedule
import com.ossadkowski.crm.mobile.domain.serwis.model.ServiceOrderSummary
import com.ossadkowski.crm.mobile.domain.serwis.model.Technician
import com.ossadkowski.crm.mobile.domain.serwis.model.TimeEntry
import com.ossadkowski.crm.mobile.domain.serwis.model.TimeSummary
import com.ossadkowski.crm.mobile.domain.serwis.model.WarrantyCheck
import java.time.LocalDate
import java.time.LocalTime

data class OrderFilters(
    val status: OrderStatus? = null,
    val custAccount: String? = null,
    val dateFrom: LocalDate? = null,
    val dateTo: LocalDate? = null,
    val missingDeadline: Boolean? = null,
    val pageNumber: Int = 1,
    val pageSize: Int = 50
)

data class PagedOrders(
    val items: List<ServiceOrderSummary>,
    val totalCount: Int
)

data class OrderWithCards(
    val order: ServiceOrderSummary,
    val jobCards: List<JobCard>,
    val log: List<OrderLogEntry>
)

data class NewTimeEntry(
    val transDate: LocalDate,
    val timeBegin: LocalTime,
    val timeEnd: LocalTime,
    val kilometers: Double? = null,
    val travelToMinutes: Int? = null,
    val travelFromMinutes: Int? = null
)

data class NewActivity(
    val transDate: LocalDate,
    val activity: String,
    val qtyPlan: Double? = null,
    val qtyReal: Double? = null
)

data class ScheduleFilters(
    val accountNum: String? = null,
    val status: String? = null,
    val monthsAhead: Int? = null,
    val machineId: Long? = null
)

interface SerwisRepository {
    suspend fun getMyOrders(): Result<List<MyOrder>>
    suspend fun listOrders(filters: OrderFilters = OrderFilters()): Result<PagedOrders>
    suspend fun getOrder(orderNum: String): Result<ServiceOrderSummary>
    suspend fun getOrderWithCards(orderNum: String): Result<OrderWithCards>
    suspend fun getJobCards(orderNum: String): Result<List<JobCard>>
    suspend fun getJobCard(orderNum: String, cardNum: String): Result<JobCard>
    suspend fun getOrderLog(orderNum: String): Result<List<OrderLogEntry>>
    suspend fun getTimeEntries(cardNum: String): Result<List<TimeEntry>>
    suspend fun addTimeEntry(cardNum: String, req: NewTimeEntry): Result<TimeEntry>
    suspend fun getActivities(cardNum: String): Result<List<Activity>>
    suspend fun addActivity(cardNum: String, req: NewActivity): Result<Activity>
    suspend fun getOrderTechnicians(orderNum: String): Result<List<Technician>>
    suspend fun getOrderMachine(orderNum: String): Result<Machine>
    suspend fun getMachineBySerial(serial: String, accountNum: String? = null): Result<Machine>
    suspend fun getCustomerMachines(accountNum: String? = null): Result<List<Machine>>
    suspend fun checkWarranty(machineId: Long): Result<WarrantyCheck>
    suspend fun getSchedules(filters: ScheduleFilters = ScheduleFilters()): Result<List<Schedule>>
    suspend fun getMyTimeSummary(dateFrom: LocalDate, dateTo: LocalDate): Result<TimeSummary>
}
