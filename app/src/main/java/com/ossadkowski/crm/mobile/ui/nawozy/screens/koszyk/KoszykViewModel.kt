package com.ossadkowski.crm.mobile.ui.nawozy.screens.koszyk

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.model.Koszyk
import com.ossadkowski.crm.mobile.domain.nawozy.model.LimitStatus
import com.ossadkowski.crm.mobile.domain.nawozy.model.SlownikPozycja
import com.ossadkowski.crm.mobile.domain.nawozy.model.ZamowienieStatus
import com.ossadkowski.crm.mobile.domain.nawozy.repository.KoszykHeader
import com.ossadkowski.crm.mobile.domain.nawozy.repository.SlownikKategoria
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.DeletePozycjaUseCase
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.GetKoszykUseCase
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.GetLimitStatusUseCase
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.GetSlownikUseCase
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.SubmitKoszykUseCase
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.UpdateHeaderUseCase
import com.ossadkowski.crm.mobile.ui.nawozy.nav.NawozyRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KoszykState(
    val loading: Boolean = true,
    val koszyk: Koszyk? = null,
    val error: String? = null,
    val paymentTerms: List<SlownikPozycja> = emptyList(),
    val dlvModes: List<SlownikPozycja> = emptyList(),
    val dlvTerms: List<SlownikPozycja> = emptyList(),
    val limitStatus: LimitStatus? = null,
    val riskAcknowledged: Boolean = false,
    val savingHeader: Boolean = false,
    val submitting: Boolean = false,
    /** One-shot signal: submit succeeded with this status — leave the cart. */
    val submittedStatus: ZamowienieStatus? = null,
    val message: String? = null,
) {
    /**
     * Risk acknowledgement is required when the customer is frozen/blocked OR over the
     * credit limit. Over-limit orders are allowed — the excess is invoiced normally —
     * but the salesperson must confirm awareness first.
     */
    val requiresRisk: Boolean
        get() = limitStatus?.let { it.isRestricted || (it.dostepne ?: Double.MAX_VALUE) <= 0.0 } == true
    val canSubmit: Boolean
        get() = !submitting && (koszyk?.pozycje?.isNotEmpty() == true) && (!requiresRisk || riskAcknowledged)
}

@HiltViewModel
class KoszykViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getKoszyk: GetKoszykUseCase,
    private val updateHeader: UpdateHeaderUseCase,
    private val submitKoszyk: SubmitKoszykUseCase,
    private val deletePozycja: DeletePozycjaUseCase,
    private val getSlownik: GetSlownikUseCase,
    private val getLimitStatus: GetLimitStatusUseCase,
) : ViewModel() {

    val koszykId: Long = checkNotNull(savedStateHandle[NawozyRoutes.ARG_KOSZYK_ID]) {
        "KoszykViewModel requires a ${NawozyRoutes.ARG_KOSZYK_ID} argument"
    }

    private val _state = MutableStateFlow(KoszykState())
    val state: StateFlow<KoszykState> = _state.asStateFlow()

    init {
        loadDictionaries()
        reload()
    }

    /** Reloads the cart only — used on first load and on return from the product picker. */
    fun reload() {
        viewModelScope.launch {
            if (_state.value.koszyk == null) _state.update { it.copy(loading = true, error = null) }
            when (val r = getKoszyk(koszykId)) {
                is Result.Success -> {
                    _state.update { it.copy(loading = false, koszyk = r.data, error = null) }
                    ensureLimit(r.data.kontrahentId)
                }
                is Result.Error -> _state.update { it.copy(loading = false, error = r.message) }
                Result.Loading -> Unit
            }
        }
    }

    private fun loadDictionaries() {
        viewModelScope.launch {
            (getSlownik(SlownikKategoria.PAYM_TERM) as? Result.Success)?.let { res ->
                _state.update { it.copy(paymentTerms = res.data) }
            }
        }
        viewModelScope.launch {
            (getSlownik(SlownikKategoria.DLV_MODE) as? Result.Success)?.let { res ->
                _state.update { it.copy(dlvModes = res.data) }
            }
        }
        viewModelScope.launch {
            (getSlownik(SlownikKategoria.DLV_TERM) as? Result.Success)?.let { res ->
                _state.update { it.copy(dlvTerms = res.data) }
            }
        }
    }

    /** [accountNum] is the cart's `kontrahentAccountNum` — the value limit-status expects. */
    private fun ensureLimit(accountNum: String) {
        if (accountNum.isBlank() || _state.value.limitStatus != null) return
        viewModelScope.launch {
            (getLimitStatus(accountNum) as? Result.Success)?.let { res ->
                _state.update { it.copy(limitStatus = res.data) }
            }
        }
    }

    // ── Header edits (each PUT returns {ok}; the repo re-fetches the cart) ──────

    fun setPaymentTerm(kod: String) = patchHeader(KoszykHeader(paymentTerm = kod))
    fun setDlvMode(kod: String) = patchHeader(KoszykHeader(dlvMode = kod))
    fun setDlvTerm(kod: String) = patchHeader(KoszykHeader(dlvTerm = kod))

    private fun patchHeader(header: KoszykHeader) {
        viewModelScope.launch {
            _state.update { it.copy(savingHeader = true, message = null) }
            when (val r = updateHeader(koszykId, header)) {
                is Result.Success -> _state.update { it.copy(savingHeader = false, koszyk = r.data) }
                is Result.Error -> _state.update { it.copy(savingHeader = false, message = r.message) }
                Result.Loading -> Unit
            }
        }
    }

    // ── Lines ────────────────────────────────────────────────────────────────

    fun deleteLine(lineId: Long) {
        viewModelScope.launch {
            when (val r = deletePozycja(koszykId, lineId)) {
                is Result.Success -> _state.update { it.copy(koszyk = r.data) }
                is Result.Error -> _state.update { it.copy(message = r.message) }
                Result.Loading -> Unit
            }
        }
    }

    // ── Risk + submit ──────────────────────────────────────────────────────────

    fun toggleRisk() = _state.update { it.copy(riskAcknowledged = !it.riskAcknowledged) }

    fun submit() {
        val s = _state.value
        if (s.submitting) return
        if (s.koszyk?.pozycje.isNullOrEmpty()) {
            _state.update { it.copy(message = "Dodaj co najmniej jedną pozycję.") }
            return
        }
        if (s.requiresRisk && !s.riskAcknowledged) {
            _state.update { it.copy(message = "Potwierdź ryzyko, aby wysłać zamówienie.") }
            return
        }
        _state.update { it.copy(submitting = true, message = null) }
        viewModelScope.launch {
            when (val r = submitKoszyk(koszykId, warningsAcknowledged = s.riskAcknowledged)) {
                is Result.Success -> _state.update {
                    it.copy(submitting = false, koszyk = r.data, submittedStatus = r.data.status)
                }
                is Result.Error -> _state.update { it.copy(submitting = false, message = r.message) }
                Result.Loading -> Unit
            }
        }
    }

    fun consumeSubmission() = _state.update { it.copy(submittedStatus = null) }
    fun consumeMessage() = _state.update { it.copy(message = null) }
}
