package com.ossadkowski.crm.mobile.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.LoginResponse
import com.ossadkowski.crm.mobile.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository
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

class LoginViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = LoginViewModel(repository) as T
}
