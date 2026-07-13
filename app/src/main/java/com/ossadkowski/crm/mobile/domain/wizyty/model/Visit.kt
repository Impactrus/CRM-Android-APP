package com.ossadkowski.crm.mobile.domain.wizyty.model

import java.time.Instant

/**
 * Domain model for a visit signal in the offline outbox.
 *
 * One row per GPS/manual signal. Auto-detected events arrive [VisitStatus.DETECTED]
 * and require manual confirmation before they are eligible for sync; manual visits
 * are created already [VisitStatus.CONFIRMED]. Enums are persisted as their `.name`
 * strings with safe fallbacks — see `data.wizyty.mapper`.
 *
 * Backend visit endpoints do not exist yet, so this is local-first with a
 * [syncStatus] a future sync worker acts on (only CONFIRMED + PENDING_SYNC rows sync).
 */
data class VisitEvent(
    val id: Long,
    val contractorAccountNum: String?,   // null = not yet tied to an ax.* contractor
    val contractorName: String?,
    val addressLabel: String?,            // human label of the attached location
    val lat: Double?,
    val lng: Double?,
    val eventType: VisitEventType,
    val source: VisitSource,
    val status: VisitStatus,
    val syncStatus: VisitSyncStatus,
    val occurredAt: Instant,
    val dwellSeconds: Long?,              // populated for DWELL/EXIT
    val idempotencyKey: String,           // UUID; backend dedupe key
    val createdAt: Instant,
    val updatedAt: Instant,
    val note: String?,
)

/**
 * Input model for creating a visit event — from the manual-add screen or the
 * detection engine. The repository fills in source/status/timestamps/idempotency.
 */
data class NewVisitEvent(
    val contractorAccountNum: String? = null,
    val contractorName: String? = null,
    val addressLabel: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val eventType: VisitEventType = VisitEventType.MANUAL,
    val occurredAt: Instant? = null,      // null → now
    val dwellSeconds: Long? = null,
    val note: String? = null,
)

enum class VisitEventType { ENTER, DWELL, EXIT, MANUAL }

enum class VisitStatus { DETECTED, CONFIRMED, REJECTED }

enum class VisitSource { AUTO_GPS, MANUAL }

enum class VisitSyncStatus { LOCAL_ONLY, PENDING_SYNC, SYNCED, SYNC_FAILED }
