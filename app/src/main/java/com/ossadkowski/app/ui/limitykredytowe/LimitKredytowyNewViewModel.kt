package com.ossadkowski.app.ui.limitykredytowe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.model.*
import com.ossadkowski.app.data.repository.LimityKredytoweRepository
import kotlinx.coroutines.launch

class LimitKredytowyNewViewModel(
    private val repository: LimityKredytoweRepository = LimityKredytoweRepository()
) : ViewModel() {

    private val _createResult = MutableLiveData<NetworkResult<Any>>()
    val createResult: LiveData<NetworkResult<Any>> = _createResult

    private val _kontrahenci = MutableLiveData<NetworkResult<Any>>()
    val kontrahenci: LiveData<NetworkResult<Any>> = _kontrahenci

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
