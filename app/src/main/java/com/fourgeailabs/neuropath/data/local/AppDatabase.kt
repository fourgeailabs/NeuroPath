package com.fourgeailabs.neuropath.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fourgeailabs.neuropath.data.local.entity.ChildProfileEntity
import com.fourgeailabs.neuropath.data.local.entity.ChatMessageEntity
import com.fourgeailabs.neuropath.data.local.entity.DownloadedCurriculumEntity
import com.fourgeailabs.neuropath.data.local.entity.LessonRecordEntity
import com.fourgeailabs.neuropath.data.local.entity.OerCurriculumEntity
import com.fourgeailabs.neuropath.data.local.entity.OfflineMaterialLessonEntity
import com.fourgeailabs.neuropath.data.local.entity.OfflineMaterialPackEntity
import com.fourgeailabs.neuropath.data.local.entity.ProgressLogEntity
import com.fourgeailabs.neuropath.data.local.entity.RewardGrantEntity
import com.fourgeailabs.neuropath.data.local.entity.SensorySessionEntity
import kotlinx.coroutines.flow.Flow

data class ChatSessionSummary(
    val sessionId: String,
    val sessionTitle: String,
    val messageCount: Int,
    val lastTimestamp: Long,
    val subjectTag: String
)

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE profileId = :profileId AND sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(profileId: Long, sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE profileId = :profileId ORDER BY timestamp DESC")
    fun getAllMessagesForProfile(profileId: Long): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE profileId = :profileId AND isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedMessages(profileId: Long): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE profileId = :profileId AND text LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMessages(profileId: Long, query: String): Flow<List<ChatMessageEntity>>

    /**
     * Deterministic session summaries: the title/tag come from each session's latest message
     * (timestamp, then row id, breaks ties), and sessions with identical last timestamps are
     * ordered by sessionId. No bare GROUP BY columns, so the result cannot vary run to run.
     */
    @Query(
        """SELECT m.sessionId AS sessionId,
            (SELECT m2.sessionTitle FROM chat_messages m2
             WHERE m2.profileId = :profileId AND m2.sessionId = m.sessionId
             ORDER BY m2.timestamp DESC, m2.id DESC LIMIT 1) AS sessionTitle,
            COUNT(*) AS messageCount,
            MAX(m.timestamp) AS lastTimestamp,
            (SELECT m3.subjectTag FROM chat_messages m3
             WHERE m3.profileId = :profileId AND m3.sessionId = m.sessionId
             ORDER BY m3.timestamp DESC, m3.id DESC LIMIT 1) AS subjectTag
        FROM chat_messages m
        WHERE m.profileId = :profileId
        GROUP BY m.sessionId
        ORDER BY lastTimestamp DESC, sessionId ASC"""
    )
    fun getSessionSummaries(profileId: Long): Flow<List<ChatSessionSummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("UPDATE chat_messages SET isBookmarked = :isBookmarked WHERE id = :messageId")
    suspend fun updateBookmark(messageId: Long, isBookmarked: Boolean)

    @Query("DELETE FROM chat_messages WHERE profileId = :profileId AND sessionId = :sessionId")
    suspend fun deleteSession(profileId: Long, sessionId: String)

    @Query("DELETE FROM chat_messages WHERE profileId = :profileId")
    suspend fun clearAllMessages(profileId: Long)

    /**
     * Retention cap: keeps the newest [keepNewest] non-bookmarked messages per child and
     * deletes the rest. Bookmarked messages are the parent's explicit "keep this" signal
     * and are never pruned. Called after every insert via [NeuroPathRepository.saveChatMessage].
     */
    @Query(
        """DELETE FROM chat_messages
           WHERE profileId = :profileId AND isBookmarked = 0 AND id NOT IN (
               SELECT id FROM chat_messages
               WHERE profileId = :profileId AND isBookmarked = 0
               ORDER BY timestamp DESC, id DESC LIMIT :keepNewest
           )"""
    )
    suspend fun pruneOldMessages(profileId: Long, keepNewest: Int)
}

@Dao
interface OerCurriculumDao {
    @Query("SELECT * FROM oer_curriculum_units ORDER BY id ASC")
    fun getAllCurriculumUnitsFlow(): Flow<List<OerCurriculumEntity>>

    @Query("SELECT * FROM oer_curriculum_units ORDER BY id ASC")
    suspend fun getAllCurriculumUnitsDirect(): List<OerCurriculumEntity>

