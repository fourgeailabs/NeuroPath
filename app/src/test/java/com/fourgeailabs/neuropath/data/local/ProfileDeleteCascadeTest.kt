package com.fourgeailabs.neuropath.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.fourgeailabs.neuropath.data.local.entity.ChatMessageEntity
import com.fourgeailabs.neuropath.data.local.entity.ChildProfileEntity
import com.fourgeailabs.neuropath.data.local.entity.ProgressLogEntity
import com.fourgeailabs.neuropath.data.local.entity.SensorySessionEntity
import com.fourgeailabs.neuropath.data.repository.NeuroPathRepository
import com.fourgeailabs.neuropath.learning.LearnerPersonalizationEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

/**
 * Slice 2 (P0 privacy): deleting a child profile must remove every child-owned row.
 *
 * Child-owned tables: chat_messages, progress_logs, sensory_sessions — each carries
 * profileId with ON DELETE CASCADE to child_profiles(id). Not child-owned and
 * therefore out of scope: child_profiles itself, downloaded_curriculum (shared
 * district curriculum), oer_curriculum_units (preinstalled global content), and
 * lesson_records (currently global per-lesson aggregates with no profileId —
 * converting them to per-child is a separate behaviour change, see KDoc).
 */
@RunWith(RobolectricTestRunner::class)
class ProfileDeleteCascadeTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var db: AppDatabase
    private lateinit var repo: NeuroPathRepository

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = NeuroPathRepository(db)
        context.getSharedPreferences("learner_personalization", Context.MODE_PRIVATE)
            .edit().clear().apply()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun deleteProfile_cascadesToAllChildOwnedRows() = runBlocking {
        val p1 = db.childProfileDao().insertProfile(ChildProfileEntity(name = "Kid A"))
        val p2 = db.childProfileDao().insertProfile(ChildProfileEntity(name = "Kid B"))

        db.chatMessageDao().insertMessage(chatMessage(p1, "hello a"))
        db.chatMessageDao().insertMessage(chatMessage(p1, "more a"))
        db.chatMessageDao().insertMessage(chatMessage(p2, "hello b"))
        db.progressLogDao().insertLog(progressLog(p1))
        db.progressLogDao().insertLog(progressLog(p2))
        db.sensorySessionDao().insertSensorySession(sensorySession(p1))
        db.sensorySessionDao().insertSensorySession(sensorySession(p2))

        // Sanity: both children's rows exist before the delete.
        assertEquals(2, messagesFor(p1).size)
        assertEquals(1, messagesFor(p2).size)

        repo.deleteProfile(p1)

        // The profile row is gone…
        assertNull(db.childProfileDao().getProfileDirect(p1))
        // …and every child-owned row cascaded with it.
        assertTrue(messagesFor(p1).isEmpty())
        assertTrue(allProgressLogs().none { it.profileId == p1 })
        assertTrue(allSensorySessions().none { it.profileId == p1 })
        // The surviving child's data is untouched.
        assertEquals(1, messagesFor(p2).size)
        assertTrue(allProgressLogs().any { it.profileId == p2 })
        assertTrue(allSensorySessions().any { it.profileId == p2 })
        assertEquals("Kid B", db.childProfileDao().getProfileDirect(p2)?.name)
    }

    @Test
    fun deleteProfile_clearsPersonalizationPrefs() {
        val p1 = 111L
        val p2 = 222L
        LearnerPersonalizationEngine.recordAnswer(context, p1, "MATH", true, "fractions")
        LearnerPersonalizationEngine.recordAnswer(context, p2, "MATH", false, "decimals")
        LearnerPersonalizationEngine.recordPreferredStyle(context, p1, "step_by_step")

        val prefs = context.getSharedPreferences("learner_personalization", Context.MODE_PRIVATE)
        assertTrue(prefs.getInt("attempts_$p1", 0) > 0)

        LearnerPersonalizationEngine.clearProfileData(context, p1)

        val after = prefs.all.keys
        assertTrue(after.none { it.contains("_${p1}_") || it.endsWith("_$p1") })
        // The other child's fingerprint survives.
        assertTrue(after.any { it.contains("_${p2}_") || it.endsWith("_$p2") })
        assertEquals(1, prefs.getInt("attempts_$p2", 0))
    }

    @Test
    fun chatRetention_prunesOldestNonBookmarkedButKeepsBookmarks() = runBlocking {
        val p = db.childProfileDao().insertProfile(ChildProfileEntity(name = "Kid"))
        // Oldest message is bookmarked; it must survive pruning.
        val bookmarkedId = db.chatMessageDao().insertMessage(chatMessage(p, "keep me", bookmarked = true, ts = 1))
        for (i in 2..6) db.chatMessageDao().insertMessage(chatMessage(p, "msg $i", ts = i.toLong()))

        db.chatMessageDao().pruneOldMessages(p, keepNewest = 3)

        val remaining = messagesFor(p)
        assertEquals(4, remaining.size) // 3 newest + the bookmarked one
        assertTrue(remaining.any { it.id == bookmarkedId })
        assertTrue(remaining.none { it.text == "msg 2" || it.text == "msg 3" })
        assertTrue(remaining.any { it.text == "msg 6" })
    }

    @Test
    fun saveChatMessage_enforcesRetentionCap() = runBlocking {
        val p = db.childProfileDao().insertProfile(ChildProfileEntity(name = "Kid"))
        val cap = NeuroPathRepository.MAX_STORED_CHAT_MESSAGES_PER_CHILD
        // 3 bookmarked among the oldest + cap+5 plain messages.
        repeat(3) { i -> repo.saveChatMessage(chatMessage(p, "saved $i", bookmarked = true, ts = i.toLong())) }
        repeat(cap + 5) { i -> repo.saveChatMessage(chatMessage(p, "msg $i", ts = (100 + i).toLong())) }

        val remaining = messagesFor(p)
        assertEquals(cap + 3, remaining.size)
        assertTrue(remaining.none { it.text == "msg 0" }) // oldest plain message pruned
        assertTrue(remaining.any { it.text == "msg ${cap + 4}" }) // newest kept
        assertEquals(3, remaining.count { it.isBookmarked })
    }

    @Test
    fun migration11to12_preservesDataAndEnforcesCascade() {
        val dbFile = File(context.cacheDir, "cascade-migration-test.db")
        if (dbFile.exists()) dbFile.delete()

        // Build a genuine v11 database with raw SQL (no room_master_table, like a real upgrade).
        val factory = FrameworkSQLiteOpenHelperFactory()
        val v11 = factory.create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbFile.absolutePath)
                .callback(object : SupportSQLiteOpenHelper.Callback(11) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(V11_CHILD_PROFILES)
                        db.execSQL(V11_PROGRESS_LOGS)
                        db.execSQL(V11_SENSORY_SESSIONS)
                        db.execSQL(V11_CHAT_MESSAGES)
                        db.execSQL(V11_DOWNLOADED_CURRICULUM)
                        db.execSQL(V11_LESSON_RECORDS)
                        db.execSQL(V11_OER_CURRICULUM_UNITS)
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                }).build()
        )
        val raw = v11.writableDatabase
        raw.execSQL(
            "INSERT INTO child_profiles ($PROFILE_COLUMNS) VALUES " +
                "('Kid A',6,'KINDERGARTEN','ELEMENTARY','','','','','', 'en-US','dino','','','','','',0,'PASTEL',0.88,1.05,1,0,'OFF',0,0,0,'',0,'SOCRATIC_AND_FULL',0,'av_robot',NULL,NULL,NULL,'av_robot','',15,1,0,'','Llama 3.2 3B (Hugging Face)','','MANUAL',0)," +
                "('Kid B',6,'KINDERGARTEN','ELEMENTARY','','','','','', 'en-US','dino','','','','','',0,'PASTEL',0.88,1.05,1,0,'OFF',0,0,0,'',0,'SOCRATIC_AND_FULL',0,'av_robot',NULL,NULL,NULL,'av_robot','',15,1,0,'','Llama 3.2 3B (Hugging Face)','','MANUAL',0)"
        )
        raw.execSQL("INSERT INTO chat_messages (profileId,sessionId,sessionTitle,sender,text,explanationMode,modelUsed,isFreeModel,subjectTag,isBookmarked,timestamp) VALUES (1,'s','t','USER','hi','STEP_BY_STEP','m',1,'GENERAL',0,1),(2,'s','t','USER','yo','STEP_BY_STEP','m',1,'GENERAL',0,2)")
        raw.execSQL("INSERT INTO progress_logs (profileId,subjectId,lessonId,lessonTitle,scorePercent,totalQuestions,correctQuestions,durationSeconds,sensoryBreaksTaken,timestamp) VALUES (1,'MATH','l1','L1',80,5,4,60,0,1),(2,'MATH','l1','L1',90,5,5,60,0,2)")
        raw.execSQL("INSERT INTO sensory_sessions (profileId,activityType,durationSeconds,countAction,timestamp) VALUES (1,'POP_IT',60,10,1),(2,'BREATHING',60,5,2)")
        v11.close()

        // Run MIGRATION_11_12 directly against the raw v11 file via a raw
        // SupportSQLiteOpenHelper. This keeps the test independent of the current
        // Room database version: it validates the migration's own SQL, not the
        // whole upgrade chain.
        val upgraded = factory.create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbFile.absolutePath)
                .callback(object : SupportSQLiteOpenHelper.Callback(12) {
                    override fun onCreate(db: SupportSQLiteDatabase) = Unit
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                        if (oldVersion == 11) MIGRATION_11_12.migrate(db)
                    }
                }).build()
        )
        val sql = upgraded.writableDatabase
        sql.execSQL("PRAGMA foreign_keys=ON")

        // All rows survived the table rebuilds…
        assertEquals(2, count(sql, "child_profiles"))
        assertEquals(2, count(sql, "chat_messages"))
        assertEquals(2, count(sql, "progress_logs"))
        assertEquals(2, count(sql, "sensory_sessions"))

        // …each child table now has an ON DELETE CASCADE foreign key to
        // child_profiles(id) plus the profileId index Room expects.
        for (table in listOf("progress_logs", "sensory_sessions", "chat_messages")) {
            val fk = sql.query("PRAGMA foreign_key_list(`$table`)")
            var sawCascade = false
            while (fk.moveToNext()) {
                if (fk.getString(fk.getColumnIndexOrThrow("table")) == "child_profiles" &&
                    fk.getString(fk.getColumnIndexOrThrow("from")) == "profileId" &&
                    fk.getString(fk.getColumnIndexOrThrow("to")) == "id" &&
                    fk.getString(fk.getColumnIndexOrThrow("on_delete")) == "CASCADE"
                ) sawCascade = true
            }
            fk.close()
            assertTrue("expected ON DELETE CASCADE FK on $table", sawCascade)

            val idx = sql.query("PRAGMA index_list(`$table`)")
            var sawIndex = false
            while (idx.moveToNext()) {
                if (idx.getString(idx.getColumnIndexOrThrow("name")) == "index_${table}_profileId") sawIndex = true
            }
            idx.close()
            assertTrue("expected index_${table}_profileId", sawIndex)
        }

        // …and the cascade is live: deleting one profile removes only its rows.
        sql.execSQL("DELETE FROM child_profiles WHERE id = 1")
        assertEquals(0, countWhere(sql, "chat_messages", "profileId = 1"))
        assertEquals(0, countWhere(sql, "progress_logs", "profileId = 1"))
        assertEquals(0, countWhere(sql, "sensory_sessions", "profileId = 1"))
        assertEquals(1, countWhere(sql, "chat_messages", "profileId = 2"))
        assertEquals(1, countWhere(sql, "progress_logs", "profileId = 2"))
        assertEquals(1, countWhere(sql, "sensory_sessions", "profileId = 2"))

        upgraded.close()
        dbFile.delete()
    }

    private fun count(db: SupportSQLiteDatabase, table: String): Int {
        val c = db.query("SELECT COUNT(*) FROM `$table`")
        c.moveToFirst()
        val n = c.getInt(0)
        c.close()
        return n
    }

    private fun countWhere(db: SupportSQLiteDatabase, table: String, where: String): Int {
        val c = db.query("SELECT COUNT(*) FROM `$table` WHERE $where")
        c.moveToFirst()
        val n = c.getInt(0)
        c.close()
        return n
    }

    // ---------- helpers ----------

    private fun chatMessage(profileId: Long, text: String, bookmarked: Boolean = false, ts: Long = System.currentTimeMillis()) =
        ChatMessageEntity(
            profileId = profileId,
            sessionId = "s1",
            sessionTitle = "Test",
            sender = "USER",
            text = text,
            isBookmarked = bookmarked,
            timestamp = ts
        )

    private fun progressLog(profileId: Long) = ProgressLogEntity(
        profileId = profileId,
        subjectId = "MATH",
        lessonId = "l1",
        lessonTitle = "L1",
        scorePercent = 80,
        totalQuestions = 5,
        correctQuestions = 4,
        durationSeconds = 60,
        sensoryBreaksTaken = 0
    )

    private fun sensorySession(profileId: Long) = SensorySessionEntity(
        profileId = profileId,
        activityType = "POP_IT",
        durationSeconds = 60,
        countAction = 10
    )

    private suspend fun messagesFor(profileId: Long): List<ChatMessageEntity> =
        db.chatMessageDao().getAllMessagesForProfile(profileId).first()

    private suspend fun allProgressLogs() = db.progressLogDao().getAllProgressLogs().first()

    private suspend fun allSensorySessions() = db.sensorySessionDao().getAllSensorySessions().first()

    companion object {
        // Exact v11 DDL, copied from app/schemas/.../AppDatabase/11.json.
        private const val V11_CHILD_PROFILES =
            "CREATE TABLE IF NOT EXISTS `child_profiles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `age` INTEGER NOT NULL, `gradeLevel` TEXT NOT NULL, `ageGroupTier` TEXT NOT NULL, `stateStandard` TEXT NOT NULL, `country` TEXT NOT NULL, `stateOrProvince` TEXT NOT NULL, `city` TEXT NOT NULL, `schoolDistrict` TEXT NOT NULL, `appLanguageCode` TEXT NOT NULL, `activeThemeId` TEXT NOT NULL, `neurodivergentTypesCsv` TEXT NOT NULL, `strugglesCsv` TEXT NOT NULL, `strengthsCsv` TEXT NOT NULL, `hyperFixationsCsv` TEXT NOT NULL, `customAccentColorHex` TEXT, `dyslexiaFontEnabled` INTEGER NOT NULL, `highContrastMode` TEXT NOT NULL, `ttsSpeed` REAL NOT NULL, `ttsVoicePitch` REAL NOT NULL, `autoHighlightWords` INTEGER NOT NULL, `readAnswersAloud` INTEGER NOT NULL, `ambientSound` TEXT NOT NULL, `totalStars` INTEGER NOT NULL, `totalGems` INTEGER NOT NULL, `currentStreakDays` INTEGER NOT NULL, `lastActiveDate` TEXT NOT NULL, `learningBuddyDisabled` INTEGER NOT NULL, `aiVersionMode` TEXT NOT NULL, `localLlamaInstalled` INTEGER NOT NULL, `currentAvatarId` TEXT NOT NULL, `equippedHatId` TEXT, `equippedPetId` TEXT, `equippedBadgeId` TEXT, `unlockedItemIdsCsv` TEXT NOT NULL, `parentPin` TEXT NOT NULL, `dailyGoalMinutes` INTEGER NOT NULL, `isCoppaConsented` INTEGER NOT NULL, `isInitialSetupComplete` INTEGER NOT NULL, `zipOrPostalCodeOverride` TEXT NOT NULL, `customAiPlatform` TEXT NOT NULL, `customApiKey` TEXT NOT NULL, `themeRotationSchedule` TEXT NOT NULL, `lastThemeRotationTimestamp` INTEGER NOT NULL)"
        private const val V11_PROGRESS_LOGS =
            "CREATE TABLE IF NOT EXISTS `progress_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `profileId` INTEGER NOT NULL, `subjectId` TEXT NOT NULL, `lessonId` TEXT NOT NULL, `lessonTitle` TEXT NOT NULL, `scorePercent` INTEGER NOT NULL, `totalQuestions` INTEGER NOT NULL, `correctQuestions` INTEGER NOT NULL, `durationSeconds` INTEGER NOT NULL, `sensoryBreaksTaken` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)"
        private const val V11_SENSORY_SESSIONS =
            "CREATE TABLE IF NOT EXISTS `sensory_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `profileId` INTEGER NOT NULL, `activityType` TEXT NOT NULL, `durationSeconds` INTEGER NOT NULL, `countAction` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)"
        private const val V11_CHAT_MESSAGES =
            "CREATE TABLE IF NOT EXISTS `chat_messages` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `profileId` INTEGER NOT NULL, `sessionId` TEXT NOT NULL, `sessionTitle` TEXT NOT NULL, `sender` TEXT NOT NULL, `text` TEXT NOT NULL, `explanationMode` TEXT NOT NULL, `modelUsed` TEXT NOT NULL, `isFreeModel` INTEGER NOT NULL, `subjectTag` TEXT NOT NULL, `isBookmarked` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)"

        // Remaining v11 tables, copied verbatim from the exported v11 schema so the
        // migration validation sees a genuine complete v11 database.
        private const val V11_DOWNLOADED_CURRICULUM =
            "CREATE TABLE IF NOT EXISTS `downloaded_curriculum` (`id` TEXT NOT NULL, `country` TEXT NOT NULL, `stateOrProvince` TEXT NOT NULL, `schoolDistrict` TEXT NOT NULL, `postalCode` TEXT NOT NULL, `standardTitle` TEXT NOT NULL, `officialSourceAgency` TEXT NOT NULL, `officialSourceUrl` TEXT NOT NULL, `gradesCoveredSummary` TEXT NOT NULL, `rawCurriculumJson` TEXT NOT NULL, `lastSyncTimestamp` INTEGER NOT NULL, `lastSyncDateFormatted` TEXT NOT NULL, `syncStatus` TEXT NOT NULL, PRIMARY KEY(`id`))"

        private const val V11_LESSON_RECORDS =
            "CREATE TABLE IF NOT EXISTS `lesson_records` (`lessonId` TEXT NOT NULL, `subjectId` TEXT NOT NULL, `gradeLevel` TEXT NOT NULL, `stateStandard` TEXT NOT NULL, `title` TEXT NOT NULL, `standardCode` TEXT NOT NULL, `completed` INTEGER NOT NULL, `scorePercent` INTEGER NOT NULL, `attempts` INTEGER NOT NULL, `lastAttemptTimestamp` INTEGER NOT NULL, `customThemeId` TEXT, PRIMARY KEY(`lessonId`))"

        private const val V11_OER_CURRICULUM_UNITS =
            "CREATE TABLE IF NOT EXISTS `oer_curriculum_units` (`id` TEXT NOT NULL, `subjectName` TEXT NOT NULL, `gradeLevelCode` TEXT NOT NULL, `gradeBandName` TEXT NOT NULL, `collectionTitle` TEXT NOT NULL, `unitTitle` TEXT NOT NULL, `standardCode` TEXT NOT NULL, `oerCommonsUrl` TEXT NOT NULL, `openLicense` TEXT NOT NULL, `summary` TEXT NOT NULL, `learningObjectivesCsv` TEXT NOT NULL, `keyConceptsCsv` TEXT NOT NULL, `vocabularyCsv` TEXT NOT NULL, `essentialQuestionsCsv` TEXT NOT NULL, `socraticGuidingQuestionsCsv` TEXT NOT NULL, `commonMisconceptionsCsv` TEXT NOT NULL, `practiceProblemsJson` TEXT NOT NULL, `accommodationsCsv` TEXT NOT NULL, `isPreinstalled` INTEGER NOT NULL, `lastUpdatedTimestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))"

        private const val PROFILE_COLUMNS =
            "name,age,gradeLevel,ageGroupTier,stateStandard,country,stateOrProvince,city,schoolDistrict," +
                "appLanguageCode,activeThemeId,neurodivergentTypesCsv,strugglesCsv,strengthsCsv,hyperFixationsCsv," +
                "customAccentColorHex,dyslexiaFontEnabled,highContrastMode,ttsSpeed,ttsVoicePitch,autoHighlightWords," +
                "readAnswersAloud,ambientSound,totalStars,totalGems,currentStreakDays,lastActiveDate," +
                "learningBuddyDisabled,aiVersionMode,localLlamaInstalled,currentAvatarId,equippedHatId,equippedPetId," +
                "equippedBadgeId,unlockedItemIdsCsv,parentPin,dailyGoalMinutes,isCoppaConsented,isInitialSetupComplete," +
                "zipOrPostalCodeOverride,customAiPlatform,customApiKey,themeRotationSchedule,lastThemeRotationTimestamp"
    }
}

