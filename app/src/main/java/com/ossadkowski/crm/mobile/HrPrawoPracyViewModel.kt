package com.ossadkowski.crm.mobile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.model.HrPrawoPracyTypDto
import kotlinx.coroutines.launch

class HrPrawoPracyViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    private val _data = MutableLiveData<NetworkResult<List<HrPrawoPracyTypDto>>>()
    val data: LiveData<NetworkResult<List<HrPrawoPracyTypDto>>> = _data

    fun loadAll() {
        _data.value = NetworkResult.Loading()
        viewModelScope.launch {
            try {
                val list = api.getPrawoPracyAll()
                _data.value = NetworkResult.Success(list)
            } catch (e: Exception) {
                _data.value = NetworkResult.Error("Błąd pobierania danych prawa pracy: ${e.message}")
            }
        }
    }
}
