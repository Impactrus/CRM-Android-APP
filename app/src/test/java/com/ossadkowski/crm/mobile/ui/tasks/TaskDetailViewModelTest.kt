package com.ossadkowski.crm.mobile.ui.tasks

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.*
import com.ossadkowski.crm.mobile.data.repository.TasksRepository
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
class TaskDetailViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    lateinit var repository: TasksRepository

    private lateinit var viewModel: TaskDetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TaskDetailViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadDetail updates detail LiveData`() = runTest {
        val detail = TaskDetailDto(1, null, "typ", "Title", "desc", "Firma", "2026-01-01", "user", 1, "open", false, "2026-01-01", "admin", 1, null, null, null, null)
        whenever(repository.getDetail(1)).thenReturn(NetworkResult.Success(detail))

        viewModel.loadDetail(1)
        advanceUntilIdle()

        assertTrue(viewModel.detail.value is NetworkResult.Success)
        assertEquals("Title", (viewModel.detail.value as NetworkResult.Success).data?.tytul)
    }

    @Test
    fun `loadDetail sets loading state`() = runTest {
        val detail = TaskDetailDto(1, null, "t", "T", "d", "F", "d", "u", 1, "o", false, "c", "a", 1, null, null, null, null)
        whenever(repository.getDetail(1)).thenReturn(NetworkResult.Success(detail))

        viewModel.loadDetail(1)
        assertTrue(viewModel.detail.value is NetworkResult.Loading)

        advanceUntilIdle()
    }

    @Test
    fun `loadComments updates comments LiveData`() = runTest {
        val comments = listOf(TaskCommentDto(1, 1, "user", "Hello", "2026-01-01"))
        whenever(repository.getComments(1)).thenReturn(NetworkResult.Success(comments))

        viewModel.loadComments(1)
        advanceUntilIdle()

        assertTrue(viewModel.comments.value is NetworkResult.Success)
        assertEquals(1, (viewModel.comments.value as NetworkResult.Success).data?.size)
    }

    @Test
    fun `loadFiles updates files LiveData`() = runTest {
        val files = listOf(TaskFileDto(1, "doc.pdf", "2026-01-01", "user"))
        whenever(repository.getFiles(1)).thenReturn(NetworkResult.Success(files))

        viewModel.loadFiles(1)
        advanceUntilIdle()

        assertTrue(viewModel.files.value is NetworkResult.Success)
    }

    @Test
    fun `loadHistoria updates historia LiveData`() = runTest {
        val historia = listOf(TaskHistoriaDto(1, "zmiana", "user", "old → new", "2026-01-01"))
        whenever(repository.getHistoria(1)).thenReturn(NetworkResult.Success(historia))

        viewModel.loadHistoria(1)
        advanceUntilIdle()

        assertTrue(viewModel.historia.value is NetworkResult.Success)
    }

    @Test
    fun `loadObservers updates observers LiveData`() = runTest {
        val observers = listOf(TaskObserverDto(1, 1, "user", "IT", "admin", "2026-01-01"))
        whenever(repository.getObservers(1)).thenReturn(NetworkResult.Success(observers))

        viewModel.loadObservers(1)
        advanceUntilIdle()

        assertTrue(viewModel.observers.value is NetworkResult.Success)
    }

    @Test
    fun `changeStatus updates statusResult`() = runTest {
        whenever(repository.changeStatus(1, "completed")).thenReturn(NetworkResult.Success(Unit))

        viewModel.changeStatus(1, "completed")
        advanceUntilIdle()

        assertTrue(viewModel.statusResult.value is NetworkResult.Success)
        verify(repository).changeStatus(1, "completed")
    }

    @Test
    fun `changeStatus error`() = runTest {
        whenever(repository.changeStatus(1, "invalid")).thenReturn(NetworkResult.Error("Invalid status"))

        viewModel.changeStatus(1, "invalid")
        advanceUntilIdle()

        assertTrue(viewModel.statusResult.value is NetworkResult.Error)
    }

    @Test
    fun `addComment success`() = runTest {
        whenever(repository.addComment(1, "My comment")).thenReturn(NetworkResult.Success(Unit))

        viewModel.addComment(1, "My comment")
        advanceUntilIdle()

        assertTrue(viewModel.commentResult.value is NetworkResult.Success)
        verify(repository).addComment(1, "My comment")
    }

    @Test
    fun `addComment error`() = runTest {
        whenever(repository.addComment(1, "")).thenReturn(NetworkResult.Error("Empty comment"))

        viewModel.addComment(1, "")
        advanceUntilIdle()

        assertTrue(viewModel.commentResult.value is NetworkResult.Error)
    }
}
