package com.ossadkowski.crm.mobile.data.serwis.parts.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for offline parts requests.
 *
 * Enums are stored as their `.name` strings — see
 * [com.ossadkowski.crm.mobile.data.serwis.parts.mapper] for the mapping with
 * unknown-value fallbacks.
 */
@Entity(tableName = "part_requests")
data class PartRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderRegNum: String?,
    val jobCardNum: String?,
    val name: String,
    val partNumber: String?,
    val quantity: Double,
    val unit: String,
    val status: String,        // PartStatus.name
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: String,    // PartSyncStatus.name
)
