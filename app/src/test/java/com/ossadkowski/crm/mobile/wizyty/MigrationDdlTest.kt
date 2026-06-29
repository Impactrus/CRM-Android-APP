package com.ossadkowski.crm.mobile.wizyty

import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.JsonParser
import com.ossadkowski.crm.mobile.di.AppModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.io.File

/**
 * Regression guard for the hand-written Room migrations in [AppModule].
 *
 * The on-device verification of the Wizyty feature was a fresh install, which runs Room's
 * `createAllTables` — the migration DDL was never exercised. A production user upgrading from
 * an older `crm-mobile.db` runs [AppModule.MIGRATION_1_2] / [AppModule.MIGRATION_2_3] instead;
 * if a single column type, nullability, PK, or index differs from what Room expects, Room's
 * `TableInfo` validation throws and the app crashes on first launch.
 *
 * This test captures the exact SQL each migration executes and asserts it matches the
 * `createSql` Room generated into the exported schema (`app/schemas/.../3.json`) — the very
 * statement Room's runtime validation derives its expected `TableInfo` from. It runs as a
 * plain JVM unit test (no device, no Robolectric). If it fails, the migration DDL has drifted
 * from the entity definitions and must be brought back in line before shipping.
 */
class MigrationDdlTest {

    @Test
    fun `migration DDL matches Room generated schema for the Wizyty tables`() {
        val executed = captureMigrationSql()
        val schema = loadExportedSchema()

        val validatedTables = mutableListOf<String>()
        for (entityElement in schema.getAsJsonObject("database").getAsJsonArray("entities")) {
            val entity = entityElement.asJsonObject
            val tableName = entity.get("tableName").asString
            // Only the tables our migrations create. `PartRequest*` predates v1 (createAllTables).
            if (tableName != "visit_events" && tableName != "contractor_coords") continue
            validatedTables += tableName

            val createTable = entity.get("createSql").asString.withTableName(tableName).normalizeSql()
            assertTrue(
                "Migration is missing / does not match Room's CREATE TABLE for `$tableName`.\n" +
                    "Expected (from exported schema):\n  $createTable\n" +
                    "Executed by migrations:\n  ${executed.joinToString("\n  ")}",
                executed.contains(createTable),
            )

            if (entity.has("indices")) {
                for (indexElement in entity.getAsJsonArray("indices")) {
                    val createIndex =
                        indexElement.asJsonObject.get("createSql").asString.withTableName(tableName).normalizeSql()
                    assertTrue(
                        "Migration is missing / does not match Room's CREATE INDEX on `$tableName`.\n" +
                            "Expected:\n  $createIndex\n" +
                            "Executed by migrations:\n  ${executed.joinToString("\n  ")}",
                        executed.contains(createIndex),
                    )
                }
            }
        }

        assertEquals(
            "Exported schema should contain both Wizyty tables. Found: $validatedTables",
            setOf("visit_events", "contractor_coords"),
            validatedTables.toSet(),
        )
    }

    /** Run both migrations against a recording mock and return the normalized SQL they execute. */
    private fun captureMigrationSql(): List<String> {
        val db = mock<SupportSQLiteDatabase>()
        AppModule.MIGRATION_1_2.migrate(db)
        AppModule.MIGRATION_2_3.migrate(db)
        val captor = argumentCaptor<String>()
        verify(db, atLeastOnce()).execSQL(captor.capture())
        return captor.allValues.map { it.normalizeSql() }
    }

    private fun loadExportedSchema() =
        JsonParser.parseString(schemaFile().readText()).asJsonObject

    private fun schemaFile(): File {
        val relative = "schemas/com.ossadkowski.crm.mobile.data.db.CrmDatabase/3.json"
        val candidates = listOf(
            File(relative),
            File("app/$relative"),
            File(System.getProperty("user.dir"), relative),
            File(System.getProperty("user.dir"), "app/$relative"),
        )
        return candidates.firstOrNull { it.exists() }
            ?: throw AssertionError(
                "Exported Room schema not found (looked in: ${candidates.joinToString { it.path }}). " +
                    "Ensure CrmDatabase has exportSchema = true and build.gradle sets room.schemaLocation.",
            )
    }

    private fun String.withTableName(tableName: String): String =
        replace("\${TABLE_NAME}", tableName)

    private fun String.normalizeSql(): String =
        trim().replace(Regex("\\s+"), " ")
}
