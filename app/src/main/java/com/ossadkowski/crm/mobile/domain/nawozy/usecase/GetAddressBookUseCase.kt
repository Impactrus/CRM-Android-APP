package com.ossadkowski.crm.mobile.domain.nawozy.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.model.AdresDostawy
import com.ossadkowski.crm.mobile.domain.nawozy.repository.NawozyRepository
import javax.inject.Inject

class GetAddressBookUseCase @Inject constructor(
    private val repo: NawozyRepository,
) {
    suspend operator fun invoke(search: String? = null): Result<List<AdresDostawy>> =
        repo.getAddressBook(search)
}
