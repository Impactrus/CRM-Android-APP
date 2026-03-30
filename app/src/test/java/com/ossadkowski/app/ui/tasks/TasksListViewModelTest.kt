package com.ossadkowski.app.ui.tasks

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.model.*
import com.ossadkowski.app.data.repository.TasksRepository
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class TasksListViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    lateinit var repository: TasksRepository

    private lateinit var viewModel: TasksListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TasksListViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load updates items LiveData`() = runTest {
        val item = TaskListItemDto(1, null, "typ", "Title", "Firma", "2026-01-01", "user", 1, "open", false, "2026-01-01", "admin")
        val response = PaginatedResponse(listOf(item), 1, 1)
        whenever(repository.getList(1, 10, null, null, null)).thenReturn(NetworkResult.Success(response))

        viewModel.load()
        advanceUntilIdle()

        assertTrue(viewModel.items.value is NetworkResult.Success)
        assertEquals(1, (viewModel.items.value as NetworkResult.Success).data?.items?.size)
    }

    @Test
    fun `load sets loading state`() = runTest {
        whenever(repository.getList(1, 10, null, null, null)).thenReturn(
            NetworkResult.Success(PaginatedResponse(emptyList<TaskListItemDto>(), 0, 0))
        )

        viewModel.load()
        assertTrue(viewModel.items.value is NetworkResult.Loading)

        advanceUntilIdle()
    }

    @Test
    fun `load with search`() = runTest {
        viewModel.search = "query"
        whenever(repository.getList(1, 10, "query", null, null)).thenReturn(
            NetworkResult.Success(PaginatedResponse(emptyList<TaskListItemDto>(), 0, 0))
        )

        viewModel.load()
        advanceUntilIdle()

        verify(repository).getList(1, 10, "query", null, null)
    }

    @Test
    fun `load with status filter`() = runTest {
        viewModel.statusFilter = "open"
        whenever(repository.getList(1, 10, null, "open", null)).thenReturn(
            NetworkResult.Success(PaginatedResponse(emptyList<TaskListItemDto>(), 0, 0))
        )

        viewModel.load()
        advanceUntilIdle()

        verify(repository).getList(1, 10, null, "open", null)
    }

    @Test
    fun `load with pagination`() = runTest {
        viewModel.page = 4
        whenever(repository.getList(4, 10, null, null, null)).thenReturn(
            NetworkResult.Success(PaginatedResponse(emptyList<TaskListItemDto>(), 0, 0))
        )

        viewModel.load()
        advanceUntilIdle()

        verify(repository).getList(4, 10, null, null, null)
    }

    @Test
    fun `default state`() {
        assertEquals(1, viewModel.page)
        assertNull(viewModel.search)
        assertNull(viewModel.statusFilter)
    }
}
