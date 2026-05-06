package com.ossadkowski.crm.mobile.ui.serwis.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.SessionManager
import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.usecase.GetMyTimeSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val session: SessionManager,
    private val getTimeSummary: GetMyTimeSummaryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val profile = readProfile()
        _uiState.value = ProfileUiState.Success(profile, monthSummary = null)
        loadMonthSummary()
    }

    /** Re-reads the month summary; the profile snapshot is already in state. */
    fun refresh() {
        // Profile may have been updated meanwhile (full name, claims).
        val current = _uiState.value
        val profile = readProfile()
        _uiState.value = when (current) {
            is ProfileUiState.Success -> current.copy(profile = profile)
            else -> ProfileUiState.Success(profile, monthSummary = null)
        }
        loadMonthSummary()
    }

    fun logout() {
        session.clear()
    }

    private fun loadMonthSummary() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val from = today.withDayOfMonth(1)
            val to = today.withDayOfMonth(today.lengthOfMonth())
            when (val r = getTimeSummary(from, to)) {
                is Result.Success -> {
                    val current = _uiState.value
                    if (current is ProfileUiState.Success) {
                        _uiState.value = current.copy(monthSummary = r.data)
                    }
                }
                is Result.Error -> {
                    // Don't tear down the screen on a stat-load failure —
                    // leave monthSummary = null so the cells render placeholders.
                }
                Result.Loading -> Unit
            }
        }
    }

    private fun readProfile(): ProfileInfo {
        val username = session.username
        val fullName = session.fullName.ifBlank { username }
        return ProfileInfo(
            initials = computeInitials(fullName, username),
            fullName = if (fullName.isBlank()) "Technik" else fullName,
            username = username,
            dzial = session.dzial.ifBlank { null },
            role = session.role,
            claimsCount = session.claims.size,
            isLoggedIn = session.isLoggedIn,
        )
    }

    private fun computeInitials(fullName: String, username: String): String {
        val source = fullName.takeIf { it.isNotBlank() } ?: username
        if (source.isBlank()) return "TM"
        val parts = source.trim().split(Regex("[\\s._-]+")).filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "TM"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> (parts[0].first().toString() + parts[1].first().toString()).uppercase()
        }
    }
}
