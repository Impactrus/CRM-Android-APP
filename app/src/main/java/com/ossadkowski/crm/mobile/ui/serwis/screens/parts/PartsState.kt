package com.ossadkowski.crm.mobile.ui.serwis.screens.parts

import com.ossadkowski.crm.mobile.domain.serwis.parts.model.PartRequest
import com.ossadkowski.crm.mobile.domain.serwis.parts.model.PartStatus

/** UI state for the Części screen. */
data class PartsUiState(
    val parts: List<PartRequest> = emptyList(),
    val filter: PartStatus? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

/** Events emitted by the Części screen for higher-level coordination (currently unused). */
sealed interface PartsEvent {
    data object AddRequested : PartsEvent
    data class StatusChanged(val id: Long, val newStatus: PartStatus) : PartsEvent
    data class DeleteRequested(val id: Long) : PartsEvent
}
