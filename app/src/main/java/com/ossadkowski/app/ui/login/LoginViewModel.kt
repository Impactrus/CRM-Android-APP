package com.ossadkowski.app.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.model.LoginResponse
import com.ossadkowski.app.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _loginResult = MutableLiveData<NetworkResult<LoginResponse>>()
    val loginResult: LiveData<NetworkResult<LoginResponse>> = _loginResult

    fun login(username: String, password: String) {
        _loginResult.value = NetworkResult.Loading()
        viewModelScope.launch {
            _loginResult.value = repository.login(username, password)
        }
    }
}
