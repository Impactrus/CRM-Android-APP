package com.ossadkowski.crm.mobile.ui.serwis.screens.machine

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.model.Machine
import com.ossadkowski.crm.mobile.domain.serwis.usecase.GetMachineHistoryUseCase
import com.ossadkowski.crm.mobile.ui.serwis.nav.SerwisRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface Machine360UiState {
    data object Loading : Machine360UiState
    data class Success(val machine: Machine) : Machine360UiState
    data class Error(val message: String) : Machine360UiState
}

@HiltViewModel
class Machine360ViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMachineHistory: GetMachineHistoryUseCase,
) : ViewModel() {

    private val serial: String = savedStateHandle.get<String>(SerwisRoutes.ARG_SERIAL).orEmpty()

    private val _uiState = MutableStateFlow<Machine360UiState>(Machine360UiState.Loading)
    val uiState: StateFlow<Machine360UiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = Machine360UiState.Loading
            when (val r = getMachineHistory(serial)) {
                is Result.Success -> _uiState.value = Machine360UiState.Success(r.data)
                is Result.Error -> _uiState.value = Machine360UiState.Error(r.message)
                Result.Loading -> Unit
            }
        }
    }
}
