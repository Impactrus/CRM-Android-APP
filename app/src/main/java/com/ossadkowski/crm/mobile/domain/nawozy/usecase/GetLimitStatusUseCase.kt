package com.ossadkowski.crm.mobile.domain.nawozy.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.model.LimitStatus
import com.ossadkowski.crm.mobile.domain.nawozy.repository.NawozyRepository
import javax.inject.Inject

class GetLimitStatusUseCase @Inject constructor(
    private val repo: NawozyRepository,
) {
    suspend operator fun invoke(accountNum: String): Result<LimitStatus> =
        repo.getLimitStatus(accountNum)
}
