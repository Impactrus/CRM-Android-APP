package com.ossadkowski.crm.mobile.domain.nawozy.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.model.Koszyk
import com.ossadkowski.crm.mobile.domain.nawozy.repository.NawozyRepository
import com.ossadkowski.crm.mobile.domain.nawozy.repository.NowaPozycja
import javax.inject.Inject

class AddPozycjaUseCase @Inject constructor(
    private val repo: NawozyRepository,
) {
    suspend operator fun invoke(koszykId: Long, pozycja: NowaPozycja): Result<Koszyk> =
        repo.addPozycja(koszykId, pozycja)
}
