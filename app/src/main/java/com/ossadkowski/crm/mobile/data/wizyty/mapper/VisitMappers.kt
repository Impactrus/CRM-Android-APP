package com.ossadkowski.crm.mobile.data.wizyty.mapper

import com.ossadkowski.crm.mobile.data.wizyty.db.VisitEventEntity
import com.ossadkowski.crm.mobile.domain.wizyty.model.NewVisitEvent
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitEvent
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitEventType
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitSource
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitStatus
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitSyncStatus
import java.time.Instant

/**
 * Entity → domain. Unknown enum strings fall back to safe defaults instead of
 * throwing — protects against future schema changes that leave stale rows.
 */
fun VisitEventEntity.toDomain(): VisitEvent = VisitEvent(
    id = id,
    contractorAccountNum = contractorAccountNum,
    contractorName = contractorName,
    addressLabel = addressLabel,
    lat = lat,
    lng = lng,
    eventType = parseEventType(eventType),
    source = parseSource(source),
    status = parseStatus(status),
    syncStatus = parseSyncStatus(syncStatus),
    occurredAt = occurredAt,
    dwellSeconds = dwellSeconds,
    idempotencyKey = idempotencyKey,
    createdAt = createdAt,
    updatedAt = updatedAt,
    note = note,
)

/**
 * Build an entity ready for insert. [source] and [status] are decided by the
 * repository (MANUAL→CONFIRMED, AUTO_GPS→DETECTED); rows always start PENDING_SYNC.
 */
fun NewVisitEvent.toEntity(
    source: VisitSource,
    status: VisitStatus,
    now: Instant,
    idempotencyKey: String,
): VisitEventEntity = VisitEventEntity(
    id = 0,
    contractorAccountNum = contractorAccountNum,
    contractorName = contractorName,
    addressLabel = addressLabel,
    lat = lat,
    lng = lng,
    eventType = eventType.name,
    source = source.name,
    status = status.name,
    syncStatus = VisitSyncStatus.PENDING_SYNC.name,
    occurredAt = occurredAt ?: now,
    dwellSeconds = dwellSeconds,
    idempotencyKey = idempotencyKey,
    createdAt = now,
    updatedAt = now,
    note = note,
)

private fun parseEventType(raw: String): VisitEventType = try {
    VisitEventType.valueOf(raw)
} catch (_: IllegalArgumentException) {
    VisitEventType.MANUAL
}

private fun parseSource(raw: String): VisitSource = try {
    VisitSource.valueOf(raw)
} catch (_: IllegalArgumentException) {
    VisitSource.MANUAL
}

private fun parseStatus(raw: String): VisitStatus = try {
    VisitStatus.valueOf(raw)
} catch (_: IllegalArgumentException) {
    VisitStatus.DETECTED
}

private fun parseSyncStatus(raw: String): VisitSyncStatus = try {
    VisitSyncStatus.valueOf(raw)
} catch (_: IllegalArgumentException) {
    VisitSyncStatus.LOCAL_ONLY
}
