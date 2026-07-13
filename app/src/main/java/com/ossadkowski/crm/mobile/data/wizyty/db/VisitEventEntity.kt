package com.ossadkowski.crm.mobile.data.wizyty.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for the Wizyty (GPS visit-detection) outbox.
 *
 * Enums are stored as their `.name` strings — see `data.wizyty.mapper` for the
 * mapping with unknown-value fallbacks. [idempotencyKey] is uniquely indexed so a
 * retried detection signal can't create a duplicate row.
 */
@Entity(
    tableName = "visit_events",
    indices = [Index(value = ["idempotencyKey"], unique = true)],
)
data class VisitEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contractorAccountNum: String?,
    val contractorName: String?,
    val addressLabel: String?,
    val lat: Double?,
    val lng: Double?,
    val eventType: String,      // VisitEventType.name
    val source: String,         // VisitSource.name
    val status: String,         // VisitStatus.name
    val syncStatus: String,     // VisitSyncStatus.name
    val occurredAt: Instant,
    val dwellSeconds: Long?,
    val idempotencyKey: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val note: String? = null,
)
