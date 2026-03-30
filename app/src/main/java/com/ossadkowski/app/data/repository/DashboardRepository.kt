package com.ossadkowski.app.data.repository

import com.google.gson.reflect.TypeToken
import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.api.ApiService
import com.ossadkowski.app.data.api.RetrofitClient
import com.ossadkowski.app.data.cache.ActionQueue
import com.ossadkowski.app.data.cache.AppDatabase
import com.ossadkowski.app.data.cache.CacheTtl
import com.ossadkowski.app.data.cache.IdUserPayload
import com.ossadkowski.app.data.model.*
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

    suspend fun getTasks(page: Int, pageSize: Int, search: String?): NetworkResult<PaginatedResponse<TaskItem>> {
        val key = "dash_tasks_p${page}_q${search ?: ""}"
        return cachedApiCall(db, key, CacheTtl.SHORT,
            object : TypeToken<PaginatedResponse<TaskItem>>() {}.type
        ) { apiService.getTasks(PaginatedRequest(page, pageSize, search)) }
    }

    suspend fun getWnioski(userId: Int, page: Int, pageSize: Int): NetworkResult<PaginatedResponse<WniosekItem>> {
        return safeApiCall { apiService.getWnioski(WnioskiListRequest(userId, page, pageSize)) }
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

    suspend fun logout(): NetworkResult<Any> {
        return safeApiCall {
            try { apiService.logout() } catch (_: Exception) { }
        }
    }
}
