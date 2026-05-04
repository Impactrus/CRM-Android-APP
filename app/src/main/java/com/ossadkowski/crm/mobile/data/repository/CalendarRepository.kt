package com.ossadkowski.crm.mobile.data.repository

import com.google.gson.reflect.TypeToken
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.ApiService
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.cache.AppDatabase
import com.ossadkowski.crm.mobile.data.cache.CacheTtl
import com.ossadkowski.crm.mobile.data.model.*

class CalendarRepository(
    private val apiService: ApiService = RetrofitClient.apiService,
    private val db: AppDatabase = RetrofitClient.cacheDb
) : BaseRepository() {

    suspend fun getZamrozeniaMiesiac(rok: Int, miesiac: Int): NetworkResult<List<ZamrozenieDto>> {
        return cachedApiCall(db, "calendar_${rok}_${miesiac}", CacheTtl.LONG,
            object : TypeToken<List<ZamrozenieDto>>() {}.type
        ) { apiService.getZamrozeniaMiesiac(rok, miesiac) }
    }
}
