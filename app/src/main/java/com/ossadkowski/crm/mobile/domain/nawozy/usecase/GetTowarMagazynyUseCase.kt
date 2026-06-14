package com.ossadkowski.crm.mobile.domain.nawozy.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.model.MagazynStan
import com.ossadkowski.crm.mobile.domain.nawozy.repository.NawozyRepository
import javax.inject.Inject

class GetTowarMagazynyUseCase @Inject constructor(
    private val repo: NawozyRepository,
) {
    suspend operator fun invoke(itemId: String): Result<List<MagazynStan>> =
        repo.getTowarMagazyny(itemId)
}
