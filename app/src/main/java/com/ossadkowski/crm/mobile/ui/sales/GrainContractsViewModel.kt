package com.ossadkowski.crm.mobile.ui.sales

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.model.*
import com.ossadkowski.crm.mobile.data.repository.GrainContractsRepository
import kotlinx.coroutines.launch

class GrainContractsViewModel : ViewModel() {
    private val repository = GrainContractsRepository(RetrofitClient.apiService)

    private val _contractsList = MutableLiveData<NetworkResult<GenericPageResponse<GrainContractListItem>>>()
    val contractsList: LiveData<NetworkResult<GenericPageResponse<GrainContractListItem>>> = _contractsList

    private val _contractDetail = MutableLiveData<NetworkResult<GrainContractDetail>>()
    val contractDetail: LiveData<NetworkResult<GrainContractDetail>> = _contractDetail

    private val _paymentTerms = MutableLiveData<NetworkResult<List<PaymentTerm>>>()
    val paymentTerms: LiveData<NetworkResult<List<PaymentTerm>>> = _paymentTerms

    private val _createResult = MutableLiveData<NetworkResult<Any>>()
    val createResult: LiveData<NetworkResult<Any>> = _createResult

    var currentPage = 1
    var pageSize = 15
    var search: String? = null
    var status: String? = null
    var dateFrom: String? = null
    var dateTo: String? = null
    var tab: String = "mine"

    private var isLastPage = false
    private var isPageLoading = false
    private val currentItems = mutableListOf<GrainContractListItem>()
    private var searchJob: kotlinx.coroutines.Job? = null

    fun loadContracts(isNextPage: Boolean = false) {
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
            if (!isNextPage) _contractsList.value = NetworkResult.Loading()
            
            Log.d("GrainVM", "Loading contracts: page=$currentPage, tab=$tab, search=$search")
            val result = repository.getGrainContracts(currentPage, pageSize, tab, search, status, dateFrom, dateTo)
            
            if (result is NetworkResult.Success) {
                val newData = result.data?.data ?: emptyList()
                Log.d("GrainVM", "Success! Received ${newData.size} items. Total=${result.data?.total}")
                currentItems.addAll(newData)
                
                // Sprawdź czy to ostatnia strona
                val total = result.data?.total ?: 0
                isLastPage = currentItems.size >= total
                
                // Wysyłamy KOPIĘ listy (.toList()), aby uniknąć problemów z synchronizacją w RecyclerView
                _contractsList.value = NetworkResult.Success(GenericPageResponse(currentItems.toList(), total, currentPage, pageSize))
            } else {
                Log.e("GrainVM", "Error loading contracts: ${result.message}")
                _contractsList.value = result
            }
            isPageLoading = false
        }
    }

    fun loadContractDetail(id: Int) {
        viewModelScope.launch {
            _contractDetail.value = NetworkResult.Loading()
            _contractDetail.value = repository.getGrainContract(id)
        }
    }

    fun loadPaymentTerms() {
        viewModelScope.launch {
            _paymentTerms.value = NetworkResult.Loading()
            _paymentTerms.value = repository.getPaymentTerms()
        }
    }

    fun createContract(request: CreateGrainContractRequest) {
        viewModelScope.launch {
            _createResult.value = NetworkResult.Loading()
            _createResult.value = repository.createGrainContract(request)
        }
    }
}
