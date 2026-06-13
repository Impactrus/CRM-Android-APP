package com.ossadkowski.crm.mobile.ui.vacation

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.SessionManager
import com.ossadkowski.crm.mobile.data.model.*
import com.ossadkowski.crm.mobile.data.repository.VacationRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.GregorianCalendar

class VacationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = VacationRepository()
    private val calendarRepository = com.ossadkowski.crm.mobile.data.repository.CalendarRepository()
    private val sessionManager = SessionManager(application)

    var currentYear by mutableStateOf(Calendar.getInstance().get(Calendar.YEAR))
        private set

    var myPlan by mutableStateOf<VacationPlanDto?>(null)
        private set

    var globalFreezes by mutableStateOf<List<VacationFreezeDto>>(emptyList())
        private set

    var localPlannedDates by mutableStateOf<Set<String>>(emptySet())

        private set

    var submissionInfo by mutableStateOf<VacationSubmissionDto?>(null)
        private set

    var teamPlans by mutableStateOf<List<TeamEmployeePlanDto>>(emptyList())
        private set

    var pendingPlans by mutableStateOf<List<TeamEmployeePlanDto>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    val userRole: String
        get() = sessionManager.role

    val isManager: Boolean
        get() = sessionManager.role.contains("Manager", ignoreCase = true) ||
                sessionManager.role.contains("HR", ignoreCase = true) ||
                sessionManager.role.contains("Admin", ignoreCase = true)

    val currentStatus: String
        get() = submissionInfo?.current?.status ?: "draft"

    val rejectReason: String
        get() = submissionInfo?.current?.rejectReason ?: ""

    init {
        loadAll()
    }

    fun changeYear(offset: Int) {
        currentYear += offset
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            isLoading = true
            error = null
            loadMyPlan()
            loadSubmissionInfo()
            loadGlobalFreezes()
            if (isManager) {
                loadTeamPlans()
                loadPendingPlans()
            }
            isLoading = false
        }
    }

    private suspend fun loadGlobalFreezes() {
        val list = mutableListOf<VacationFreezeDto>()
        for (month in 1..12) {
            when (val result = calendarRepository.getZamrozeniaMiesiac(currentYear, month)) {
                is NetworkResult.Success -> {
                    result.data?.forEach { z ->
                        list.add(
                            VacationFreezeDto(
                                dataOd = z.dataOd ?: "",
                                dataDo = z.dataDo ?: "",
                                opis = z.opis,
                                dzial = z.dzial
                            )
                        )
                    }
                }
                else -> {}
            }
        }
        globalFreezes = list
    }


    private suspend fun loadMyPlan() {
        when (val result = repository.getMyVacationPlan(currentYear)) {
            is NetworkResult.Success -> {
                myPlan = result.data
                android.util.Log.d("VacationViewModel", "MyPlan freezes: ${result.data?.freezes}")
                localPlannedDates = result.data?.plannedDates?.toSet() ?: emptySet()
            }
            is NetworkResult.Error -> {
                error = result.message ?: "Błąd podczas pobierania planu"
            }
            else -> {}
        }
    }

    private suspend fun loadSubmissionInfo() {
        when (val result = repository.getVacationSubmission(currentYear)) {
            is NetworkResult.Success -> {
                submissionInfo = result.data
            }
            else -> {}
        }
    }

    private suspend fun loadTeamPlans() {
        when (val result = repository.getTeamVacationPlans(currentYear)) {
            is NetworkResult.Success -> {
                teamPlans = result.data ?: emptyList()
            }
            else -> {}
        }
    }

    private suspend fun loadPendingPlans() {
        when (val result = repository.getPendingVacationPlans()) {
            is NetworkResult.Success -> {
                pendingPlans = result.data ?: emptyList()
            }
            else -> {}
        }
    }

    fun toggleDay(dateStr: String) {
        // Can only edit in draft, rejected or revoked status
        val status = currentStatus
        if (status != "draft" && status != "rejected" && status != "revoked") {
            showToast("Plan został już wysłany do akceptacji lub zatwierdzony i nie można go edytować.")
            return
        }

        val limit = myPlan?.balance?.vacDays ?: 26
        val prevLimit = myPlan?.balance?.prevLimitD ?: 0
        val totalLimit = limit + prevLimit
        val plannedCount = localPlannedDates.size

        if (localPlannedDates.contains(dateStr)) {
            localPlannedDates = localPlannedDates - dateStr
        } else {
            if (plannedCount >= totalLimit) {
                showToast("Przekroczono limit urlopowy ($totalLimit dni)!")
                return
            }




            localPlannedDates = localPlannedDates + dateStr
        }
    }

    fun saveDraft() {
        val original = myPlan?.plannedDates?.toSet() ?: emptySet()
        val addDates = (localPlannedDates - original).toList()
        val removeDates = (original - localPlannedDates).toList()

        if (addDates.isEmpty() && removeDates.isEmpty()) {
            showToast("Plan nie uległ zmianie.")
            return
        }

        viewModelScope.launch {
            isLoading = true
            when (val result = repository.saveVacationPlanBulk(currentYear, addDates, removeDates)) {
                is NetworkResult.Success -> {
                    showToast("Szkic planu urlopowego został zapisany.")
                    loadMyPlan()
                    loadSubmissionInfo()
                }
                is NetworkResult.Error -> {
                    showToast("Nie udało się zapisać planu: ${result.message}")
                }
                else -> {}
            }
            isLoading = false
        }
    }

    fun submitPlan() {
        viewModelScope.launch {
            isLoading = true
            when (val result = repository.submitVacationPlan(currentYear)) {
                is NetworkResult.Success -> {
                    showToast("Plan został wysłany do akceptacji.")
                    loadAll()
                }
                is NetworkResult.Error -> {
                    showToast("Nie udało się wysłać planu: ${result.message}")
                }
                else -> {}
            }
            isLoading = false
        }
    }

    fun revokePlan() {
        viewModelScope.launch {
            isLoading = true
            when (val result = repository.revokeVacationPlan(currentYear)) {
                is NetworkResult.Success -> {
                    showToast("Plan został wycofany do szkicu.")
                    loadAll()
                }
                is NetworkResult.Error -> {
                    showToast("Nie udało się wycofać planu: ${result.message}")
                }
                else -> {}
            }
            isLoading = false
        }
    }

    fun clearPlan() {
        viewModelScope.launch {
            isLoading = true
            when (val result = repository.clearVacationPlan(currentYear)) {
                is NetworkResult.Success -> {
                    showToast("Wszystkie zaplanowane dni zostały usunięte.")
                    loadAll()
                }
                is NetworkResult.Error -> {
                    showToast("Nie udało się wyczyścić planu: ${result.message}")
                }
                else -> {}
            }
            isLoading = false
        }
    }

    fun decidePlan(submissionId: String, approve: Boolean, rejectReason: String? = null) {
        viewModelScope.launch {
            isLoading = true
            when (val result = repository.decideVacationPlan(submissionId, approve, rejectReason)) {
                is NetworkResult.Success -> {
                    showToast(if (approve) "Plan został zaakceptowany." else "Plan został odrzucony.")
                    loadAll()
                }
                is NetworkResult.Error -> {
                    showToast("Błąd zapisu decyzji: ${result.message}")
                }
                else -> {}
            }
            isLoading = false
        }
    }

    private fun showToast(msg: String) {
        viewModelScope.launch {
            _toastEvent.emit(msg)
        }
    }

    // Holiday calculation helper matching CRM
    fun getHolidaysForYear(year: Int): Set<String> {
        val holidays = mutableSetOf<String>()
        fun addHoliday(month: Int, day: Int) {
            holidays.add(String.format("%d-%02d-%02d", year, month, day))
        }

        // Fixed holidays in Poland
        addHoliday(1, 1)   // Nowy Rok
        addHoliday(1, 6)   // Trzech Króli
        addHoliday(5, 1)   // 1 Maja
        addHoliday(5, 3)   // 3 Maja
        addHoliday(8, 15)  // Wniebowzięcie NMP
        addHoliday(11, 1)  // Wszystkich Świętych
        addHoliday(11, 11) // Święto Niepodległości
        addHoliday(12, 25) // Pierwszy dzień Świąt
        addHoliday(12, 26) // Drugi dzień Świąt

        // Easter Calculation (Meeus/Jones/Butcher algorithm)
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = (h + l - 7 * m + 114) % 31 + 1

        val calendar = GregorianCalendar(year, month - 1, day)

        fun getFormattedDate(offset: Int): String {
            val temp = calendar.clone() as Calendar
            temp.add(Calendar.DAY_OF_YEAR, offset)
            return String.format("%d-%02d-%02d",
                temp.get(Calendar.YEAR),
                temp.get(Calendar.MONTH) + 1,
                temp.get(Calendar.DAY_OF_MONTH)
            )
        }

        holidays.add(getFormattedDate(0))  // Wielkanoc (Niedziela)
        holidays.add(getFormattedDate(1))  // Poniedziałek Wielkanocny
        holidays.add(getFormattedDate(49)) // Zielone Świątki (Zesłanie Ducha Św.)
        holidays.add(getFormattedDate(60)) // Boże Ciało

        return holidays
    }
}
