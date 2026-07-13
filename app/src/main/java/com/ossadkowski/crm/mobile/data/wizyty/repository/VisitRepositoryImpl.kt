package com.ossadkowski.crm.mobile.data.wizyty.repository

import android.content.Context
import android.location.Geocoder
import com.ossadkowski.crm.mobile.data.api.ApiService
import com.ossadkowski.crm.mobile.data.wizyty.db.ContractorCoordDao
import com.ossadkowski.crm.mobile.data.wizyty.db.ContractorCoordEntity
import com.ossadkowski.crm.mobile.data.wizyty.db.VisitEventDao
import com.ossadkowski.crm.mobile.data.wizyty.db.VisitEventEntity
import com.ossadkowski.crm.mobile.data.wizyty.location.DetectionTuning
import com.ossadkowski.crm.mobile.data.wizyty.mapper.toDomain
import com.ossadkowski.crm.mobile.data.wizyty.mapper.toEntity
import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.wizyty.model.AddressSuggestion
import com.ossadkowski.crm.mobile.domain.wizyty.model.ContractorLocation
import com.ossadkowski.crm.mobile.domain.wizyty.model.CoordSource
import com.ossadkowski.crm.mobile.domain.wizyty.model.NewVisitEvent
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitEvent
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitSource
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitStatus
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitSyncStatus
import com.ossadkowski.crm.mobile.domain.wizyty.repository.VisitRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VisitRepositoryImpl @Inject constructor(
    private val dao: VisitEventDao,
    private val coordDao: ContractorCoordDao,
    private val apiService: ApiService,
    @ApplicationContext private val context: Context,
) : VisitRepository {

    override fun observeVisits(): Flow<List<VisitEvent>> =
        dao.observeVisible().map { list -> list.map(VisitEventEntity::toDomain) }

    override suspend fun addManualVisit(new: NewVisitEvent): Result<VisitEvent> {
        val result = insert(new, VisitSource.MANUAL, VisitStatus.DETECTED)
        // Remember the attached location so the geofence engine can watch this
        // contractor. Best-effort — never fail the visit save on a coord write.
        if (result is Result.Success && new.lat != null && new.lng != null) {
            val key = new.contractorAccountNum?.takeIf { it.isNotBlank() }
                ?: new.contractorName?.trim()?.takeIf { it.isNotBlank() }
            if (key != null) {
                runCatching {
                    coordDao.upsert(
                        ContractorCoordEntity(
                            key = key,
                            name = new.contractorName?.trim() ?: key,
                            lat = new.lat,
                            lng = new.lng,
                            label = new.addressLabel,
                            updatedAt = Instant.now(),
                        )
                    )
                }
            }
        }
        return result
    }

    override suspend fun recordDetectedEvent(new: NewVisitEvent): Result<VisitEvent> {
        // Suppress duplicate auto-detections for the same contractor within the dedup
        // window (re-fired DWELL / re-registered geofences would otherwise pile up rows).
        val name = new.contractorName?.takeIf { it.isNotBlank() }
        if (name != null) {
            val since = Instant.now().minusMillis(DetectionTuning.DETECTION_DEDUP_WINDOW_MS)
            val recent = runCatching { dao.countRecentDetected(name, since) }.getOrDefault(0)
            if (recent > 0) return Result.Error("Wizyta u tego kontrahenta została już wykryta.")
        }
        return insert(new, VisitSource.AUTO_GPS, VisitStatus.CONFIRMED)
    }

    private suspend fun insert(
        new: NewVisitEvent,
        source: VisitSource,
        status: VisitStatus,
    ): Result<VisitEvent> = try {
        val now = Instant.now()
        val entity = new.toEntity(source, status, now, UUID.randomUUID().toString())
        val id = dao.insert(entity)
        if (id > 0) {
            Result.Success(entity.copy(id = id).toDomain())
        } else {
            // Ignored on a unique-key conflict (duplicate signal) — nothing to do.
            Result.Error("Wizyta już istnieje.")
        }
    } catch (e: Exception) {
        Result.Error("Nie udało się zapisać wizyty.", e)
    }

    override suspend fun confirm(id: Long): Result<Unit> =
        setStatus(id, VisitStatus.CONFIRMED, VisitSyncStatus.PENDING_SYNC)

    override suspend fun reject(id: Long): Result<Unit> =
        // Rejected visits stay local; the worker only uploads CONFIRMED rows.
        setStatus(id, VisitStatus.REJECTED, VisitSyncStatus.LOCAL_ONLY)

    override suspend fun updateNote(id: Long, note: String?): Result<Unit> = try {
        val rows = dao.updateNote(id, note, VisitSyncStatus.PENDING_SYNC.name, Instant.now())
        if (rows > 0) Result.Success(Unit) else Result.Error("Nie znaleziono wizyty.")
    } catch (e: Exception) {
        Result.Error("Błąd zapisu notatki.", e)
    }

    override suspend fun delete(id: Long): Result<Unit> = try {
        val rows = dao.delete(id)
        if (rows > 0) Result.Success(Unit) else Result.Error("Nie znaleziono wizyty.")
    } catch (e: Exception) {
        Result.Error("Błąd usuwania wizyty.", e)
    }

    private suspend fun setStatus(
        id: Long,
        status: VisitStatus,
        syncStatus: VisitSyncStatus,
    ): Result<Unit> = try {
        val rows = dao.updateStatus(id, status.name, syncStatus.name, Instant.now())
        if (rows > 0) Result.Success(Unit) else Result.Error("Nie znaleziono wizyty.")
    } catch (e: Exception) {
        Result.Error("Błąd zapisu wizyty.", e)
    }

    override suspend fun pendingForSync(): List<VisitEvent> =
        dao.pendingForSync().map(VisitEventEntity::toDomain)

    override suspend fun markSynced(ids: List<Long>): Result<Unit> =
        mark(ids, VisitSyncStatus.SYNCED)

    override suspend fun markSyncFailed(ids: List<Long>): Result<Unit> =
        mark(ids, VisitSyncStatus.SYNC_FAILED)

    private suspend fun mark(ids: List<Long>, syncStatus: VisitSyncStatus): Result<Unit> {
        if (ids.isEmpty()) return Result.Success(Unit)
        return try {
            dao.markSync(ids, syncStatus.name, Instant.now())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Błąd aktualizacji synchronizacji.", e)
        }
    }

    override suspend fun searchAddress(query: String): Result<List<AddressSuggestion>> = try {
        val suggestions = apiService.searchGeocode(query).map {
            AddressSuggestion(label = it.label, lat = it.lat, lng = it.lng)
        }
        if (suggestions.isNotEmpty()) {
            Result.Success(suggestions)
        } else {
            val native = getNativeGeocoding(query)
            if (native.isNotEmpty()) Result.Success(native) else Result.Success(generateFallback(query))
        }
    } catch (e: Exception) {
        val native = getNativeGeocoding(query)
        if (native.isNotEmpty()) Result.Success(native) else Result.Success(generateFallback(query))
    }

    private suspend fun getNativeGeocoding(query: String): List<AddressSuggestion> = withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) return@withContext emptyList()
            val geocoder = Geocoder(context)
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(query, 5) ?: emptyList()
            addresses.map { address ->
                val label = address.getAddressLine(0) ?: buildString {
                    append(address.locality ?: "")
                    if (address.thoroughfare != null) {
                        if (isNotEmpty()) append(", ")
                        append(address.thoroughfare)
                    }
                }.ifBlank { query }
                AddressSuggestion(
                    label = label,
                    lat = address.latitude,
                    lng = address.longitude
                )
            }
        } catch (e: Throwable) {
            emptyList()
        }
    }

    private fun generateFallback(query: String): List<AddressSuggestion> {
        val fallbackCoords = tryParseCoordinates(query) ?: return emptyList()
        val lat = fallbackCoords.first
        val lng = fallbackCoords.second
        return listOf(
            AddressSuggestion(
                label = "Współrzędne: $lat, $lng",
                lat = lat,
                lng = lng
            )
        )
    }

    private fun tryParseCoordinates(query: String): Pair<Double, Double>? {
        return try {
            val parts = query.split(Regex("[,;\\s]+")).mapNotNull { it.toDoubleOrNull() }
            if (parts.size >= 2) {
                Pair(parts[0], parts[1])
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun saveContractorLocation(
        name: String,
        lat: Double,
        lng: Double,
        label: String?,
    ): Result<Unit> = try {
        val key = name.trim().ifBlank { "lok-${System.currentTimeMillis()}" }
        coordDao.upsert(
            ContractorCoordEntity(
                key = key,
                name = name.trim().ifBlank { key },
                lat = lat,
                lng = lng,
                label = label,
                updatedAt = Instant.now(),
            ),
        )
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error("Nie udało się zapisać lokalizacji.", e)
    }

    override fun observeContractorLocations(): Flow<List<ContractorLocation>> =
        coordDao.observeAll().map { list ->
            list.map { c ->
                ContractorLocation(
                    key = c.key,
                    name = c.name,
                    lat = c.lat,
                    lng = c.lng,
                    label = c.label,
                    isLive = false,
                    coordSource = CoordSource.USER_GEOCODED,
                )
            }
        }

    override suspend fun deleteContractorLocation(key: String): Result<Unit> = try {
        val rows = coordDao.delete(key)
        if (rows > 0) Result.Success(Unit) else Result.Error("Nie znaleziono lokalizacji.")
    } catch (e: Exception) {
        Result.Error("Nie udało się usunąć lokalizacji.", e)
    }
}
