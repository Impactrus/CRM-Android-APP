package com.ossadkowski.crm.mobile.ui.nawozy.screens.towar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.model.AdresDostawy
import com.ossadkowski.crm.mobile.domain.nawozy.model.MagazynStan
import com.ossadkowski.crm.mobile.domain.nawozy.model.PricingResult
import com.ossadkowski.crm.mobile.domain.nawozy.model.TowarNawoz
import com.ossadkowski.crm.mobile.domain.nawozy.model.WariantLogistyczny
import com.ossadkowski.crm.mobile.domain.nawozy.repository.Cennik
import com.ossadkowski.crm.mobile.domain.nawozy.repository.NowaPozycja
import com.ossadkowski.crm.mobile.domain.nawozy.repository.PricingReverseZapytanie
import com.ossadkowski.crm.mobile.domain.nawozy.repository.PricingZapytanie
import com.ossadkowski.crm.mobile.domain.nawozy.repository.WariantyZapytanie
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.AddPozycjaUseCase
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.CalcPricingReverseUseCase
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.CalcPricingUseCase
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.GetAddressBookUseCase
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.GetKoszykUseCase
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.GetTowarMagazynyUseCase
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.GetWariantyLogistykaUseCase
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.SearchTowaryNawozyUseCase
import com.ossadkowski.crm.mobile.ui.nawozy.nav.NawozyRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TowarPickerState(
    val query: String = "",
    val loading: Boolean = false,
    val products: List<TowarNawoz> = emptyList(),
    val error: String? = null,

    // cart context (loaded once), needed for pricing + logistics
    val paymTermId: String? = null,
    val addressQuery: String = "",
    val addresses: List<AdresDostawy> = emptyList(),
    val addressLoading: Boolean = false,
    val selectedAddress: AdresDostawy? = null,

    // selected-product sheet
    val selected: TowarNawoz? = null,
    val magazyny: List<MagazynStan> = emptyList(),
    val magazynyLoading: Boolean = false,
    val qty: Double = TowarNawoz.FULL_TRUCK_TONS,
    val rabatKwotowy: Double = 0.0,
    val pricing: PricingResult? = null,
    val pricingLoading: Boolean = false,

    // logistics
    val warianty: List<WariantLogistyczny> = emptyList(),
    val wariantyLoading: Boolean = false,
    val selectedWariant: WariantLogistyczny? = null,

    val adding: Boolean = false,
    /** One-shot: a line was added — close the picker and return to the cart. */
    val added: Boolean = false,
    val message: String? = null,
) {
    /** Warehouse pinned to the line: from the chosen logistics variant, else first in stock. */
    val effectiveMagazynId: String?
        get() = selectedWariant?.loadLocationId
            ?: magazyny.firstOrNull { (it.dostepne ?: 0.0) > 0.0 }?.magazynId
            ?: magazyny.firstOrNull()?.magazynId

    val canAdd: Boolean get() = !adding && selected != null && qty > 0.0
}

