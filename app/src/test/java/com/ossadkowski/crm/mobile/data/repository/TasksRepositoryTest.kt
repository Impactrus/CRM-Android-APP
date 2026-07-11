package com.ossadkowski.crm.mobile.data.repository

import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.ApiService
import com.ossadkowski.crm.mobile.data.cache.AppDatabase
import com.ossadkowski.crm.mobile.data.cache.ActionQueue
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
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class TasksRepositoryTest {

    @Mock lateinit var apiService: ApiService
    @Mock lateinit var db: AppDatabase
    @Mock lateinit var actionQueue: ActionQueue
    private lateinit var repository: TasksRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = TasksRepository(apiService, db, actionQueue)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getList success`() = runTest {
        val item = TaskListItemDto(1, null, "typ", "Title", "Firma", "2026-01-01", "User", 1, "nowy", false, "2026-01-01", "Creator")
        val response = PaginatedResponse(_items = listOf(item), _totalCount = 1, _totalPages = 1)
        whenever(apiService.getTasksV2(any(), any(), anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(response)

        val result = repository.getList(1, 10, null, null, null)

        assertTrue(result is NetworkResult.Success<*>)
        assertEquals(1, result.data?.items?.size)
    }

    @Test
    fun `getList error`() = runTest {
        whenever(apiService.getTasksV2(any(), any(), anyOrNull(), anyOrNull(), anyOrNull())).thenThrow(RuntimeException("Error"))

        val result = repository.getList(1, 10, null, null, null)

        assertTrue(result is NetworkResult.Error<*>)
    }

    @Test
    fun `getDetail success`() = runTest {
        val detail = TaskDetailDto(1, null, "typ", "Title", "desc", "Firma", "2026-01-01", "User", 1, "nowy", false, "2026-01-01", "Creator", 1, null, null, null, null)
        whenever(apiService.getTaskDetail(any())).thenReturn(detail)

        val result = repository.getDetail(1)

        assertTrue(result is NetworkResult.Success<*>)
        assertEquals(1, result.data?.id)
    }

    @Test
    fun `getDetail error`() = runTest {
        whenever(apiService.getTaskDetail(any())).thenThrow(RuntimeException("Not found"))

        val result = repository.getDetail(99)

        assertTrue(result is NetworkResult.Error<*>)
    }

    @Test
    fun `getComments success`() = runTest {
        val comments = listOf(TaskCommentDto(1, 1, "user", "comment text", "2026-01-01"))
        whenever(apiService.getTaskComments(any())).thenReturn(comments)

        val result = repository.getComments(1)

        assertTrue(result is NetworkResult.Success<*>)
        assertEquals(1, result.data?.size)
    }

    @Test
    fun `getComments error`() = runTest {
        whenever(apiService.getTaskComments(any())).thenThrow(RuntimeException("Error"))

        val result = repository.getComments(1)

        assertTrue(result is NetworkResult.Error<*>)
    }

    @Test
    fun `addComment success`() = runTest {
        whenever(apiService.addTaskComment(any(), any())).thenReturn(Unit)

        val result = repository.addComment(1, "My comment")

        assertTrue(result is NetworkResult.Success<*>)
    }

    @Test
    fun `addComment error`() = runTest {
        whenever(apiService.addTaskComment(any(), any())).thenThrow(RuntimeException("Error"))

        val result = repository.addComment(1, "My comment")

        assertTrue(result is NetworkResult.Success<*>)
        assertEquals("queued_offline", (result as NetworkResult.Success).data)
    }

    @Test
    fun `getFiles success`() = runTest {
        val files = listOf(TaskFileDto(1, "file.pdf", "2026-01-01", "user"))
        whenever(apiService.getTaskFiles(any())).thenReturn(files)

        val result = repository.getFiles(1)

        assertTrue(result is NetworkResult.Success<*>)
    }

    @Test
    fun `getHistoria success`() = runTest {
        val historia = listOf(TaskHistoriaDto(1, "zmiana", "user", "szczegoly", "2026-01-01"))
        whenever(apiService.getTaskHistoria(any())).thenReturn(historia)

        val result = repository.getHistoria(1)

        assertTrue(result is NetworkResult.Success<*>)
    }

    @Test
    fun `getObservers success`() = runTest {
        val observers = listOf(TaskObserverDto(1, 1, "user", "IT", "admin", "2026-01-01"))
        whenever(apiService.getTaskObservers(any())).thenReturn(observers)

        val result = repository.getObservers(1)

        assertTrue(result is NetworkResult.Success<*>)
    }

    @Test
    fun `changeStatus success`() = runTest {
        whenever(apiService.changeTaskStatus(any(), any())).thenReturn(Unit)

        val result = repository.changeStatus(1, "w_trakcie")

        assertTrue(result is NetworkResult.Success<*>)
    }

    @Test
    fun `changeStatus error`() = runTest {
        whenever(apiService.changeTaskStatus(any(), any())).thenThrow(RuntimeException("Forbidden"))

        val result = repository.changeStatus(1, "w_trakcie")

        assertTrue(result is NetworkResult.Success<*>)
        assertEquals("queued_offline", (result as NetworkResult.Success).data)
    }
}
