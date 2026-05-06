package com.ossadkowski.crm.mobile.data.serwis.dto

import com.google.gson.annotations.SerializedName

// =====================================================================
// Service order — lite ("/my")
// =====================================================================

data class MyOrderDto(
    @SerializedName("orderRegNum") val orderRegNum: String,
    @SerializedName("custAccount") val custAccount: String?,
    @SerializedName("custName") val custName: String?,
    @SerializedName("orderDate") val orderDate: String?,
    @SerializedName("orderType") val orderType: Int?,
    @SerializedName("mpeOrderStatus") val mpeOrderStatus: Int?,
    @SerializedName("jobCards") val jobCards: List<JobCardLiteDto>? = emptyList()
)

data class JobCardLiteDto(
    @SerializedName("mpeOrderJobCardNum") val mpeOrderJobCardNum: String,
    // CRITICAL: backend field is "technican" (typo) — frozen contract, do NOT rename.
    @SerializedName("technican") val technican: String?
)

// =====================================================================
// Service order — full
// =====================================================================

data class ServiceOrderDto(
    @SerializedName("orderRegNum") val orderRegNum: String,
    @SerializedName("custAccount") val custAccount: String?,
    @SerializedName("custName") val custName: String?,
    @SerializedName("orderDate") val orderDate: String?,
    @SerializedName("orderType") val orderType: Int?,
    @SerializedName("mpeOrderStatus") val mpeOrderStatus: Int?,
    @SerializedName("estimatedHours") val estimatedHours: Double?,
    @SerializedName("deadline") val deadline: String?,
    @SerializedName("machineId") val machineId: Long?,
    @SerializedName("numerSeryjny") val numerSeryjny: String?,
    @SerializedName("isWarranty") val isWarranty: Boolean?,
    @SerializedName("scheduledStart") val scheduledStart: String?,
    @SerializedName("scheduledEnd") val scheduledEnd: String?,
    // null on list endpoints, populated on detail
    @SerializedName("jobCards") val jobCards: List<JobCardDto>? = null
)

data class JobCardDto(
    @SerializedName("mpeOrderJobCardNum") val mpeOrderJobCardNum: String,
    @SerializedName("orderRegNum") val orderRegNum: String?,
    @SerializedName("cardNo") val cardNo: Int?,
    @SerializedName("technican") val technican: String?,
    @SerializedName("machineType") val machineType: String?,
    @SerializedName("closed") val closed: Int?,
    @SerializedName("serviceType") val serviceType: String?,
    @SerializedName("reportedSymptoms") val reportedSymptoms: String?,
    @SerializedName("arrangements") val arrangements: String?,
    @SerializedName("fixLocation") val fixLocation: String?,
    @SerializedName("fuel0") val fuel0: Int?,
    @SerializedName("fuel14") val fuel14: Int?,
    @SerializedName("fuel12") val fuel12: Int?,
    @SerializedName("fuel34") val fuel34: Int?,
    @SerializedName("fuel44") val fuel44: Int?,
    @SerializedName("remarks") val remarks: String?
)

// =====================================================================
// Time entries
// =====================================================================

data class TimeEntryDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("mpeOrderJobCardNum") val mpeOrderJobCardNum: String,
    @SerializedName("technican") val technican: String?,
    @SerializedName("transDate") val transDate: String,
    @SerializedName("timeBegin") val timeBegin: String,
    @SerializedName("timeEnd") val timeEnd: String,
    @SerializedName("kilometers") val kilometers: Double?,
    @SerializedName("travelToMinutes") val travelToMinutes: Int?,
    @SerializedName("travelFromMinutes") val travelFromMinutes: Int?
)

data class CreateTimeEntryRequest(
    // backend overrides from JWT — caller may leave null
    @SerializedName("technican") val technican: String? = null,
    @SerializedName("transDate") val transDate: String,
    @SerializedName("timeBegin") val timeBegin: String,
    @SerializedName("timeEnd") val timeEnd: String,
    @SerializedName("kilometers") val kilometers: Double? = null,
    @SerializedName("travelToMinutes") val travelToMinutes: Int? = null,
    @SerializedName("travelFromMinutes") val travelFromMinutes: Int? = null
)

// =====================================================================
// Activities
// =====================================================================

data class ActivityDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("mpeOrderJobCardNum") val mpeOrderJobCardNum: String,
    @SerializedName("technican") val technican: String?,
    @SerializedName("transDate") val transDate: String,
    @SerializedName("activity") val activity: String,
    @SerializedName("qtyPlan") val qtyPlan: Double?,
    @SerializedName("qtyReal") val qtyReal: Double?
)

