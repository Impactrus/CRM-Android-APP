package com.ossadkowski.crm.mobile.ui.transport

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.model.ReviewTransportPriceRequest
import com.ossadkowski.crm.mobile.data.model.TransportPriceHistoryItem
import com.ossadkowski.crm.mobile.data.model.TransportPriceListItem
import com.ossadkowski.crm.mobile.data.repository.TransportRepository
import kotlinx.coroutines.launch

class TransportCenyDetailViewModel : ViewModel() {
    private val repository = TransportRepository(RetrofitClient.apiService)

    var requestDetails by mutableStateOf<TransportPriceListItem?>(null)
        private set
    var history by mutableStateOf<List<TransportPriceHistoryItem>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    var isSubmittingReview by mutableStateOf(false)
        private set
    var reviewError by mutableStateOf<String?>(null)
        private set
    var reviewSuccess by mutableStateOf(false)
        private set

    fun loadDetail(id: Int) {
        viewModelScope.launch {
            isLoading = true
            error = null
            when (val result = repository.getTransportPriceDetail(id)) {
                is NetworkResult.Success -> {
                    requestDetails = result.data?.request
                    history = result.data?.history ?: emptyList()
                }
                is NetworkResult.Error -> {
                    error = result.message
                }
                is NetworkResult.Loading -> { /* no-op */ }
            }
            isLoading = false
        }
    }

    fun submitReview(id: Int, approved: Boolean, zatwierdzonyKoszt: Double?, komentarz: String?) {
        viewModelScope.launch {
            isSubmittingReview = true
            reviewError = null
            val req = ReviewTransportPriceRequest(
                approved = approved,
                zatwierdzonyKoszt = zatwierdzonyKoszt,
                komentarz = komentarz
            )
            when (val result = repository.reviewTransportPrice(id, req)) {
                is NetworkResult.Success -> {
                    reviewSuccess = true
                    // reload details
                    loadDetail(id)
                }
                is NetworkResult.Error -> {
                    reviewError = result.message ?: "Błąd podczas zapisywania decyzji"
                }
                is NetworkResult.Loading -> { /* no-op */ }
            }
            isSubmittingReview = false
        }
    }
    
    fun resetReviewState() {
        reviewSuccess = false
        reviewError = null
    }
}
