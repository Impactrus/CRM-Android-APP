package com.ossadkowski.app.ui.limitykredytowe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.model.*
import com.ossadkowski.app.data.repository.LimityKredytoweRepository
import kotlinx.coroutines.launch

class LimityKredytoweListViewModel(
    private val repository: LimityKredytoweRepository = LimityKredytoweRepository()
) : ViewModel() {

    private val _items = MutableLiveData<NetworkResult<GenericPageResponse<LimitKredytowyListItem>>>()
    val items: LiveData<NetworkResult<GenericPageResponse<LimitKredytowyListItem>>> = _items

    var page = 1
    var search: String? = null
    var statusFilter: String? = null
    var tab: String? = null

    fun load(pageSize: Int = 20) {
        _items.value = NetworkResult.Loading()
        viewModelScope.launch {
            _items.value = repository.getList(page, pageSize, statusFilter, search, tab)
        }
    }
}
