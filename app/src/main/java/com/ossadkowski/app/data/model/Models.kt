package com.ossadkowski.app.data.model

import com.google.gson.annotations.SerializedName

// ── Auth ──
data class LoginRequest(val username: String, val password: String)
data class LoginResponse(
    val token: String?,
    val userId: Int?,
    val role: String?,
    val username: String?,
    val success: Boolean?,
    val message: String?,
    val dzial: String?,
    val employeeCacheId: Int?,
    val claims: Array<String>?,
    val claimsVersion: Int?
)

data class LogoutRequest(val placeholder: String? = null)

data class RefreshTokenResponse(
    val success: Boolean,
    val token: String?
)

data class AuthProfileResponse(
    val userId: Int,
    val username: String?,
    val role: String?,
    val dzial: String?,
    val employeeCacheId: Int?,
    val claims: Array<String>?,
    val claimsVersion: Int?
)

// ── Profile (legacy employee) ──
data class ProfileResponse(
    val name: String?,
    val fname: String?,
    val role: String?,
    val position: String?,
    val department: String?,
    val email: String?,
    val phone: String?
)

// ── Paginated ──
data class PaginatedRequest(
    val page: Int,
    val pageSize: Int,
    val search: String? = null
)

data class PaginatedResponse<T>(
    val items: List<T>,
    val totalCount: Int,
    val totalPages: Int
)

// ── Generic page response (transport-ceny, limity-kredytowe style) ──
data class GenericPageResponse<T>(
    val data: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)

// ── Tasks (dashboard) ──
data class TaskItem(
    val id: Int,
    val title: String?,
    val description: String?,
    val status: String?,
    val assignedTo: String?,
    val createdAt: String?,
    val dueDate: String?
)

// ── Tasks V2 ──
data class TaskListItemDto(
    val id: Int,
    val templateId: Int?,
    val typ: String?,
    val tytul: String?,
    val kontrahentNazwa: String?,
    val termin: String?,
    val assignedToName: String?,
    val assignedTo: Int?,
    val status: String?,
    val isOverdue: Boolean?,
    val createdAt: String?,
    val createdByName: String?
)

data class TaskDetailDto(
    val id: Int,
    val templateId: Int?,
    val typ: String?,
    val tytul: String?,
    val opis: String?,
    val kontrahentNazwa: String?,
    val termin: String?,
    val assignedToName: String?,
    val assignedTo: Int?,
    val status: String?,
    val isOverdue: Boolean?,
    val createdAt: String?,
    val createdByName: String?,
    val createdBy: Int?,
    val totalInstances: Int?,
    val completedInstances: Int?,
    val startedAt: String?,
    val completedAt: String?
)

data class TaskCommentDto(
    val id: Int,
    val userId: Int?,
    val username: String?,
    val tresc: String?,
    val createdAt: String?
)

data class TaskFileDto(
    val id: Int,
    val nazwaPliku: String?,
    val createdAt: String?,
    val uploadedBy: String?
)

data class TaskHistoriaDto(
    val id: Int,
    val username: String?,
    val akcja: String?,
    val staryWartosc: String?,
    val nowyWartosc: String?,
    val szczegoly: String?,
    val createdAt: String?
)

data class TaskObserverDto(
    val id: Int,
    val userId: Int?,
    val username: String?,
    val dzial: String?,
    val addedByName: String?,
    val createdAt: String?
)

data class TaskTypDto(
    val id: Int,
    val kod: String?,
    val nazwa: String?
)

data class TaskListRequest(
    val page: Int,
    val pageSize: Int,
    val search: String? = null,
    val status: String? = null,
    val typ: String? = null
)

data class ChangeTaskStatusRequest(val status: String)
data class AddTaskCommentRequest(val tresc: String)

// ── Wnioski ──
data class WnioskiListRequest(
    val userId: Int,
    val page: Int,
    val pageSize: Int
)

data class WniosekItem(
    val id: Int,
    val username: String?,
    val typ: String?,
    val odDo: String?,
    val godziny: Int?,
    val powod: String?,
    val iloscDni: Int?,
    val status: String?
)

