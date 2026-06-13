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

class DelegacjeZaliczkiViewModel : ViewModel() {
    private val repository = DelegacjaRepository()

    var items by mutableStateOf<List<DelegacjaFinanseItem>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var selectedTab by mutableStateOf(0)
        private set
    var actionResult by mutableStateOf<String?>(null)
        private set

    val tabs = listOf("Do wypłaty", "Wypłacone", "Do zwrotu")

    private val tabStatusMap = mapOf(
        0 to "Oczekuje Finansów",
        1 to "Rozliczona",
        2 to null
    )

    fun selectTab(index: Int) {
        selectedTab = index
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            isLoading = true
            error = null
            val status = tabStatusMap[selectedTab]
            when (val result = repository.getFinansePool(status)) {
                is NetworkResult.Success -> {
                    val data = result.data ?: emptyList()
                    items = when (selectedTab) {
                        0 -> data.filter { (it.zaliczkaKwota ?: 0.0) > 0 }
                        2 -> data.filter {
                            val zaliczka = it.zaliczkaKwota ?: 0.0
                            val ogolem = it.ogolem ?: 0.0
                            zaliczka > 0 && ogolem < zaliczka
                        }
                        else -> data
                    }
                }
                is NetworkResult.Error -> error = result.message
                is NetworkResult.Loading -> { /* no-op */ }
            }
            isLoading = false
        }
    }

    fun zaliczkaWyplacona(id: Int) {
        viewModelScope.launch {
            when (val result = repository.zaliczkaWyplacona(id)) {
                is NetworkResult.Success -> { actionResult = "Zaliczka wypłacona"; loadItems() }
                is NetworkResult.Error -> actionResult = "Błąd: ${result.message}"
                is NetworkResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun clearActionResult() { actionResult = null }
}
