package com.ossadkowski.crm.mobile.ui.transport

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.model.CreateTransportRequest
import com.ossadkowski.crm.mobile.data.repository.TransportRepository
import kotlinx.coroutines.launch

class TransportCenyNewViewModel : ViewModel() {
    private val repository = TransportRepository(RetrofitClient.apiService)

    var kontraktAx by mutableStateOf<String?>(null)
    var kontrahentId by mutableStateOf("")
    var kontrahentNazwa by mutableStateOf("")
    var towar by mutableStateOf("")
    var ilosc by mutableStateOf("")
    var adresZaladunku by mutableStateOf("")
    var odbiorca by mutableStateOf("")
    var adresOdbioru by mutableStateOf("")
    var szacowanyKoszt by mutableStateOf("")
    var komentarz by mutableStateOf("")
    var skladId by mutableStateOf(1) // Główny

    var isSubmitting by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var isSuccess by mutableStateOf(false)
        private set

    fun submitRequest() {
        if (kontrahentNazwa.isBlank() || towar.isBlank() || ilosc.isBlank() || szacowanyKoszt.isBlank()) {
            error = "Proszę wypełnić wymagane pola (*)"
            return
        }

        val iloscVal = ilosc.toDoubleOrNull() ?: 0.0
        val kosztVal = szacowanyKoszt.toDoubleOrNull() ?: 0.0

        if (kosztVal <= 0.0) {
            error = "Szacowany koszt musi być większy niż 0"
            return
        }

        viewModelScope.launch {
            isSubmitting = true
            error = null
            val request = CreateTransportRequest(
                kontraktAx = kontraktAx,
                kontrahentId = kontrahentId,
                kontrahentNazwa = kontrahentNazwa,
                towar = towar,
                ilosc = iloscVal,
                skladId = skladId,
                adresZaladunku = adresZaladunku,
                odbiorca = odbiorca,
                adresOdbioru = adresOdbioru,
                szacowanyKoszt = kosztVal,
                komentarz = komentarz
            )

            when (val result = repository.createTransportCena(request)) {
                is NetworkResult.Success -> {
                    isSuccess = true
                }
                is NetworkResult.Error -> {
                    error = result.message ?: "Wystąpił błąd podczas wysyłania wniosku"
                }
                is NetworkResult.Loading -> { /* no-op */ }
            }
            isSubmitting = false
        }
    }
}
