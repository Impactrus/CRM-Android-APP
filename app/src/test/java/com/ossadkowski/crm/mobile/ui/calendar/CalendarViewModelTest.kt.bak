package com.ossadkowski.crm.mobile.ui.calendar

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.ZamrozenieDto
import com.ossadkowski.crm.mobile.data.repository.CalendarRepository
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class CalendarViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    lateinit var repository: CalendarRepository

    private lateinit var viewModel: CalendarViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CalendarViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadMonth updates zamrozenia LiveData`() = runTest {
        val year = viewModel.currentYear
        val month = viewModel.currentMonth
        val items = listOf(ZamrozenieDto(1, "IT", "2026-03-01", "2026-03-15", "Freeze"))
        whenever(repository.getZamrozeniaMiesiac(year, month)).thenReturn(NetworkResult.Success(items))

        viewModel.loadMonth()
        advanceUntilIdle()

        assertTrue(viewModel.zamrozenia.value is NetworkResult.Success)
        assertEquals(1, (viewModel.zamrozenia.value as NetworkResult.Success).data?.size)
    }

    @Test
    fun `loadMonth sets loading state`() = runTest {
        val year = viewModel.currentYear
        val month = viewModel.currentMonth
        whenever(repository.getZamrozeniaMiesiac(year, month)).thenReturn(
            NetworkResult.Success(emptyList())
        )

        viewModel.loadMonth()
        assertTrue(viewModel.zamrozenia.value is NetworkResult.Loading)

        advanceUntilIdle()
    }

    @Test
    fun `nextMonth increments month`() = runTest {
        val initialMonth = viewModel.currentMonth
        val initialYear = viewModel.currentYear
        whenever(repository.getZamrozeniaMiesiac(any(), any())).thenReturn(
            NetworkResult.Success(emptyList())
        )

        viewModel.nextMonth()
        advanceUntilIdle()

        if (initialMonth == 12) {
            assertEquals(1, viewModel.currentMonth)
            assertEquals(initialYear + 1, viewModel.currentYear)
        } else {
            assertEquals(initialMonth + 1, viewModel.currentMonth)
            assertEquals(initialYear, viewModel.currentYear)
        }
    }

    @Test
    fun `prevMonth decrements month`() = runTest {
        val initialMonth = viewModel.currentMonth
        val initialYear = viewModel.currentYear
        whenever(repository.getZamrozeniaMiesiac(any(), any())).thenReturn(
            NetworkResult.Success(emptyList())
        )

        viewModel.prevMonth()
        advanceUntilIdle()

        if (initialMonth == 1) {
            assertEquals(12, viewModel.currentMonth)
            assertEquals(initialYear - 1, viewModel.currentYear)
        } else {
            assertEquals(initialMonth - 1, viewModel.currentMonth)
            assertEquals(initialYear, viewModel.currentYear)
        }
    }

    @Test
    fun `nextMonth wraps from December to January`() = runTest {
        viewModel.currentMonth = 12
        val year = viewModel.currentYear
        whenever(repository.getZamrozeniaMiesiac(any(), any())).thenReturn(
            NetworkResult.Success(emptyList())
        )

        viewModel.nextMonth()
        advanceUntilIdle()

        assertEquals(1, viewModel.currentMonth)
        assertEquals(year + 1, viewModel.currentYear)
    }

    @Test
    fun `prevMonth wraps from January to December`() = runTest {
        viewModel.currentMonth = 1
        val year = viewModel.currentYear
        whenever(repository.getZamrozeniaMiesiac(any(), any())).thenReturn(
            NetworkResult.Success(emptyList())
        )

        viewModel.prevMonth()
        advanceUntilIdle()

        assertEquals(12, viewModel.currentMonth)
        assertEquals(year - 1, viewModel.currentYear)
    }

    @Test
    fun `nextMonth calls loadMonth`() = runTest {
        val expectedMonth = if (viewModel.currentMonth == 12) 1 else viewModel.currentMonth + 1
        val expectedYear = if (viewModel.currentMonth == 12) viewModel.currentYear + 1 else viewModel.currentYear
        whenever(repository.getZamrozeniaMiesiac(any(), any())).thenReturn(
            NetworkResult.Success(emptyList())
        )

        viewModel.nextMonth()
        advanceUntilIdle()

        verify(repository).getZamrozeniaMiesiac(expectedYear, expectedMonth)
    }
}
