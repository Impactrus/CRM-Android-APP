package com.ossadkowski.crm.mobile.data.model

import com.google.gson.annotations.SerializedName

data class VacationBalanceDto(
    @SerializedName("vacDays") val vacDays: Int,
    @SerializedName("prevLimitD") val prevLimitD: Int,
    @SerializedName("limitConsD") val limitConsD: Int,
    @SerializedName("restLimitD") val restLimitD: Int,
    @SerializedName("plannedDays") val plannedDays: Int? = null
)

data class VacationFreezeDto(
    @SerializedName("dataOd") val dataOd: String, // YYYY-MM-DD
    @SerializedName("dataDo") val dataDo: String, // YYYY-MM-DD
    @SerializedName("opis") val opis: String?,
    @SerializedName("dzial") val dzial: String?
)

data class VacationPlanDto(
    @SerializedName("plannedDates") val plannedDates: List<String>,
    @SerializedName("freezes") val freezes: List<VacationFreezeDto>,
    @SerializedName("balance") val balance: VacationBalanceDto?
)

data class VacationSubmissionDto(
    @SerializedName("current") val current: VacationSubmissionCurrentDto?,
    @SerializedName("history") val history: List<VacationHistoryEntryDto>?
)

data class VacationSubmissionCurrentDto(
    @SerializedName("status") val status: String, // draft, submitted, approved, rejected, revoked
    @SerializedName("rejectReason") val rejectReason: String?
)

data class VacationHistoryEntryDto(
    @SerializedName("id") val id: Int,
    @SerializedName("status") val status: String,
    @SerializedName("submittedAt") val submittedAt: String?,
    @SerializedName("decidedAt") val decidedAt: String?,
    @SerializedName("submittedDays") val submittedDays: Int,
    @SerializedName("rejectReason") val rejectReason: String?
)

data class BulkSaveVacationRequest(
    @SerializedName("year") val year: Int,
    @SerializedName("addDates") val addDates: List<String>,
    @SerializedName("removeDates") val removeDates: List<String>
)

data class DecideVacationRequest(
    @SerializedName("approve") val approve: Boolean,
    @SerializedName("rejectReason") val rejectReason: String?
)

data class TeamVacationDayDto(
    @SerializedName("planDate") val planDate: String,
    @SerializedName("status") val status: String // planned, submitted
)

data class TeamEmployeePlanDto(
    @SerializedName("userId") val userId: String,
    @SerializedName("fname") val fname: String,
    @SerializedName("name") val name: String,
    @SerializedName("depart") val depart: String?,
    @SerializedName("days") val days: List<TeamVacationDayDto>
)

data class TeamVacationPlansResponse(
    @SerializedName("items") val items: List<TeamEmployeePlanDto>
)
