package com.ossadkowski.crm.mobile.domain.serwis.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.model.Machine
import com.ossadkowski.crm.mobile.domain.serwis.repository.SerwisRepository
import javax.inject.Inject

/**
 * Returns the machine identified by serial number, including service-order history.
 * Backed by `GET /service-orders/machines/{serial}`.
 */
class GetMachineHistoryUseCase @Inject constructor(
    private val repo: SerwisRepository
) {
    suspend operator fun invoke(serial: String, accountNum: String? = null): Result<Machine> =
        repo.getMachineBySerial(serial, accountNum)
}
