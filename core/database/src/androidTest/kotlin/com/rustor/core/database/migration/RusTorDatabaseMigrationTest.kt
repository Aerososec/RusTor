package com.rustor.core.database.migration

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.rustor.core.database.RusTorDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RusTorDatabaseMigrationTest {

    @get:Rule
    val migrationHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        RusTorDatabase::class.java,
    )

    @Test
    fun migrateFrom1To2KeepsCatalogAndAddsWritablePopularTable() {
        migrationHelper.createDatabase(
            name = TEST_DATABASE_NAME,
            version = 1
        ).use { database ->
            database.execSQL("INSERT INTO categories (id, title) VALUES ('tools', 'Tools')")
            database.execSQL(
                """
                INSERT INTO apps (
                    id, name, developer, category_id, age_rating,
                    summary, description, screenshots, feed_position
                ) VALUES (
                    'legacy.app', 'Legacy App', 'Example Dev', 'tools', 0,
                    'Short summary', 'Full description', '[]', 1
                )
                """.trimIndent(),
            )
        }

        migrationHelper.runMigrationsAndValidate(
            name = TEST_DATABASE_NAME,
            version = 2,
            validateDroppedTables = true,
        ).use { database ->
            database.query(
                """
                SELECT apps.name, categories.title, apps.feed_position
                FROM apps
                INNER JOIN categories ON categories.id = apps.category_id
                WHERE apps.id = 'legacy.app' AND categories.id = 'tools'
                """.trimIndent(),
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("Legacy App", cursor.getString(0))
                assertEquals("Tools", cursor.getString(1))
                assertEquals(1, cursor.getInt(2))
            }

            database.query("SELECT COUNT(*) FROM popular_apps").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(0, cursor.getInt(0))
            }

            database.execSQL(
                "INSERT INTO popular_apps (app_id, position) VALUES ('legacy.app', 0)",
            )

            database.query("SELECT app_id, position FROM popular_apps").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("legacy.app", cursor.getString(0))
                assertEquals(0, cursor.getInt(1))
            }
        }
    }

    private companion object {
        const val TEST_DATABASE_NAME = "rustor-migration-1-2.db"
    }
}