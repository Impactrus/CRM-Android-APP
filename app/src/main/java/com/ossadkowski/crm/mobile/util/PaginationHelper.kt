package com.ossadkowski.crm.mobile.util

import kotlin.math.min

class PaginationHelper(
    private val pageSize: Int = 10,
    private val onPageChanged: (page: Int) -> Unit
) {
    var currentPage = 1
        private set
    var totalPages = 1
        private set
    var totalCount = 0
        private set

    fun updateFromResponse(totalCount: Int, totalPages: Int) {
        this.totalCount = totalCount
        this.totalPages = totalPages
    }

    fun updateFromGenericResponse(total: Int, pageSize: Int) {
        this.totalCount = total
        this.totalPages = if (pageSize > 0) ((total + pageSize - 1) / pageSize) else 1
    }

    fun nextPage() {
        if (currentPage < totalPages) {
            currentPage++
            onPageChanged(currentPage)
        }
    }

    fun prevPage() {
        if (currentPage > 1) {
            currentPage--
            onPageChanged(currentPage)
        }
    }

    fun reset() {
        currentPage = 1
    }

    fun hasPrev(): Boolean = currentPage > 1
    fun hasNext(): Boolean = currentPage < totalPages

    fun getShowingStart(): Int = ((currentPage - 1) * pageSize) + 1
    fun getShowingEnd(): Int = min(currentPage * pageSize, totalCount)
}
