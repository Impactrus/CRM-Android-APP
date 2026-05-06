package com.ossadkowski.crm.mobile.serwis.parts

import com.ossadkowski.crm.mobile.data.serwis.parts.db.PartRequestEntity
import com.ossadkowski.crm.mobile.data.serwis.parts.mapper.toDomain
import com.ossadkowski.crm.mobile.data.serwis.parts.mapper.toEntity
import com.ossadkowski.crm.mobile.domain.serwis.parts.model.NewPartRequest
import com.ossadkowski.crm.mobile.domain.serwis.parts.model.PartStatus
import com.ossadkowski.crm.mobile.domain.serwis.parts.model.PartSyncStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class PartMappersTest {

    @Test
    fun `entity to domain round-trips fields`() {
        val now = Instant.parse("2026-05-06T10:00:00Z")
        val later = Instant.parse("2026-05-06T11:00:00Z")
        val entity = PartRequestEntity(
            id = 7L,
            orderRegNum = "MPE-000123",
            jobCardNum = "MPE-000123/1",
            name = "Filtr oleju",
            partNumber = "OF-9876",
            quantity = 2.5,
            unit = "szt",
            status = PartStatus.ORDERED.name,
            notes = "Pilne",
            createdAt = now,
            updatedAt = later,
            syncStatus = PartSyncStatus.PENDING_SYNC.name,
        )

        val domain = entity.toDomain()

        assertEquals(7L, domain.id)
        assertEquals("MPE-000123", domain.orderRegNum)
        assertEquals("MPE-000123/1", domain.jobCardNum)
        assertEquals("Filtr oleju", domain.name)
        assertEquals("OF-9876", domain.partNumber)
        assertEquals(2.5, domain.quantity, 0.0001)
        assertEquals("szt", domain.unit)
        assertEquals(PartStatus.ORDERED, domain.status)
        assertEquals("Pilne", domain.notes)
        assertEquals(now, domain.createdAt)
        assertEquals(later, domain.updatedAt)
        assertEquals(PartSyncStatus.PENDING_SYNC, domain.syncStatus)
    }

    @Test
    fun `unknown PartStatus string falls back to REQUESTED`() {
        val entity = baseEntity().copy(status = "BANANAS")
        assertEquals(PartStatus.REQUESTED, entity.toDomain().status)
    }

    @Test
    fun `unknown PartSyncStatus string falls back to LOCAL_ONLY`() {
        val entity = baseEntity().copy(syncStatus = "QUANTUM")
        assertEquals(PartSyncStatus.LOCAL_ONLY, entity.toDomain().syncStatus)
    }

    @Test
    fun `NewPartRequest toEntity uses now and defaults`() {
        val now = Instant.parse("2026-05-06T12:00:00Z")
        val req = NewPartRequest(name = "Olej", quantity = 5.0, unit = "l")

        val entity = req.toEntity(now)

        assertEquals(0L, entity.id)
        assertEquals("Olej", entity.name)
        assertEquals(5.0, entity.quantity, 0.0001)
        assertEquals("l", entity.unit)
        assertEquals(PartStatus.REQUESTED.name, entity.status)
        assertEquals(PartSyncStatus.LOCAL_ONLY.name, entity.syncStatus)
        assertEquals(now, entity.createdAt)
        assertEquals(now, entity.updatedAt)
    }

    private fun baseEntity() = PartRequestEntity(
        id = 1L,
        orderRegNum = null,
        jobCardNum = null,
        name = "x",
        partNumber = null,
        quantity = 1.0,
        unit = "szt",
        status = PartStatus.REQUESTED.name,
        notes = null,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
        syncStatus = PartSyncStatus.LOCAL_ONLY.name,
    )
}
