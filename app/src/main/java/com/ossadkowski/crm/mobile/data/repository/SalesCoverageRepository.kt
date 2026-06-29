package com.ossadkowski.crm.mobile.data.repository

import com.ossadkowski.crm.mobile.data.model.*
import com.ossadkowski.crm.mobile.data.api.ApiService
import com.ossadkowski.crm.mobile.data.NetworkResult
import retrofit2.Response

class SalesCoverageRepository(private val apiService: ApiService) : BaseRepository() {

    suspend fun getSalesCoverage(
        page: Int,
        pageSize: Int,
        search: String? = null,
        itemId: String? = null,
        periodMonth: String? = null,
        dkz: String? = null,
        risk: String? = null
    ): NetworkResult<GenericPageResponse<SalesCoverageListItem>> {
        return safeApiCall {
            apiService.getSalesCoverage(page, pageSize, search, itemId, periodMonth, dkz, risk)
        }
    }

    suspend fun getSalesCoverageFacets(): NetworkResult<SalesCoverageFacetsResponse> {
        return safeApiCall {
            apiService.getSalesCoverageFacets()
        }
    }

    suspend fun syncSalesCoverage(): NetworkResult<Any> {
        return try {
            val response = apiService.syncSalesCoverage()
            if (response.isSuccessful) {
                NetworkResult.Success(response.body() ?: Any())
            } else {
                NetworkResult.Error(response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    suspend fun getSalesCoverageDetail(id: String): NetworkResult<SalesCoverageDetailResponse> {
        return safeApiCall {
            apiService.getSalesCoverageDetail(id)
        }
    }
}
