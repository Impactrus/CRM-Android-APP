package com.ossadkowski.crm.mobile.ui.serwis.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.model.MyOrder
import com.ossadkowski.crm.mobile.domain.serwis.usecase.GetMyOrdersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyDayViewModel @Inject constructor(
    private val getMyOrders: GetMyOrdersUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyDayUiState>(MyDayUiState.Loading)
    val uiState: StateFlow<MyDayUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = MyDayUiState.Loading
            when (val r = getMyOrders()) {
                is Result.Success -> _uiState.value =
                    MyDayUiState.Success(r.data, computeFilter(r.data))
                is Result.Error -> _uiState.value = MyDayUiState.Error(r.message)
                Result.Loading -> Unit
            }
        }
    }

    fun setFilter(filter: TaskFilter) {
        val current = _uiState.value
        if (current is MyDayUiState.Success) {
            _uiState.value = current.copy(filter = filter)
        }
    }

    /** MVP: no real classification yet. Reserved for future SLA / recall logic. */
    @Suppress("UNUSED_PARAMETER")
    private fun computeFilter(orders: List<MyOrder>): TaskFilter = TaskFilter.ALL
}
