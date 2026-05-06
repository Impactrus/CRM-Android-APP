package com.ossadkowski.crm.mobile.data.serverstatus

import com.ossadkowski.crm.mobile.data.api.ApiService
import com.ossadkowski.crm.mobile.data.serverstatus.mapper.toDomain
import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serverstatus.model.ServerStatus
import com.ossadkowski.crm.mobile.domain.serverstatus.repository.ServerStatusRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class ServerStatusRepositoryImpl @Inject constructor(
    private val api: ApiService
) : ServerStatusRepository {

    override suspend fun checkStatus(): Result<ServerStatus> = withContext(Dispatchers.IO) {
        try {
            Result.Success(api.getAuthProfile().toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error", e)
        }
    }
}
