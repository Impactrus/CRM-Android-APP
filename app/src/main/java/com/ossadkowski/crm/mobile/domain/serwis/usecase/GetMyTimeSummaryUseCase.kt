package com.ossadkowski.crm.mobile.domain.serwis.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.model.TimeSummary
import com.ossadkowski.crm.mobile.domain.serwis.repository.SerwisRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Fetches the calling JWT user's time / travel / km summary for an inclusive
 * `[dateFrom, dateTo]` window. Used by the **Mój czas** (weekly) and
 * **Profil** (monthly stats) screens.
 */
class GetMyTimeSummaryUseCase @Inject constructor(
    private val repo: SerwisRepository
) {
    suspend operator fun invoke(
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): Result<TimeSummary> = repo.getMyTimeSummary(dateFrom, dateTo)
}
