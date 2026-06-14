package com.ossadkowski.crm.mobile.domain.nawozy.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.model.Koszyk
import com.ossadkowski.crm.mobile.domain.nawozy.repository.NawozyRepository
import javax.inject.Inject

class DeletePozycjaUseCase @Inject constructor(
    private val repo: NawozyRepository,
) {
    suspend operator fun invoke(koszykId: Long, lineId: Long): Result<Koszyk> =
        repo.deletePozycja(koszykId, lineId)
}
