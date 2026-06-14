package com.ossadkowski.crm.mobile.nawozy

import com.ossadkowski.crm.mobile.data.model.TowarListItem
import com.ossadkowski.crm.mobile.data.model.TowaryPageResponse
import com.ossadkowski.crm.mobile.data.nawozy.api.NawozyApi
import com.ossadkowski.crm.mobile.data.nawozy.dto.StartKoszykResponse
import com.ossadkowski.crm.mobile.data.nawozy.dto.WariantDto
import com.ossadkowski.crm.mobile.data.nawozy.dto.WariantyResponse
import com.ossadkowski.crm.mobile.data.nawozy.repository.NawozyRepositoryImpl
import com.ossadkowski.crm.mobile.domain.common.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class NawozyRepositoryImplTest {

    @Mock lateinit var api: NawozyApi
    private lateinit var repo: NawozyRepositoryImpl

    @Before
    fun setUp() {
        repo = NawozyRepositoryImpl(api)
    }

    private fun towar(kod: String, branza: String, dostepne: Double = 30.0) = TowarListItem(
        kod = kod, nazwa = "Nawóz $kod", branza = branza, producent = null,
        cena = 100.0, jm = "T", dostepne = dostepne, magazyn = null, grupaNazwa = null,
    )

    @Test
    fun `startKoszyk returns koszykId`() = runTest {
        whenever(api.startKoszyk(any())).thenReturn(StartKoszykResponse(koszykId = 555))

        val result = repo.startKoszyk("ACC-1")

        assertTrue(result is Result.Success)
        assertEquals(555L, (result as Result.Success).data)
    }

    @Test
    fun `searchTowaryNawozy merges branza N and D and dedups by itemId`() = runTest {
        whenever(api.searchTowary(eq("N"), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any()))
            .thenReturn(TowaryPageResponse(items = listOf(towar("N-1", "N"), towar("SHARED", "N"))))
        whenever(api.searchTowary(eq("D"), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any()))
            .thenReturn(TowaryPageResponse(items = listOf(towar("D-1", "D"), towar("SHARED", "D"))))

        val result = repo.searchTowaryNawozy(null)

        assertTrue(result is Result.Success)
        val ids = (result as Result.Success).data.map { it.itemId }
        assertEquals(listOf("N-1", "SHARED", "D-1"), ids) // SHARED appears once, N wins
    }

    @Test
    fun `getWarianty sorts ascending by total cost`() = runTest {
        whenever(api.getWarianty(any())).thenReturn(
            WariantyResponse(
                warianty = listOf(
                    WariantDto("W3", "Mag 3", "Cel", 300.0, 30.0, 300.0, null, 5.0),
                    WariantDto("W1", "Mag 1", "Cel", 100.0, 10.0, 100.0, null, 5.0),
                    WariantDto("W2", "Mag 2", "Cel", 200.0, 20.0, 200.0, null, 5.0),
                ),
            ),
        )

        val result = repo.getWarianty(
            com.ossadkowski.crm.mobile.domain.nawozy.repository.WariantyZapytanie(itemId = "N-1", qtyTons = 24.0),
        )

        assertTrue(result is Result.Success)
        assertEquals(listOf(100.0, 200.0, 300.0), (result as Result.Success).data.map { it.kosztTotal })
    }

    @Test
    fun `getOstatnieCeny with empty itemIds short-circuits without calling api`() = runTest {
        val result = repo.getOstatnieCeny("ACC-1", emptyList())

        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data.isEmpty())
        verify(api, never()).getOstatnieCeny(any(), any())
    }

    @Test
    fun `403 maps to permission error message`() = runTest {
        val err = Response.error<Any>(403, "{}".toResponseBody("application/json".toMediaType()))
        whenever(api.startKoszyk(any())).thenAnswer { throw HttpException(err) }

        val result = repo.startKoszyk("ACC-1")

        assertTrue(result is Result.Error)
        assertEquals("Brak uprawnień do zamówień nawozowych.", (result as Result.Error).message)
    }

    @Test
    fun `abandon with unsuccessful response yields error`() = runTest {
        whenever(api.abandonKoszyk(any())).thenReturn(
            Response.error(500, "{}".toResponseBody("application/json".toMediaType())),
        )

        val result = repo.abandonKoszyk(1L)

        assertTrue(result is Result.Error)
    }
}
