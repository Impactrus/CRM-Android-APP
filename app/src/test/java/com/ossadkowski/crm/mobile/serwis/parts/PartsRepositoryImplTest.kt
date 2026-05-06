package com.ossadkowski.crm.mobile.serwis.parts

import org.junit.Ignore
import org.junit.Test

/**
 * Room in-memory tests for [com.ossadkowski.crm.mobile.data.serwis.parts.repository.PartsRepositoryImpl].
 *
 * Disabled: Room requires the Android runtime, which on the JVM `test` source set
 * needs Robolectric. Robolectric is not yet configured in this project (no
 * `robolectric` dependency or `@RunWith(RobolectricTestRunner::class)` setup).
 * Adding it would touch test infra outside the scope of this feature.
 *
 * Once Robolectric is wired up, drop the `@Ignore` annotations and replace each
 * stub with the corresponding `runTest { ... }` body using
 * `Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), CrmDatabase::class.java)
 *     .allowMainThreadQueries().build()`.
 *
 * Mapper coverage and the fallback-on-unknown-enum semantics already live in
 * [PartMappersTest], which doesn't need Robolectric.
 */
class PartsRepositoryImplTest {

    @Ignore("Robolectric not configured — see class kdoc.")
    @Test
    fun `add then observeAll emits new part with id assigned`() = Unit

    @Ignore("Robolectric not configured — see class kdoc.")
    @Test
    fun `updateStatus transitions status, bumps updatedAt and sets syncStatus PENDING_SYNC`() = Unit

    @Ignore("Robolectric not configured — see class kdoc.")
    @Test
    fun `delete removes the row and subsequent get returns null`() = Unit

    @Ignore("Robolectric not configured — see class kdoc.")
    @Test
    fun `add with empty name still succeeds (validation lives in UI layer)`() = Unit
}
