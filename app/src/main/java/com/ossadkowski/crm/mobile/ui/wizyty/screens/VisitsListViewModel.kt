package com.ossadkowski.crm.mobile.ui.wizyty.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitEvent
import com.ossadkowski.crm.mobile.domain.wizyty.usecase.ConfirmVisitUseCase
import com.ossadkowski.crm.mobile.domain.wizyty.usecase.ObserveVisitsUseCase
import com.ossadkowski.crm.mobile.domain.wizyty.usecase.RejectVisitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VisitsListViewModel @Inject constructor(
    observeVisits: ObserveVisitsUseCase,
    private val confirmVisit: ConfirmVisitUseCase,
    private val rejectVisit: RejectVisitUseCase,
) : ViewModel() {

    val visits: StateFlow<List<VisitEvent>> = observeVisits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun confirm(id: Long) {
        viewModelScope.launch { confirmVisit(id) }
    }

    fun reject(id: Long) {
        viewModelScope.launch { rejectVisit(id) }
    }
}
