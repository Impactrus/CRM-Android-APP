package com.ossadkowski.crm.mobile.serwis.ui

import androidx.lifecycle.SavedStateHandle
import com.ossadkowski.crm.mobile.domain.serwis.usecase.AddActivityUseCase
import com.ossadkowski.crm.mobile.domain.serwis.usecase.AddTimeEntryUseCase
import com.ossadkowski.crm.mobile.ui.serwis.components.LiveTimerState
import com.ossadkowski.crm.mobile.ui.serwis.nav.SerwisRoutes
import com.ossadkowski.crm.mobile.ui.serwis.screens.workcard.ActivityRow
import com.ossadkowski.crm.mobile.ui.serwis.screens.workcard.WorkCardUiState
import com.ossadkowski.crm.mobile.ui.serwis.screens.workcard.WorkCardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class WorkCardViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun newVm(): WorkCardViewModel {
        val handle = SavedStateHandle(
            mapOf(
                SerwisRoutes.ARG_ORDER_NUM to "MPE-000123",
                SerwisRoutes.ARG_CARD_NUM to "1",
            ),
        )
        return WorkCardViewModel(
            savedStateHandle = handle,
            addTimeEntry = mock<AddTimeEntryUseCase>(),
            addActivity = mock<AddActivityUseCase>(),
        )
    }

    @Test
    fun `travel timer transitions IDLE to RUNNING to PAUSED to RUNNING to DONE`() = runTest {
        val vm = newVm()
        val initial = (vm.uiState.value as WorkCardUiState.Editing).form
        assertEquals(LiveTimerState.IDLE, initial.travelTimerState)

        vm.startTravelTimer()
        assertEquals(
            LiveTimerState.RUNNING,
            (vm.uiState.value as WorkCardUiState.Editing).form.travelTimerState,
        )

        vm.pauseTravelTimer()
        assertEquals(
            LiveTimerState.PAUSED,
            (vm.uiState.value as WorkCardUiState.Editing).form.travelTimerState,
        )

        vm.pauseTravelTimer() // toggle back to RUNNING
        assertEquals(
            LiveTimerState.RUNNING,
            (vm.uiState.value as WorkCardUiState.Editing).form.travelTimerState,
        )

        vm.stopTravelTimer()
        assertEquals(
            LiveTimerState.DONE,
            (vm.uiState.value as WorkCardUiState.Editing).form.travelTimerState,
        )
    }

    @Test
    fun `starting work timer auto-pauses running travel timer`() = runTest {
        val vm = newVm()
        vm.startTravelTimer()
        vm.startWorkTimer()

        val form = (vm.uiState.value as WorkCardUiState.Editing).form
        assertEquals(LiveTimerState.RUNNING, form.workTimerState)
        assertEquals(LiveTimerState.PAUSED, form.travelTimerState)
    }

    @Test
    fun `activity with end before start sets validation error`() = runTest {
        val vm = newVm()
        vm.addActivity(
            ActivityRow(
                title = "Bad",
                start = LocalTime.of(10, 0),
                end = LocalTime.of(9, 0),
            ),
        )
        val form = (vm.uiState.value as WorkCardUiState.Editing).form
        assertNotNull(form.activityError)
        assertTrue(form.activities.isEmpty())
        assertTrue(!vm.canSave())
    }
}
