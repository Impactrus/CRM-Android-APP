package com.ossadkowski.app.ui.editrequest

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.model.*
import com.ossadkowski.app.data.repository.EditRequestRepository
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
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class EditRequestViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    lateinit var repository: EditRequestRepository

    private lateinit var viewModel: EditRequestViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = EditRequestViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadDetail updates detail LiveData`() = runTest {
        val detail = WniosekDetailDto(
            1, 1, 2, 3, "Urlop", "Wypoczynkowy", "01-05", 8, "reason", 1,
            null, "draft", null, null, "2026-01-01", null, null, null, null, "user"
        )
        whenever(repository.getDetail(1)).thenReturn(NetworkResult.Success(detail))

        viewModel.loadDetail(1)
        advanceUntilIdle()

        assertTrue(viewModel.detail.value is NetworkResult.Success)
        assertEquals("Urlop", (viewModel.detail.value as NetworkResult.Success).data?.typ)
    }

    @Test
    fun `loadDetail sets loading state`() = runTest {
        val detail = WniosekDetailDto(1, 1, 2, 3, "U", "W", "01", 8, "r", 1, null, "d", null, null, "c", null, null, null, null, "u")
        whenever(repository.getDetail(1)).thenReturn(NetworkResult.Success(detail))

        viewModel.loadDetail(1)
        assertTrue(viewModel.detail.value is NetworkResult.Loading)

        advanceUntilIdle()
    }

    @Test
    fun `loadDetail error`() = runTest {
        whenever(repository.getDetail(999)).thenReturn(NetworkResult.Error("Not found"))

        viewModel.loadDetail(999)
        advanceUntilIdle()

        assertTrue(viewModel.detail.value is NetworkResult.Error)
    }

    @Test
    fun `update success updates updateResult`() = runTest {
        val request = CreateWniosekRequest(1, "Urlop", null, "01-05", null, "reason", 1)
        whenever(repository.update(eq(1), any())).thenReturn(NetworkResult.Success(Unit))

        viewModel.update(1, request)
        advanceUntilIdle()

        assertTrue(viewModel.updateResult.value is NetworkResult.Success)
    }

    @Test
    fun `update sets loading state`() = runTest {
        val request = CreateWniosekRequest(1, "Urlop", null, "01-05", null, "reason", 1)
        whenever(repository.update(eq(1), any())).thenReturn(NetworkResult.Success(Unit))

        viewModel.update(1, request)
        assertTrue(viewModel.updateResult.value is NetworkResult.Loading)

        advanceUntilIdle()
    }

    @Test
    fun `update failure`() = runTest {
        val request = CreateWniosekRequest(1, "Urlop", null, "01-05", null, "reason", 1)
        whenever(repository.update(eq(1), any())).thenReturn(NetworkResult.Error("Validation error"))

        viewModel.update(1, request)
        advanceUntilIdle()

        assertTrue(viewModel.updateResult.value is NetworkResult.Error)
        assertEquals("Validation error", viewModel.updateResult.value?.message)
    }
}