data class CreateActivityRequest(
    @SerializedName("technican") val technican: String? = null,
    @SerializedName("transDate") val transDate: String,
    @SerializedName("activity") val activity: String,
    @SerializedName("qtyPlan") val qtyPlan: Double?,
    @SerializedName("qtyReal") val qtyReal: Double?
)

// =====================================================================
// Technicians
// =====================================================================

data class TechnicianDto(
    @SerializedName("technicianId") val technicianId: String,
    @SerializedName("isLead") val isLead: Boolean
)

// =====================================================================
// Order log
// =====================================================================

data class OrderLogEntryDto(
    @SerializedName("orderRegNum") val orderRegNum: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("createdBy") val createdBy: String?,
    @SerializedName("createdDateTime") val createdDateTime: String?
)

// =====================================================================
// Machines
// =====================================================================

data class MachineDto(
    @SerializedName("id") val id: Long,
    @SerializedName("accountNum") val accountNum: String?,
    @SerializedName("marka") val marka: String?,
    @SerializedName("model") val model: String?,
    @SerializedName("numerSeryjny") val numerSeryjny: String?,
    @SerializedName("typMaszyny") val typMaszyny: String?,
    @SerializedName("rokProdukcji") val rokProdukcji: Int?,
    @SerializedName("gwarancjaOd") val gwarancjaOd: String?,
    @SerializedName("gwarancjaDo") val gwarancjaDo: String?,
    @SerializedName("dataSprzedazy") val dataSprzedazy: String?,
    @SerializedName("nrRejestracyjny") val nrRejestracyjny: String?,
    @SerializedName("itemId") val itemId: String?,
    @SerializedName("itemName") val itemName: String?,
    @SerializedName("zrodlo") val zrodlo: String?,
    @SerializedName("uwagi") val uwagi: String?,
    @SerializedName("warrantyStatus") val warrantyStatus: String?,
    @SerializedName("totalOrders") val totalOrders: Int? = null,
    @SerializedName("openOrders") val openOrders: Int? = null,
    @SerializedName("serviceOrders") val serviceOrders: List<MachineHistoryEntryDto>? = null
)

data class MachineHistoryEntryDto(
    @SerializedName("orderRegNum") val orderRegNum: String,
    @SerializedName("orderDate") val orderDate: String?,
    @SerializedName("orderType") val orderType: Int?,
    @SerializedName("mpeOrderStatus") val mpeOrderStatus: Int?,
    @SerializedName("reportedSymptoms") val reportedSymptoms: String?,
    @SerializedName("serviceType") val serviceType: String?,
    @SerializedName("isWarranty") val isWarranty: Boolean?
)

// =====================================================================
// Warranty
// =====================================================================

data class WarrantyCheckDto(
    @SerializedName("machineId") val machineId: Long,
    @SerializedName("gwarancjaOd") val gwarancjaOd: String?,
    @SerializedName("gwarancjaDo") val gwarancjaDo: String?,
    @SerializedName("warrantyStatus") val warrantyStatus: String?,
    @SerializedName("isActive") val isActive: Boolean?
)

// =====================================================================
// Time summary (weekly / monthly)
// =====================================================================

data class TimeSummaryDto(
    @SerializedName("technicianId") val technicianId: String?,
    @SerializedName("totalHours") val totalHours: Double?,
    @SerializedName("totalTravelHours") val totalTravelHours: Double?,
    @SerializedName("totalKilometers") val totalKilometers: Double?,
    @SerializedName("entries") val entries: List<TimeSummaryDayDto>? = null
)

data class TimeSummaryDayDto(
    @SerializedName("date") val date: String?,         // yyyy-MM-dd
    @SerializedName("hours") val hours: Double?,
    @SerializedName("travelHours") val travelHours: Double?,
    @SerializedName("kilometers") val kilometers: Double?
)

// =====================================================================
// Schedules
// =====================================================================

data class ScheduleDto(
    @SerializedName("id") val id: Long,
    @SerializedName("machineId") val machineId: Long?,
    @SerializedName("marka") val marka: String?,
    @SerializedName("model") val model: String?,
    @SerializedName("numerSeryjny") val numerSeryjny: String?,
    @SerializedName("accountNum") val accountNum: String?,
    @SerializedName("scheduleType") val scheduleType: String?,
    @SerializedName("intervalMonths") val intervalMonths: Int?,
    @SerializedName("lastServiceDate") val lastServiceDate: String?,
    @SerializedName("nextServiceDate") val nextServiceDate: String?,
    @SerializedName("orderRegNum") val orderRegNum: String?,
    @SerializedName("scheduleStatus") val scheduleStatus: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("createdBy") val createdBy: String?
)
