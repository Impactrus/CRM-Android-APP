package com.ossadkowski.crm.mobile.domain.nawozy.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.model.OstatniaCena
import com.ossadkowski.crm.mobile.domain.nawozy.repository.NawozyRepository
import javax.inject.Inject

class GetOstatnieCenyUseCase @Inject constructor(
    private val repo: NawozyRepository,
) {
    suspend operator fun invoke(accountNum: String, itemIds: List<String>): Result<List<OstatniaCena>> =
        repo.getOstatnieCeny(accountNum, itemIds)
}
