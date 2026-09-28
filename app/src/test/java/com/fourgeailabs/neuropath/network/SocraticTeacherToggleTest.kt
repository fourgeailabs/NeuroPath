package com.fourgeailabs.neuropath.network

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.fourgeailabs.neuropath.data.local.MIGRATION_14_15
import com.fourgeailabs.neuropath.data.local.entity.ChildProfileEntity
import com.fourgeailabs.neuropath.data.model.AppLanguageDictionary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * The three user-facing AI options ("Cloud AI", "Local AI", "Socratic Teacher")
 * and the parent-controlled Socratic Teacher on/off switch.
 *
 * Contract under test:
 * - Socratic Teacher is a selectable mode with localised labels in every language.
 * - The parent switch persists per child profile and defaults to ON.
 * - When the parent turns Socratic Teacher OFF, a dead AI surfaces an honest
 *   connection-required error (ChatReplySource.ERROR) — it must never silently
 *   teach via the disabled Socratic engine.
 * - Room migration 14 -> 15 adds the flag defaulting to ON for upgraded installs.
 */
@RunWith(RobolectricTestRunner::class)
class SocraticTeacherToggleTest {

    // Exact child_profiles schema at Room version 14 (from 14.json), so the
    // migration test starts from a faithful pre-upgrade database.
    private val v14CreateChildProfiles = """
        CREATE TABLE IF NOT EXISTS `child_profiles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `age` INTEGER NOT NULL, `gradeLevel` TEXT NOT NULL, `ageGroupTier` TEXT NOT NULL, `stateStandard` TEXT NOT NULL, `country` TEXT NOT NULL, `stateOrProvince` TEXT NOT NULL, `city` TEXT NOT NULL, `schoolDistrict` TEXT NOT NULL, `appLanguageCode` TEXT NOT NULL, `activeThemeId` TEXT NOT NULL, `neurodivergentTypesCsv` TEXT NOT NULL, `strugglesCsv` TEXT NOT NULL, `strengthsCsv` TEXT NOT NULL, `hyperFixationsCsv` TEXT NOT NULL, `customAccentColorHex` TEXT, `dyslexiaFontEnabled` INTEGER NOT NULL, `highContrastMode` TEXT NOT NULL, `ttsSpeed` REAL NOT NULL, `ttsVoicePitch` REAL NOT NULL, `autoHighlightWords` INTEGER NOT NULL, `readAnswersAloud` INTEGER NOT NULL, `ambientSound` TEXT NOT NULL, `totalStars` INTEGER NOT NULL, `totalGems` INTEGER NOT NULL, `currentStreakDays` INTEGER NOT NULL, `lastActiveDate` TEXT NOT NULL, `learningBuddyDisabled` INTEGER NOT NULL, `aiVersionMode` TEXT NOT NULL, `localLlamaInstalled` INTEGER NOT NULL, `allowMeteredModelDownload` INTEGER NOT NULL, `currentAvatarId` TEXT NOT NULL, `equippedHatId` TEXT, `equippedPetId` TEXT, `equippedBadgeId` TEXT, `unlockedItemIdsCsv` TEXT NOT NULL, `parentPin` TEXT NOT NULL, `dailyGoalMinutes` INTEGER NOT NULL, `isCoppaConsented` INTEGER NOT NULL, `isInitialSetupComplete` INTEGER NOT NULL, `zipOrPostalCodeOverride` TEXT NOT NULL, `customAiPlatform` TEXT NOT NULL, `customApiKey` TEXT NOT NULL, `themeRotationSchedule` TEXT NOT NULL, `lastThemeRotationTimestamp` INTEGER NOT NULL)
    """.trimIndent()