    @Query("SELECT * FROM oer_curriculum_units WHERE subjectName = :subjectName")
    suspend fun getUnitsBySubject(subjectName: String): List<OerCurriculumEntity>

    @Query("SELECT * FROM oer_curriculum_units WHERE gradeLevelCode = :gradeCode")
    suspend fun getUnitsByGrade(gradeCode: String): List<OerCurriculumEntity>

    @Query("SELECT * FROM oer_curriculum_units WHERE id = :id LIMIT 1")
    suspend fun getUnitById(id: String): OerCurriculumEntity?

    @Query("SELECT COUNT(*) FROM oer_curriculum_units")
    suspend fun getCurriculumCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<OerCurriculumEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: OerCurriculumEntity)

    /**
     * Non-destructive insert: only rows whose id is not already present are added.
     * Used by the library verification pass so existing rows are never overwritten.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUnitsIfMissing(units: List<OerCurriculumEntity>): List<Long>

    @Query("SELECT id FROM oer_curriculum_units")
    suspend fun getAllIds(): List<String>

    /**
     * Inserts only the units whose ids are not already present, atomically: concurrent
     * verifications cannot interleave a check and an insert.
     * @return how many units were actually inserted.
     */
    @Transaction
    suspend fun ensureUnitsPresent(units: List<OerCurriculumEntity>): Int {
        val existing = getAllIds().toSet()
        val missing = units.filter { it.id !in existing }
        if (missing.isEmpty()) return 0
        // IGNORE inserts return -1 for rows a concurrent verification already inserted,
        // so count only the rows that were actually written.
        return insertUnitsIfMissing(missing).count { it != -1L }
    }

    @Query("DELETE FROM oer_curriculum_units")
    suspend fun deleteAllUnits()
}

@Dao
interface CurriculumDao {
    @Query("SELECT * FROM downloaded_curriculum WHERE id = :id LIMIT 1")
    suspend fun getCurriculumById(id: String): DownloadedCurriculumEntity?

    @Query("SELECT * FROM downloaded_curriculum ORDER BY lastSyncTimestamp DESC LIMIT 1")
    fun getLatestCurriculumFlow(): Flow<DownloadedCurriculumEntity?>

    @Query("SELECT * FROM downloaded_curriculum ORDER BY lastSyncTimestamp DESC LIMIT 1")
    suspend fun getLatestCurriculumDirect(): DownloadedCurriculumEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurriculum(curriculum: DownloadedCurriculumEntity)
}

@Dao
interface ChildProfileDao {
    @Query("SELECT * FROM child_profiles ORDER BY id ASC")
    fun getAllProfilesFlow(): Flow<List<ChildProfileEntity>>

    @Query("SELECT * FROM child_profiles ORDER BY id ASC")
    suspend fun getAllProfilesDirect(): List<ChildProfileEntity>

    @Query("SELECT * FROM child_profiles WHERE id = :id LIMIT 1")
    fun getProfileFlow(id: Long): Flow<ChildProfileEntity?>

    @Query("SELECT * FROM child_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileDirect(id: Long): ChildProfileEntity?

    @Query("SELECT * FROM child_profiles WHERE isInitialSetupComplete = 1 LIMIT 1")
    suspend fun getFirstCompletedProfile(): ChildProfileEntity?

    @Query("SELECT COUNT(*) FROM child_profiles")
    suspend fun getProfileCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: ChildProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ChildProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: ChildProfileEntity)

