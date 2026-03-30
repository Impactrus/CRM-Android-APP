package com.ossadkowski.app.ui.newrequest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.model.*
import com.ossadkowski.app.data.repository.NewRequestRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class NewRequestViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    lateinit var repository: NewRequestRepository

    private lateinit var viewModel: NewRequestViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = NewRequestViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadFormData loads types`() = runTest {
        val types = listOf(SlownikItemDto(1, "Urlop"), SlownikItemDto(2, "Delegacja"))
        whenever(repository.getTypy()).thenReturn(NetworkResult.Success(types))
        whenever(repository.getRodzajeUrlopu()).thenReturn(NetworkResult.Success(emptyList()))
        whenever(repository.getUzytkownicy()).thenReturn(NetworkResult.Success(emptyList()))

        viewModel.loadFormData()
        advanceUntilIdle()

        val result = viewModel.typy.value
        assertTrue(result is NetworkResult.Success)
        assertEquals(2, (result as NetworkResult.Success).data?.size)
    }

    @Test
    fun `loadFormData loads leave types`() = runTest {
        whenever(repository.getTypy()).thenReturn(NetworkResult.Success(emptyList()))
        whenever(repository.getRodzajeUrlopu()).thenReturn(
            NetworkResult.Success(listOf(SlownikItemDto(1, "Wypoczynkowy")))
        )
        whenever(repository.getUzytkownicy()).thenReturn(NetworkResult.Success(emptyList()))

        viewModel.loadFormData()
        advanceUntilIdle()

        assertTrue(viewModel.rodzajeUrlopu.value is NetworkResult.Success)
        assertEquals(1, (viewModel.rodzajeUrlopu.value as NetworkResult.Success).data?.size)
    }

    @Test
    fun `loadFormData loads users`() = runTest {
        whenever(repository.getTypy()).thenReturn(NetworkResult.Success(emptyList()))
        whenever(repository.getRodzajeUrlopu()).thenReturn(NetworkResult.Success(emptyList()))
        whenever(repository.getUzytkownicy()).thenReturn(
            NetworkResult.Success(listOf(SlownikItemDto(1, "Jan Kowalski")))
        )

        viewModel.loadFormData()
        advanceUntilIdle()

        assertTrue(viewModel.uzytkownicy.value is NetworkResult.Success)
    }

    @Test
    fun `submitWniosek success updates submitResult`() = runTest {
        val request = CreateWniosekRequest(1, "Urlop", null, "01-05", null, "reason", 1)
        whenever(repository.createWniosek(any())).thenReturn(NetworkResult.Success(Unit))

        viewModel.submitWniosek(request)
        advanceUntilIdle()

        assertTrue(viewModel.submitResult.value is NetworkResult.Success)
    }

    @Test
    fun `submitWniosek sets loading state`() = runTest {
        val request = CreateWniosekRequest(1, "Urlop", null, "01-05", null, "reason", 1)
        whenever(repository.createWniosek(any())).thenReturn(NetworkResult.Success(Unit))

        viewModel.submitWniosek(request)
        assertTrue(viewModel.submitResult.value is NetworkResult.Loading)

        advanceUntilIdle()
        assertTrue(viewModel.submitResult.value is NetworkResult.Success)
    }

    @Test
    fun `submitWniosek error updates submitResult`() = runTest {
        val request = CreateWniosekRequest(1, "Urlop", null, "01-05", null, "reason", 1)
        whenever(repository.createWniosek(any())).thenReturn(NetworkResult.Error("Validation error"))

        viewModel.submitWniosek(request)
        advanceUntilIdle()

        assertTrue(viewModel.submitResult.value is NetworkResult.Error)
        assertEquals("Validation error", viewModel.submitResult.value?.message)
    }

    @Test
    fun `submitWniosek network error`() = runTest {
        val request = CreateWniosekRequest(1, "Urlop", null, "01-05", null, "reason", 1)
        whenever(repository.createWniosek(any())).thenReturn(NetworkResult.Error("Network error"))

        viewModel.submitWniosek(request)
        advanceUntilIdle()

        assertTrue(viewModel.submitResult.value is NetworkResult.Error)
    }
}
