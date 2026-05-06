package com.ossadkowski.crm.mobile.domain.serwis.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.model.MyOrder
import com.ossadkowski.crm.mobile.domain.serwis.repository.SerwisRepository
import javax.inject.Inject

class GetMyOrdersUseCase @Inject constructor(
    private val repo: SerwisRepository
) {
    suspend operator fun invoke(): Result<List<MyOrder>> = repo.getMyOrders()
}
