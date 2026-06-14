package com.ossadkowski.crm.mobile.domain.nawozy.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.repository.NawozyRepository
import com.ossadkowski.crm.mobile.domain.nawozy.repository.PagedZamowienia
import com.ossadkowski.crm.mobile.domain.nawozy.repository.ZamowieniaFilters
import javax.inject.Inject

class ListZamowieniaUseCase @Inject constructor(
    private val repo: NawozyRepository,
) {
    suspend operator fun invoke(filters: ZamowieniaFilters = ZamowieniaFilters()): Result<PagedZamowienia> =
        repo.listZamowienia(filters)
}
