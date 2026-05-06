package com.ossadkowski.crm.mobile.domain.serwis.parts.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.parts.model.NewPartRequest
import com.ossadkowski.crm.mobile.domain.serwis.parts.model.PartRequest
import com.ossadkowski.crm.mobile.domain.serwis.parts.repository.PartsRepository
import javax.inject.Inject

class AddPartUseCase @Inject constructor(
    private val repo: PartsRepository,
) {
    suspend operator fun invoke(req: NewPartRequest): Result<PartRequest> = repo.add(req)
}
