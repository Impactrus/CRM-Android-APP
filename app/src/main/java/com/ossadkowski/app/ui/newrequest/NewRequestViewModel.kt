package com.ossadkowski.app.ui.newrequest

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.model.*
import com.ossadkowski.app.data.repository.NewRequestRepository
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
            _typy.value = repository.getTypy()
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
