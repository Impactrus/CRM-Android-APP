package com.ossadkowski.crm.mobile.example.serverstatus.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serverstatus.model.ServerStatus
import com.ossadkowski.crm.mobile.domain.serverstatus.usecase.CheckServerStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServerStatusViewModel @Inject constructor(
    private val checkStatus: CheckServerStatusUseCase
) : ViewModel() {

    private val _state = MutableLiveData<Result<ServerStatus>>()
    val state: LiveData<Result<ServerStatus>> = _state

    fun check() {
        _state.value = Result.Loading
        viewModelScope.launch {
            _state.value = checkStatus()
        }
    }
}
