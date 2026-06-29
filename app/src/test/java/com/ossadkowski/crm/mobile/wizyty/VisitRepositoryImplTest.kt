package com.ossadkowski.crm.mobile.wizyty

import com.ossadkowski.crm.mobile.data.api.ApiService
import com.ossadkowski.crm.mobile.data.model.GeocodeSuggestionDto
import com.ossadkowski.crm.mobile.data.wizyty.db.ContractorCoordDao
import com.ossadkowski.crm.mobile.data.wizyty.db.ContractorCoordEntity
import com.ossadkowski.crm.mobile.data.wizyty.db.VisitEventDao
import com.ossadkowski.crm.mobile.data.wizyty.db.VisitEventEntity
import com.ossadkowski.crm.mobile.data.wizyty.repository.VisitRepositoryImpl
import com.ossadkowski.crm.mobile.domain.common.Result
import com.ossadkowski.crm.mobile.domain.wizyty.model.NewVisitEvent
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitEventType
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitSource
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitStatus
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitSyncStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class VisitRepositoryImplTest {

    @Mock lateinit var visitDao: VisitEventDao
    @Mock lateinit var coordDao: ContractorCoordDao
    @Mock lateinit var api: ApiService

    private lateinit var repo: VisitRepositoryImpl

    @Before
    fun setUp() {
        repo = VisitRepositoryImpl(visitDao, coordDao, api)
    }

    @Test
    fun `addManualVisit stores MANUAL CONFIRMED PENDING_SYNC and upserts coord when lat lng present`() = runTest {
        whenever(visitDao.insert(any())).thenReturn(10L)

        val result = repo.addManualVisit(
            NewVisitEvent(contractorName = "Foo", addressLabel = "Dębowa 21", lat = 51.0, lng = 16.9),
        )

        assertTrue(result is Result.Success)
        val visit = (result as Result.Success).data
        assertEquals(10L, visit.id)
        assertEquals(VisitSource.MANUAL, visit.source)
        assertEquals(VisitStatus.CONFIRMED, visit.status)
        assertEquals(VisitSyncStatus.PENDING_SYNC, visit.syncStatus)
        verify(coordDao).upsert(check<ContractorCoordEntity> {
            assertEquals("Foo", it.key)
            assertEquals("Foo", it.name)
            assertEquals(51.0, it.lat, 0.00001)
            assertEquals(16.9, it.lng, 0.00001)
            assertEquals("Dębowa 21", it.label)
        })
    }

    @Test
    fun `addManualVisit without coordinates does not upsert a contractor coord`() = runTest {
        whenever(visitDao.insert(any())).thenReturn(5L)

        val result = repo.addManualVisit(NewVisitEvent(contractorName = "Bar"))

        assertTrue(result is Result.Success)
        verify(coordDao, never()).upsert(any())
    }

    @Test
    fun `addManualVisit returns error when insert is ignored on conflict`() = runTest {
        whenever(visitDao.insert(any())).thenReturn(-1L)

        val result = repo.addManualVisit(NewVisitEvent(contractorName = "Dup", lat = 1.0, lng = 2.0))

        assertTrue(result is Result.Error)
        verify(coordDao, never()).upsert(any())
    }

    @Test
    fun `recordDetectedEvent stores AUTO_GPS DETECTED`() = runTest {
        whenever(visitDao.insert(any())).thenReturn(3L)

        val result = repo.recordDetectedEvent(
            NewVisitEvent(contractorName = "X", eventType = VisitEventType.DWELL),
        )

        assertTrue(result is Result.Success)
        val visit = (result as Result.Success).data
        assertEquals(VisitSource.AUTO_GPS, visit.source)
        assertEquals(VisitStatus.DETECTED, visit.status)
        assertEquals(VisitEventType.DWELL, visit.eventType)
    }

    @Test
    fun `confirm sets CONFIRMED and PENDING_SYNC`() = runTest {
        whenever(visitDao.updateStatus(eq(1L), eq("CONFIRMED"), eq("PENDING_SYNC"), any())).thenReturn(1)

        val result = repo.confirm(1L)

        assertTrue(result is Result.Success)
        verify(visitDao).updateStatus(eq(1L), eq("CONFIRMED"), eq("PENDING_SYNC"), any())
    }

    @Test
    fun `confirm returns error when no row matched`() = runTest {
        whenever(visitDao.updateStatus(any(), any(), any(), any())).thenReturn(0)

        assertTrue(repo.confirm(99L) is Result.Error)
    }

    @Test
    fun `reject sets REJECTED and keeps it local`() = runTest {
        whenever(visitDao.updateStatus(eq(2L), eq("REJECTED"), eq("LOCAL_ONLY"), any())).thenReturn(1)

        val result = repo.reject(2L)

        assertTrue(result is Result.Success)
        verify(visitDao).updateStatus(eq(2L), eq("REJECTED"), eq("LOCAL_ONLY"), any())
    }

    @Test
    fun `searchAddress maps geocode DTOs to domain suggestions`() = runTest {
        whenever(api.searchGeocode(eq("Wrocław"), any()))
            .thenReturn(listOf(GeocodeSuggestionDto(label = "Rynek, Wrocław", lat = 51.11, lng = 17.03)))

        val result = repo.searchAddress("Wrocław")

        assertTrue(result is Result.Success)
        val suggestions = (result as Result.Success).data
        assertEquals(1, suggestions.size)
        assertEquals("Rynek, Wrocław", suggestions[0].label)
        assertEquals(51.11, suggestions[0].lat, 0.00001)
        assertEquals(17.03, suggestions[0].lng, 0.00001)
    }

    @Test
    fun `searchAddress returns error when the call throws`() = runTest {
        whenever(api.searchGeocode(any(), any())).thenThrow(RuntimeException("boom"))

        assertTrue(repo.searchAddress("anything") is Result.Error)
    }

    @Test
    fun `saveContractorLocation upserts keyed by name`() = runTest {
        val result = repo.saveContractorLocation("Biuro", 51.0, 16.9, "addr")

        assertTrue(result is Result.Success)
        verify(coordDao).upsert(check<ContractorCoordEntity> {
            assertEquals("Biuro", it.key)
            assertEquals(51.0, it.lat, 0.00001)
            assertEquals("addr", it.label)
        })
    }

    @Test
    fun `deleteContractorLocation returns success when a row is removed and error otherwise`() = runTest {
        whenever(coordDao.delete("present")).thenReturn(1)
        whenever(coordDao.delete("absent")).thenReturn(0)

        assertTrue(repo.deleteContractorLocation("present") is Result.Success)
        assertTrue(repo.deleteContractorLocation("absent") is Result.Error)
    }

    @Test
    fun `observeVisits maps entities to domain`() = runTest {
        whenever(visitDao.observeVisible()).thenReturn(flowOf(listOf(visitEntity(id = 7L))))

        val visits = repo.observeVisits().first()

        assertEquals(1, visits.size)
        assertEquals(7L, visits[0].id)
    }

    @Test
    fun `observeContractorLocations marks rows as non-live`() = runTest {
        whenever(coordDao.observeAll()).thenReturn(
            flowOf(listOf(ContractorCoordEntity("k", "Name", 51.0, 16.9, "addr", Instant.EPOCH))),
        )

        val locations = repo.observeContractorLocations().first()

        assertEquals(1, locations.size)
        assertEquals("Name", locations[0].name)
        assertEquals(false, locations[0].isLive)
    }

    @Test
    fun `pendingForSync maps the dao rows`() = runTest {
        whenever(visitDao.pendingForSync()).thenReturn(listOf(visitEntity(id = 1L), visitEntity(id = 2L)))

        assertEquals(2, repo.pendingForSync().size)
    }

    @Test
    fun `markSynced with empty list short-circuits without touching the dao`() = runTest {
        val result = repo.markSynced(emptyList())

        assertTrue(result is Result.Success)
        verify(visitDao, never()).markSync(any(), any(), any())
    }

    @Test
    fun `markSynced flags rows as SYNCED`() = runTest {
        val result = repo.markSynced(listOf(1L, 2L))

        assertTrue(result is Result.Success)
        verify(visitDao).markSync(eq(listOf(1L, 2L)), eq("SYNCED"), any())
    }

    private fun visitEntity(id: Long) = VisitEventEntity(
        id = id,
        contractorAccountNum = null,
        contractorName = "C$id",
        addressLabel = null,
        lat = null,
        lng = null,
        eventType = VisitEventType.MANUAL.name,
        source = VisitSource.MANUAL.name,
        status = VisitStatus.CONFIRMED.name,
        syncStatus = VisitSyncStatus.PENDING_SYNC.name,
        occurredAt = Instant.EPOCH,
        dwellSeconds = null,
        idempotencyKey = "k$id",
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
    )
}
