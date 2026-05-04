package com.ossadkowski.crm.mobile.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.TaskCommentDto
import com.ossadkowski.crm.mobile.data.model.TaskDetailDto
import com.ossadkowski.crm.mobile.data.repository.TasksRepository
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: TasksRepository = TasksRepository()
) : ViewModel() {

    private val _taskDetail = MutableLiveData<NetworkResult<TaskDetailDto>>()
    val taskDetail: LiveData<NetworkResult<TaskDetailDto>> = _taskDetail

    private val _comments = MutableLiveData<NetworkResult<List<TaskCommentDto>>>()
    val comments: LiveData<NetworkResult<List<TaskCommentDto>>> = _comments

    private val _sendCommentStatus = MutableLiveData<NetworkResult<Any>>()
    val sendCommentStatus: LiveData<NetworkResult<Any>> = _sendCommentStatus

    fun loadTaskDetail(id: Int) {
        _taskDetail.value = NetworkResult.Loading()
        viewModelScope.launch {
            _taskDetail.value = repository.getDetail(id)
        }
    }

    fun loadComments(id: Int) {
        _comments.value = NetworkResult.Loading()
        viewModelScope.launch {
            _comments.value = repository.getComments(id)
        }
    }

    fun sendComment(id: Int, text: String) {
        _sendCommentStatus.value = NetworkResult.Loading()
        viewModelScope.launch {
            val result = repository.addComment(id, text)
            _sendCommentStatus.value = result
            // Jeśli sukces, odświeżamy komentarze (lub repozytorium zrobi to samo)
            if (result is NetworkResult.Success) {
                loadComments(id)
            }
        }
    }
}
