package com.ossadkowski.crm.mobile.domain.nawozy.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.model.Koszyk
import com.ossadkowski.crm.mobile.domain.nawozy.repository.NawozyRepository
import javax.inject.Inject

/**
 * Submits the cart. When the customer limit is frozen/blocked or the max discount
 * was exceeded, [warningsAcknowledged] must be true for the backend to accept it;
 * otherwise the order lands in `czeka_na_zatwierdzenie`.
 */
class SubmitKoszykUseCase @Inject constructor(
    private val repo: NawozyRepository,
) {
    suspend operator fun invoke(koszykId: Long, warningsAcknowledged: Boolean = false): Result<Koszyk> =
        repo.submitKoszyk(koszykId, warningsAcknowledged)
}
