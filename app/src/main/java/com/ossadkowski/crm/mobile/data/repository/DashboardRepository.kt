package com.ossadkowski.crm.mobile.data.repository

import com.google.gson.reflect.TypeToken
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.ApiService
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.cache.ActionQueue
import com.ossadkowski.crm.mobile.data.cache.AppDatabase
import com.ossadkowski.crm.mobile.data.cache.CacheTtl
import com.ossadkowski.crm.mobile.data.cache.IdUserPayload
import com.ossadkowski.crm.mobile.data.model.*
import com.google.gson.Gson

class DashboardRepository(
    private val apiService: ApiService = RetrofitClient.apiService,
    private val db: AppDatabase = RetrofitClient.cacheDb,
    private val actionQueue: ActionQueue = ActionQueue(RetrofitClient.cacheDb)
) : BaseRepository() {

    private val gson = Gson()

    suspend fun getAuthProfile(): NetworkResult<AuthProfileResponse> {
        return cachedApiCall(db, "profile", CacheTtl.PROFILE,
            object : TypeToken<AuthProfileResponse>() {}.type
        ) { apiService.getAuthProfile() }
    }

    suspend fun getProfile(userId: Int): NetworkResult<ProfileResponse> {
        return cachedApiCall(db, "employee_profile_$userId", CacheTtl.PROFILE,
            object : TypeToken<ProfileResponse>() {}.type
        ) { apiService.getProfile(userId) }
    }

    suspend fun getTasks(page: Int, pageSize: Int, scope: String = "moje", userId: Int?, status: String?): NetworkResult<PaginatedResponse<TaskItem>> {
        // Serwer ma błąd 500 na widoku listy, ale Board działa (view=board).
        // Pobieramy dane z Boarda i robimy z nich listę jako workaround.
        val boardResult = safeApiCall { apiService.getBoardTasks(view = "board", scope = scope) }
        
        return when (boardResult) {
            is NetworkResult.Success -> {
                val allTasks = mutableListOf<TaskItem>()
                boardResult.data?.columns?.values?.forEach { column ->
                    column.items.forEach { task ->
                        allTasks.add(TaskItem(
                            id = task.id,
                            title = task.tytul,
                            description = task.kontrahentNazwa ?: "",
                            status = task.status,
                            assignedTo = task.assignedToName,
                            createdAt = task.createdAt,
                            dueDate = task.termin
                        ))
                    }
                }
                // Sortujemy po ID malejąco (najnowsze na górze)
                val sortedTasks = allTasks.distinctBy { it.id }.sortedByDescending { it.id }
                NetworkResult.Success(PaginatedResponse(sortedTasks, sortedTasks.size, 1))
            }
            is NetworkResult.Error -> NetworkResult.Error(boardResult.message ?: "Błąd pobierania zadań")
            else -> NetworkResult.Loading()
        }
    }

    suspend fun getWnioski(userId: Int, page: Int, pageSize: Int): NetworkResult<PaginatedResponse<WniosekItem>> {
        val result = safeApiCall { apiService.getWnioski(WnioskiListRequest(userId, page, pageSize)) }
        if (result is NetworkResult.Success) {
            val data = result.data
            if (data != null) {
                val sortedList = data.items.sortedByDescending { it.id }
                return NetworkResult.Success(
                    PaginatedResponse(sortedList, data.totalCount, data.totalPages)
                )
            }
        }
        return result
    }

    suspend fun sendWniosek(wniosekId: Int, userId: Int): NetworkResult<Any> {
        val result = safeApiCall { apiService.sendWniosek(wniosekId, UserIdRequest(userId)) }
        if (result is NetworkResult.Success) {
            db.invalidateByPrefix("wnioski_")
            db.invalidateByPrefix("dash_wnioski_")
        } else if (result is NetworkResult.Error) {
            actionQueue.enqueue("send_wniosek", gson.toJson(IdUserPayload(wniosekId, userId)))
            return NetworkResult.Success("queued_offline")
        }
        return result
    }

    suspend fun resubmitWniosek(wniosekId: Int, userId: Int): NetworkResult<Any> {
        val result = safeApiCall { apiService.resubmitWniosek(wniosekId, UserIdRequest(userId)) }
        if (result is NetworkResult.Success) {
            db.invalidateByPrefix("wnioski_")
            db.invalidateByPrefix("dash_wnioski_")
        } else if (result is NetworkResult.Error) {
            actionQueue.enqueue("resubmit_wniosek", gson.toJson(IdUserPayload(wniosekId, userId)))
            return NetworkResult.Success("queued_offline")
        }
        return result
    }

    suspend fun deleteWniosek(wniosekId: Int, userId: Int): NetworkResult<Any> {
        val result = safeApiCall { apiService.deleteWniosek(wniosekId, userId) }
        if (result is NetworkResult.Success) {
            db.invalidateByPrefix("wnioski_")
            db.invalidateByPrefix("dash_wnioski_")
        } else if (result is NetworkResult.Error) {
            actionQueue.enqueue("delete_wniosek", gson.toJson(IdUserPayload(wniosekId, userId)))
            return NetworkResult.Success("queued_offline")
        }
        return result
    }

    suspend fun getPrawoPracySaldo(): NetworkResult<List<PrawoPracySaldoDto>> {
        return cachedApiCall(db, "prawo_pracy_saldo", CacheTtl.SHORT,
            object : TypeToken<List<PrawoPracySaldoDto>>() {}.type
        ) { apiService.getPrawoPracySaldo() }
    }

    suspend fun getVacationSummary(): NetworkResult<VacationSummaryDto> {
        return safeApiCall { apiService.getVacationSummary() }
    }

    suspend fun getHomeOfficeSaldo(): NetworkResult<HomeOfficeSaldoDto> {
        return safeApiCall { apiService.getHomeOfficeSaldo() }
    }

    suspend fun getOvertimeSaldo(): NetworkResult<List<OvertimeSaldoDto>> {
        return safeApiCall { apiService.getOvertimeSaldo() }
    }

    suspend fun logout(): NetworkResult<Any> {
        return safeApiCall {
            try { apiService.logout() } catch (_: Exception) { }
        }
    }
    suspend fun getApprovals(userId: Int, page: Int, pageSize: Int, role: String? = null): NetworkResult<PaginatedResponse<WniosekItem>> {
        val request = ApprovalsRequest(userId, page, pageSize, role = role ?: "User")
        val result = safeApiCall { apiService.getApprovals(request) }
        if (result is NetworkResult.Error) {
            android.util.Log.e("DASH_DEBUG", "API Approvals Error: ${result.message}")
        }
        return result
    }

    suspend fun getZastepstwa(status: String): NetworkResult<List<WniosekItem>> {
        val result = safeApiCall { apiService.getMojeZastepstwa(status) }
        if (result is NetworkResult.Error) {
            android.util.Log.e("DASH_DEBUG", "API Zastepstwa Error: ${result.message}")
        }
        return result
    }

    suspend fun createPoleceniePracy(request: CreatePoleceniePracyRequest): NetworkResult<CreateWniosekResponse> {
        return safeApiCall { apiService.createPoleceniePracy(request) }
    }

    suspend fun getWnioskiUzytkownicy(): NetworkResult<List<SlownikItemDto>> {
        return safeApiCall { apiService.getWnioskiUzytkownicy() }
    }

    suspend fun getBoardTasks(scope: String = "moje"): NetworkResult<BoardResponse> {
        return safeApiCall { apiService.getBoardTasks(scope = scope) }
    }

    suspend fun getConversations(): NetworkResult<ConversationResponse> {
        return safeApiCall { apiService.getConversations() }
    }
}

