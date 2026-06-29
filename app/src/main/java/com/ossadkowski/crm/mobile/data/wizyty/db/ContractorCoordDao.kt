package com.ossadkowski.crm.mobile.data.wizyty.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface ContractorCoordDao {

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
