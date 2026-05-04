package com.ossadkowski.crm.mobile.ui.approval

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.*
import com.ossadkowski.crm.mobile.data.repository.ApprovalRepository
import kotlinx.coroutines.launch

class ApprovalViewModel(
    private val repository: ApprovalRepository = ApprovalRepository()
) : ViewModel() {

    private val _approvals = MutableLiveData<NetworkResult<PaginatedResponse<WniosekItem>>>()
    val approvals: LiveData<NetworkResult<PaginatedResponse<WniosekItem>>> = _approvals

    var page = 1
    var search: String? = null

    fun loadApprovals(userId: Int, pageSize: Int = 10) {
        _approvals.value = NetworkResult.Loading()
        viewModelScope.launch {
            _approvals.value = repository.getApprovals(userId, page, pageSize, search)
        }
    }

    fun approve(wniosekId: Int, userId: Int, role: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = if (role == "Manager") {
                repository.approveManager(wniosekId, userId, true)
            } else {
                repository.approveHr(wniosekId, userId, true)
            }
            onResult(result is NetworkResult.Success)
        }
    }

    fun reject(wniosekId: Int, userId: Int, role: String, onResult: (Boolean) -> Unit) {
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
