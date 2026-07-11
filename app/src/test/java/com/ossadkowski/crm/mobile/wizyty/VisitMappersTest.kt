package com.ossadkowski.crm.mobile.wizyty

import com.ossadkowski.crm.mobile.data.wizyty.db.VisitEventEntity
import com.ossadkowski.crm.mobile.data.wizyty.mapper.toDomain
import com.ossadkowski.crm.mobile.data.wizyty.mapper.toEntity
import com.ossadkowski.crm.mobile.domain.wizyty.model.NewVisitEvent
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitEventType
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitSource
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitStatus
import com.ossadkowski.crm.mobile.domain.wizyty.model.VisitSyncStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class VisitMappersTest {

    @Test
    fun `entity to domain round-trips fields and parses enums`() {
        val now = Instant.parse("2026-06-20T08:14:00Z")
        val later = Instant.parse("2026-06-20T09:00:00Z")
        val entity = VisitEventEntity(
            id = 42L,
            contractorAccountNum = "ACC-1",
            contractorName = "Kontrahent A",
            addressLabel = "Dębowa 21, Kobierzyce",
            lat = 51.0374,
            lng = 16.9752,
            eventType = VisitEventType.DWELL.name,
            source = VisitSource.AUTO_GPS.name,
            status = VisitStatus.DETECTED.name,
            syncStatus = VisitSyncStatus.PENDING_SYNC.name,
            occurredAt = now,
            dwellSeconds = 300L,
            idempotencyKey = "key-1",
            createdAt = now,
            updatedAt = later,
        )

        val domain = entity.toDomain()

        assertEquals(42L, domain.id)
        assertEquals("ACC-1", domain.contractorAccountNum)
        assertEquals("Kontrahent A", domain.contractorName)
        assertEquals("Dębowa 21, Kobierzyce", domain.addressLabel)
        assertEquals(51.0374, domain.lat!!, 0.00001)
        assertEquals(16.9752, domain.lng!!, 0.00001)
        assertEquals(VisitEventType.DWELL, domain.eventType)
        assertEquals(VisitSource.AUTO_GPS, domain.source)
        assertEquals(VisitStatus.DETECTED, domain.status)
        assertEquals(VisitSyncStatus.PENDING_SYNC, domain.syncStatus)
        assertEquals(300L, domain.dwellSeconds)
        assertEquals("key-1", domain.idempotencyKey)
        assertEquals(now, domain.occurredAt)
        assertEquals(later, domain.updatedAt)
    }

    @Test
    fun `unknown enum strings fall back to safe defaults`() {
        val entity = baseEntity().copy(
            eventType = "TELEPORT",
            source = "DRONE",
            status = "PURGATORY",
            syncStatus = "QUANTUM",
        )

        val domain = entity.toDomain()

        assertEquals(VisitEventType.MANUAL, domain.eventType)
        assertEquals(VisitSource.MANUAL, domain.source)
        assertEquals(VisitStatus.DETECTED, domain.status)
        assertEquals(VisitSyncStatus.LOCAL_ONLY, domain.syncStatus)
    }

    @Test
    fun `NewVisitEvent toEntity applies given source-status, always PENDING_SYNC, and now`() {
        val now = Instant.parse("2026-06-20T12:00:00Z")
        val new = NewVisitEvent(
            contractorName = "Foo",
            addressLabel = "addr",
            lat = 1.0,
            lng = 2.0,
            eventType = VisitEventType.DWELL,
        )

        val entity = new.toEntity(VisitSource.AUTO_GPS, VisitStatus.DETECTED, now, "uuid-1")

        assertEquals(0L, entity.id)
        assertEquals("Foo", entity.contractorName)
        assertEquals("addr", entity.addressLabel)
        assertEquals(VisitEventType.DWELL.name, entity.eventType)
        assertEquals(VisitSource.AUTO_GPS.name, entity.source)
        assertEquals(VisitStatus.DETECTED.name, entity.status)
        assertEquals(VisitSyncStatus.PENDING_SYNC.name, entity.syncStatus)
        assertEquals("uuid-1", entity.idempotencyKey)
        assertEquals(now, entity.occurredAt)
        assertEquals(now, entity.createdAt)
        assertEquals(now, entity.updatedAt)
    }

    @Test
    fun `toEntity keeps explicit occurredAt when provided`() {
        val now = Instant.parse("2026-06-20T12:00:00Z")
        val occurred = Instant.parse("2026-06-20T08:00:00Z")
        val new = NewVisitEvent(contractorName = "Bar", occurredAt = occurred)

        val entity = new.toEntity(VisitSource.MANUAL, VisitStatus.CONFIRMED, now, "uuid-2")

        assertEquals(occurred, entity.occurredAt)
        assertEquals(now, entity.createdAt)
    }

    private fun baseEntity() = VisitEventEntity(
        id = 1L,
        contractorAccountNum = null,
        contractorName = "x",
        addressLabel = null,
        lat = null,
        lng = null,
        eventType = VisitEventType.MANUAL.name,
        source = VisitSource.MANUAL.name,
        status = VisitStatus.DETECTED.name,
        syncStatus = VisitSyncStatus.LOCAL_ONLY.name,
        occurredAt = Instant.EPOCH,
        dwellSeconds = null,
        idempotencyKey = "k",
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
    )
}
