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
class NewRequestRepositoryTest {

    @Mock lateinit var apiService: ApiService
    private lateinit var repository: NewRequestRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = NewRequestRepository(apiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getTypy success`() = runTest {
        val types = listOf(SlownikItemDto(1, "Urlop"), SlownikItemDto(2, "Delegacja"))
        whenever(apiService.getWnioskiTypy()).thenReturn(types)

        val result = repository.getTypy()

        assertTrue(result is NetworkResult.Success)
        assertEquals(2, result.data?.size)
    }

    @Test
    fun `getTypy error`() = runTest {
        whenever(apiService.getWnioskiTypy()).thenThrow(RuntimeException("Error"))

        val result = repository.getTypy()

        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `getRodzajeUrlopu success`() = runTest {
        val types = listOf(SlownikItemDto(1, "Wypoczynkowy"))
        whenever(apiService.getRodzajeUrlopu()).thenReturn(types)

        val result = repository.getRodzajeUrlopu()

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, result.data?.size)
    }

    @Test
    fun `getRodzajeUrlopu error`() = runTest {
        whenever(apiService.getRodzajeUrlopu()).thenThrow(RuntimeException("Error"))

        val result = repository.getRodzajeUrlopu()

        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `getUzytkownicy success`() = runTest {
        val users = listOf(SlownikItemDto(1, "Jan Kowalski"))
        whenever(apiService.getWnioskiUzytkownicy()).thenReturn(users)

        val result = repository.getUzytkownicy()

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, result.data?.size)
    }

    @Test
    fun `getUzytkownicy error`() = runTest {
        whenever(apiService.getWnioskiUzytkownicy()).thenThrow(RuntimeException("Error"))

        val result = repository.getUzytkownicy()

        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `createWniosek success`() = runTest {
        whenever(apiService.createWniosek(any())).thenReturn(Unit)

        val request = CreateWniosekRequest(userId = 1, typ = "Urlop", odDo = "01-05", powod = "test", iloscDni = 1)
        val result = repository.createWniosek(request)

        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `createWniosek validation error`() = runTest {
        whenever(apiService.createWniosek(any())).thenThrow(RuntimeException("400 Bad Request"))

        val request = CreateWniosekRequest(userId = 1, typ = "Urlop", odDo = "01-05", powod = "test", iloscDni = 1)
        val result = repository.createWniosek(request)

        assertTrue(result is NetworkResult.Error)
        assertEquals("400 Bad Request", result.message)
    }
}