@HiltViewModel
class TowarPickerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getKoszyk: GetKoszykUseCase,
    private val searchTowary: SearchTowaryNawozyUseCase,
    private val getMagazyny: GetTowarMagazynyUseCase,
    private val calcPricing: CalcPricingUseCase,
    private val calcPricingReverse: CalcPricingReverseUseCase,
    private val getWarianty: GetWariantyLogistykaUseCase,
    private val getAddressBook: GetAddressBookUseCase,
    private val addPozycja: AddPozycjaUseCase,
) : ViewModel() {

    val koszykId: Long = checkNotNull(savedStateHandle[NawozyRoutes.ARG_KOSZYK_ID]) {
        "TowarPickerViewModel requires a ${NawozyRoutes.ARG_KOSZYK_ID} argument"
    }

    private val _state = MutableStateFlow(TowarPickerState())
    val state: StateFlow<TowarPickerState> = _state.asStateFlow()

    private var searchJob: Job? = null
    private var pricingJob: Job? = null
    private var addressJob: Job? = null

    init {
        loadCartContext()
        search()
    }

    private fun loadCartContext() {
        viewModelScope.launch {
            (getKoszyk(koszykId) as? Result.Success)?.data?.let { k ->
                _state.update { it.copy(paymTermId = k.paymentTerm) }
            }
        }
    }

    /**
     * Address book is a hybrid AX+LOCAL search that REQUIRES search ≥ 2 chars
     * (shorter/blank returns an empty page on purpose), so we query as the user types.
     */
    fun setAddressQuery(query: String) {
        _state.update { it.copy(addressQuery = query) }
        addressJob?.cancel()
        if (query.trim().length < 2) {
            _state.update { it.copy(addresses = emptyList(), addressLoading = false) }
            return
        }
        addressJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            _state.update { it.copy(addressLoading = true) }
            when (val r = getAddressBook(query.trim())) {
                is Result.Success -> _state.update { it.copy(addressLoading = false, addresses = r.data) }
                is Result.Error -> _state.update { it.copy(addressLoading = false, addresses = emptyList()) }
                Result.Loading -> Unit
            }
        }
    }

    fun selectAddress(address: AdresDostawy) {
        _state.update {
            it.copy(
                selectedAddress = address,
                addressQuery = address.label.ifBlank { address.adres.orEmpty() },
                addresses = emptyList(),
            )
        }
    }

    fun setQuery(query: String) {
        _state.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            search()
        }
    }

    fun search() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val r = searchTowary(_state.value.query.trim().ifBlank { null })) {
                is Result.Success -> _state.update { it.copy(loading = false, products = r.data) }
                is Result.Error -> _state.update { it.copy(loading = false, error = r.message) }
                Result.Loading -> Unit
            }
        }
    }

    // ── Product sheet ──────────────────────────────────────────────────────────

    fun selectProduct(towar: TowarNawoz) {
        _state.update {
            it.copy(
                selected = towar,
                magazyny = emptyList(),
                magazynyLoading = true,
                qty = TowarNawoz.FULL_TRUCK_TONS,
                rabatKwotowy = 0.0,
                pricing = null,
                warianty = emptyList(),
                selectedWariant = null,
                message = null,
            )
        }
        viewModelScope.launch {
            (getMagazyny(towar.itemId) as? Result.Success)?.let { res ->
                _state.update { it.copy(magazynyLoading = false, magazyny = res.data) }
            } ?: _state.update { it.copy(magazynyLoading = false) }
        }
        recalcPricing()
    }

    fun closeSheet() {
        _state.update {
            it.copy(selected = null, pricing = null, warianty = emptyList(), selectedWariant = null)
        }
    }

    fun setQty(qty: Double) {
        _state.update { it.copy(qty = qty.coerceAtLeast(0.0)) }
    }

    /** Discount (kwotowy) → price. Backend is the source of truth; debounced. */
    fun setRabat(rabat: Double) {
        _state.update { it.copy(rabatKwotowy = rabat) }
        recalcPricing()
    }

    private fun recalcPricing() {
        val s = _state.value
        val item = s.selected ?: return
        val paymTermId = s.paymTermId ?: return // no term yet → keep base price only
        pricingJob?.cancel()
        pricingJob = viewModelScope.launch {
            delay(PRICING_DEBOUNCE_MS)
            _state.update { it.copy(pricingLoading = true) }
            val r = calcPricing(
                PricingZapytanie(
                    itemId = item.itemId,
                    cennik = Cennik.BAZOWY,
                    paymTermId = paymTermId,
                    rabatKwotowy = _state.value.rabatKwotowy,
                ),
            )
            when (r) {
                is Result.Success -> _state.update { it.copy(pricingLoading = false, pricing = r.data) }
                is Result.Error -> _state.update { it.copy(pricingLoading = false, message = r.message) }
                Result.Loading -> Unit
            }
        }
    }

    /** Target sale price → required discount (reverse). */
    fun setTargetPrice(cenaSprzedazy: Double) {
        val s = _state.value
        val item = s.selected ?: return
        val paymTermId = s.paymTermId ?: return
        pricingJob?.cancel()
        pricingJob = viewModelScope.launch {
            delay(PRICING_DEBOUNCE_MS)
            _state.update { it.copy(pricingLoading = true) }
            val r = calcPricingReverse(
                PricingReverseZapytanie(
                    itemId = item.itemId,
                    cennik = Cennik.BAZOWY,
                    paymTermId = paymTermId,
                    cenaSprzedazy = cenaSprzedazy,
                ),
            )
            when (r) {
                is Result.Success -> _state.update {
                    it.copy(pricingLoading = false, pricing = r.data, rabatKwotowy = r.data.cenaBazowa - r.data.cenaSprzedazy)
                }
                is Result.Error -> _state.update { it.copy(pricingLoading = false, message = r.message) }
                Result.Loading -> Unit
            }
        }
    }

    // ── Logistics ──────────────────────────────────────────────────────────────

    fun loadWarianty() {
        val s = _state.value
        val item = s.selected ?: return
        _state.update { it.copy(wariantyLoading = true, message = null) }
        viewModelScope.launch {
            val r = getWarianty(
                WariantyZapytanie(
                    itemId = item.itemId,
                    qtyTons = s.qty,
                    addressBookId = s.selectedAddress?.id,
                    deliveryAddress = s.selectedAddress?.label,
                ),
            )
            when (r) {
                is Result.Success -> _state.update {
                    // Repository already sorts ascending by cost — auto-select the cheapest.
                    it.copy(wariantyLoading = false, warianty = r.data, selectedWariant = r.data.firstOrNull())
                }
                is Result.Error -> _state.update { it.copy(wariantyLoading = false, message = r.message) }
                Result.Loading -> Unit
            }
        }
    }

    fun selectWariant(wariant: WariantLogistyczny) {
        _state.update { it.copy(selectedWariant = wariant) }
    }

    // ── Add to cart ────────────────────────────────────────────────────────────

    fun addToCart() {
        val s = _state.value
        val item = s.selected ?: return
        if (s.adding) return
        _state.update { it.copy(adding = true, message = null) }
        viewModelScope.launch {
            val r = addPozycja(
                koszykId,
                NowaPozycja(
                    itemId = item.itemId,
                    qty = s.qty,
                    magazynId = s.effectiveMagazynId,
                    cenaOverride = s.pricing?.cenaSprzedazy,
                ),
            )
            when (r) {
                is Result.Success -> _state.update { it.copy(adding = false, added = true, selected = null) }
                is Result.Error -> _state.update { it.copy(adding = false, message = r.message) }
                Result.Loading -> Unit
            }
        }
    }

    fun consumeAdded() = _state.update { it.copy(added = false) }
    fun consumeMessage() = _state.update { it.copy(message = null) }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
        const val PRICING_DEBOUNCE_MS = 400L
    }
}
