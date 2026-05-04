package com.ossadkowski.crm.mobile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.model.HrOrgItemDto
import kotlinx.coroutines.launch
import java.util.*

class HrOrgViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    private val _state = MutableLiveData<NetworkResult<HrOrgItemDto>>()
    val state: LiveData<NetworkResult<HrOrgItemDto>> = _state

    private val _currentPath = MutableLiveData<List<HrOrgItemDto>>()
    val currentPath: LiveData<List<HrOrgItemDto>> = _currentPath

    private var fullRoot: HrOrgItemDto? = null
    private val navigationStack = Stack<HrOrgItemDto>()

    fun loadStructure() {
        if (fullRoot != null) return // Already loaded
        
        _state.value = NetworkResult.Loading()
        viewModelScope.launch {
            try {
                val list = api.getHrOrgStructure()
                if (list.isNotEmpty()) {
                    val root = list[0]
                    fullRoot = root
                    navigationStack.clear()
                    navigateTo(root)
                } else {
                    _state.value = NetworkResult.Error("Pusta struktura organizacyjna")
                }
            } catch (e: Exception) {
                _state.value = NetworkResult.Error("Błąd pobierania struktury: ${e.message}")
            }
        }
    }

    fun navigateTo(item: HrOrgItemDto) {
        navigationStack.push(item)
        updateDisplay()
    }

    fun navigateBack(): Boolean {
        if (navigationStack.size > 1) {
            navigationStack.pop()
            updateDisplay()
            return true
        }
        return false
    }

    fun navigateToBreadcrumb(index: Int) {
        while (navigationStack.size > index + 1) {
            navigationStack.pop()
        }
        updateDisplay()
    }

    private fun updateDisplay() {
        val current = navigationStack.peek()
        _state.value = NetworkResult.Success(current)
        _currentPath.value = navigationStack.toList()
    }
}
