package com.ossadkowski.crm.mobile.domain.serwis.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.repository.OrderWithCards
import com.ossadkowski.crm.mobile.domain.serwis.repository.SerwisRepository
import javax.inject.Inject

class GetOrderWithCardsUseCase @Inject constructor(
    private val repo: SerwisRepository
) {
    suspend operator fun invoke(orderNum: String): Result<OrderWithCards> =
        repo.getOrderWithCards(orderNum)
}
