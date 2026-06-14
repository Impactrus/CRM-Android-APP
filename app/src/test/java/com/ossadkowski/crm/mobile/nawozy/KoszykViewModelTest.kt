package com.ossadkowski.crm.mobile.nawozy

import androidx.lifecycle.SavedStateHandle
import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.nawozy.model.Koszyk
import com.ossadkowski.crm.mobile.domain.nawozy.model.KoszykPozycja
import com.ossadkowski.crm.mobile.domain.nawozy.model.LimitStatus
import com.ossadkowski.crm.mobile.domain.nawozy.model.ZamowienieStatus
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.AbandonKoszykUseCase
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.DeletePozycjaUseCase
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.GetKoszykUseCase
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.GetLimitStatusUseCase
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.GetSlownikUseCase
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.SubmitKoszykUseCase
import com.ossadkowski.crm.mobile.domain.nawozy.usecase.UpdateHeaderUseCase
import com.ossadkowski.crm.mobile.ui.nawozy.nav.NawozyRoutes
import com.ossadkowski.crm.mobile.ui.nawozy.screens.koszyk.KoszykViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class KoszykViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val getKoszyk: GetKoszykUseCase = mock()
    private val updateHeader: UpdateHeaderUseCase = mock()
    private val submitKoszyk: SubmitKoszykUseCase = mock()
    private val deletePozycja: DeletePozycjaUseCase = mock()
    private val getSlownik: GetSlownikUseCase = mock()
    private val getLimitStatus: GetLimitStatusUseCase = mock()

    @Suppress("unused")
    private val abandon: AbandonKoszykUseCase = mock()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun koszyk(pozycje: List<KoszykPozycja>) = Koszyk(
        id = 1, kontrahentId = "ACC-1", kontrahentNazwa = "Klient", status = ZamowienieStatus.KOSZYK,
        qtyTons = 24.0, dlvMode = null, dlvTerm = null, paymentTerm = "PT14", dataDostawy = null,
        adresDostawy = null, addressBookId = null, customerRef = null, notes = null,
        pozycje = pozycje, wartoscNetto = if (pozycje.isEmpty()) 0.0 else 2280.0,
    )

    private fun line() = KoszykPozycja(
        lineId = 10, itemId = "N-1", nazwa = "Saletra", qty = 24.0, magazynId = "W1",
        cenaBazowa = 100.0, cenaSprzedazy = 95.0, rabatProcent = 5.0, transportPlnT = 12.0, wartoscNetto = 2280.0,
    )

    private fun build() = KoszykViewModel(
        SavedStateHandle(mapOf(NawozyRoutes.ARG_KOSZYK_ID to 1L)),
        getKoszyk, updateHeader, submitKoszyk, deletePozycja, getSlownik, getLimitStatus,
    )

    @Test
    fun `submit with empty cart blocks and shows message`() = runTest {
        whenever_getKoszyk(koszyk(emptyList()))
        val vm = build()
        advanceUntilIdle()

        vm.submit()
        advanceUntilIdle()

        assertNotNull(vm.state.value.message)
        assertNull(vm.state.value.submittedStatus)
        verify(submitKoszyk, never()).invoke(any(), any())
    }

    @Test
    fun `submit blocked when limit restricted and risk not acknowledged`() = runTest {
        whenever_getKoszyk(koszyk(listOf(line())))
        org.mockito.kotlin.whenever(getLimitStatus.invoke(org.mockito.kotlin.eq("ACC-1")))
            .thenReturn(Result.Success(LimitStatus(1000.0, 0.0, isFrozen = true, isBlocked = false, frozenReason = null)))
        val vm = build()
        advanceUntilIdle()

        vm.submit()
        advanceUntilIdle()

        assertNotNull(vm.state.value.message)
        assertNull(vm.state.value.submittedStatus)
        verify(submitKoszyk, never()).invoke(any(), any())
    }

    @Test
    fun `submit succeeds when cart valid and not restricted`() = runTest {
        whenever_getKoszyk(koszyk(listOf(line())))
        org.mockito.kotlin.whenever(getLimitStatus.invoke(any()))
            .thenReturn(Result.Success(LimitStatus(1000.0, 500.0, isFrozen = false, isBlocked = false, frozenReason = null)))
        org.mockito.kotlin.whenever(submitKoszyk.invoke(any(), any()))
            .thenReturn(Result.Success(koszyk(listOf(line())).copy(status = ZamowienieStatus.DRAFT)))
        val vm = build()
        advanceUntilIdle()

        vm.submit()
        advanceUntilIdle()

        assertEquals(ZamowienieStatus.DRAFT, vm.state.value.submittedStatus)
        verify(submitKoszyk).invoke(any(), any())
    }

    private suspend fun whenever_getKoszyk(value: Koszyk) {
        org.mockito.kotlin.whenever(getKoszyk.invoke(any())).thenReturn(Result.Success(value))
    }
}
