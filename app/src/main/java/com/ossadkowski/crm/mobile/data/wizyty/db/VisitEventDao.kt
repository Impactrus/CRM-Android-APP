package com.ossadkowski.crm.mobile.data.wizyty.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
@JvmSuppressWildcards
interface VisitEventDao {

    /** Everything except rejected rows, newest first. */
    @Query("SELECT * FROM visit_events WHERE status != 'REJECTED' ORDER BY occurredAt DESC")
    fun observeVisible(): Flow<List<VisitEventEntity>>

    @Query("SELECT * FROM visit_events WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): VisitEventEntity?

    /** IGNORE so a retried signal with the same idempotencyKey is a no-op (returns -1). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(e: VisitEventEntity): Long

    @Query("SELECT * FROM visit_events WHERE status = 'CONFIRMED' AND syncStatus = 'PENDING_SYNC'")
    suspend fun pendingForSync(): List<VisitEventEntity>

    /**
     * How many auto-detected, still-pending visits exist for this contractor since [since].
     * Used to suppress duplicate DWELL signals for the same stay. Rejected rows are excluded
     * so a user who rejected a visit can still have a fresh one detected later.
     */
    @Query(
        "SELECT COUNT(*) FROM visit_events WHERE source = 'AUTO_GPS' AND status != 'REJECTED' " +
            "AND contractorName = :name AND occurredAt >= :since",
    )
    suspend fun countRecentDetected(name: String, since: Instant): Int

    @Query("UPDATE visit_events SET status = :status, syncStatus = :syncStatus, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, syncStatus: String, updatedAt: Instant): Int

    @Query("UPDATE visit_events SET note = :note, syncStatus = :syncStatus, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateNote(id: Long, note: String?, syncStatus: String, updatedAt: Instant): Int

    @Query("UPDATE visit_events SET syncStatus = :syncStatus, updatedAt = :updatedAt WHERE id IN (:ids)")
    suspend fun markSync(ids: List<Long>, syncStatus: String, updatedAt: Instant): Int

    @Query("DELETE FROM visit_events WHERE id = :id")
    suspend fun delete(id: Long): Int
}