    @Query("DELETE FROM child_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Long)

    @Query("UPDATE child_profiles SET parentPin = :pin")
    suspend fun updateParentPinForAll(pin: String)

    @Query("UPDATE child_profiles SET customApiKey = ''")
    suspend fun clearAllCustomApiKeys()

    /**
     * Atomic increment: no read-modify-write race between concurrent awards.
     */
    @Query("UPDATE child_profiles SET totalStars = totalStars + :stars, totalGems = totalGems + :gems WHERE id = :profileId")
    suspend fun addStarsAndGems(profileId: Long, stars: Int, gems: Int)

    @Query("UPDATE child_profiles SET currentStreakDays = :streakDays, lastActiveDate = :lastActiveDate WHERE id = :profileId")
    suspend fun updateStreak(profileId: Long, streakDays: Int, lastActiveDate: String)

    /**
     * Atomic conditional unlock: the balance check, the already-owned check, and the debit
     * happen in a single statement, so two concurrent unlocks cannot both spend the same
     * stars/gems, and re-unlocking an owned item never charges again.
     * Returns the number of rows updated (0 = insufficient balance, already unlocked,
     * or unknown profile).
     */
    @Query(
        """UPDATE child_profiles
        SET totalStars = totalStars - :starCost,
            totalGems = totalGems - :gemCost,
            unlockedItemIdsCsv = CASE
                WHEN unlockedItemIdsCsv = '' THEN :itemId
                ELSE unlockedItemIdsCsv || ',' || :itemId
            END
        WHERE id = :profileId
            AND totalStars >= :starCost
            AND totalGems >= :gemCost
            AND instr(',' || unlockedItemIdsCsv || ',', ',' || :itemId || ',') = 0"""
    )
    suspend fun tryUnlockItem(profileId: Long, itemId: String, starCost: Int, gemCost: Int): Int
}

@Dao
interface RewardGrantDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGrant(grant: RewardGrantEntity): Long

    @Query("UPDATE child_profiles SET totalStars = totalStars + :stars, totalGems = totalGems + :gems WHERE id = :profileId")
    suspend fun applyGrant(profileId: Long, stars: Int, gems: Int)

    @Query("SELECT COUNT(*) FROM reward_grants WHERE grantKey = :grantKey")
    suspend fun countByKey(grantKey: String): Long

    /**
     * Idempotent grant in a single transaction: the stars/gems increment
     * happens only when [grant] was actually inserted (INSERT OR IGNORE
     * returns -1 for a duplicate key), so concurrent or retried grants with
     * the same key award exactly once.
     * @return true when the grant was applied, false when it was a duplicate.
     */
    @Transaction
    suspend fun grantOnce(grant: RewardGrantEntity): Boolean {
        if (insertGrant(grant) == -1L) return false
        applyGrant(grant.profileId, grant.stars, grant.gems)
        return true
    }
}

@Dao
interface LessonRecordDao {
    @Query("SELECT * FROM lesson_records")
    fun getAllLessonRecords(): Flow<List<LessonRecordEntity>>

    @Query("SELECT * FROM lesson_records WHERE subjectId = :subjectId")
    fun getLessonsBySubject(subjectId: String): Flow<List<LessonRecordEntity>>

    @Query("SELECT * FROM lesson_records WHERE lessonId = :lessonId LIMIT 1")
    suspend fun getLessonRecord(lessonId: String): LessonRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessonRecord(record: LessonRecordEntity)
}

@Dao
interface ProgressLogDao {
    @Query("SELECT * FROM progress_logs ORDER BY timestamp DESC")
    fun getAllProgressLogs(): Flow<List<ProgressLogEntity>>

    @Query("SELECT * FROM progress_logs WHERE subjectId = :subjectId ORDER BY timestamp DESC")
    fun getLogsBySubject(subjectId: String): Flow<List<ProgressLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ProgressLogEntity)
}

@Dao
interface SensorySessionDao {
    @Query("SELECT * FROM sensory_sessions ORDER BY timestamp DESC")
    fun getAllSensorySessions(): Flow<List<SensorySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSensorySession(session: SensorySessionEntity)
}

// Room Migrations to prevent data loss on schema changes
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys=ON;")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE child_profiles ADD COLUMN localLlamaInstalled INTEGER NOT NULL DEFAULT 0;")
    }
}

/**
 * 11 -> 12: attach ON DELETE CASCADE foreign keys from every child-owned table
 * (progress_logs, sensory_sessions, chat_messages) to child_profiles(id).
 *
 * SQLite cannot add a FOREIGN KEY to an existing table, so each table is recreated:
 * create the new table with the constraint, copy the rows, drop the old table,
 * rename, and rebuild the profileId index Room expects. Foreign-key enforcement is
 * disabled during the copy (a child row is briefly parentless mid-move) and
 * re-enabled afterwards.
 */
