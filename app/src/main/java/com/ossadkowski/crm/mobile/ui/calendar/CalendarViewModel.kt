package com.ossadkowski.crm.mobile.ui.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.CalendarDay
import com.ossadkowski.crm.mobile.data.model.TaskListItemDto
import com.ossadkowski.crm.mobile.data.model.ZamrozenieDto
import com.ossadkowski.crm.mobile.data.repository.CalendarRepository
import com.ossadkowski.crm.mobile.data.repository.TasksRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarViewModel(
    private val calendarRepository: CalendarRepository = CalendarRepository(),
    private val tasksRepository: TasksRepository = TasksRepository()
) : ViewModel() {

    private val _days = MutableLiveData<List<CalendarDay>>()
    val days: LiveData<List<CalendarDay>> = _days

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    var currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)
    var currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1 // 1-12

    private val _selectedDayTasks = MutableLiveData<List<TaskListItemDto>>()
    val selectedDayTasks: LiveData<List<TaskListItemDto>> = _selectedDayTasks

    private var allTasks: List<TaskListItemDto> = emptyList()
    private var allZamrozenia: List<ZamrozenieDto> = emptyList()
    var selectedDate: Date? = null

    // Normalizacja terminu do yyyy-MM-dd z różnych formatów
    private fun String?.toDateOnly(): String {
        if (this.isNullOrBlank()) return ""
        val s = this.take(10)
        // Jeśli jest w formacie dd.MM.yyyy lub dd-MM-yyyy, zamieńmy to:
        if (s.length == 10 && (s[2] == '.' || s[2] == '-')) {
            val day = s.substring(0, 2)
            val month = s.substring(3, 5)
            val year = s.substring(6, 10)
            return "$year-$month-$day"
        }
        return s // Zakładamy iso yyyy-MM-dd
    }
    fun loadMonth() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val zamrozeniaDeferred = async { calendarRepository.getZamrozeniaMiesiac(currentYear, currentMonth) }
                val tasksDeferred = async { tasksRepository.getTasksForMonth(currentYear, currentMonth) }

                val zamrozeniaResult = zamrozeniaDeferred.await()
                val tasksResult = tasksDeferred.await()

                if (zamrozeniaResult is NetworkResult.Error || tasksResult is NetworkResult.Error) {
                    _error.value = "Błąd podczas pobierania danych"
                }

                val zamrozenia = (zamrozeniaResult as? NetworkResult.Success)?.data ?: emptyList()
                val tasks = (tasksResult as? NetworkResult.Success)?.data ?: emptyList()

                allTasks = tasks
                allZamrozenia = zamrozenia

                generateDays(zamrozenia, tasks)
                
                // If today is in the current view, select it
                if (selectedDate == null) {
                    selectToday()
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Wystąpił nieoczekiwany błąd"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateDays(zamrozenia: List<ZamrozenieDto>, tasks: List<TaskListItemDto>) {
        val daysList = mutableListOf<CalendarDay>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        // Map elements to dates for faster lookup (normalizacja: bierzemy tylko yyyy-MM-dd)
        val tasksByDate = tasks.groupBy { it.termin.toDateOnly() }
        
        // Dzisiejsza data do porównania
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
        val cal = Calendar.getInstance()
        cal.set(currentYear, currentMonth - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        
        // Ile dni z poprzedniego miesiąca pokazać (poniedziałek jako pierwszy)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1-7 (Sun-Sat)
        val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2
        
        cal.add(Calendar.DAY_OF_MONTH, -offset)

        // Zawsze 42 kafelki (6 tygodni)
        for (i in 0 until 42) {
            val date = cal.time
            val dateStr = dateFormat.format(date)
            val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
            val isCurrentMonth = cal.get(Calendar.MONTH) == (currentMonth - 1)
            val isToday = cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                          cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
            
            // Task matching
            val dayTasks = tasksByDate[dateStr] ?: emptyList()
            val hasTasks = dayTasks.isNotEmpty()
            
            // Zamrożenie matching (needs to check range)
            val dayZamrozenie = zamrozenia.find { z ->
                try {
                    val start = z.dataOd ?: ""
                    val end = z.dataDo ?: ""
                    dateStr >= start && dateStr <= end
                } catch (e: Exception) { false }
            }
            val hasZamrozenia = dayZamrozenie != null

            // Abbreviation (skrót)
            val weekdayNames = arrayOf("Ndz", "Pn", "Wt", "Śr", "Czw", "Pt", "Sob")
            val weekday = weekdayNames[cal.get(Calendar.DAY_OF_WEEK) - 1]
            
            val taskTitle = dayTasks.firstOrNull()?.tytul
            val taskSummary = null // Nie wymuszamy tekstu ze skrótem dnia tygodnia

            daysList.add(CalendarDay(
                date = date,
                dayOfMonth = dayOfMonth,
                isCurrentMonth = isCurrentMonth,
                isToday = isToday,
                isSelected = selectedDate?.let { dateFormat.format(it) == dateStr } ?: false,
                hasTasks = hasTasks,
                hasZamrozenia = hasZamrozenia,
                taskTitle = taskTitle,
                taskSummary = taskSummary
            ))
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        _days.value = daysList
    }

    fun selectDay(date: Date) {
        selectedDate = date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = dateFormat.format(date)
        
        // Update selection state in the day list
        _days.value = _days.value?.map { it.copy(isSelected = dateFormat.format(it.date) == dateStr) }
        
        // Filter tasks for the selected day (normalizujemy termin)
        _selectedDayTasks.value = allTasks.filter { it.termin.toDateOnly() == dateStr }
    }

    private fun selectToday() {
        val today = Calendar.getInstance().time
        selectDay(today)
    }

    fun prevMonth() {
        if (currentMonth == 1) {
            currentMonth = 12
            currentYear--
        } else {
            currentMonth--
        }
        loadMonth()
    }

    fun nextMonth() {
        if (currentMonth == 12) {
            currentMonth = 1
            currentYear++
        } else {
            currentMonth++
        }
        loadMonth()
    }
}
