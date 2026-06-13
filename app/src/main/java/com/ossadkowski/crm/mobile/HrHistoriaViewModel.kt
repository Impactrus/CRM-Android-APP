package com.ossadkowski.crm.mobile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.model.PaginatedResponse
import com.ossadkowski.crm.mobile.data.model.SlownikItemDto
import com.ossadkowski.crm.mobile.data.model.WniosekItem
import com.ossadkowski.crm.mobile.data.model.WniosekHistoryItem
import com.ossadkowski.crm.mobile.data.model.WnioskiListRequest
import kotlinx.coroutines.launch

class HrHistoriaViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    private val _users = MutableLiveData<NetworkResult<List<SlownikItemDto>>>()
    val users: LiveData<NetworkResult<List<SlownikItemDto>>> = _users

    private val _history = MutableLiveData<NetworkResult<List<WniosekHistoryItem>>>()
    val history: LiveData<NetworkResult<List<WniosekHistoryItem>>> = _history

    // Caching all users for local filtering
    private var allUsers: List<SlownikItemDto> = emptyList()
    
    // Caching all history items for status filtering
    private var allHistory: List<WniosekHistoryItem> = emptyList()

    fun loadUsers() {
        _users.value = NetworkResult.Loading()
        viewModelScope.launch {
            try {
                val rawEmployees = api.getHrEmployees()
                val list = rawEmployees.map { emp ->
                    val fullName = listOfNotNull(emp.fname, emp.name)
                        .joinToString(" ")
                        .trim()
                    SlownikItemDto(
                        id = emp.userId,
                        nazwa = fullName,
                        opis = emp.workpost
                    )
                }
                allUsers = list
                _users.value = NetworkResult.Success(list)
            } catch (e: Exception) {
                _users.value = NetworkResult.Error("Błąd pobierania pracowników: ${e.message}")
            }
        }
    }

    fun filterUsers(query: String) {
        if (query.isBlank()) {
            _users.value = NetworkResult.Success(allUsers)
            return
        }
        val lowerQuery = query.lowercase()
        val filtered = allUsers.filter { it.nazwa.lowercase().contains(lowerQuery) }
        _users.value = NetworkResult.Success(filtered)
    }

    fun loadHistoryForUser(userId: Int) {
        _history.value = NetworkResult.Loading()
        viewModelScope.launch {
            try {
                val response = api.getWnioskiHistory(userId)
                allHistory = response
                _history.value = NetworkResult.Success(response)
            } catch (e: Exception) {
                _history.value = NetworkResult.Error("Błąd pobierania historii: ${e.message}")
            }
        }
    }
    
    fun filterHistoryByStatus(status: String) {
        if (allHistory.isEmpty()) return
        
        if (status == "Wszystkie") {
            _history.value = NetworkResult.Success(allHistory)
            return
        }
        
        val filtered = allHistory.filter { 
            it.status?.equals(status, ignoreCase = true) == true ||
            (status == "Szkice" && it.status?.lowercase() == "szkic") ||
            (status == "Zaakceptowane" && (it.status?.lowercase() == "zaakceptowane" || it.status?.lowercase() == "zatwierdzone")) ||
            (status == "Odrzucone" && (it.status?.lowercase() == "odrzucone" || it.status?.lowercase() == "anulowane"))
        }
        _history.value = NetworkResult.Success(filtered)
    }
}
