package com.ossadkowski.crm.mobile.ui.wizyty.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.wizyty.location.WizytySessionController
import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.wizyty.model.AddressSuggestion
import com.ossadkowski.crm.mobile.domain.wizyty.usecase.SaveTestLocationUseCase
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

data class AddTestLocationUiState(
    val name: String = "",
    val addressQuery: String = "",
    val suggestions: List<AddressSuggestion> = emptyList(),
    val selected: AddressSuggestion? = null,
    val saving: Boolean = false,
    val error: String? = null,
) {
    val canSave: Boolean get() = name.isNotBlank() && selected != null && !saving
}

@HiltViewModel
class AddTestLocationViewModel @Inject constructor(
    private val searchAddress: SearchAddressUseCase,
    private val saveTestLocation: SaveTestLocationUseCase,
    private val sessionController: WizytySessionController,
) : ViewModel() {

    private val _state = MutableStateFlow(AddTestLocationUiState())
    val state: StateFlow<AddTestLocationUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun onNameChange(value: String) {
        _state.update { it.copy(name = value, error = null) }
    }

    fun onAddressQueryChange(value: String) {
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
            it.copy(selected = suggestion, addressQuery = suggestion.label, suggestions = emptyList())
        }
    }

    /** Call only after foreground location permission is granted. */
    fun save(onSaved: () -> Unit) {
        val current = _state.value
        val address = current.selected ?: return
        if (!current.canSave) return
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            when (val r = saveTestLocation(current.name.trim(), address)) {
                is Result.Success -> {
                    // (Re)start the session so the new location is geofenced immediately.
                    sessionController.start()
                    onSaved()
                }
                is Result.Error -> _state.update { it.copy(saving = false, error = r.message) }
                Result.Loading -> Unit
            }
        }
    }

    private companion object {
        const val DEBOUNCE_MS = 300L
    }
}
