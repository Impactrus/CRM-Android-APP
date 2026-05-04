package com.ossadkowski.crm.mobile.ui.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.CreateTaskRequest
import com.ossadkowski.crm.mobile.data.model.KontrahentSearchItem
import com.ossadkowski.crm.mobile.data.model.PaginatedResponse
import com.ossadkowski.crm.mobile.data.repository.LimityKredytoweRepository
import com.ossadkowski.crm.mobile.data.repository.TasksRepository
import kotlinx.coroutines.launch

class NewDebtTaskViewModel(
    private val tasksRepository: TasksRepository = TasksRepository(),
    private val limityRepository: LimityKredytoweRepository = LimityKredytoweRepository()
) : ViewModel() {

    private val _createResult = MutableLiveData<NetworkResult<Any>>()
    val createResult: LiveData<NetworkResult<Any>> = _createResult

    private val _kontrahenci = MutableLiveData<NetworkResult<List<KontrahentSearchItem>>>()
    val kontrahenci: LiveData<NetworkResult<List<KontrahentSearchItem>>> = _kontrahenci

    fun searchKontrahenci(query: String) {
        viewModelScope.launch {
            _kontrahenci.value = limityRepository.searchKontrahenci(query)
        }
    }

    fun createTask(request: CreateTaskRequest) {
        _createResult.value = NetworkResult.Loading()
        viewModelScope.launch {
            _createResult.value = tasksRepository.createTask(request)
        }
    }
}
