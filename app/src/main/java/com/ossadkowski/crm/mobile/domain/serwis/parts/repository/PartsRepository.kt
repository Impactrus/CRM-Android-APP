package com.ossadkowski.crm.mobile.domain.serwis.parts.repository

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.parts.model.NewPartRequest
import com.ossadkowski.crm.mobile.domain.serwis.parts.model.PartRequest
import com.ossadkowski.crm.mobile.domain.serwis.parts.model.PartStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for offline parts requests.
 *
 * All flows are backed by Room and emit on every DB change.
 */
interface PartsRepository {
    fun observeAll(): Flow<List<PartRequest>>
    fun observeForOrder(orderRegNum: String): Flow<List<PartRequest>>
    suspend fun get(id: Long): PartRequest?
    suspend fun add(req: NewPartRequest): Result<PartRequest>
    suspend fun updateStatus(id: Long, status: PartStatus): Result<Unit>
    suspend fun delete(id: Long): Result<Unit>
}
