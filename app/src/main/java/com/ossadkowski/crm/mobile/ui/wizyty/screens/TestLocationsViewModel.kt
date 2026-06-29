package com.ossadkowski.crm.mobile.ui.wizyty.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.wizyty.location.WizytySessionController
import com.ossadkowski.crm.mobile.domain.wizyty.model.ContractorLocation
import com.ossadkowski.crm.mobile.domain.wizyty.usecase.DeleteTestLocationUseCase
import com.ossadkowski.crm.mobile.domain.wizyty.usecase.ObserveTestLocationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestLocationsViewModel @Inject constructor(
    observeTestLocations: ObserveTestLocationsUseCase,
    private val deleteTestLocation: DeleteTestLocationUseCase,
    private val sessionController: WizytySessionController,
) : ViewModel() {

    val locations: StateFlow<List<ContractorLocation>> = observeTestLocations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(key: String) {
        viewModelScope.launch {
            deleteTestLocation(key)
            // Stop geofencing the removed location.
            sessionController.removeGeofence(key)
        }
    }
}
