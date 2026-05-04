package com.ossadkowski.crm.mobile.ui.approval

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.WniosekDetailDto
import com.ossadkowski.crm.mobile.data.repository.ApprovalRepository
import kotlinx.coroutines.launch

class ApprovalDetailViewModel(
    private val repository: ApprovalRepository = ApprovalRepository()
) : ViewModel() {

    private val _detail = MutableLiveData<NetworkResult<WniosekDetailDto>>()
    val detail: LiveData<NetworkResult<WniosekDetailDto>> = _detail

    fun loadDetail(id: Int) {
        _detail.value = NetworkResult.Loading()
        viewModelScope.launch {
            _detail.value = repository.getDetail(id)
        }
    }

    fun approve(wniosekId: Int, userId: Int, role: String, komentarz: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = if (role == "Manager") {
                repository.approveManager(wniosekId, userId, true)
            } else {
                repository.approveHr(wniosekId, userId, true)
            }
            onResult(result is NetworkResult.Success)
        }
    }

    fun reject(wniosekId: Int, userId: Int, role: String, komentarz: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = if (role == "Manager") {
                repository.approveManager(wniosekId, userId, false)
            } else {
                repository.approveHr(wniosekId, userId, false)
            }
            onResult(result is NetworkResult.Success)
        }
    }
}
