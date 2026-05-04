package com.ossadkowski.crm.mobile.ui.sales

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.model.*
import com.ossadkowski.crm.mobile.data.repository.TransportRepository
import kotlinx.coroutines.launch

class TransportViewModel : ViewModel() {
    private val repository = TransportRepository(RetrofitClient.apiService)

    private val _transportList = MutableLiveData<NetworkResult<GenericPageResponse<TransportCenyItem>>>()
    val transportList: LiveData<NetworkResult<GenericPageResponse<TransportCenyItem>>> = _transportList

    private val _axContracts = MutableLiveData<NetworkResult<List<TransportAxContract>>>()
    val axContracts: LiveData<NetworkResult<List<TransportAxContract>>> = _axContracts

    private val _createResult = MutableLiveData<NetworkResult<Any>>()
    val createResult: LiveData<NetworkResult<Any>> = _createResult

    var currentPage = 1
    var pageSize = 20
    var search: String? = null

    fun loadTransportList() {
        viewModelScope.launch {
            _transportList.value = NetworkResult.Loading()
            _transportList.value = repository.getTransportList(currentPage, pageSize, search)
        }
    }

    fun searchAxContracts(query: String?) {
        viewModelScope.launch {
            _axContracts.value = NetworkResult.Loading()
            _axContracts.value = repository.searchAxKontrakty(query)
        }
    }

    fun createTransportPrice(request: CreateTransportRequest) {
        viewModelScope.launch {
            _createResult.value = NetworkResult.Loading()
            _createResult.value = repository.createTransportCena(request)
        }
    }
}
