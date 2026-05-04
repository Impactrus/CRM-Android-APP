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
import com.ossadkowski.crm.mobile.data.model.WnioskiListRequest
import kotlinx.coroutines.launch

class HrHistoriaViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    private val _users = MutableLiveData<NetworkResult<List<SlownikItemDto>>>()
    val users: LiveData<NetworkResult<List<SlownikItemDto>>> = _users

    private val _history = MutableLiveData<NetworkResult<PaginatedResponse<WniosekItem>>>()
    val history: LiveData<NetworkResult<PaginatedResponse<WniosekItem>>> = _history

    // Caching all users for local filtering
    private var allUsers: List<SlownikItemDto> = emptyList()

    fun loadUsers() {
        _users.value = NetworkResult.Loading()
        viewModelScope.launch {
            try {
                val list = api.getWnioskiUzytkownicy()
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

    fun loadHistoryForUser(userId: Int, page: Int = 1) {
        _history.value = NetworkResult.Loading()
        viewModelScope.launch {
            try {
                val req = WnioskiListRequest(userId = userId, page = page, pageSize = 50)
                val response = api.getWnioski(req)
                _history.value = NetworkResult.Success(response)
            } catch (e: Exception) {
                _history.value = NetworkResult.Error("Błąd pobierania historii: ${e.message}")
            }
        }
    }
}
