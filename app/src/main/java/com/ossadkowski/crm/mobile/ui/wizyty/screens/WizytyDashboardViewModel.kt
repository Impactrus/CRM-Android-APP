package com.ossadkowski.crm.mobile.ui.wizyty.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.wizyty.location.WizytySessionController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WizytyDashboardViewModel @Inject constructor(
    private val sessionController: WizytySessionController,
) : ViewModel() {

    val sessionActive: StateFlow<Boolean> = sessionController.active

    /** Call only after the required foreground location permissions are granted. */
    fun startSession() {
        viewModelScope.launch { sessionController.start() }
    }

    fun stopSession() {
        sessionController.stop()
    }
}