    private fun openV14Db(): SupportSQLiteDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(null)
            .callback(object : SupportSQLiteOpenHelper.Callback(14) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(v14CreateChildProfiles)
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                    // Not used: the test drives MIGRATION_14_15 directly.
                }
            })
            .build()
        return FrameworkSQLiteOpenHelperFactory().create(config).writableDatabase
    }

    @Test
    fun `migration 14 to 15 adds socraticTeacherEnabled defaulting to ON and keeps existing data`() {
        val db = openV14Db()
        val values = ContentValues().apply {
            put("name", "Test Kid")
            put("age", 8)
            put("gradeLevel", "GRADE_3")
            put("ageGroupTier", "ELEMENTARY")
            put("stateStandard", "Arizona Academic Standards")
            put("country", "United States")
            put("stateOrProvince", "Arizona")
            put("city", "Surprise")
            put("schoolDistrict", "Dysart")
            put("appLanguageCode", "en-US")
            put("activeThemeId", "space")
            put("neurodivergentTypesCsv", "")
            put("strugglesCsv", "")
            put("strengthsCsv", "")
            put("hyperFixationsCsv", "")
            put("dyslexiaFontEnabled", 0)
            put("highContrastMode", "off")
            put("ttsSpeed", 1.0)
            put("ttsVoicePitch", 1.0)
            put("autoHighlightWords", 0)
            put("readAnswersAloud", 0)
            put("ambientSound", "none")
            put("totalStars", 5)
            put("totalGems", 10)
            put("currentStreakDays", 2)
            put("lastActiveDate", "2026-09-27")
            put("learningBuddyDisabled", 0)
            put("aiVersionMode", "FULL_AI")
            put("localLlamaInstalled", 0)
            put("allowMeteredModelDownload", 0)
            put("currentAvatarId", "rex")
            put("unlockedItemIdsCsv", "")
            put("parentPin", "1234")
            put("dailyGoalMinutes", 20)
            put("isCoppaConsented", 1)
            put("isInitialSetupComplete", 1)
            put("zipOrPostalCodeOverride", "")
            put("customAiPlatform", "")
            put("customApiKey", "")
            put("themeRotationSchedule", "off")
            put("lastThemeRotationTimestamp", 0L)
        }
        db.insert("child_profiles", SQLiteDatabase.CONFLICT_REPLACE, values)

        MIGRATION_14_15.migrate(db)

        db.query("SELECT name, aiVersionMode, socraticTeacherEnabled FROM child_profiles").use { cursor ->
            assertTrue("expected the pre-migration profile row to survive", cursor.moveToFirst())
            assertEquals("Test Kid", cursor.getString(0))
            assertEquals("FULL_AI", cursor.getString(1))
            // Upgraded installs keep Socratic Teacher ON (the parent can turn it off).
            assertEquals(1, cursor.getInt(2))
        }
        db.close()
    }

    @Test
    fun `new profiles default socraticTeacherEnabled to true`() {
        assertTrue(ChildProfileEntity().socraticTeacherEnabled)
    }

    @Test
    fun `disabled socratic teacher returns an honest connection error, never a lesson`() {
        val reply = LlamaClient.socraticChatReply(
            lastUserMessage = "What is 7 times 8?",
            languageCode = "en-US",
            allowSocraticFallback = false
        )
        assertEquals(ChatReplySource.ERROR, reply.source)
        assertTrue("error text must be non-blank", reply.text.isNotBlank())
        assertEquals(
            AppLanguageDictionary.getString("ai_connection_required", "en-US"),
            reply.text
        )
    }

    @Test
    fun `enabled socratic teacher still teaches when on`() {
        val reply = LlamaClient.socraticChatReply(
            lastUserMessage = "What is 7 times 8?",
            languageCode = "en-US",
            allowSocraticFallback = true
        )
        assertEquals(ChatReplySource.SOCRATIC_FALLBACK, reply.source)
        assertTrue("socratic engine must still answer when enabled", reply.text.isNotBlank())
    }

    @Test
    fun `all three AI option labels are localised in every supported language`() {
        val languages = listOf("en-US", "es", "fr", "de")
        val keys = listOf(
            "ai_option_cloud",
            "ai_option_cloud_desc",
            "ai_option_local",
            "ai_option_local_desc",
            "ai_option_socratic",
            "ai_option_socratic_desc",
            "socratic_teacher",
            "socratic_teacher_switch_desc",
            "ai_connection_required",
            "ai_engine_unavailable"
        )
        for (language in languages) {
            for (key in keys) {
                val text = AppLanguageDictionary.getString(key, language)
                assertTrue("missing translation for $key in $language", text.isNotBlank())
                assertNotEquals("untranslated key $key in $language", key, text)
            }
        }
        // Eric's exact user-facing labels in English.
        assertEquals("Cloud AI", AppLanguageDictionary.getString("ai_option_cloud", "en-US"))
        assertEquals("Local AI", AppLanguageDictionary.getString("ai_option_local", "en-US"))
        assertEquals("Socratic Teacher", AppLanguageDictionary.getString("ai_option_socratic", "en-US"))
        // The picker enum carries the same keys the UI renders.
        assertEquals("ai_option_cloud", ChatModelMode.GENERAL.labelKey)
        assertEquals("ai_option_local", ChatModelMode.LLAMA_LOCAL.labelKey)
        assertEquals("ai_option_socratic", ChatModelMode.OFFLINE.labelKey)
    }
}
