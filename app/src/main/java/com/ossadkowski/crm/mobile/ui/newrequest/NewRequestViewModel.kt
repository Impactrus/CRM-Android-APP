package com.ossadkowski.crm.mobile.ui.newrequest

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.*
import com.ossadkowski.crm.mobile.data.repository.NewRequestRepository
import kotlinx.coroutines.launch

class NewRequestViewModel(
    private val repository: NewRequestRepository = NewRequestRepository()
) : ViewModel() {

    private val _typy = MutableLiveData<NetworkResult<List<SlownikItemDto>>>()
    val typy: LiveData<NetworkResult<List<SlownikItemDto>>> = _typy

    private val _rodzajeUrlopu = MutableLiveData<NetworkResult<List<SlownikItemDto>>>()
    val rodzajeUrlopu: LiveData<NetworkResult<List<SlownikItemDto>>> = _rodzajeUrlopu

    private val _uzytkownicy = MutableLiveData<NetworkResult<List<SlownikItemDto>>>()
    val uzytkownicy: LiveData<NetworkResult<List<SlownikItemDto>>> = _uzytkownicy

    private val _submitResult = MutableLiveData<NetworkResult<CreateWniosekResponse>>()
    val submitResult: LiveData<NetworkResult<CreateWniosekResponse>> = _submitResult

    fun loadFormData() {
        viewModelScope.launch {
            val typyResult = repository.getTypy()
            if (typyResult is NetworkResult.Success) {
                val filtered = typyResult.data?.filter {
                    !it.nazwa.contains("sobotę", ignoreCase = true) &&
                    !it.nazwa.contains("niedzielę", ignoreCase = true)
                } ?: emptyList()
                _typy.value = NetworkResult.Success(filtered)
            } else {
                _typy.value = typyResult
            }
            _rodzajeUrlopu.value = repository.getRodzajeUrlopu()
            _uzytkownicy.value = repository.getUzytkownicy()
        }
    }

    fun submitWniosek(request: CreateWniosekRequest, photoUris: List<Uri>, context: Context) {
        _submitResult.value = NetworkResult.Loading()
        viewModelScope.launch {
            _submitResult.value = repository.createWniosekWithPhotos(request, photoUris, context)
        }
    }
}
