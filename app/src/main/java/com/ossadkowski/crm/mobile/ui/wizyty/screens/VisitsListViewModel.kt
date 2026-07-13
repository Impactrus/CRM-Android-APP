package com.ossadkowski.crm.mobile.ui.wizyty.screens

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.ossadkowski.crm.mobile.data.wizyty.location.DetectionTuning
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitEvent
import com.ossadkowski.crm.mobile.domain.wizyty.repository.VisitRepository
import com.ossadkowski.crm.mobile.domain.wizyty.usecase.ConfirmVisitUseCase
import com.ossadkowski.crm.mobile.domain.wizyty.usecase.ObserveVisitsUseCase
import com.ossadkowski.crm.mobile.domain.wizyty.usecase.RejectVisitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VisitsListViewModel @Inject constructor(
    observeVisits: ObserveVisitsUseCase,
    private val confirmVisit: ConfirmVisitUseCase,
    private val rejectVisit: RejectVisitUseCase,
    private val repo: VisitRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val visits: StateFlow<List<VisitEvent>> = observeVisits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val locationClient = LocationServices.getFusedLocationProviderClient(context)
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    init {
        refreshLocation()
    }

    fun refreshLocation() {
        try {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                locationClient.lastLocation.addOnSuccessListener { loc ->
                    _currentLocation.value = loc
                }
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    fun isWithinRange(visit: VisitEvent): Boolean {
        val visitLat = visit.lat ?: return true
        val visitLng = visit.lng ?: return true
        val currentLoc = _currentLocation.value ?: return false
        val results = FloatArray(1)
        Location.distanceBetween(visitLat, visitLng, currentLoc.latitude, currentLoc.longitude, results)
        return results[0] <= DetectionTuning.GEOFENCE_RADIUS_M
    }

    fun confirm(id: Long) {
        viewModelScope.launch { confirmVisit(id) }
    }

    fun reject(id: Long) {
        viewModelScope.launch { rejectVisit(id) }
    }

    fun updateNote(id: Long, note: String) {
        viewModelScope.launch {
            repo.updateNote(id, note.trim().takeIf { it.isNotBlank() })
        }
     }

    fun delete(id: Long) {
        viewModelScope.launch {
            repo.delete(id)
        }
    }
}
