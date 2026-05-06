package com.ossadkowski.crm.mobile.domain.serwis.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.model.Machine
import com.ossadkowski.crm.mobile.domain.serwis.repository.SerwisRepository
import javax.inject.Inject

class GetCustomerMachinesUseCase @Inject constructor(
    private val repo: SerwisRepository
) {
    suspend operator fun invoke(accountNum: String? = null): Result<List<Machine>> =
        repo.getCustomerMachines(accountNum)
}
