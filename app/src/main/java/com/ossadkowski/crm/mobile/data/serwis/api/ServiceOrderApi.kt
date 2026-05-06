package com.ossadkowski.crm.mobile.data.serwis.api

import com.ossadkowski.crm.mobile.data.serwis.dto.ActivityDto
import com.ossadkowski.crm.mobile.data.serwis.dto.CreateActivityRequest
import com.ossadkowski.crm.mobile.data.serwis.dto.CreateTimeEntryRequest
import com.ossadkowski.crm.mobile.data.serwis.dto.JobCardDto
import com.ossadkowski.crm.mobile.data.serwis.dto.MachineDto
import com.ossadkowski.crm.mobile.data.serwis.dto.MyOrderDto
import com.ossadkowski.crm.mobile.data.serwis.dto.OrderLogEntryDto
import com.ossadkowski.crm.mobile.data.serwis.dto.ScheduleDto
import com.ossadkowski.crm.mobile.data.serwis.dto.ServiceOrderDto
import com.ossadkowski.crm.mobile.data.serwis.dto.TechnicianDto
import com.ossadkowski.crm.mobile.data.serwis.dto.TimeEntryDto
import com.ossadkowski.crm.mobile.data.serwis.dto.TimeSummaryDto
import com.ossadkowski.crm.mobile.data.serwis.dto.WarrantyCheckDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Mobile Serwis API — section endpoints A, B, C, partial D-read, E, G + warranty.
 *
 * Path-param notes:
 *   `orderNum` and `cardNum` may contain `/` (e.g. "MPE-000123/1"). Retrofit URL-encodes
 *   them automatically when `encoded = false`. The backend then runs `Uri.UnescapeDataString`
 *   so callers MUST pass the raw, un-encoded value.
 */
interface ServiceOrderApi {

    // ---- A. Service orders ----

    @GET("service-orders/my")
    suspend fun getMyOrders(): List<MyOrderDto>

    @GET("service-orders")
    suspend fun listOrders(
        @Query("status") status: Int? = null,
        @Query("custAccount") custAccount: String? = null,
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null,
        @Query("missingDeadline") missingDeadline: Boolean? = null,
        @Query("pageNumber") pageNumber: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): Response<List<ServiceOrderDto>>

    @GET("service-orders/{orderNum}")
    suspend fun getOrder(
        @Path("orderNum", encoded = false) orderNum: String
    ): ServiceOrderDto

    @GET("service-orders/{orderNum}/job-cards")
    suspend fun getJobCards(
        @Path("orderNum", encoded = false) orderNum: String
    ): List<JobCardDto>

    @GET("service-orders/{orderNum}/job-cards/{cardNum}")
    suspend fun getJobCard(
        @Path("orderNum", encoded = false) orderNum: String,
        @Path("cardNum", encoded = false) cardNum: String
    ): JobCardDto

    @GET("service-orders/{orderNum}/log")
    suspend fun getOrderLog(
        @Path("orderNum", encoded = false) orderNum: String
    ): List<OrderLogEntryDto>

    // ---- C. Time entries & activities ----

    @GET("service-orders/job-cards/{cardNum}/time-entries")
    suspend fun getTimeEntries(
        @Path("cardNum", encoded = false) cardNum: String
    ): List<TimeEntryDto>

    @POST("service-orders/job-cards/{cardNum}/time-entries")
    suspend fun addTimeEntry(
        @Path("cardNum", encoded = false) cardNum: String,
        @Body req: CreateTimeEntryRequest
    ): TimeEntryDto

    @GET("service-orders/job-cards/{cardNum}/activities")
    suspend fun getActivities(
        @Path("cardNum", encoded = false) cardNum: String
    ): List<ActivityDto>

    @POST("service-orders/job-cards/{cardNum}/activities")
    suspend fun addActivity(
        @Path("cardNum", encoded = false) cardNum: String,
        @Body req: CreateActivityRequest
    ): ActivityDto

    // ---- D. Technicians (read) ----

    @GET("service-orders/job-cards/{cardNum}/technicians")
    suspend fun getJobCardTechnicians(
        @Path("cardNum", encoded = false) cardNum: String
    ): List<TechnicianDto>

    @GET("service-orders/{orderNum}/technicians")
    suspend fun getOrderTechnicians(
        @Path("orderNum", encoded = false) orderNum: String
    ): List<TechnicianDto>

    // ---- E. Machines ----

    @GET("service-orders/{orderNum}/machine")
    suspend fun getOrderMachine(
        @Path("orderNum", encoded = false) orderNum: String
    ): MachineDto

    @GET("service-orders/machines/{serial}")
    suspend fun getMachineBySerial(
        @Path("serial", encoded = false) serial: String,
        @Query("accountNum") accountNum: String? = null
    ): MachineDto

    @GET("service-orders/machines")
    suspend fun getCustomerMachines(
        @Query("accountNum") accountNum: String? = null
    ): List<MachineDto>

    // ---- F. Warranty ----

    @GET("service-orders/warranty/{machineId}")
    suspend fun checkWarranty(
        @Path("machineId") machineId: Long
    ): WarrantyCheckDto

    // ---- H. Time summary (weekly / monthly) ----

    /**
     * Aggregated time / travel / km report for the calling JWT user.
     * Without [technicianId], the backend scopes the result to the JWT user
     * and returns a single [TimeSummaryDto] object (not a list).
     */
    @GET("service-orders/time-summary")
    suspend fun getMyTimeSummary(
        @Query("technicianId") technicianId: String? = null,
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null
    ): TimeSummaryDto

    // ---- G. Schedules ----

    @GET("service-orders/schedules")
    suspend fun getSchedules(
        @Query("accountNum") accountNum: String? = null,
        @Query("status") status: String? = null,
        @Query("monthsAhead") monthsAhead: Int? = null,
        @Query("machineId") machineId: Long? = null
    ): List<ScheduleDto>
}
