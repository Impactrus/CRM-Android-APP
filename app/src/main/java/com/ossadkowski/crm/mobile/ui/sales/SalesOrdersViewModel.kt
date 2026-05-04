package com.ossadkowski.crm.mobile.ui.sales

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.model.GenericPageResponse
import com.ossadkowski.crm.mobile.data.model.SalesOrderListItem
import kotlinx.coroutines.launch

class SalesOrdersViewModel : ViewModel() {

    private val _orders = MutableLiveData<NetworkResult<GenericPageResponse<SalesOrderListItem>>>()
    val orders: LiveData<NetworkResult<GenericPageResponse<SalesOrderListItem>>> = _orders

    var page = 1
    var pageSize = 15
    var search: String? = null
    var status: String? = null

    fun loadOrders() {
        _orders.value = NetworkResult.Loading()
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getSalesOrders(page, pageSize, search, status)
                _orders.postValue(NetworkResult.Success(response))
            } catch (e: Exception) {
                _orders.postValue(NetworkResult.Error(e.message ?: "Błąd pobierania zamówień"))
            }
        }
    }
}
