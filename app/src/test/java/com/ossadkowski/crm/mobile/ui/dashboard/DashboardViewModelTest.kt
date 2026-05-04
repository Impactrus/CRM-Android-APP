package com.ossadkowski.crm.mobile.ui.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.*
import com.ossadkowski.crm.mobile.data.repository.DashboardRepository
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
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class DashboardViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    lateinit var repository: DashboardRepository

    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DashboardViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProfile updates profile LiveData`() = runTest {
        val profile = AuthProfileResponse(1, "test", "admin", "IT", null, null, null)
        whenever(repository.getAuthProfile()).thenReturn(NetworkResult.Success(profile))

        viewModel.loadProfile()
        advanceUntilIdle()

        val result = viewModel.profile.value
        assertTrue(result is NetworkResult.Success)
        assertEquals("test", (result as NetworkResult.Success).data?.username)
    }

    @Test
    fun `loadProfile error updates profile with error`() = runTest {
        whenever(repository.getAuthProfile()).thenReturn(NetworkResult.Error("Unauthorized"))

        viewModel.loadProfile()
        advanceUntilIdle()

        assertTrue(viewModel.profile.value is NetworkResult.Error)
    }

    @Test
    fun `loadTasks updates tasks LiveData`() = runTest {
        val tasks = PaginatedResponse(
            items = listOf(TaskItem(1, "Task 1", "desc", "open", "user", "2026-01-01", "2026-02-01")),
            totalCount = 1, totalPages = 1
        )
        whenever(repository.getTasks(1, 10, null)).thenReturn(NetworkResult.Success(tasks))

        viewModel.loadTasks()
        advanceUntilIdle()

        val result = viewModel.tasks.value
        assertTrue(result is NetworkResult.Success)
        assertEquals(1, (result as NetworkResult.Success).data?.items?.size)
    }

    @Test
    fun `loadTasks sets final state after completion`() = runTest {
        whenever(repository.getTasks(any(), any(), anyOrNull())).thenReturn(
            NetworkResult.Success(PaginatedResponse(emptyList<TaskItem>(), 0, 0))
        )

        viewModel.loadTasks()
        advanceUntilIdle()

        assertTrue(viewModel.tasks.value is NetworkResult.Success)
    }

    @Test
    fun `loadTasks uses current page and search`() = runTest {
        viewModel.tasksPage = 3
        viewModel.tasksSearch = "query"
        whenever(repository.getTasks(3, 10, "query")).thenReturn(
            NetworkResult.Success(PaginatedResponse(emptyList<TaskItem>(), 0, 0))
        )

        viewModel.loadTasks()
        advanceUntilIdle()

        verify(repository).getTasks(3, 10, "query")
    }

    @Test
    fun `loadWnioski updates wnioski LiveData`() = runTest {
        val wnioski = PaginatedResponse(
            items = listOf(WniosekItem(1, "user", "Urlop", "01-05", null, "reason", "1", "draft")),
            totalCount = 1, totalPages = 1
        )
        whenever(repository.getWnioski(1, 1, 10)).thenReturn(NetworkResult.Success(wnioski))

        viewModel.loadWnioski(1)
        advanceUntilIdle()

        assertTrue(viewModel.wnioski.value is NetworkResult.Success)
    }

    @Test
    fun `sendWniosek success calls onResult with true`() = runTest {
        whenever(repository.sendWniosek(1, 1)).thenReturn(NetworkResult.Success(Unit))
        var result: Boolean? = null

        viewModel.sendWniosek(1, 1) { result = it }
        advanceUntilIdle()

        assertTrue(result == true)
    }

    @Test
    fun `sendWniosek failure calls onResult with false`() = runTest {
        whenever(repository.sendWniosek(1, 1)).thenReturn(NetworkResult.Error("fail"))
        var result: Boolean? = null

        viewModel.sendWniosek(1, 1) { result = it }
        advanceUntilIdle()

        assertTrue(result == false)
    }

    @Test
    fun `deleteWniosek success calls onResult with true`() = runTest {
        whenever(repository.deleteWniosek(1, 1)).thenReturn(NetworkResult.Success(Unit))
        var result: Boolean? = null

        viewModel.deleteWniosek(1, 1) { result = it }
        advanceUntilIdle()

        assertTrue(result == true)
    }

    @Test
    fun `deleteWniosek failure calls onResult with false`() = runTest {
        whenever(repository.deleteWniosek(1, 1)).thenReturn(NetworkResult.Error("fail"))
        var result: Boolean? = null

        viewModel.deleteWniosek(1, 1) { result = it }
        advanceUntilIdle()

        assertTrue(result == false)
    }

    @Test
    fun `resubmitWniosek success calls onResult with true`() = runTest {
        whenever(repository.resubmitWniosek(1, 1)).thenReturn(NetworkResult.Success(Unit))
        var result: Boolean? = null

        viewModel.resubmitWniosek(1, 1) { result = it }
        advanceUntilIdle()

        assertTrue(result == true)
    }

    @Test
    fun `logout calls onDone`() = runTest {
        whenever(repository.logout()).thenReturn(NetworkResult.Success(Unit))
        var done = false

        viewModel.logout { done = true }
        advanceUntilIdle()

        assertTrue(done)
    }

    @Test
    fun `pagination state defaults`() {
        assertEquals(1, viewModel.tasksPage)
        assertNull(viewModel.tasksSearch)
        assertEquals(1, viewModel.wnioskiPage)
    }
}
