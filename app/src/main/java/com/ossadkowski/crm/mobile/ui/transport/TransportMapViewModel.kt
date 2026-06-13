package com.ossadkowski.crm.mobile.ui.transport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransportMapViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    private val _vehiclesState = MutableStateFlow<NetworkResult<List<TransportVehicle>>>(NetworkResult.Loading())
    val vehiclesState: StateFlow<NetworkResult<List<TransportVehicle>>> = _vehiclesState.asStateFlow()

    private val _startSuggestions = MutableStateFlow<List<GeocodeSuggestionDto>>(emptyList())
    val startSuggestions: StateFlow<List<GeocodeSuggestionDto>> = _startSuggestions.asStateFlow()

    private val _endSuggestions = MutableStateFlow<List<GeocodeSuggestionDto>>(emptyList())
    val endSuggestions: StateFlow<List<GeocodeSuggestionDto>> = _endSuggestions.asStateFlow()

    private val _routeState = MutableStateFlow<NetworkResult<RouteResponseDto>?>(null)
    val routeState: StateFlow<NetworkResult<RouteResponseDto>?> = _routeState.asStateFlow()

    init {
        fetchVehicles()
    }

    fun fetchVehicles() {
        viewModelScope.launch {
            _vehiclesState.value = NetworkResult.Loading()
            try {
                val response = api.getTransportVehicles()
                _vehiclesState.value = NetworkResult.Success(response)
            } catch (e: Exception) {
                _vehiclesState.value = NetworkResult.Error(e.message ?: "Wystąpił błąd podczas pobierania pojazdów")
            }
        }
    }

    fun searchStartGeocode(query: String) {
        if (query.length < 3) {
            _startSuggestions.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                val response = api.searchGeocode(query)
                _startSuggestions.value = response
            } catch (e: Exception) {
                _startSuggestions.value = emptyList()
            }
        }
    }

    fun searchEndGeocode(query: String) {
        if (query.length < 3) {
            _endSuggestions.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                val response = api.searchGeocode(query)
                _endSuggestions.value = response
            } catch (e: Exception) {
                _endSuggestions.value = emptyList()
            }
        }
    }

    fun clearSuggestions() {
        _startSuggestions.value = emptyList()
        _endSuggestions.value = emptyList()
    }

    fun calculateRoute(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
        weight: Int,
        height: Double,
        width: Double,
        length: Double,
        axleWeight: Int,
        axles: Int,
        maxSpeed: Int?
    ) {
        _routeState.value = NetworkResult.Loading()
        viewModelScope.launch {
            try {
                val request = CalculateRouteRequest(
                    startLat = startLat,
                    startLng = startLng,
                    endLat = endLat,
                    endLng = endLng,
                    truckType = TruckTypeDto(
                        vehicleWeight = weight,
                        vehicleHeight = height,
                        vehicleWidth = width,
                        vehicleLength = length,
                        vehicleAxleWeight = axleWeight,
                        vehicleNumberOfAxles = axles,
                        vehicleMaxSpeed = maxSpeed
                    )
                )
                val response = api.calculateRoute(request)
                _routeState.value = NetworkResult.Success(response)
            } catch (e: Exception) {
                _routeState.value = NetworkResult.Error(e.message ?: "Błąd obliczania trasy")
            }
        }
    }

    fun clearRoute() {
        _routeState.value = null
    }
}
