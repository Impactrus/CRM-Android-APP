package com.ossadkowski.crm.mobile.ui.nawozy.screens.kontrahent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.model.Kontrahent
import com.ossadkowski.crm.mobile.domain.nawozy.model.LimitStatus
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.GetLimitStatusUseCase
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.SearchKontrahenciUseCase
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.StartKoszykUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KontrahentPickerState(
    val query: String = "",
    val myOnly: Boolean = false,
    val loading: Boolean = false,
    val customers: List<Kontrahent> = emptyList(),
    val error: String? = null,
    val selected: Kontrahent? = null,
    val limitStatus: LimitStatus? = null,
    val limitLoading: Boolean = false,
    val starting: Boolean = false,
    /** One-shot navigation signal: the cart was created, open it. */
    val startedKoszykId: Long? = null,
)

@HiltViewModel
class KontrahentPickerViewModel @Inject constructor(
    private val searchKontrahenci: SearchKontrahenciUseCase,
    private val getLimitStatus: GetLimitStatusUseCase,
    private val startKoszyk: StartKoszykUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(KontrahentPickerState())
    val state: StateFlow<KontrahentPickerState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        search()
    }

    fun setQuery(query: String) {
        _state.update { it.copy(query = query) }
        scheduleSearch()
    }

    fun toggleMyOnly() {
        _state.update { it.copy(myOnly = !it.myOnly) }
        search()
    }

    /** Debounce typing so we don't fire a request per keystroke. */
    private fun scheduleSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            search()
        }
    }

    fun search() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val current = _state.value
            _state.update { it.copy(loading = true, error = null) }
            when (val r = searchKontrahenci(current.query.trim().ifBlank { null }, current.myOnly)) {
                is Result.Success -> _state.update { it.copy(loading = false, customers = r.data) }
                is Result.Error -> _state.update { it.copy(loading = false, error = r.message) }
                Result.Loading -> Unit
            }
        }
    }

    fun select(customer: Kontrahent) {
        _state.update { it.copy(selected = customer, limitStatus = null, limitLoading = true) }
        viewModelScope.launch {
            when (val r = getLimitStatus(customer.accountNum)) {
                is Result.Success -> _state.update { it.copy(limitLoading = false, limitStatus = r.data) }
                // A missing limit must not block ordering — just drop the banner.
                is Result.Error -> _state.update { it.copy(limitLoading = false, limitStatus = null) }
                Result.Loading -> Unit
            }
        }
    }

    fun clearSelection() {
        _state.update { it.copy(selected = null, limitStatus = null, limitLoading = false) }
    }

    /** Creates/resumes the cart for the selected customer, then signals navigation. */
    fun startOrder() {
        val customer = _state.value.selected ?: return
        if (_state.value.starting) return
        _state.update { it.copy(starting = true, error = null) }
        viewModelScope.launch {
            when (val r = startKoszyk(customer.accountNum)) {
                is Result.Success -> _state.update { it.copy(starting = false, startedKoszykId = r.data) }
                is Result.Error -> _state.update { it.copy(starting = false, error = r.message) }
                Result.Loading -> Unit
            }
        }
    }

    fun consumeNavigation() {
        _state.update { it.copy(startedKoszykId = null) }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
