package com.ossadkowski.crm.mobile.data.repository

import com.google.gson.reflect.TypeToken
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.ApiService
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.cache.ActionQueue
import com.ossadkowski.crm.mobile.data.cache.AppDatabase
import com.ossadkowski.crm.mobile.data.cache.CacheTtl
import com.ossadkowski.crm.mobile.data.cache.UpdateWniosekPayload
import com.ossadkowski.crm.mobile.data.model.*
import com.google.gson.Gson

class EditRequestRepository(
    private val apiService: ApiService = RetrofitClient.apiService,
    private val db: AppDatabase = RetrofitClient.cacheDb,
    private val actionQueue: ActionQueue = ActionQueue(RetrofitClient.cacheDb)
) : BaseRepository() {

    private val gson = Gson()

    suspend fun getDetail(id: Int): NetworkResult<WniosekDetailDto> {
        return cachedApiCall(db, "wniosek_$id", CacheTtl.MODERATE,
            object : TypeToken<WniosekDetailDto>() {}.type
        ) { apiService.getWniosekDetail(id) }
    }

    suspend fun update(id: Int, request: CreateWniosekRequest): NetworkResult<Any> {
        val result = safeApiCall { apiService.updateWniosek(id, request) }
        if (result is NetworkResult.Success) {
            db.invalidate("wniosek_$id")
            db.invalidateByPrefix("wnioski_")
            db.invalidateByPrefix("dash_wnioski_")
        } else if (result is NetworkResult.Error) {
            actionQueue.enqueue("update_wniosek", gson.toJson(UpdateWniosekPayload(id, gson.toJson(request))))
            return NetworkResult.Success("queued_offline")
        }
        return result
    }
}
