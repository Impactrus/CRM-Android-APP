package com.ossadkowski.crm.mobile.domain.nawozy.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.model.WariantLogistyczny
import com.ossadkowski.crm.mobile.domain.nawozy.repository.NawozyRepository
import com.ossadkowski.crm.mobile.domain.nawozy.repository.WariantyZapytanie
import javax.inject.Inject

/**
 * Logistics calculator: returns warehouse→delivery variants (km, PLN/t, total
 * cost, max discount) sorted cheapest-first. PLN/t is read-only downstream.
 */
class GetWariantyLogistykaUseCase @Inject constructor(
    private val repo: NawozyRepository,
) {
    suspend operator fun invoke(req: WariantyZapytanie): Result<List<WariantLogistyczny>> =
        repo.getWarianty(req)
}
