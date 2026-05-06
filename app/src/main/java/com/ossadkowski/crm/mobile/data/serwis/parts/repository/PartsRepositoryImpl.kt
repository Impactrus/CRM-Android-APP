package com.ossadkowski.crm.mobile.data.serwis.parts.repository

import com.ossadkowski.crm.mobile.data.serwis.parts.db.PartRequestDao
import com.ossadkowski.crm.mobile.data.serwis.parts.db.PartRequestEntity
import com.ossadkowski.crm.mobile.data.serwis.parts.mapper.toDomain
import com.ossadkowski.crm.mobile.data.serwis.parts.mapper.toEntity
import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.parts.model.NewPartRequest
import com.ossadkowski.crm.mobile.domain.serwis.parts.model.PartRequest
import com.ossadkowski.crm.mobile.domain.serwis.parts.model.PartStatus
import com.ossadkowski.crm.mobile.domain.serwis.parts.model.PartSyncStatus
import com.ossadkowski.crm.mobile.domain.serwis.parts.repository.PartsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PartsRepositoryImpl @Inject constructor(
    private val dao: PartRequestDao,
) : PartsRepository {

    override fun observeAll(): Flow<List<PartRequest>> =
        dao.observeAll().map { list -> list.map(PartRequestEntity::toDomain) }

    override fun observeForOrder(orderRegNum: String): Flow<List<PartRequest>> =
        dao.observeForOrder(orderRegNum).map { list -> list.map(PartRequestEntity::toDomain) }

    override suspend fun get(id: Long): PartRequest? = dao.get(id)?.toDomain()

    override suspend fun add(req: NewPartRequest): Result<PartRequest> = try {
        val now = Instant.now()
        val entity = req.toEntity(now)
        val id = dao.insert(entity)
        Result.Success(entity.copy(id = id).toDomain())
    } catch (e: Exception) {
        Result.Error("Nie udało się zapisać części.", e)
    }

    override suspend fun updateStatus(id: Long, status: PartStatus): Result<Unit> = try {
        val rows = dao.updateStatus(
            id = id,
            status = status.name,
            updatedAt = Instant.now(),
            syncStatus = PartSyncStatus.PENDING_SYNC.name,
        )
        if (rows > 0) Result.Success(Unit) else Result.Error("Nie znaleziono części.")
    } catch (e: Exception) {
        Result.Error("Błąd zapisu.", e)
    }

    override suspend fun delete(id: Long): Result<Unit> = try {
        val rows = dao.delete(id)
        if (rows > 0) Result.Success(Unit) else Result.Error("Nie znaleziono części.")
    } catch (e: Exception) {
        Result.Error("Błąd usunięcia.", e)
    }
}
