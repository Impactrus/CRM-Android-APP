package com.ossadkowski.crm.mobile.domain.nawozy.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.model.SlownikPozycja
import com.ossadkowski.crm.mobile.domain.nawozy.repository.NawozyRepository
import javax.inject.Inject

class GetSlownikUseCase @Inject constructor(
    private val repo: NawozyRepository,
) {
    suspend operator fun invoke(kategoria: String): Result<List<SlownikPozycja>> =
        repo.getSlownik(kategoria)
}
