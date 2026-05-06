package com.ossadkowski.crm.mobile.domain.serwis.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.model.TimeEntry
import com.ossadkowski.crm.mobile.domain.serwis.repository.NewTimeEntry
import com.ossadkowski.crm.mobile.domain.serwis.repository.SerwisRepository
import javax.inject.Inject

class AddTimeEntryUseCase @Inject constructor(
    private val repo: SerwisRepository
) {
    suspend operator fun invoke(cardNum: String, req: NewTimeEntry): Result<TimeEntry> =
        repo.addTimeEntry(cardNum, req)
}
