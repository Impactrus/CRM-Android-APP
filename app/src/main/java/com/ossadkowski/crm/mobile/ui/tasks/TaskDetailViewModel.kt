package com.ossadkowski.crm.mobile.ui.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.*
import com.ossadkowski.crm.mobile.data.repository.TasksRepository
import kotlinx.coroutines.launch

class TaskDetailViewModel(
    private val repository: TasksRepository = TasksRepository()
) : ViewModel() {

    private val _detail = MutableLiveData<NetworkResult<TaskDetailDto>>()
    val detail: LiveData<NetworkResult<TaskDetailDto>> = _detail

    private val _comments = MutableLiveData<NetworkResult<List<TaskCommentDto>>>()
    val comments: LiveData<NetworkResult<List<TaskCommentDto>>> = _comments

    private val _files = MutableLiveData<NetworkResult<List<TaskFileDto>>>()
    val files: LiveData<NetworkResult<List<TaskFileDto>>> = _files

    private val _historia = MutableLiveData<NetworkResult<List<TaskHistoriaDto>>>()
    val historia: LiveData<NetworkResult<List<TaskHistoriaDto>>> = _historia

    private val _observers = MutableLiveData<NetworkResult<List<TaskObserverDto>>>()
    val observers: LiveData<NetworkResult<List<TaskObserverDto>>> = _observers

    private val _statusResult = MutableLiveData<NetworkResult<Any>>()
    val statusResult: LiveData<NetworkResult<Any>> = _statusResult

    private val _commentResult = MutableLiveData<NetworkResult<Any>>()
    val commentResult: LiveData<NetworkResult<Any>> = _commentResult

    fun loadDetail(id: Int) {
        _detail.value = NetworkResult.Loading()
        viewModelScope.launch {
            _detail.value = repository.getDetail(id)
        }
    }

    fun loadComments(id: Int) {
        viewModelScope.launch { _comments.value = repository.getComments(id) }
    }

    fun loadFiles(id: Int) {
        viewModelScope.launch { _files.value = repository.getFiles(id) }
    }

    fun loadHistoria(id: Int) {
        viewModelScope.launch { _historia.value = repository.getHistoria(id) }
    }

    fun loadObservers(id: Int) {
        viewModelScope.launch { _observers.value = repository.getObservers(id) }
    }

    fun changeStatus(id: Int, status: String) {
        viewModelScope.launch {
            _statusResult.value = repository.changeStatus(id, status)
        }
    }

    fun addComment(id: Int, tresc: String) {
        viewModelScope.launch {
            _commentResult.value = repository.addComment(id, tresc)
        }
    }
}
