package com.ossadkowski.crm.mobile.data.wizyty.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface ContractorCoordDao {

    /**
     * Insert-or-replace keyed by [ContractorCoordEntity.key]. REPLACE is intentional: re-saving
     * the same contractor (same accountNum, or same typed name for a test location) updates the
     * stored coordinates rather than creating a duplicate geofence target.
     *
     * Known limitation (acceptable while coordinates are local demo data): two *different*
     * contractors that both lack an accountNum and share a name collapse onto one row. Once the
     * backend exposes real `/kontrahenci` coordinates, the key becomes the stable accountNum and
     * the collision disappears — see [ContractorCoordEntity.key].
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(e: ContractorCoordEntity): Long

    @Query("SELECT * FROM contractor_coords ORDER BY updatedAt DESC")
    suspend fun getAll(): List<ContractorCoordEntity>

    @Query("SELECT * FROM contractor_coords ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<ContractorCoordEntity>>

    @Query("SELECT * FROM contractor_coords WHERE key = :key LIMIT 1")
    suspend fun get(key: String): ContractorCoordEntity?

    @Query("DELETE FROM contractor_coords WHERE key = :key")
    suspend fun delete(key: String): Int
}
