package com.ossadkowski.crm.mobile.data.repository

import com.ossadkowski.crm.mobile.data.model.*
import com.ossadkowski.crm.mobile.data.api.ApiService
import com.ossadkowski.crm.mobile.data.NetworkResult

class TransportRepository(private val apiService: ApiService) : BaseRepository() {

    suspend fun getTransportList(page: Int, pageSize: Int, search: String? = null): NetworkResult<GenericPageResponse<TransportCenyItem>> {
        return safeApiCall { apiService.getTransportCeny(page, pageSize, search) }
    }

    suspend fun searchAxKontrakty(query: String?): NetworkResult<List<TransportAxContract>> {
        return safeApiCall { apiService.searchTransportAxKontrakty(query) }
    }

    suspend fun createTransportCena(request: CreateTransportRequest): NetworkResult<Any> {
        val response = apiService.createTransportCena(request)
        return if (response.isSuccessful) {
            NetworkResult.Success(response.body() ?: Any())
        } else {
            NetworkResult.Error(response.message())
        }
    }

    suspend fun getTransportPrices(page: Int, pageSize: Int, status: String?, search: String?, tab: String?): NetworkResult<GenericPageResponse<TransportPriceListItem>> {
        return safeApiCall { apiService.getTransportPrices(page, pageSize, status, search, tab) }
    }

    suspend fun getTransportPriceDetail(id: Int): NetworkResult<TransportPriceDetailResponse> {
        return safeApiCall { apiService.getTransportPriceDetail(id) }
    }

    suspend fun reviewTransportPrice(id: Int, request: ReviewTransportPriceRequest): NetworkResult<Any> {
        val response = apiService.reviewTransportPrice(id, request)
        return if (response.isSuccessful) {
            NetworkResult.Success(response.body() ?: Any())
        } else {
            NetworkResult.Error(response.message())
        }
    }
}

