package com.ossadkowski.crm.mobile.domain.serwis.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.model.Activity
import com.ossadkowski.crm.mobile.domain.serwis.repository.NewActivity
import com.ossadkowski.crm.mobile.domain.serwis.repository.SerwisRepository
import javax.inject.Inject

class AddActivityUseCase @Inject constructor(
    private val repo: SerwisRepository
) {
    suspend operator fun invoke(cardNum: String, req: NewActivity): Result<Activity> =
        repo.addActivity(cardNum, req)
}
