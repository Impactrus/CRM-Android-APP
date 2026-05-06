package com.ossadkowski.crm.mobile.data.serwis.parts.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
@JvmSuppressWildcards
interface PartRequestDao {

    @Query("SELECT * FROM part_requests ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<PartRequestEntity>>

    @Query("SELECT * FROM part_requests WHERE orderRegNum = :orderRegNum ORDER BY updatedAt DESC")
    fun observeForOrder(orderRegNum: String): Flow<List<PartRequestEntity>>

    @Query("SELECT * FROM part_requests WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): PartRequestEntity?

    @Insert
    suspend fun insert(e: PartRequestEntity): Long

    @Query("UPDATE part_requests SET status = :status, updatedAt = :updatedAt, syncStatus = :syncStatus WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, updatedAt: Instant, syncStatus: String): Int

    @Query("DELETE FROM part_requests WHERE id = :id")
    suspend fun delete(id: Long): Int
}
