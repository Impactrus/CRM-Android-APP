package com.ossadkowski.crm.mobile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import kotlinx.coroutines.launch

data class HrNadgodzinyGrouped(
    val userId: Int,
    val name: String,
    val fname: String,
    val depart: String,
    var q1: Double = 0.0,
    var q2: Double = 0.0,
    var q3: Double = 0.0,
    var q4: Double = 0.0,
    var totalNaliczone: Double = 0.0,
    var totalWykorzystane: Double = 0.0,
    var totalSaldo: Double = 0.0
)

class HrNadgodzinyViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    private val _data = MutableLiveData<NetworkResult<List<HrNadgodzinyGrouped>>>()
    val data: LiveData<NetworkResult<List<HrNadgodzinyGrouped>>> = _data

    fun loadOvertime(rok: Int) {
        _data.value = NetworkResult.Loading()
        viewModelScope.launch {
            try {
                val list = api.getHrNadgodziny(rok)
                
                // Grupowanie JSONów kwartalnych w strukturę per pracownik
                val groupedMap = mutableMapOf<Int, HrNadgodzinyGrouped>()
                for (item in list) {
                    val g = groupedMap.getOrPut(item.userId) {
                        HrNadgodzinyGrouped(
                            userId = item.userId,
                            name = item.name ?: "",
                            fname = item.fname ?: "",
                            depart = item.depart ?: ""
                        )
                    }
                    val saldo = item.saldo ?: 0.0
                    when (item.kwartal) {
                        1 -> g.q1 += saldo
                        2 -> g.q2 += saldo
                        3 -> g.q3 += saldo
                        4 -> g.q4 += saldo
                    }
                    g.totalNaliczone += (item.godzinyNaliczone ?: 0.0)
                    g.totalWykorzystane += (item.godzinyWykorzystane ?: 0.0)
                    g.totalSaldo += saldo
                }
                
                val resultList = groupedMap.values.sortedBy { it.fname }
                _data.value = NetworkResult.Success(resultList)
            } catch (e: Exception) {
                _data.value = NetworkResult.Error("Błąd ładowania nadgodzin: ${e.message}")
            }
        }
    }
}
