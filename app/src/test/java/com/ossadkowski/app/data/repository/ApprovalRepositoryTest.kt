package com.ossadkowski.app.data.repository

import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.api.ApiService
import com.ossadkowski.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class ApprovalRepositoryTest {

    @Mock lateinit var apiService: ApiService
    private lateinit var repository: ApprovalRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = ApprovalRepository(apiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getApprovals success with pagination`() = runTest {
        val response = PaginatedResponse(
            items = listOf(WniosekItem(1, "user", "urlop", "01-05", null, "powod", "2", "oczekujacy")),
            totalCount = 1, totalPages = 1
        )
        whenever(apiService.getApprovals(any())).thenReturn(response)

        val result = repository.getApprovals(1, 1, 10, null)

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, result.data?.items?.size)
    }

    @Test
    fun `getApprovals error`() = runTest {
        whenever(apiService.getApprovals(any())).thenThrow(RuntimeException("Error"))

        val result = repository.getApprovals(1, 1, 10, null)

        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `approveManager success approved true`() = runTest {
        whenever(apiService.approveManager(any(), any())).thenReturn(Unit)

        val result = repository.approveManager(1, 5, true)

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `approveManager reject approved false`() = runTest {
        whenever(apiService.approveManager(any(), any())).thenReturn(Unit)

        val result = repository.approveManager(1, 5, false)

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `approveManager error`() = runTest {
        whenever(apiService.approveManager(any(), any())).thenThrow(RuntimeException("Forbidden"))

        val result = repository.approveManager(1, 5, true)

        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `approveHr success`() = runTest {
        whenever(apiService.approveHr(any(), any())).thenReturn(Unit)

        val result = repository.approveHr(1, 3, true)

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `approveHr reject`() = runTest {
        whenever(apiService.approveHr(any(), any())).thenReturn(Unit)

        val result = repository.approveHr(1, 3, false)

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `approveHr error`() = runTest {
        whenever(apiService.approveHr(any(), any())).thenThrow(RuntimeException("Server error"))

        val result = repository.approveHr(1, 3, true)

        assertTrue(result is NetworkResult.Error)
    }
}
