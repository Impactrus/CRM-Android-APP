package com.ossadkowski.crm.mobile.domain.nawozy.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.model.Kontrahent
import com.ossadkowski.crm.mobile.domain.nawozy.repository.NawozyRepository
import javax.inject.Inject

class SearchKontrahenciUseCase @Inject constructor(
    private val repo: NawozyRepository,
) {
    suspend operator fun invoke(search: String?, myOnly: Boolean = false): Result<List<Kontrahent>> =
        repo.searchKontrahenci(search, myOnly)
}
