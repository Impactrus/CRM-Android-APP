package com.ossadkowski.app.ui.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.model.ZamrozenieDto
import com.ossadkowski.app.data.repository.CalendarRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class CalendarViewModel(
    private val repository: CalendarRepository = CalendarRepository()
) : ViewModel() {

    private val _zamrozenia = MutableLiveData<NetworkResult<List<ZamrozenieDto>>>()
    val zamrozenia: LiveData<NetworkResult<List<ZamrozenieDto>>> = _zamrozenia

    var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

    fun loadMonth() {
        _zamrozenia.value = NetworkResult.Loading()
        viewModelScope.launch {
            _zamrozenia.value = repository.getZamrozeniaMiesiac(currentYear, currentMonth)
        }
    }

    fun prevMonth() {
        if (currentMonth == 1) { currentMonth = 12; currentYear-- }
        else currentMonth--
        loadMonth()
    }

    fun nextMonth() {
        if (currentMonth == 12) { currentMonth = 1; currentYear++ }
        else currentMonth++
        loadMonth()
    }
}
