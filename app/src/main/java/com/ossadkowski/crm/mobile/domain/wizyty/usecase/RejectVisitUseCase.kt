package com.ossadkowski.crm.mobile.domain.wizyty.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.wizyty.repository.VisitRepository
import javax.inject.Inject

class RejectVisitUseCase @Inject constructor(
    private val repo: VisitRepository,
) {
    suspend operator fun invoke(id: Long): Result<Unit> = repo.reject(id)
}