val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys=OFF;")

        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `progress_logs_new`
               (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `profileId` INTEGER NOT NULL,
                `subjectId` TEXT NOT NULL, `lessonId` TEXT NOT NULL, `lessonTitle` TEXT NOT NULL,
                `scorePercent` INTEGER NOT NULL, `totalQuestions` INTEGER NOT NULL,
                `correctQuestions` INTEGER NOT NULL, `durationSeconds` INTEGER NOT NULL,
                `sensoryBreaksTaken` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL,
                FOREIGN KEY(`profileId`) REFERENCES `child_profiles`(`id`)
                ON UPDATE NO ACTION ON DELETE CASCADE)"""
        )
        db.execSQL(
            """INSERT INTO `progress_logs_new`
               (`id`, `profileId`, `subjectId`, `lessonId`, `lessonTitle`, `scorePercent`,
                `totalQuestions`, `correctQuestions`, `durationSeconds`, `sensoryBreaksTaken`, `timestamp`)
               SELECT `id`, `profileId`, `subjectId`, `lessonId`, `lessonTitle`, `scorePercent`,
                `totalQuestions`, `correctQuestions`, `durationSeconds`, `sensoryBreaksTaken`, `timestamp`
               FROM `progress_logs`"""
        )
        db.execSQL("DROP TABLE `progress_logs`")
        db.execSQL("ALTER TABLE `progress_logs_new` RENAME TO `progress_logs`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_progress_logs_profileId` ON `progress_logs` (`profileId`)")

        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `sensory_sessions_new`
               (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `profileId` INTEGER NOT NULL,
                `activityType` TEXT NOT NULL, `durationSeconds` INTEGER NOT NULL,
                `countAction` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL,
                FOREIGN KEY(`profileId`) REFERENCES `child_profiles`(`id`)
                ON UPDATE NO ACTION ON DELETE CASCADE)"""
        )
        db.execSQL(
            """INSERT INTO `sensory_sessions_new`
               (`id`, `profileId`, `activityType`, `durationSeconds`, `countAction`, `timestamp`)
               SELECT `id`, `profileId`, `activityType`, `durationSeconds`, `countAction`, `timestamp`
               FROM `sensory_sessions`"""
        )
        db.execSQL("DROP TABLE `sensory_sessions`")
        db.execSQL("ALTER TABLE `sensory_sessions_new` RENAME TO `sensory_sessions`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sensory_sessions_profileId` ON `sensory_sessions` (`profileId`)")

        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `chat_messages_new`
               (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `profileId` INTEGER NOT NULL,
                `sessionId` TEXT NOT NULL, `sessionTitle` TEXT NOT NULL, `sender` TEXT NOT NULL,
                `text` TEXT NOT NULL, `explanationMode` TEXT NOT NULL, `modelUsed` TEXT NOT NULL,
                `isFreeModel` INTEGER NOT NULL, `subjectTag` TEXT NOT NULL,
                `isBookmarked` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL,
                FOREIGN KEY(`profileId`) REFERENCES `child_profiles`(`id`)
                ON UPDATE NO ACTION ON DELETE CASCADE)"""
        )
        db.execSQL(
            """INSERT INTO `chat_messages_new`
               (`id`, `profileId`, `sessionId`, `sessionTitle`, `sender`, `text`, `explanationMode`,
                `modelUsed`, `isFreeModel`, `subjectTag`, `isBookmarked`, `timestamp`)
               SELECT `id`, `profileId`, `sessionId`, `sessionTitle`, `sender`, `text`, `explanationMode`,
                `modelUsed`, `isFreeModel`, `subjectTag`, `isBookmarked`, `timestamp`
               FROM `chat_messages`"""
        )
        db.execSQL("DROP TABLE `chat_messages`")
        db.execSQL("ALTER TABLE `chat_messages_new` RENAME TO `chat_messages`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_messages_profileId` ON `chat_messages` (`profileId`)")

        db.execSQL("PRAGMA foreign_keys=ON;")
    }
}

/**
 * 12 -> 13: add the parent's metered-download override flag to child_profiles.
 * Plain ALTER TABLE ADD COLUMN (new installs get it from the entity; the
 * DEFAULT 0 keeps the Wi-Fi-only default for upgraded installs).
 */
val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `child_profiles` ADD COLUMN `allowMeteredModelDownload` INTEGER NOT NULL DEFAULT 0")
    }
}

/**
 * 13 -> 14: per-child-profile offline material packs. Two brand-new tables, so
 * no data copy is needed — just CREATE TABLE with the CASCADE foreign keys
 * Room expects (packs -> child_profiles, lessons -> packs).
 */
val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `offline_material_packs`
               (`profileId` INTEGER NOT NULL, `frameworkKey` TEXT NOT NULL, `country` TEXT NOT NULL,
                `stateOrProvince` TEXT NOT NULL, `schoolDistrict` TEXT NOT NULL,
                `standardTitle` TEXT NOT NULL, `themeWorldId` TEXT NOT NULL,
                `status` TEXT NOT NULL, `totalLessons` INTEGER NOT NULL,
                `totalBytes` INTEGER NOT NULL, `downloadedAt` INTEGER NOT NULL,
                `packVersion` INTEGER NOT NULL, PRIMARY KEY(`profileId`),
                FOREIGN KEY(`profileId`) REFERENCES `child_profiles`(`id`)
                ON UPDATE NO ACTION ON DELETE CASCADE)"""
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_offline_material_packs_profileId` ON `offline_material_packs` (`profileId`)")
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `offline_material_lessons`
               (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `profileId` INTEGER NOT NULL,
                `lessonId` TEXT NOT NULL, `subjectName` TEXT NOT NULL,
                `gradeLevelCode` TEXT NOT NULL, `payloadJson` TEXT NOT NULL,
                `byteSize` INTEGER NOT NULL,
                FOREIGN KEY(`profileId`) REFERENCES `offline_material_packs`(`profileId`)
                ON UPDATE NO ACTION ON DELETE CASCADE)"""
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_offline_material_lessons_profileId` ON `offline_material_lessons` (`profileId`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_offline_material_lessons_profileId_lessonId` ON `offline_material_lessons` (`profileId`, `lessonId`)")
    }
}

/**
 * 14 -> 15: add the parent's Socratic Teacher on/off flag to child_profiles.
 * Plain ALTER TABLE ADD COLUMN (new installs get it from the entity; the
 * DEFAULT 1 keeps Socratic Teacher ON for upgraded installs).
 */
val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `child_profiles` ADD COLUMN `socraticTeacherEnabled` INTEGER NOT NULL DEFAULT 1")
    }
}

/**
 * 15 -> 16: idempotent reward grants + pack integrity hash. Two additive,
 * non-destructive changes:
 * - new `reward_grants` table holding one row per applied stars/gems grant
 *   (the primary-key grantKey is the idempotency key; duplicates are ignored);
 * - new nullable `contentSha256` column on `offline_material_packs`.
 *   Existing packs keep every row untouched; NULL simply means "hash not
 *   recorded" for packs built before this version.
 */
val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `reward_grants`
               (`grantKey` TEXT NOT NULL, `profileId` INTEGER NOT NULL,
                `stars` INTEGER NOT NULL, `gems` INTEGER NOT NULL,
                `reason` TEXT NOT NULL, `grantedAt` INTEGER NOT NULL,
                PRIMARY KEY(`grantKey`),
                FOREIGN KEY(`profileId`) REFERENCES `child_profiles`(`id`)
                ON UPDATE NO ACTION ON DELETE CASCADE)"""
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reward_grants_profileId` ON `reward_grants` (`profileId`)")
        // Idempotent column add: Room only runs a migration once, inside a
        // transaction, but a retried run must not crash on the duplicate
        // column, so check first.
        val hasContentSha256 = db.query("PRAGMA table_info(`offline_material_packs`)").use { c ->
            val nameIdx = c.getColumnIndex("name")
            var found = false
            while (!found && c.moveToNext()) {
                found = c.getString(nameIdx) == "contentSha256"
            }
            found
        }
        if (!hasContentSha256) {
            db.execSQL("ALTER TABLE `offline_material_packs` ADD COLUMN `contentSha256` TEXT")
        }
    }
}

@Database(
    entities = [
        ChildProfileEntity::class,
        DownloadedCurriculumEntity::class,
        LessonRecordEntity::class,
        ProgressLogEntity::class,
        SensorySessionEntity::class,
        OerCurriculumEntity::class,
        ChatMessageEntity::class,
        OfflineMaterialPackEntity::class,
        OfflineMaterialLessonEntity::class,
        RewardGrantEntity::class
    ],
    version = 16,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun childProfileDao(): ChildProfileDao
    abstract fun curriculumDao(): CurriculumDao
    abstract fun lessonRecordDao(): LessonRecordDao
    abstract fun progressLogDao(): ProgressLogDao
    abstract fun sensorySessionDao(): SensorySessionDao
    abstract fun oerCurriculumDao(): OerCurriculumDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun offlineMaterialPackDao(): OfflineMaterialPackDao
    abstract fun rewardGrantDao(): RewardGrantDao

    companion object {
        val MIGRATIONS = listOf(MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16)
    }
}

