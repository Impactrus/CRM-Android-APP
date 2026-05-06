package com.ossadkowski.crm.mobile.ui.serwis.screens.order

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.model.JobCard
import com.ossadkowski.crm.mobile.domain.serwis.model.OrderLogEntry
import com.ossadkowski.crm.mobile.domain.serwis.model.ServiceOrderSummary
import com.ossadkowski.crm.mobile.domain.serwis.usecase.GetOrderWithCardsUseCase
import com.ossadkowski.crm.mobile.ui.serwis.nav.SerwisRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class OrderDetailTab { CZYNNOSCI, CZESCI, PLIKI, LOG }

sealed interface OrderDetailUiState {
    data object Loading : OrderDetailUiState
    data class Success(
        val order: ServiceOrderSummary,
        val jobCards: List<JobCard>,
        val log: List<OrderLogEntry>,
        val selectedTab: OrderDetailTab = OrderDetailTab.CZYNNOSCI,
    ) : OrderDetailUiState
    data class Error(val message: String) : OrderDetailUiState
}

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getOrderWithCards: GetOrderWithCardsUseCase,
) : ViewModel() {

    val orderNum: String =
        savedStateHandle.get<String>(SerwisRoutes.ARG_ORDER_NUM).orEmpty()

    private val _uiState = MutableStateFlow<OrderDetailUiState>(OrderDetailUiState.Loading)
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = OrderDetailUiState.Loading
            when (val r = getOrderWithCards(orderNum)) {
                is Result.Success -> _uiState.value = OrderDetailUiState.Success(
                    order = r.data.order,
                    jobCards = r.data.jobCards,
                    log = r.data.log,
                )
                is Result.Error -> _uiState.value = OrderDetailUiState.Error(r.message)
                Result.Loading -> Unit
            }
        }
    }

    fun selectTab(tab: OrderDetailTab) {
        val current = _uiState.value
        if (current is OrderDetailUiState.Success) {
            _uiState.value = current.copy(selectedTab = tab)
        }
    }
}
