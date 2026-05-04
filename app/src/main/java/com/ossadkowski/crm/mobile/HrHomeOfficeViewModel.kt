package com.ossadkowski.crm.mobile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.model.HrHomeOfficeLimitDto
import kotlinx.coroutines.launch

class HrHomeOfficeViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    private val _limity = MutableLiveData<NetworkResult<List<HrHomeOfficeLimitDto>>>()
    val limity: LiveData<NetworkResult<List<HrHomeOfficeLimitDto>>> = _limity

    fun loadLimity(rok: Int) {
        _limity.value = NetworkResult.Loading()
        viewModelScope.launch {
            try {
                val list = api.getHrHomeOfficeLimity(rok)
                _limity.value = NetworkResult.Success(list)
            } catch (e: Exception) {
                _limity.value = NetworkResult.Error("Błąd pobierania limitów: ${e.message}")
            }
        }
    }
}
