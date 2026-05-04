package com.ossadkowski.crm.mobile.ui.sales

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.model.TowarListItem
import com.ossadkowski.crm.mobile.data.model.TowaryPageResponse
import kotlinx.coroutines.launch

class TowaryViewModel : ViewModel() {
    private val _towary = MutableLiveData<NetworkResult<TowaryPageResponse>>()
    val towary: LiveData<NetworkResult<TowaryPageResponse>> = _towary

    var search: String? = null
    var page = 1
    var pageSize = 15

    fun loadTowary() {
        _towary.value = NetworkResult.Loading()
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.searchTowary(
                    page = page,
                    pageSize = pageSize,
                    search = search ?: ""
                )
                _towary.value = NetworkResult.Success(response)
            } catch (e: Exception) {
                _towary.value = NetworkResult.Error(e.message ?: "Błąd połączenia")
            }
        }
    }
}
