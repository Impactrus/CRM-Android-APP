package com.ossadkowski.crm.mobile.ui.limitykredytowe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.LimitKredytowyDetailDto
import com.ossadkowski.crm.mobile.data.repository.LimityKredytoweRepository
import kotlinx.coroutines.launch

class LimitKredytowyDetailViewModel(
    private val repository: LimityKredytoweRepository = LimityKredytoweRepository()
) : ViewModel() {

    private val _detail = MutableLiveData<NetworkResult<LimitKredytowyDetailDto>>()
    val detail: LiveData<NetworkResult<LimitKredytowyDetailDto>> = _detail

    fun loadDetail(id: Int) {
        _detail.value = NetworkResult.Loading()
        viewModelScope.launch {
            _detail.value = repository.getDetail(id)
        }
    }
}
