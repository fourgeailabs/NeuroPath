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
import com.fourgeailabs.neuropath.data.local.entity.ProgressLogEntity
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
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("PRAGMA foreign_keys=ON;")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE child_profiles ADD COLUMN localLlamaInstalled INTEGER NOT NULL DEFAULT 0;")
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
        ChatMessageEntity::class
    ],
    version = 11,
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

    companion object {
        val MIGRATIONS = listOf(MIGRATION_9_10, MIGRATION_10_11)
    }
}

