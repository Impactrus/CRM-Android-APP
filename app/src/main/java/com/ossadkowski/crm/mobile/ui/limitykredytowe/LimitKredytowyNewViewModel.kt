package com.ossadkowski.crm.mobile.ui.limitykredytowe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.*
import com.ossadkowski.crm.mobile.data.repository.LimityKredytoweRepository
import kotlinx.coroutines.launch

class LimitKredytowyNewViewModel(
    private val repository: LimityKredytoweRepository = LimityKredytoweRepository()
) : ViewModel() {

    private val _createResult = MutableLiveData<NetworkResult<Any>>()
    val createResult: LiveData<NetworkResult<Any>> = _createResult

    private val _kontrahenci = MutableLiveData<NetworkResult<List<KontrahentSearchItem>>>()
    val kontrahenci: LiveData<NetworkResult<List<KontrahentSearchItem>>> = _kontrahenci

    fun searchKontrahenci(query: String) {
        viewModelScope.launch {
            _kontrahenci.value = repository.searchKontrahenci(query)
        }
    }

    fun create(request: CreateLimitKredytowyRequest) {
        _createResult.value = NetworkResult.Loading()
        viewModelScope.launch {
            _createResult.value = repository.create(request)
        }
    }
}
