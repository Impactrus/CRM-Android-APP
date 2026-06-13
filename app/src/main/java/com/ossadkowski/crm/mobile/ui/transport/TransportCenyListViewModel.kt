package com.ossadkowski.crm.mobile.ui.transport

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.model.TransportPriceListItem
import com.ossadkowski.crm.mobile.data.repository.TransportRepository
import kotlinx.coroutines.launch

class TransportCenyListViewModel : ViewModel() {
    private val repository = TransportRepository(RetrofitClient.apiService)

    var items by mutableStateOf<List<TransportPriceListItem>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    var currentTab by mutableStateOf("all") // "all" or "mine"
    var searchStatus by mutableStateOf<String?>(null)
    var searchQuery by mutableStateOf("")
    var currentPage by mutableIntStateOf(1)
        private set
    var totalPages by mutableIntStateOf(1)
        private set

    fun loadTransportList() {
        viewModelScope.launch {
            isLoading = true
            error = null
            val statusParam = if (searchStatus == "Wszystkie") null else searchStatus
            val queryParam = if (searchQuery.isBlank()) null else searchQuery
            val tabParam = if (currentTab == "mine") "mine" else null

            when (val result = repository.getTransportPrices(currentPage, 20, statusParam, queryParam, tabParam)) {
                is NetworkResult.Success -> {
                    items = result.data?.data ?: emptyList()
                    val total = result.data?.total ?: 0
                    totalPages = maxOf(1, kotlin.math.ceil(total.toDouble() / 20.0).toInt())
                }
                is NetworkResult.Error -> {
                    error = result.message
                }
                is NetworkResult.Loading -> { /* no-op */ }
            }
            isLoading = false
        }
    }

    fun setTab(tab: String) {
        currentTab = tab
        currentPage = 1
        loadTransportList()
    }

    fun setStatus(status: String?) {
        searchStatus = status
        currentPage = 1
        loadTransportList()
    }

    fun setSearch(query: String) {
        searchQuery = query
        currentPage = 1
        loadTransportList()
    }

    fun nextPage() {
        if (currentPage < totalPages) {
            currentPage++
            loadTransportList()
        }
    }

    fun prevPage() {
        if (currentPage > 1) {
            currentPage--
            loadTransportList()
        }
    }
}
