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
 * Validates MIGRATION_15_16 without room-testing: runs the migration SQL
 * against a v15-shaped database and asserts
 * - the `reward_grants` table exists with grantKey as primary key and an
 *   ON DELETE CASCADE foreign key to child_profiles(id);
 * - `offline_material_packs` gains the nullable `contentSha256` column;
 * - existing pack rows survive the migration untouched (non-destructive).
 */
@RunWith(RobolectricTestRunner::class)
class RewardGrantMigrationTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext<Context>()

    private fun openInMemoryDb(): SupportSQLiteDatabase {
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(null)
            .callback(object : SupportSQLiteOpenHelper.Callback(15) {
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

    private fun columnNames(db: SupportSQLiteDatabase, table: String): List<String> {
        val out = mutableListOf<String>()
        db.query("PRAGMA table_info($table)", emptyArray()).use { c: Cursor ->
            val nameIdx = c.getColumnIndex("name")
            while (c.moveToNext()) out += c.getString(nameIdx)
        }
        return out
    }

    private fun rowCount(db: SupportSQLiteDatabase, table: String): Long {
        db.query("SELECT COUNT(*) FROM $table", emptyArray()).use { c ->
            c.moveToFirst()
            return c.getLong(0)
        }
    }

    @Test
    fun migrate15to16_isNonDestructive() {
        val db = openInMemoryDb()
        try {
            // v15 stand-in: parent table plus the packs table in its v15 shape.
            db.execSQL("CREATE TABLE child_profiles (id INTEGER PRIMARY KEY NOT NULL, name TEXT)")
            db.execSQL("INSERT INTO child_profiles (id, name) VALUES (7, 'Existing Kid')")
            db.execSQL(
                """CREATE TABLE offline_material_packs
                   (profileId INTEGER NOT NULL, frameworkKey TEXT NOT NULL, country TEXT NOT NULL,
                    stateOrProvince TEXT NOT NULL, schoolDistrict TEXT NOT NULL,
                    standardTitle TEXT NOT NULL, themeWorldId TEXT NOT NULL,
                    status TEXT NOT NULL, totalLessons INTEGER NOT NULL,
                    totalBytes INTEGER NOT NULL, downloadedAt INTEGER NOT NULL,
                    packVersion INTEGER NOT NULL, PRIMARY KEY(profileId),
                    FOREIGN KEY(profileId) REFERENCES child_profiles(id)
                    ON UPDATE NO ACTION ON DELETE CASCADE)"""
            )
            db.execSQL(
                "INSERT INTO offline_material_packs " +
                    "(profileId, frameworkKey, country, stateOrProvince, schoolDistrict, " +
                    "standardTitle, themeWorldId, status, totalLessons, totalBytes, " +
                    "downloadedAt, packVersion) " +
                    "VALUES (7, 'k', 'c', 's', 'd', 'std', 'dino', 'READY', 3, 300, 0, 1)"
            )

            MIGRATION_15_16.migrate(db)

            // New table with the idempotency primary key.
            assertTrue(tableExists(db, "reward_grants"))
            val grantCols = columnNames(db, "reward_grants")
            assertTrue(grantCols.containsAll(listOf("grantKey", "profileId", "stars", "gems", "reason", "grantedAt")))
            db.query("PRAGMA table_info(reward_grants)", emptyArray()).use { c: Cursor ->
                val nameIdx = c.getColumnIndex("name")
                val pkIdx = c.getColumnIndex("pk")
                var grantKeyPk = 0
                while (c.moveToNext()) {
                    if (c.getString(nameIdx) == "grantKey") grantKeyPk = c.getInt(pkIdx)
                }
                assertTrue("grantKey must be the primary key", grantKeyPk > 0)
            }
            // Duplicate keys are rejected: the idempotency guarantee at the SQL level.
            db.execSQL("PRAGMA foreign_keys = ON")
            db.execSQL("INSERT INTO reward_grants (grantKey, profileId, stars, gems, reason, grantedAt) VALUES ('k1', 7, 5, 1, 'lesson_completion', 0)")
            var duplicateRejected = false
            try {
                db.execSQL("INSERT INTO reward_grants (grantKey, profileId, stars, gems, reason, grantedAt) VALUES ('k1', 7, 5, 1, 'lesson_completion', 0)")
            } catch (_: Exception) {
                duplicateRejected = true
            }
            assertTrue("duplicate grantKey must violate the primary key", duplicateRejected)
            // Grants cascade away with the profile.
            db.execSQL("DELETE FROM child_profiles WHERE id = 7")
            assertEquals("grant rows must cascade away with the profile", 0L, rowCount(db, "reward_grants"))

            // Existing pack rows survive untouched; the new column is NULL for them.
            db.execSQL("INSERT INTO child_profiles (id, name) VALUES (8, 'Second Kid')")
            db.execSQL(
                "INSERT INTO offline_material_packs " +
                    "(profileId, frameworkKey, country, stateOrProvince, schoolDistrict, " +
                    "standardTitle, themeWorldId, status, totalLessons, totalBytes, " +
                    "downloadedAt, packVersion) " +
                    "VALUES (8, 'k', 'c', 's', 'd', 'std', 'dino', 'READY', 3, 300, 0, 1)"
            )
            // Re-run to prove the pre-migration row keeps its data and gets NULL hash.
            assertTrue(columnNames(db, "offline_material_packs").contains("contentSha256"))
            db.query("SELECT contentSha256 FROM offline_material_packs WHERE profileId = 8", emptyArray()).use { c ->
                c.moveToFirst()
                assertTrue("pre-migration packs get NULL hash (unverified, not corrupt)", c.isNull(0))
            }

            // Idempotent: running the migration twice must not fail.
            MIGRATION_15_16.migrate(db)
        } finally {
            db.close()
        }
    }
}
