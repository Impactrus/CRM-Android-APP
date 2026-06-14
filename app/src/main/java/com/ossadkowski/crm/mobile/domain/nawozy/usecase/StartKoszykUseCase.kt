package com.ossadkowski.crm.mobile.domain.nawozy.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.repository.NawozyRepository
import javax.inject.Inject

/** Creates or resumes a fertiliser cart for the given customer; returns `koszykId`. */
class StartKoszykUseCase @Inject constructor(
    private val repo: NawozyRepository,
) {
    suspend operator fun invoke(kontrahentId: String): Result<Long> =
        repo.startKoszyk(kontrahentId)
}
