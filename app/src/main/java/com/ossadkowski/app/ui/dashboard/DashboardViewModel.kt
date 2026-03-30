package com.ossadkowski.app.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.model.*
import com.ossadkowski.app.data.repository.DashboardRepository
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: DashboardRepository = DashboardRepository()
) : ViewModel() {

    // Profile (now from /auth/profile)
    private val _profile = MutableLiveData<NetworkResult<AuthProfileResponse>>()
    val profile: LiveData<NetworkResult<AuthProfileResponse>> = _profile

    // Tasks
    private val _tasks = MutableLiveData<NetworkResult<PaginatedResponse<TaskItem>>>()
    val tasks: LiveData<NetworkResult<PaginatedResponse<TaskItem>>> = _tasks
    var tasksPage = 1
    var tasksSearch: String? = null

    // Wnioski
    private val _wnioski = MutableLiveData<NetworkResult<PaginatedResponse<WniosekItem>>>()
    val wnioski: LiveData<NetworkResult<PaginatedResponse<WniosekItem>>> = _wnioski
    var wnioskiPage = 1

    fun loadProfile() {
        viewModelScope.launch {
            _profile.value = repository.getAuthProfile()
        }
    }

    fun loadTasks(pageSize: Int = 10) {
        _tasks.value = NetworkResult.Loading()
        viewModelScope.launch {
            _tasks.value = repository.getTasks(tasksPage, pageSize, tasksSearch)
        }
    }

    fun loadWnioski(userId: Int, pageSize: Int = 10) {
        _wnioski.value = NetworkResult.Loading()
        viewModelScope.launch {
            _wnioski.value = repository.getWnioski(userId, wnioskiPage, pageSize)
        }
    }

    fun sendWniosek(wniosekId: Int, userId: Int, onResult: (Boolean, Any?) -> Unit) {
        viewModelScope.launch {
            val result = repository.sendWniosek(wniosekId, userId)
            onResult(result is NetworkResult.Success, (result as? NetworkResult.Success)?.data)
        }
    }

    fun resubmitWniosek(wniosekId: Int, userId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.resubmitWniosek(wniosekId, userId)
            onResult(result is NetworkResult.Success)
        }
    }

    fun deleteWniosek(wniosekId: Int, userId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteWniosek(wniosekId, userId)
            onResult(result is NetworkResult.Success)
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            onDone()
        }
    }
}
