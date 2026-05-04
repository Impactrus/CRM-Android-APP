package com.ossadkowski.crm.mobile.ui.sales

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.model.KontrahentSearchItem
import kotlinx.coroutines.launch

class KontrahenciViewModel : ViewModel() {

    private val _kontrahenci = MutableLiveData<NetworkResult<List<KontrahentSearchItem>>>()
    val kontrahenci: LiveData<NetworkResult<List<KontrahentSearchItem>>> = _kontrahenci

    var search: String? = null

    fun searchKontrahenci() {
        _kontrahenci.value = NetworkResult.Loading()
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.searchKontrahenci(
                    search = search
                )
                _kontrahenci.value = NetworkResult.Success(response)
            } catch (e: Exception) {
                _kontrahenci.value = NetworkResult.Error(e.message ?: "Wystąpił błąd")
            }
        }
    }
}
