package com.ossadkowski.crm.mobile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.model.ZamrozenieDto
import com.ossadkowski.crm.mobile.data.model.CalendarDay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HrKalendarzViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    private val _zamrozenia = MutableLiveData<NetworkResult<List<ZamrozenieDto>>>()
    val zamrozenia: LiveData<NetworkResult<List<ZamrozenieDto>>> = _zamrozenia

    private val _daysOfMonth = MutableLiveData<List<CalendarDay>>()
    val daysOfMonth: LiveData<List<CalendarDay>> = _daysOfMonth

    private var currentMonth: Calendar = Calendar.getInstance()

    init {
        currentMonth.set(Calendar.DAY_OF_MONTH, 1)
        loadMonthData()
    }

    fun getCurrentMonthDate(): Date = currentMonth.time

    fun prevMonth() {
        currentMonth.add(Calendar.MONTH, -1)
        loadMonthData()
    }

    fun nextMonth() {
        currentMonth.add(Calendar.MONTH, 1)
        loadMonthData()
    }

    private fun loadMonthData() {
        val rok = currentMonth.get(Calendar.YEAR)
        // parametry zapytania biorą miesiąc 1-12
        val miesiac = currentMonth.get(Calendar.MONTH) + 1 
        
        _zamrozenia.value = NetworkResult.Loading()
        
        viewModelScope.launch {
            try {
                val list = api.getZamrozeniaMiesiac(rok, miesiac)
                _zamrozenia.value = NetworkResult.Success(list)
                generateDaysWithFreezes(list)
            } catch (e: Exception) {
                _zamrozenia.value = NetworkResult.Error(e.message ?: "Błąd pobierania zamrożeń")
                generateDaysWithFreezes(emptyList()) // generate empty just to show calendar matrix
            }
        }
    }

    private fun generateDaysWithFreezes(freezy: List<ZamrozenieDto>) {
         val days = mutableListOf<CalendarDay>()
         val cal = currentMonth.clone() as Calendar

         val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
         var startDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
         if (startDayOfWeek == 0) startDayOfWeek = 7 // adjust to Monday start

         // Previous month filler
         val prevCal = cal.clone() as Calendar
         prevCal.add(Calendar.MONTH, -1)
         val prevMaxDays = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)

         for (i in 1 until startDayOfWeek) {
             val dayNum = prevMaxDays - startDayOfWeek + i + 1
             val date = prevCal.clone() as Calendar
             date.set(Calendar.DAY_OF_MONTH, dayNum)
             
             // Check if it's today
             val isToday = isSameDay(date, Calendar.getInstance())
             days.add(CalendarDay(date.time, dayNum, false, isToday))
         }

         val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

         // Current month
         for (i in 1..maxDays) {
             val date = cal.clone() as Calendar
             date.set(Calendar.DAY_OF_MONTH, i)
             
             // Check if it's frozen
             val dateStr = format.format(date.time)
             var isFrozen = false
             
             run checkLoop@{
                 freezy.forEach { z ->
                     val od = z.dataOd?.substring(0, 10) ?: ""
                     val do_ = z.dataDo?.substring(0, 10) ?: ""
                     if (od.isNotEmpty() && do_.isNotEmpty()) {
                         if (dateStr in od..do_) {
                             isFrozen = true
                             return@checkLoop
                         }
                     }
                 }
             }

             // We can map frozen status by passing it conceptually or just map it later in adapter. 
             // We'll override 'hasTasks' to represent 'isFrozen' since CalendarGridAdapter supports the red dot.
             val isToday = isSameDay(date, Calendar.getInstance())
             days.add(CalendarDay(date.time, i, true, isToday, hasTasks = isFrozen, hasZamrozenia = isFrozen)) 
         }

         // Next month filler
         val totalCells = if (days.size > 35) 42 else 35
         var nextDayCounter = 1
         val nextCal = cal.clone() as Calendar
         nextCal.add(Calendar.MONTH, 1)
         while (days.size < totalCells) {
             val dayNum = nextDayCounter++
             val date = nextCal.clone() as Calendar
             date.set(Calendar.DAY_OF_MONTH, dayNum)
             val isToday = isSameDay(date, Calendar.getInstance())
             days.add(CalendarDay(date.time, dayNum, false, isToday))
         }

         _daysOfMonth.value = days
    }
    
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
