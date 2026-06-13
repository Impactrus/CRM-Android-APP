package com.ossadkowski.crm.mobile.ui.delegacja

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.DelegacjaFinanseItem
import com.ossadkowski.crm.mobile.data.repository.DelegacjaRepository
import kotlinx.coroutines.launch

class DelegacjeRozliczViewModel : ViewModel() {
    private val repository = DelegacjaRepository()

    var items by mutableStateOf<List<DelegacjaFinanseItem>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var selectedStatus by mutableStateOf<String?>("Oczekuje Finansów")
        private set
    var actionResult by mutableStateOf<String?>(null)
        private set

    val statusOptions = listOf(
        null to "Wszystkie",
        "Oczekuje Finansów" to "Oczekuje finansów",
        "Rozliczona" to "Rozliczona",
        "Odrzucona" to "Odrzucona",
        "Do wyjaśnienia" to "Do wyjaśnienia"
    )

    fun setStatus(status: String?) {
        selectedStatus = status
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            isLoading = true
            error = null
            when (val result = repository.getFinansePool(selectedStatus)) {
                is NetworkResult.Success -> items = result.data ?: emptyList()
                is NetworkResult.Error -> error = result.message
                is NetworkResult.Loading -> { /* no-op */ }
            }
            isLoading = false
        }
    }

    fun approveItem(id: Int) {
        viewModelScope.launch {
            when (val result = repository.decyzjaFinanse(id, true)) {
                is NetworkResult.Success -> { actionResult = "Zatwierdzona"; loadItems() }
                is NetworkResult.Error -> actionResult = "Błąd: ${result.message}"
                is NetworkResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun rejectItem(id: Int, powod: String) {
        viewModelScope.launch {
            when (val result = repository.decyzjaFinanse(id, false, powod)) {
                is NetworkResult.Success -> { actionResult = "Odrzucona"; loadItems() }
                is NetworkResult.Error -> actionResult = "Błąd: ${result.message}"
                is NetworkResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun askQuestion(id: Int, pytanie: String) {
        viewModelScope.launch {
            when (val result = repository.doWyjasnienia(id, pytanie)) {
                is NetworkResult.Success -> { actionResult = "Pytanie wysłane"; loadItems() }
                is NetworkResult.Error -> actionResult = "Błąd: ${result.message}"
                is NetworkResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun clearActionResult() { actionResult = null }
}
