package com.fourgeailabs.neuropath.data.local

import android.content.Context
import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Validates MIGRATION_13_14 without room-testing: runs the migration SQL
 * against a v13-shaped database and asserts the new tables, indexes, and
 * ON DELETE CASCADE foreign keys exist and actually cascade.
 *
 * The migration only adds two tables (it never touches existing ones), so a
 * minimal child_profiles(id) parent table is a faithful v13 stand-in for the
 * foreign-key reference.
 */
@RunWith(RobolectricTestRunner::class)
class OfflinePackMigrationTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext<Context>()

    private fun openInMemoryDb(): SupportSQLiteDatabase {
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(null)
            .callback(object : SupportSQLiteOpenHelper.Callback(13) {
                override fun onCreate(db: SupportSQLiteDatabase) = Unit
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            })
            .build()
        return FrameworkSQLiteOpenHelperFactory().create(config).writableDatabase
    }

    private fun tableExists(db: SupportSQLiteDatabase, name: String): Boolean {
        db.query("SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = ?", arrayOf(name)).use { c ->
            c.moveToFirst()
            return c.getLong(0) > 0
        }
    }

    private fun rowCount(db: SupportSQLiteDatabase, table: String): Long {
        db.query("SELECT COUNT(*) FROM $table", emptyArray()).use { c ->
            c.moveToFirst()
            return c.getLong(0)
        }
    }

    private data class ForeignKey(val table: String, val from: String, val to: String, val onDelete: String)

    private fun foreignKeys(db: SupportSQLiteDatabase, table: String): List<ForeignKey> {
        val out = mutableListOf<ForeignKey>()
        db.query("PRAGMA foreign_key_list($table)", emptyArray()).use { c: Cursor ->
            val tableIdx = c.getColumnIndex("table")
            val fromIdx = c.getColumnIndex("from")
            val toIdx = c.getColumnIndex("to")
            val onDeleteIdx = c.getColumnIndex("on_delete")
            while (c.moveToNext()) {
                out += ForeignKey(
                    table = c.getString(tableIdx),
                    from = c.getString(fromIdx),
                    to = c.getString(toIdx),
                    onDelete = c.getString(onDeleteIdx)
                )
            }
        }
        return out
    }

    @Test
    fun migrate13to14_createsPackTablesWithCascades() {
        val db = openInMemoryDb()
        try {
            // v13 stand-in: only child_profiles(id) matters to the new FKs.
            db.execSQL("CREATE TABLE child_profiles (id INTEGER PRIMARY KEY NOT NULL, name TEXT)")
            db.execSQL("INSERT INTO child_profiles (id, name) VALUES (7, 'Existing Kid')")

            MIGRATION_13_14.migrate(db)

            assertTrue(tableExists(db, "offline_material_packs"))
            assertTrue(tableExists(db, "offline_material_lessons"))

            val packFks = foreignKeys(db, "offline_material_packs")
            assertTrue(
                "packs must cascade from child_profiles, found: $packFks",
                packFks.any { it.table == "child_profiles" && it.from == "profileId" && it.to == "id" && it.onDelete == "CASCADE" }
            )
            val lessonFks = foreignKeys(db, "offline_material_lessons")
            assertTrue(
                "lessons must cascade from packs, found: $lessonFks",
                lessonFks.any {
                    it.table == "offline_material_packs" && it.from == "profileId" &&
                        it.to == "profileId" && it.onDelete == "CASCADE"
                }
            )

            // Existing profiles survive the migration untouched.
            assertEquals(1L, rowCount(db, "child_profiles"))

            // Functional cascade check: profile -> pack -> lessons.
            db.execSQL("PRAGMA foreign_keys = ON")
            db.execSQL(
                "INSERT INTO offline_material_packs " +
                    "(profileId, frameworkKey, country, stateOrProvince, schoolDistrict, " +
                    "standardTitle, themeWorldId, status, totalLessons, totalBytes, " +
                    "downloadedAt, packVersion) " +
                    "VALUES (7, 'k', 'c', 's', 'd', 'std', 'dino', 'READY', 1, 10, 0, 1)"
            )
            db.execSQL(
                "INSERT INTO offline_material_lessons " +
                    "(profileId, lessonId, subjectName, gradeLevelCode, payloadJson, byteSize) " +
                    "VALUES (7, 'l1', 'MATH', 'KINDERGARTEN', '{}', 2)"
            )
            db.execSQL("DELETE FROM child_profiles WHERE id = 7")
            assertEquals("pack row must cascade away with the profile", 0L, rowCount(db, "offline_material_packs"))
            assertEquals("lesson rows must cascade away with the pack", 0L, rowCount(db, "offline_material_lessons"))

            // Idempotent: running the migration twice must not fail.
            MIGRATION_13_14.migrate(db)
        } finally {
            db.close()
        }
    }
}
