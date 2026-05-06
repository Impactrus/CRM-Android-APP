package com.ossadkowski.crm.mobile.ui.serwis.screens.machines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.model.Machine
import com.ossadkowski.crm.mobile.domain.serwis.model.WarrantyStatus
import com.ossadkowski.crm.mobile.domain.serwis.usecase.GetCustomerMachinesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MachineWarrantyFilter { ALL, WITH, WITHOUT }

sealed interface MachinesUiState {
    data object Loading : MachinesUiState
    data class Success(
        val machines: List<Machine>,
        val query: String = "",
        val warrantyFilter: MachineWarrantyFilter = MachineWarrantyFilter.ALL,
    ) : MachinesUiState {
        val filtered: List<Machine>
            get() {
                val q = query.trim().lowercase()
                return machines
                    .asSequence()
                    .filter {
                        if (q.isEmpty()) true else listOfNotNull(
                            it.numerSeryjny,
                            it.marka,
                            it.model,
                        ).any { f -> f.lowercase().contains(q) }
                    }
                    .filter {
                        when (warrantyFilter) {
                            MachineWarrantyFilter.ALL -> true
                            MachineWarrantyFilter.WITH -> it.warrantyStatus == WarrantyStatus.ACTIVE ||
                                it.warrantyStatus == WarrantyStatus.EXPIRING_SOON
                            MachineWarrantyFilter.WITHOUT -> it.warrantyStatus == WarrantyStatus.EXPIRED ||
                                it.warrantyStatus == WarrantyStatus.UNKNOWN
                        }
                    }
                    .toList()
            }
    }
    data class Error(val message: String) : MachinesUiState
}

@HiltViewModel
class MachinesViewModel @Inject constructor(
    private val getCustomerMachines: GetCustomerMachinesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MachinesUiState>(MachinesUiState.Loading)
    val uiState: StateFlow<MachinesUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = MachinesUiState.Loading
            when (val r = getCustomerMachines()) {
                is Result.Success -> _uiState.value = MachinesUiState.Success(r.data)
                is Result.Error -> _uiState.value = MachinesUiState.Error(r.message)
                Result.Loading -> Unit
            }
        }
    }

    fun setQuery(query: String) {
        val current = _uiState.value
        if (current is MachinesUiState.Success) {
            _uiState.value = current.copy(query = query)
        }
    }

    fun setWarrantyFilter(filter: MachineWarrantyFilter) {
        val current = _uiState.value
        if (current is MachinesUiState.Success) {
            _uiState.value = current.copy(warrantyFilter = filter)
        }
    }
}
