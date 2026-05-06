package com.ossadkowski.crm.mobile.domain.serwis.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.model.WarrantyCheck
import com.ossadkowski.crm.mobile.domain.serwis.repository.SerwisRepository
import javax.inject.Inject

class CheckWarrantyUseCase @Inject constructor(
    private val repo: SerwisRepository
) {
    suspend operator fun invoke(machineId: Long): Result<WarrantyCheck> =
        repo.checkWarranty(machineId)
}
