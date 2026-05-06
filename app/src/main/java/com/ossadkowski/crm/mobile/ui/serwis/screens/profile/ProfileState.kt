package com.ossadkowski.crm.mobile.ui.serwis.screens.profile

import com.ossadkowski.crm.mobile.domain.serwis.model.TimeSummary

/**
 * Snapshot of the SessionManager / JWT-derived user profile shown on the
 * Profil screen. Avatar initials are precomputed because they may fall back
 * through several sources (full name → username → "TM").
 */
data class ProfileInfo(
    val initials: String,
    val fullName: String,
    val username: String,
    val dzial: String?,
    val role: String,
    val claimsCount: Int,
    val isLoggedIn: Boolean,
)

/**
 * UI state for [ProfileScreen]. Same `Loading → Success / Error` triad as the
 * other Serwis screens.
 *
 * `monthSummary` is loaded asynchronously **after** the synchronous profile
 * data is shown — Loading covers only the initial moment before the profile
 * is read from prefs.
 */
sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(
        val profile: ProfileInfo,
        val monthSummary: TimeSummary?,
    ) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}
