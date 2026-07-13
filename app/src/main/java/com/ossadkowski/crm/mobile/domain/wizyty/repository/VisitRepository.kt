package com.ossadkowski.crm.mobile.domain.wizyty.repository

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.wizyty.model.AddressSuggestion
import com.ossadkowski.crm.mobile.domain.wizyty.model.ContractorLocation
import com.ossadkowski.crm.mobile.domain.wizyty.model.NewVisitEvent
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitEvent
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for the Wizyty (GPS visit-detection) outbox.
 *
 * Visit flows are backed by Room and emit on every DB change. Network is limited to
 * [searchAddress] (the existing server-side TomTom geocode) until the backend visit
 * endpoints exist; sync is owned by a future WorkManager worker via [pendingForSync].
 */
interface VisitRepository {

    /** Visible visits (everything except REJECTED), newest first. */
    fun observeVisits(): Flow<List<VisitEvent>>

    /** Rep-initiated visit → stored CONFIRMED + PENDING_SYNC. */
    suspend fun addManualVisit(new: NewVisitEvent): Result<VisitEvent>

    /** Engine-detected signal → stored DETECTED + PENDING_SYNC (awaits confirmation). */
    suspend fun recordDetectedEvent(new: NewVisitEvent): Result<VisitEvent>

    suspend fun confirm(id: Long): Result<Unit>
    suspend fun reject(id: Long): Result<Unit>
    suspend fun updateNote(id: Long, note: String?): Result<Unit>
    suspend fun delete(id: Long): Result<Unit>

    /** CONFIRMED + PENDING_SYNC rows the sync worker should upload. */
    suspend fun pendingForSync(): List<VisitEvent>
    suspend fun markSynced(ids: List<Long>): Result<Unit>
    suspend fun markSyncFailed(ids: List<Long>): Result<Unit>

    /** Address autocomplete via the existing server-proxied TomTom search. */
    suspend fun searchAddress(query: String): Result<List<AddressSuggestion>>

    /** Persist a (test) contractor location to geofence, keyed by [name]. */
    suspend fun saveContractorLocation(
        name: String,
        lat: Double,
        lng: Double,
        label: String?,
    ): Result<Unit>

    /** All saved (test) contractor locations, newest first. */
    fun observeContractorLocations(): Flow<List<ContractorLocation>>

    suspend fun deleteContractorLocation(key: String): Result<Unit>
}
