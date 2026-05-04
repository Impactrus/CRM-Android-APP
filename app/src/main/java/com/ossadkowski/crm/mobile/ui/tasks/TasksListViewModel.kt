package com.ossadkowski.crm.mobile.ui.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.*
import com.ossadkowski.crm.mobile.data.repository.TasksRepository
import kotlinx.coroutines.launch

class TasksListViewModel(
    private val repository: TasksRepository = TasksRepository()
) : ViewModel() {

    private val _items = MutableLiveData<NetworkResult<PaginatedResponse<TaskListItemDto>>>()
    val items: LiveData<NetworkResult<PaginatedResponse<TaskListItemDto>>> = _items

    var page = 1
    var search: String? = null
    var statusFilter: String? = null
    var typFilter: String? = null

    fun load(pageSize: Int = 10) {
        _items.value = NetworkResult.Loading()
        viewModelScope.launch {
            _items.value = repository.getList(page, pageSize, search, statusFilter, typFilter)
        }
    }
}
