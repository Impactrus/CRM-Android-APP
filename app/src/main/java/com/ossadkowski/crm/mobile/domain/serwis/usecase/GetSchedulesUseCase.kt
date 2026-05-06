package com.ossadkowski.crm.mobile.domain.serwis.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.model.Schedule
import com.ossadkowski.crm.mobile.domain.serwis.repository.ScheduleFilters
import com.ossadkowski.crm.mobile.domain.serwis.repository.SerwisRepository
import javax.inject.Inject

class GetSchedulesUseCase @Inject constructor(
    private val repo: SerwisRepository
) {
    suspend operator fun invoke(filters: ScheduleFilters = ScheduleFilters()): Result<List<Schedule>> =
        repo.getSchedules(filters)
}
