package com.ossadkowski.crm.mobile.domain.serwis.parts.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.parts.model.PartStatus
import com.ossadkowski.crm.mobile.domain.serwis.parts.repository.PartsRepository
import javax.inject.Inject

class UpdatePartStatusUseCase @Inject constructor(
    private val repo: PartsRepository,
) {
    suspend operator fun invoke(id: Long, status: PartStatus): Result<Unit> =
        repo.updateStatus(id, status)
}
