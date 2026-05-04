package com.ossadkowski.crm.mobile.data.repository

import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.ApiService
import com.ossadkowski.crm.mobile.data.model.*
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
class EditRequestRepositoryTest {

    @Mock lateinit var apiService: ApiService
    private lateinit var repository: EditRequestRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = EditRequestRepository(apiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getDetail success`() = runTest {
        val detail = WniosekDetailDto(
            id = 1, userId = 1, managerId = 2, hrId = 3, typ = "Urlop",
            rodzajUrlopu = "Wypoczynkowy", odDo = "01-05", godziny = 8,
            powod = "Wypoczynek", iloscDni = 5, dokumenty = 0, status = "nowy",
            managerApprovedAt = null, hrApprovedAt = null, createdAt = "2026-01-01",
            zastepstwoUserId = null, zastepstwoUsername = null,
            komentarzManager = null, komentarzHr = null, username = "user"
        )
        whenever(apiService.getWniosekDetail(any())).thenReturn(detail)

        val result = repository.getDetail(1)

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, result.data?.id)
    }

    @Test
    fun `getDetail error`() = runTest {
        whenever(apiService.getWniosekDetail(any())).thenThrow(RuntimeException("Not found"))

        val result = repository.getDetail(99)

        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `update success`() = runTest {
        whenever(apiService.updateWniosek(any(), any())).thenReturn(Unit)

        val request = CreateWniosekRequest(userId = 1, typ = "Urlop", odDo = "01-05", powod = "Updated", iloscDni = 3)
        val result = repository.update(1, request)

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `update error`() = runTest {
        whenever(apiService.updateWniosek(any(), any())).thenThrow(RuntimeException("Bad request"))

        val request = CreateWniosekRequest(userId = 1, typ = "Urlop", odDo = "01-05", powod = "Updated", iloscDni = 3)
        val result = repository.update(1, request)

        assertTrue(result is NetworkResult.Error)
    }
}
