package com.ossadkowski.crm.mobile.domain.serwis.usecase

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.repository.OrderFilters
import com.ossadkowski.crm.mobile.domain.serwis.repository.PagedOrders
import com.ossadkowski.crm.mobile.domain.serwis.repository.SerwisRepository
import javax.inject.Inject

class ListOrdersUseCase @Inject constructor(
    private val repo: SerwisRepository
) {
    suspend operator fun invoke(filters: OrderFilters = OrderFilters()): Result<PagedOrders> =
        repo.listOrders(filters)
}
