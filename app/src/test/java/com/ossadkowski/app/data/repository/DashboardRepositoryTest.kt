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
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class DashboardRepositoryTest {

    @Mock lateinit var apiService: ApiService
    private lateinit var repository: DashboardRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = DashboardRepository(apiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getAuthProfile success`() = runTest {
        val profile = AuthProfileResponse(userId = 1, username = "user", role = "admin", dzial = "IT", employeeCacheId = 5, claims = arrayOf("read"), claimsVersion = 1)
        whenever(apiService.getAuthProfile()).thenReturn(profile)

        val result = repository.getAuthProfile()

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, result.data?.userId)
    }

    @Test
    fun `getAuthProfile error`() = runTest {
        whenever(apiService.getAuthProfile()).thenThrow(RuntimeException("Unauthorized"))

        val result = repository.getAuthProfile()

        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `getTasks success with pagination`() = runTest {
        val tasks = PaginatedResponse(
            items = listOf(TaskItem(1, "Task 1", "desc", "open", "user1", "2026-01-01", "2026-02-01")),
            totalCount = 1, totalPages = 1
        )
        whenever(apiService.getTasks(any())).thenReturn(tasks)

        val result = repository.getTasks(1, 10, null)

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, result.data?.items?.size)
    }

    @Test
    fun `getTasks error`() = runTest {
        whenever(apiService.getTasks(any())).thenThrow(RuntimeException("Server error"))

        val result = repository.getTasks(1, 10, null)

        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `getWnioski success`() = runTest {
        val wnioski = PaginatedResponse(
            items = listOf(WniosekItem(1, "user", "urlop", "01-05", null, "powod", "2", "nowy")),
            totalCount = 1, totalPages = 1
        )
        whenever(apiService.getWnioski(any())).thenReturn(wnioski)

        val result = repository.getWnioski(1, 1, 10)

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, result.data?.items?.size)
    }

    @Test
    fun `sendWniosek success`() = runTest {
        whenever(apiService.sendWniosek(any(), any())).thenReturn(Unit)

        val result = repository.sendWniosek(1, 1)

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `sendWniosek error`() = runTest {
        whenever(apiService.sendWniosek(any(), any())).thenThrow(RuntimeException("Bad request"))

        val result = repository.sendWniosek(1, 1)

        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `resubmitWniosek success`() = runTest {
        whenever(apiService.resubmitWniosek(any(), any())).thenReturn(Unit)

        val result = repository.resubmitWniosek(1, 1)

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `resubmitWniosek error`() = runTest {
        whenever(apiService.resubmitWniosek(any(), any())).thenThrow(RuntimeException("Error"))

        val result = repository.resubmitWniosek(1, 1)

        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `deleteWniosek success`() = runTest {
        whenever(apiService.deleteWniosek(any(), any())).thenReturn(Unit)

        val result = repository.deleteWniosek(1, 1)

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `deleteWniosek error`() = runTest {
        whenever(apiService.deleteWniosek(any(), any())).thenThrow(RuntimeException("Not found"))

        val result = repository.deleteWniosek(1, 1)

        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `logout success`() = runTest {
        whenever(apiService.logout()).thenReturn(Response.success(null))

        val result = repository.logout()

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `logout error still returns Success`() = runTest {
        whenever(apiService.logout()).thenThrow(RuntimeException("Network"))

        val result = repository.logout()

        assertTrue(result is NetworkResult.Success)
    }
}
