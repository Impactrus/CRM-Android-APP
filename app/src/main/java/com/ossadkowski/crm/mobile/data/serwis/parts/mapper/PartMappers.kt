package com.ossadkowski.crm.mobile.data.serwis.parts.mapper

import com.ossadkowski.crm.mobile.data.serwis.parts.db.PartRequestEntity
import com.ossadkowski.crm.mobile.domain.serwis.parts.model.NewPartRequest
import com.ossadkowski.crm.mobile.domain.serwis.parts.model.PartRequest
import com.ossadkowski.crm.mobile.domain.serwis.parts.model.PartStatus
import com.ossadkowski.crm.mobile.domain.serwis.parts.model.PartSyncStatus
import java.time.Instant

/**
 * Map an entity to its domain representation. Unknown enum strings fall back to
 * safe defaults (`REQUESTED` / `LOCAL_ONLY`) instead of throwing — protects
 * against future schema changes that could leave stale rows.
 */
fun PartRequestEntity.toDomain(): PartRequest = PartRequest(
    id = id,
    orderRegNum = orderRegNum,
    jobCardNum = jobCardNum,
    name = name,
    partNumber = partNumber,
    quantity = quantity,
    unit = unit,
    status = parsePartStatus(status),
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = parsePartSyncStatus(syncStatus),
)

/** Build an entity ready for insert from a UI input model. */
fun NewPartRequest.toEntity(now: Instant): PartRequestEntity = PartRequestEntity(
    id = 0,
    orderRegNum = orderRegNum,
    jobCardNum = jobCardNum,
    name = name,
    partNumber = partNumber,
    quantity = quantity,
    unit = unit,
    status = PartStatus.REQUESTED.name,
    notes = notes,
    createdAt = now,
    updatedAt = now,
    syncStatus = PartSyncStatus.LOCAL_ONLY.name,
)

private fun parsePartStatus(raw: String): PartStatus = try {
    PartStatus.valueOf(raw)
} catch (_: IllegalArgumentException) {
    PartStatus.REQUESTED
}

private fun parsePartSyncStatus(raw: String): PartSyncStatus = try {
    PartSyncStatus.valueOf(raw)
} catch (_: IllegalArgumentException) {
    PartSyncStatus.LOCAL_ONLY
}
