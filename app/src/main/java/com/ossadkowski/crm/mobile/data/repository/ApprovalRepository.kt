package com.ossadkowski.crm.mobile.data.repository

import com.google.gson.reflect.TypeToken
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.ApiService
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.cache.ActionQueue
import com.ossadkowski.crm.mobile.data.cache.ApprovalPayload
import com.ossadkowski.crm.mobile.data.cache.AppDatabase
import com.ossadkowski.crm.mobile.data.cache.CacheTtl
import com.ossadkowski.crm.mobile.data.model.*
import com.google.gson.Gson

class ApprovalRepository(
    private val apiService: ApiService = RetrofitClient.apiService,
    private val db: AppDatabase = RetrofitClient.cacheDb,
    private val actionQueue: ActionQueue = ActionQueue(RetrofitClient.cacheDb)
) : BaseRepository() {

    private val gson = Gson()

    suspend fun getApprovals(userId: Int, page: Int, pageSize: Int, search: String?, role: String? = null): NetworkResult<PaginatedResponse<WniosekItem>> {
        val request = ApprovalsRequest(userId, page, pageSize, search = search, role = role ?: "User")
        return safeApiCall { apiService.getApprovals(request) }
    }

    suspend fun getDetail(id: Int): NetworkResult<WniosekDetailDto> {
        return safeApiCall { apiService.getWniosekDetail(id) }
    }


    suspend fun approveManager(wniosekId: Int, managerId: Int, approved: Boolean): NetworkResult<Any> {
        val result = safeApiCall { apiService.approveManager(wniosekId, ManagerApprovalRequest(managerId, approved)) }
        if (result is NetworkResult.Success) {
            db.invalidateByPrefix("approvals_")
            db.invalidateByPrefix("wnioski_")
            db.invalidateByPrefix("dash_wnioski_")
        } else if (result is NetworkResult.Error) {
            actionQueue.enqueue("approve_manager", gson.toJson(ApprovalPayload(wniosekId, managerId, approved)))
            return NetworkResult.Success("queued_offline")
        }
        return result
    }

    suspend fun approveHr(wniosekId: Int, hrId: Int, approved: Boolean): NetworkResult<Any> {
        val result = safeApiCall { apiService.approveHr(wniosekId, HrApprovalRequest(hrId, approved)) }
        if (result is NetworkResult.Success) {
            db.invalidateByPrefix("approvals_")
            db.invalidateByPrefix("wnioski_")
            db.invalidateByPrefix("dash_wnioski_")
        } else if (result is NetworkResult.Error) {
            actionQueue.enqueue("approve_hr", gson.toJson(ApprovalPayload(wniosekId, hrId, approved)))
            return NetworkResult.Success("queued_offline")
        }
        return result
    }
}
