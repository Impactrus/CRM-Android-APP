package com.ossadkowski.app.ui.editrequest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.model.*
import com.ossadkowski.app.data.repository.EditRequestRepository
import com.ossadkowski.app.data.repository.NewRequestRepository
import kotlinx.coroutines.launch

class EditRequestViewModel(
    private val repository: EditRequestRepository = EditRequestRepository(),
    private val formRepository: NewRequestRepository = NewRequestRepository()
) : ViewModel() {

    private val _detail = MutableLiveData<NetworkResult<WniosekDetailDto>>()
    val detail: LiveData<NetworkResult<WniosekDetailDto>> = _detail

    private val _updateResult = MutableLiveData<NetworkResult<Any>>()
    val updateResult: LiveData<NetworkResult<Any>> = _updateResult

    private val _typy = MutableLiveData<NetworkResult<List<SlownikItemDto>>>()
    val typy: LiveData<NetworkResult<List<SlownikItemDto>>> = _typy

    private val _rodzajeUrlopu = MutableLiveData<NetworkResult<List<SlownikItemDto>>>()
    val rodzajeUrlopu: LiveData<NetworkResult<List<SlownikItemDto>>> = _rodzajeUrlopu

    private val _uzytkownicy = MutableLiveData<NetworkResult<List<SlownikItemDto>>>()
    val uzytkownicy: LiveData<NetworkResult<List<SlownikItemDto>>> = _uzytkownicy

    fun loadDetail(id: Int) {
        _detail.value = NetworkResult.Loading()
        viewModelScope.launch {
            _detail.value = repository.getDetail(id)
        }
    }

    fun loadFormData() {
        viewModelScope.launch {
            _typy.value = formRepository.getTypy()
            _rodzajeUrlopu.value = formRepository.getRodzajeUrlopu()
            _uzytkownicy.value = formRepository.getUzytkownicy()
        }
    }

    fun update(id: Int, request: CreateWniosekRequest) {
        _updateResult.value = NetworkResult.Loading()
        viewModelScope.launch {
            _updateResult.value = repository.update(id, request)
        }
    }
}
