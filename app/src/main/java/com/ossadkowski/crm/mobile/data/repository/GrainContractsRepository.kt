package com.ossadkowski.crm.mobile.data.repository

import com.ossadkowski.crm.mobile.data.model.*
import com.ossadkowski.crm.mobile.data.api.ApiService
import com.ossadkowski.crm.mobile.data.NetworkResult

class GrainContractsRepository(private val apiService: ApiService) : BaseRepository() {

    suspend fun getGrainContracts(
        page: Int,
        pageSize: Int,
        tab: String = "mine",
        search: String? = null,
        status: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ): NetworkResult<GenericPageResponse<GrainContractListItem>> {
        return safeApiCall { apiService.getGrainContracts(page, pageSize, tab, search, status, dateFrom, dateTo) }
    }

    suspend fun getGrainContract(id: Int): NetworkResult<GrainContractDetail> {
        return safeApiCall { apiService.getGrainContract(id) }
    }

    suspend fun getPaymentTerms(): NetworkResult<List<PaymentTerm>> {
        return safeApiCall { apiService.getPaymentTerms() }
    }

    suspend fun createGrainContract(request: CreateGrainContractRequest): NetworkResult<Any> {
        val response = apiService.createGrainContract(request)
        return if (response.isSuccessful) {
            NetworkResult.Success(response.body() ?: Any())
        } else {
            NetworkResult.Error(response.message())
        }
    }
}
