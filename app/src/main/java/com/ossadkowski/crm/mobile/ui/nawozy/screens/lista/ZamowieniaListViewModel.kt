package com.ossadkowski.crm.mobile.ui.nawozy.screens.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.model.ZamowienieNawozy
import com.ossadkowski.crm.mobile.domain.nawozy.model.ZamowienieStatus
import com.ossadkowski.crm.mobile.domain.nawozy.repository.ZamowieniaFilters
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.ListZamowieniaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ZamowieniaListState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val items: List<ZamowienieNawozy> = emptyList(),
    val statusFilter: ZamowienieStatus? = null,
    val error: String? = null,
)

@HiltViewModel
class ZamowieniaListViewModel @Inject constructor(
    private val listZamowienia: ListZamowieniaUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ZamowieniaListState())
    val state: StateFlow<ZamowieniaListState> = _state.asStateFlow()

    init {
        load(status = null, initial = true)
    }

    /** Pull-to-refresh — keeps the current list visible while reloading. */
    fun refresh() = load(status = _state.value.statusFilter, initial = false)

    fun setStatusFilter(status: ZamowienieStatus?) {
        if (status == _state.value.statusFilter) return
        _state.update { it.copy(statusFilter = status) }
        load(status = status, initial = true)
    }

    fun retry() = load(status = _state.value.statusFilter, initial = true)

    /** [status] is captured by the caller so the request never races the state update below. */
    private fun load(status: ZamowienieStatus?, initial: Boolean) {
        viewModelScope.launch {
            _state.update {
                it.copy(loading = initial, refreshing = !initial, error = null)
            }
            when (val r = listZamowienia(ZamowieniaFilters(status = status))) {
                is Result.Success -> _state.update {
                    it.copy(loading = false, refreshing = false, items = r.data.items, error = null)
                }
                is Result.Error -> _state.update {
                    it.copy(loading = false, refreshing = false, error = r.message)
                }
                Result.Loading -> Unit
            }
        }
    }
}
