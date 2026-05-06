package com.ossadkowski.crm.mobile.serwis.ui

import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.serwis.model.TimeSummary
import com.ossadkowski.crm.mobile.domain.serwis.usecase.GetMyTimeSummaryUseCase
import com.ossadkowski.crm.mobile.ui.serwis.screens.mytime.MyTimeUiState
import com.ossadkowski.crm.mobile.ui.serwis.screens.mytime.MyTimeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class MyTimeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun emptySummary() = TimeSummary(
        technicianId = "tech",
        totalHours = 0.0,
        totalTravelHours = 0.0,
        totalKilometers = 0.0,
        entries = emptyList(),
    )

    @Test
    fun `Loading transitions to Success on Result_Success`() = runTest {
        val summary = emptySummary().copy(totalHours = 32.5, totalKilometers = 220.0)
        val useCase: GetMyTimeSummaryUseCase = mock {
            onBlocking { invoke(any(), any()) }.thenReturn(Result.Success(summary))
        }
        val vm = MyTimeViewModel(useCase)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is MyTimeUiState.Success)
        assertEquals(summary, (state as MyTimeUiState.Success).summary)
    }

    @Test
    fun `Loading transitions to Error on Result_Error`() = runTest {
        val useCase: GetMyTimeSummaryUseCase = mock {
            onBlocking { invoke(any(), any()) }.thenReturn(Result.Error("Boom"))
        }
        val vm = MyTimeViewModel(useCase)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is MyTimeUiState.Error)
        assertEquals("Boom", (state as MyTimeUiState.Error).message)
    }

    @Test
    fun `nextWeek shifts dateFrom and dateTo by 7 days`() = runTest {
        val useCase: GetMyTimeSummaryUseCase = mock {
            onBlocking { invoke(any(), any()) }.thenReturn(Result.Success(emptySummary()))
        }
        val vm = MyTimeViewModel(useCase)
        advanceUntilIdle()

        // Capture every (from, to) pair the use case is invoked with.
        val fromCap = argumentCaptor<LocalDate>()
        val toCap = argumentCaptor<LocalDate>()

        // First invocation already happened in init; capture it together with
        // the post-nextWeek invocation.
        vm.nextWeek()
        advanceUntilIdle()

        verify(useCase, atLeastOnce()).invoke(fromCap.capture(), toCap.capture())

        val fromValues = fromCap.allValues
        val toValues = toCap.allValues
        assertTrue("expected at least 2 invocations, got ${fromValues.size}", fromValues.size >= 2)

        val firstFrom = fromValues[0]
        val firstTo = toValues[0]
        val secondFrom = fromValues[1]
        val secondTo = toValues[1]

        assertEquals(firstFrom.plusDays(7), secondFrom)
        assertEquals(firstTo.plusDays(7), secondTo)

        // Sanity: each window is Mon..Sun (6 days inclusive).
        assertEquals(6L, java.time.temporal.ChronoUnit.DAYS.between(firstFrom, firstTo))
        assertEquals(6L, java.time.temporal.ChronoUnit.DAYS.between(secondFrom, secondTo))
    }

    @Test
    fun `prevWeek shifts dateFrom by minus 7 days`() = runTest {
        val useCase: GetMyTimeSummaryUseCase = mock {
            onBlocking { invoke(any(), any()) }.thenReturn(Result.Success(emptySummary()))
        }
        val vm = MyTimeViewModel(useCase)
        advanceUntilIdle()

        val fromCap = argumentCaptor<LocalDate>()
        val toCap = argumentCaptor<LocalDate>()

        vm.prevWeek()
        advanceUntilIdle()

        verify(useCase, atLeastOnce()).invoke(fromCap.capture(), toCap.capture())

        val firstFrom = fromCap.allValues[0]
        val secondFrom = fromCap.allValues[1]
        assertEquals(firstFrom.minusDays(7), secondFrom)
    }
}
