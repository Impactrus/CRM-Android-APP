package com.ossadkowski.crm.mobile.ui.sales

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.model.*
import com.ossadkowski.crm.mobile.data.repository.SalesCoverageRepository
import kotlinx.coroutines.launch

class SalesCoverageViewModel : ViewModel() {
    private val repository = SalesCoverageRepository(RetrofitClient.apiService)

    private val _coverageList = MutableLiveData<NetworkResult<GenericPageResponse<SalesCoverageListItem>>>()
    val coverageList: LiveData<NetworkResult<GenericPageResponse<SalesCoverageListItem>>> = _coverageList

    private val _facets = MutableLiveData<NetworkResult<SalesCoverageFacetsResponse>>()
    val facets: LiveData<NetworkResult<SalesCoverageFacetsResponse>> = _facets

    private val _syncResult = MutableLiveData<NetworkResult<Any>>()
    val syncResult: LiveData<NetworkResult<Any>> = _syncResult

    var currentPage = 1
    var pageSize = 20
    var search: String? = null
    var itemId: String? = null
    var periodMonth: String? = null
    var dkz: String? = null
    var risk: String? = null

    private var isLastPage = false
    private var isPageLoading = false
    private val currentItems = mutableListOf<SalesCoverageListItem>()
    private var searchJob: kotlinx.coroutines.Job? = null

    fun loadCoverage(isNextPage: Boolean = false) {
        if (isPageLoading && isNextPage) return
        if (isNextPage && isLastPage) return

        if (!isNextPage) {
            searchJob?.cancel()
            currentPage = 1
            isLastPage = false
            currentItems.clear()
        } else {
            currentPage++
        }

        isPageLoading = true
        searchJob = viewModelScope.launch {
            if (!isNextPage) _coverageList.value = NetworkResult.Loading()

            Log.d("SalesCoverageVM", "Loading sales coverage: page=$currentPage, search=$search, itemId=$itemId, periodMonth=$periodMonth, dkz=$dkz, risk=$risk")
            val result = repository.getSalesCoverage(currentPage, pageSize, search, itemId, periodMonth, dkz, risk)

            if (result is NetworkResult.Success) {
                val newData = result.data?.data ?: emptyList()
                Log.d("SalesCoverageVM", "Success! Received ${newData.size} items. Total=${result.data?.total}")
                currentItems.addAll(newData)

                val total = result.data?.total ?: 0
                isLastPage = currentItems.size >= total

                _coverageList.value = NetworkResult.Success(
                    GenericPageResponse(currentItems.toList(), total, currentPage, pageSize)
                )
            } else {
                Log.e("SalesCoverageVM", "Error loading sales coverage: ${result.message}")
                _coverageList.value = result
            }
            isPageLoading = false
        }
    }

    fun loadFacets() {
        viewModelScope.launch {
            _facets.value = NetworkResult.Loading()
            _facets.value = repository.getSalesCoverageFacets()
        }
    }

    fun syncData() {
        viewModelScope.launch {
            _syncResult.value = NetworkResult.Loading()
            val result = repository.syncSalesCoverage()
            _syncResult.value = result
            if (result is NetworkResult.Success) {
                // Reload list from page 1 on success
                loadCoverage(isNextPage = false)
            }
        }
    }
}
