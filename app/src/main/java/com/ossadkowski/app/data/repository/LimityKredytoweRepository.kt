package com.ossadkowski.app.data.repository

import com.google.gson.reflect.TypeToken
import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.api.ApiService
import com.ossadkowski.app.data.api.RetrofitClient
import com.ossadkowski.app.data.cache.ActionQueue
import com.ossadkowski.app.data.cache.AppDatabase
import com.ossadkowski.app.data.cache.CacheTtl
import com.ossadkowski.app.data.model.*
import com.google.gson.Gson

class LimityKredytoweRepository(
    private val apiService: ApiService = RetrofitClient.apiService,
    private val db: AppDatabase = RetrofitClient.cacheDb,
    private val actionQueue: ActionQueue = ActionQueue(RetrofitClient.cacheDb)
) : BaseRepository() {

    private val gson = Gson()

    suspend fun getList(page: Int, pageSize: Int, status: String?, search: String?, tab: String?): NetworkResult<GenericPageResponse<LimitKredytowyListItem>> {
        val key = "limity_p${page}_s${status ?: ""}_q${search ?: ""}"
        return cachedApiCall(db, key, CacheTtl.MODERATE,
            object : TypeToken<GenericPageResponse<LimitKredytowyListItem>>() {}.type
        ) { apiService.getLimityKredytowe(page, pageSize, status, search, tab) }
    }

    suspend fun getDetail(id: Int): NetworkResult<LimitKredytowyDetailDto> {
        return cachedApiCall(db, "limit_$id", CacheTtl.LIMIT_DETAIL,
            object : TypeToken<LimitKredytowyDetailDto>() {}.type
        ) { apiService.getLimitKredytowyDetail(id) }
    }

    suspend fun create(request: CreateLimitKredytowyRequest): NetworkResult<Any> {
        val result = safeApiCall { apiService.createLimitKredytowy(request) }
        if (result is NetworkResult.Success) {
            db.invalidateByPrefix("limity_")
        } else if (result is NetworkResult.Error) {
            actionQueue.enqueue("create_limit", gson.toJson(request))
            return NetworkResult.Success("queued_offline")
        }
        return result
    }

    suspend fun searchKontrahenci(search: String): NetworkResult<Any> {
        return safeApiCall { apiService.searchKontrahenci(search) }
    }
}
