package com.ossadkowski.crm.mobile.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.*
import com.ossadkowski.crm.mobile.data.repository.DashboardRepository
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: DashboardRepository = DashboardRepository()
) : ViewModel() {

    // Profile (now from /auth/profile)
    private val _profile = MutableLiveData<NetworkResult<AuthProfileResponse>>()
    val profile: LiveData<NetworkResult<AuthProfileResponse>> = _profile

    private val _employeeProfile = MutableLiveData<NetworkResult<ProfileResponse>>()
    val employeeProfile: LiveData<NetworkResult<ProfileResponse>> = _employeeProfile
    
    // Saldo
    private val _saldo = MutableLiveData<NetworkResult<List<PrawoPracySaldoDto>>>()
    val saldo: LiveData<NetworkResult<List<PrawoPracySaldoDto>>> = _saldo

    private val _vacationSummary = MutableLiveData<NetworkResult<VacationSummaryDto>>()
    val vacationSummary: LiveData<NetworkResult<VacationSummaryDto>> = _vacationSummary

    private val _homeOfficeSaldo = MutableLiveData<NetworkResult<HomeOfficeSaldoDto>>()
    val homeOfficeSaldo: LiveData<NetworkResult<HomeOfficeSaldoDto>> = _homeOfficeSaldo

    private val _overtimeSaldo = MutableLiveData<NetworkResult<List<OvertimeSaldoDto>>>()
    val overtimeSaldo: LiveData<NetworkResult<List<OvertimeSaldoDto>>> = _overtimeSaldo

    // Tasks
    private val _tasks = MutableLiveData<NetworkResult<PaginatedResponse<TaskItem>>>()
    val tasks: LiveData<NetworkResult<PaginatedResponse<TaskItem>>> = _tasks
    var tasksPage = 1
    var tasksSearch: String? = null

    // Wnioski
    private val _wnioski = MutableLiveData<NetworkResult<PaginatedResponse<WniosekItem>>>()
    val wnioski: LiveData<NetworkResult<PaginatedResponse<WniosekItem>>> = _wnioski
    var wnioskiPage = 1

    // Approvals
    private val _approvals = MutableLiveData<NetworkResult<PaginatedResponse<WniosekItem>>>()
    val approvals: LiveData<NetworkResult<PaginatedResponse<WniosekItem>>> = _approvals

    fun loadApprovals(userId: Int, role: String? = null, pageSize: Int = 10) {
        _approvals.value = NetworkResult.Loading()
        viewModelScope.launch {
            _approvals.value = repository.getApprovals(userId, 1, pageSize, role)
        }
    }

    // Zastepstwa
    private val _zastepstwaOczekujace = MutableLiveData<NetworkResult<List<WniosekItem>>>()
    val zastepstwaOczekujace: LiveData<NetworkResult<List<WniosekItem>>> = _zastepstwaOczekujace

    private val _zastepstwaZaakceptowane = MutableLiveData<NetworkResult<List<WniosekItem>>>()
    val zastepstwaZaakceptowane: LiveData<NetworkResult<List<WniosekItem>>> = _zastepstwaZaakceptowane

    // Conversations
    private val _conversations = MutableLiveData<NetworkResult<ConversationResponse>>()
    val conversations: LiveData<NetworkResult<ConversationResponse>> = _conversations

    fun loadMessages() {
        _conversations.value = NetworkResult.Loading()
        viewModelScope.launch {
            _conversations.value = repository.getConversations()
        }
    }

    private val _boardTasks = MutableLiveData<NetworkResult<BoardResponse>>()
    val boardTasks: LiveData<NetworkResult<BoardResponse>> = _boardTasks

    fun loadZastepstwa() {
        _zastepstwaOczekujace.value = NetworkResult.Loading()
        _zastepstwaZaakceptowane.value = NetworkResult.Loading()
        viewModelScope.launch {
            _zastepstwaOczekujace.value = repository.getZastepstwa("Oczekuje")
            _zastepstwaZaakceptowane.value = repository.getZastepstwa("Zaakceptowane")
        }
    }

    fun loadBoard(scope: String = "moje") {
        _boardTasks.value = NetworkResult.Loading()
        viewModelScope.launch {
            _boardTasks.value = repository.getBoardTasks(scope)
        }
    }

    // Uzytkownicy (do polecenia pracy)
    private val _uzytkownicy = MutableLiveData<NetworkResult<List<SlownikItemDto>>>()
    val uzytkownicy: LiveData<NetworkResult<List<SlownikItemDto>>> = _uzytkownicy

    fun loadUzytkownicy() {
        _uzytkownicy.value = NetworkResult.Loading()
        viewModelScope.launch {
            _uzytkownicy.value = repository.getWnioskiUzytkownicy()
        }
    }

    // Create Polecenie
    private val _createPolecenieStatus = MutableLiveData<NetworkResult<CreateWniosekResponse>>()
    val createPolecenieStatus: LiveData<NetworkResult<CreateWniosekResponse>> = _createPolecenieStatus

    fun createPoleceniePracy(userId: Int, pracownikId: Int, data: String, dzien: String) {
        _createPolecenieStatus.value = NetworkResult.Loading()
        viewModelScope.launch {
            val request = CreatePoleceniePracyRequest(userId, pracownikId, data, dzien)
            _createPolecenieStatus.value = repository.createPoleceniePracy(request)
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profile.value = repository.getAuthProfile()
        }
    }

    fun loadEmployeeProfile(userId: Int) {
        viewModelScope.launch {
            _employeeProfile.value = repository.getProfile(userId)
        }
    }

    fun loadSaldo() {
        viewModelScope.launch {
            // Load all different balances in parallel using separate coroutines
            launch { _saldo.value = repository.getPrawoPracySaldo() }
            launch { _vacationSummary.value = repository.getVacationSummary() }
            launch { _homeOfficeSaldo.value = repository.getHomeOfficeSaldo() }
            launch { _overtimeSaldo.value = repository.getOvertimeSaldo() }
        }
    }

    fun loadTasks(pageSize: Int = 10) {
        _tasks.value = NetworkResult.Loading()
        viewModelScope.launch {
            _tasks.value = repository.getTasks(tasksPage, pageSize, scope = "moje", userId = null, status = tasksSearch)
        }
    }

    fun loadWnioski(userId: Int, pageSize: Int = 10) {
        _wnioski.value = NetworkResult.Loading()
        viewModelScope.launch {
            _wnioski.value = repository.getWnioski(userId, wnioskiPage, pageSize)
        }
    }

    fun sendWniosek(wniosekId: Int, userId: Int, onResult: (Boolean, Any?) -> Unit) {
        viewModelScope.launch {
            val result = repository.sendWniosek(wniosekId, userId)
            onResult(result is NetworkResult.Success, (result as? NetworkResult.Success)?.data)
        }
    }

    fun resubmitWniosek(wniosekId: Int, userId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.resubmitWniosek(wniosekId, userId)
            onResult(result is NetworkResult.Success)
        }
    }

    fun deleteWniosek(wniosekId: Int, userId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteWniosek(wniosekId, userId)
            onResult(result is NetworkResult.Success)
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            onDone()
        }
    }
}
