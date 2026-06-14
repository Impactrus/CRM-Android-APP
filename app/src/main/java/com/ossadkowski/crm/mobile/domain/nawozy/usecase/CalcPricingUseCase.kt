package com.ossadkowski.crm.mobile.domain.nawozy.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.model.PricingResult
import com.ossadkowski.crm.mobile.domain.nawozy.repository.NawozyRepository
import com.ossadkowski.crm.mobile.domain.nawozy.repository.PricingZapytanie
import javax.inject.Inject

/** Discount (kwotowy) → sale price. Backend is the source of truth. */
class CalcPricingUseCase @Inject constructor(
    private val repo: NawozyRepository,
) {
    suspend operator fun invoke(req: PricingZapytanie): Result<PricingResult> =
        repo.calcPricing(req)
}
