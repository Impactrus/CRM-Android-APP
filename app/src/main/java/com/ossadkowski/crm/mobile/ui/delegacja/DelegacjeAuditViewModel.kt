package com.ossadkowski.crm.mobile.ui.delegacja

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.DelegacjaAuditItem
import com.ossadkowski.crm.mobile.data.model.DelegacjaAuditDetail
import com.ossadkowski.crm.mobile.data.repository.DelegacjaRepository
import kotlinx.coroutines.launch

class DelegacjeAuditViewModel : ViewModel() {
    private val repository = DelegacjaRepository()

    var delegacje by mutableStateOf<List<DelegacjaAuditItem>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var selectedId by mutableStateOf<Int?>(null)
        private set
    var auditDetail by mutableStateOf<DelegacjaAuditDetail?>(null)
        private set
    var detailLoading by mutableStateOf(false)
        private set
    var detailError by mutableStateOf<String?>(null)
        private set

    fun loadList() {
        viewModelScope.launch {
            isLoading = true
            error = null
            when (val result = repository.getHrDelegacjeList()) {
                is NetworkResult.Success -> delegacje = result.data ?: emptyList()
                is NetworkResult.Error -> error = result.message
                is NetworkResult.Loading -> { /* no-op */ }
            }
            isLoading = false
        }
    }

    fun selectDelegacja(id: Int) {
        selectedId = id
        auditDetail = null
        detailError = null
        viewModelScope.launch {
            detailLoading = true
            when (val result = repository.getHrAudit(id)) {
                is NetworkResult.Success -> auditDetail = result.data
                is NetworkResult.Error -> detailError = result.message
                is NetworkResult.Loading -> { /* no-op */ }
            }
            detailLoading = false
        }
    }

    fun clearSelection() {
        selectedId = null
        auditDetail = null
        detailError = null
    }
}
