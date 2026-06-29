package com.ossadkowski.crm.mobile.domain.wizyty.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.wizyty.repository.VisitRepository
import javax.inject.Inject

class DeleteTestLocationUseCase @Inject constructor(
    private val repo: VisitRepository,
) {
    suspend operator fun invoke(key: String): Result<Unit> = repo.deleteContractorLocation(key)
}
