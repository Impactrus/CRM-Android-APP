package com.ossadkowski.crm.mobile.domain.serwis.parts.model

import java.time.Instant

/**
 * Domain model for an offline parts ("Części") request.
 *
 * Backend has no parts endpoints yet (per spec), so this is local-only with
 * a [syncStatus] field that future sync workers can act on.
 */
data class PartRequest(
    val id: Long,
    val orderRegNum: String?,            // null = unscoped/general request
    val jobCardNum: String?,
    val name: String,
    val partNumber: String?,
    val quantity: Double,
    val unit: String,                     // "szt", "l", "kg"
    val status: PartStatus,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: PartSyncStatus,
)

enum class PartStatus { REQUESTED, ORDERED, IN_TRANSIT, RECEIVED, CANCELLED }

enum class PartSyncStatus { LOCAL_ONLY, PENDING_SYNC, SYNCED, SYNC_FAILED }
