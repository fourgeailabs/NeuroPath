package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.local.entity.ChildProfileEntity
import com.example.data.local.entity.DownloadedCurriculumEntity
import com.example.data.local.entity.LessonRecordEntity
import com.example.data.local.entity.OerCurriculumEntity
import com.example.data.local.entity.ProgressLogEntity
import com.example.data.local.entity.SensorySessionEntity
import kotlinx.coroutines.flow.Flow

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

@Database(
    entities = [
        ChildProfileEntity::class,
        DownloadedCurriculumEntity::class,
        LessonRecordEntity::class,
        ProgressLogEntity::class,
        SensorySessionEntity::class,
        OerCurriculumEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun childProfileDao(): ChildProfileDao
    abstract fun curriculumDao(): CurriculumDao
    abstract fun lessonRecordDao(): LessonRecordDao
    abstract fun progressLogDao(): ProgressLogDao
    abstract fun sensorySessionDao(): SensorySessionDao
    abstract fun oerCurriculumDao(): OerCurriculumDao
}
