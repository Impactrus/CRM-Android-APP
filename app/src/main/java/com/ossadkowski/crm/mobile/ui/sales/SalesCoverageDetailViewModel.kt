package com.ossadkowski.crm.mobile.ui.sales

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.model.SalesCoverageDetailResponse
import com.ossadkowski.crm.mobile.data.repository.SalesCoverageRepository
import kotlinx.coroutines.launch

class SalesCoverageDetailViewModel : ViewModel() {
    private val repository = SalesCoverageRepository(RetrofitClient.apiService)

    private val _detail = MutableLiveData<NetworkResult<SalesCoverageDetailResponse>>()
    val detail: LiveData<NetworkResult<SalesCoverageDetailResponse>> = _detail

    fun loadDetail(id: String) {
        viewModelScope.launch {
            _detail.value = NetworkResult.Loading()
            val result = repository.getSalesCoverageDetail(id)
            _detail.value = result
        }
    }
}
