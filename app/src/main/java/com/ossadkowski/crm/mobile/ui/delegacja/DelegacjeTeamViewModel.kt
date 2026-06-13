package com.ossadkowski.crm.mobile.ui.delegacja

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.DelegacjaListItem
import com.ossadkowski.crm.mobile.data.repository.DelegacjaRepository
import kotlinx.coroutines.launch

class DelegacjeTeamViewModel : ViewModel() {
    private val repository = DelegacjaRepository()

    var delegacje by mutableStateOf<List<DelegacjaListItem>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun loadDelegacje() {
        viewModelScope.launch {
            isLoading = true
            error = null
            when (val result = repository.getManagerTeamDelegacje()) {
                is NetworkResult.Success -> delegacje = result.data ?: emptyList()
                is NetworkResult.Error -> error = result.message
                is NetworkResult.Loading -> { /* no-op */ }
            }
            isLoading = false
        }
    }
}