data class WniosekDetailDto(
    val id: Int,
    val userId: Int?,
    val managerId: Int?,
    val hrId: Int?,
    val typ: String?,
    val rodzajUrlopu: String?,
    val odDo: String?,
    val godziny: Int?,
    val powod: String?,
    val iloscDni: Int?,
    val dokumenty: Int?,
    val status: String?,
    val managerApprovedAt: String?,
    val hrApprovedAt: String?,
    val createdAt: String?,
    val zastepstwoUserId: Int?,
    val zastepstwoUsername: String?,
    val komentarzManager: String?,
    val komentarzHr: String?,
    val username: String?
)

data class CreateWniosekRequest(
    val userId: Int,
    val typ: String,
    val rodzajUrlopu: String? = null,
    val odDo: String,
    val godziny: Int? = null,
    val powod: String,
    val iloscDni: Int,
    val dokumenty: Int? = null,
    val zastepstwoUserId: Int? = null
)

data class CreateWniosekResponse(
    val id: Int,
    val message: String?
)

data class SlownikItemDto(
    val id: Int,
    val nazwa: String
)

data class UserIdRequest(val userId: Int)

// ── Approvals ──
data class ApprovalsRequest(
    val userId: Int,
    val page: Int,
    val pageSize: Int,
    val search: String? = null
)

data class ManagerApprovalRequest(
    val managerId: Int,
    val approved: Boolean,
    val komentarz: String? = null,
    val data: String? = null
)

data class HrApprovalRequest(
    val hrId: Int,
    val approved: Boolean,
    val komentarz: String? = null,
    val data: String? = null
)

// ── Limity Kredytowe ──
data class LimitKredytowyListItem(
    val id: Int,
    val userId: Int?,
    val kontrahentAccountNum: String?,
    val kontrahentNazwa: String?,
    val obecnyLimit: Double?,
    val wnioskowanyLimit: Double?,
    val status: String?,
    val axSync: Boolean?,
    val createdAt: String?,
    val createdBy: String?
)

data class LimitKredytowyDetailDto(
    val id: Int,
    val userId: Int?,
    val kontrahentAccountNum: String?,
    val kontrahentNazwa: String?,
    val obecnyLimit: Double?,
    val saldo: Double?,
    val zamowione: Double?,
    val pozostalyKredyt: Double?,
    val wartoscZabezpieczen: Double?,
    val nakladyPoprzedni: Double?,
    val nakladyBiezacy: Double?,
    val przychodyPoprzedni: Double?,
    val przychodyBiezacy: Double?,
    val zadluzeniePrzeterminowane: Double?,
    val wnioskowanyLimit: Double?,
    val terminZabezpieczen: String?,
    val opisZabezpieczen: String?,
    val noweZabezpieczenia: String?,
    val dodatkoweDochody: String?,
    val zobowiazania: String?,
    val uwagi: String?,
    val potwierdzonePrzeterminowane: Boolean?,
    val rozliczeniePlonami: Boolean?,
    val status: String?,
    val approvedBy: Int?,
    val approvedAt: String?,
    val komentarzDecyzja: String?,
    val axSync: Boolean?,
    val axDataSync: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class CreateLimitKredytowyRequest(
    val userId: Int,
    val kontrahentAccountNum: String,
    val wnioskowanyLimit: Double,
    val terminZabezpieczen: String? = null,
    val opisZabezpieczen: String? = null,
    val noweZabezpieczenia: String? = null,
    val dodatkoweDochody: String? = null,
    val zobowiazania: String? = null,
    val uwagi: String? = null,
    val potwierdzonePrzeterminowane: Boolean = false,
    val rozliczeniePlonami: Boolean = false
)

// ── Zamrożenie check ──
data class ZamrozenieCheckResponse(
    val zamrozone: Boolean,
    val dzial: String?,
    val dataOd: String?,
    val dataDo: String?,
    val opis: String?
)

// ── Calendar ──
data class ZamrozenieDto(
    val id: Int,
    val dzial: String?,
    val dataOd: String?,
    val dataDo: String?,
    val opis: String?
)

// ── Kontrahenci search ──
data class KontrahentSearchItem(
    val accountNum: String?,
    val name: String?,
    val address: String?
)
