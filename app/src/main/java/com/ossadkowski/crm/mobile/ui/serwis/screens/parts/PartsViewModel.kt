package com.ossadkowski.crm.mobile.ui.serwis.screens.parts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.parts.model.NewPartRequest
import com.ossadkowski.crm.mobile.domain.serwis.parts.model.PartStatus
import com.ossadkowski.crm.mobile.domain.serwis.parts.usecase.AddPartUseCase
import com.ossadkowski.crm.mobile.domain.serwis.parts.usecase.DeletePartUseCase
import com.ossadkowski.crm.mobile.domain.serwis.parts.usecase.ObserveAllPartsUseCase
import com.ossadkowski.crm.mobile.domain.serwis.parts.usecase.UpdatePartStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PartsViewModel @Inject constructor(
    private val observeAll: ObserveAllPartsUseCase,
    private val addPart: AddPartUseCase,
    private val updateStatus: UpdatePartStatusUseCase,
    private val delete: DeletePartUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PartsUiState())
    val uiState: StateFlow<PartsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeAll().collect { list ->
                _state.update { it.copy(parts = list, isLoading = false) }
            }
        }
    }

    fun setFilter(status: PartStatus?) = _state.update { it.copy(filter = status) }

    fun add(req: NewPartRequest) = viewModelScope.launch {
        when (val r = addPart(req)) {
            is Result.Error -> _state.update { it.copy(error = r.message) }
            else -> Unit
        }
    }

    fun changeStatus(id: Long, status: PartStatus) = viewModelScope.launch {
        when (val r = updateStatus(id, status)) {
            is Result.Error -> _state.update { it.copy(error = r.message) }
            else -> Unit
        }
    }

    fun remove(id: Long) = viewModelScope.launch {
        when (val r = delete(id)) {
            is Result.Error -> _state.update { it.copy(error = r.message) }
            else -> Unit
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
