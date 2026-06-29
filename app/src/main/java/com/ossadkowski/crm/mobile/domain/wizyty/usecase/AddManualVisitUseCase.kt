package com.ossadkowski.crm.mobile.domain.wizyty.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.wizyty.model.NewVisitEvent
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitEvent
import com.ossadkowski.crm.mobile.domain.wizyty.repository.VisitRepository
import javax.inject.Inject

class AddManualVisitUseCase @Inject constructor(
    private val repo: VisitRepository,
) {
    suspend operator fun invoke(new: NewVisitEvent): Result<VisitEvent> = repo.addManualVisit(new)
}
