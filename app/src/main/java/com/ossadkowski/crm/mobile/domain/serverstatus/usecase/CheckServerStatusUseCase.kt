package com.ossadkowski.crm.mobile.domain.serverstatus.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serverstatus.model.ServerStatus
import com.ossadkowski.crm.mobile.domain.serverstatus.repository.ServerStatusRepository
import javax.inject.Inject

class CheckServerStatusUseCase @Inject constructor(
    private val repo: ServerStatusRepository
) {
    suspend operator fun invoke(): Result<ServerStatus> = repo.checkStatus()
}
