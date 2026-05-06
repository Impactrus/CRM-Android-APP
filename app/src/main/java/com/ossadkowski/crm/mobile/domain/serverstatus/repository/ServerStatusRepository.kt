package com.ossadkowski.crm.mobile.domain.serverstatus.repository

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serverstatus.model.ServerStatus

interface ServerStatusRepository {
    suspend fun checkStatus(): Result<ServerStatus>
}
