package com.ossadkowski.crm.mobile.domain.nawozy.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.model.PricingResult
import com.ossadkowski.crm.mobile.domain.nawozy.repository.NawozyRepository
import com.ossadkowski.crm.mobile.domain.nawozy.repository.PricingReverseZapytanie
import javax.inject.Inject

/** Target sale price → required discount. Backend is the source of truth. */
class CalcPricingReverseUseCase @Inject constructor(
    private val repo: NawozyRepository,
) {
    suspend operator fun invoke(req: PricingReverseZapytanie): Result<PricingResult> =
        repo.calcPricingReverse(req)
}
