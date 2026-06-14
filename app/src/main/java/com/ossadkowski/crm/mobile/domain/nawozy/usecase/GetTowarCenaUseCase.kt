package com.ossadkowski.crm.mobile.domain.nawozy.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.repository.Cennik
import com.ossadkowski.crm.mobile.domain.nawozy.repository.NawozyRepository
import javax.inject.Inject

class GetTowarCenaUseCase @Inject constructor(
    private val repo: NawozyRepository,
) {
    suspend operator fun invoke(itemId: String, cennik: String = Cennik.BAZOWY): Result<Double?> =
        repo.getTowarCena(itemId, cennik)
}
