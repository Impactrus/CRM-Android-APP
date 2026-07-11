package com.ossadkowski.crm.mobile.ui.wizyty.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.wizyty.model.AddressSuggestion
import com.ossadkowski.crm.mobile.domain.wizyty.model.NewVisitEvent
import com.ossadkowski.crm.mobile.domain.wizyty.usecase.AddManualVisitUseCase
import com.ossadkowski.crm.mobile.domain.wizyty.usecase.SearchAddressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddVisitUiState(
    val contractorName: String = "",
    val addressQuery: String = "",
    val suggestions: List<AddressSuggestion> = emptyList(),
    val selected: AddressSuggestion? = null,
    val saving: Boolean = false,
    val error: String? = null,
) {
    val canSave: Boolean get() = contractorName.isNotBlank() && !saving
}

@HiltViewModel
class AddVisitViewModel @Inject constructor(
    private val searchAddress: SearchAddressUseCase,
    private val addManualVisit: AddManualVisitUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AddVisitUiState())
    val state: StateFlow<AddVisitUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun onContractorNameChange(value: String) {
        _state.update { it.copy(contractorName = value, error = null) }
    }

    fun onAddressQueryChange(value: String) {
        // Typing again clears a prior pick and re-arms the debounced search.
        _state.update { it.copy(addressQuery = value, selected = null) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            when (val r = searchAddress(value)) {
                is Result.Success -> _state.update { it.copy(suggestions = r.data) }
                is Result.Error -> _state.update { it.copy(suggestions = emptyList()) }
                Result.Loading -> Unit
            }
        }
    }

    fun onSuggestionSelected(suggestion: AddressSuggestion) {
        searchJob?.cancel()
        _state.update {
            it.copy(
                selected = suggestion,
                addressQuery = suggestion.label,
                suggestions = emptyList(),
            )
        }
    }

    fun save(onSaved: () -> Unit) {
        val current = _state.value
        if (!current.canSave) return
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val new = NewVisitEvent(
                contractorName = current.contractorName.trim(),
                addressLabel = current.selected?.label,
                lat = current.selected?.lat,
                lng = current.selected?.lng,
            )
            when (val r = addManualVisit(new)) {
                is Result.Success -> onSaved()
                is Result.Error -> _state.update { it.copy(saving = false, error = r.message) }
                Result.Loading -> Unit
            }
        }
    }

    private companion object {
        const val DEBOUNCE_MS = 300L
    }
}
