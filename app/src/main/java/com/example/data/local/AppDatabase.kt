package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.local.entity.ChildProfileEntity
import com.example.data.local.entity.LessonRecordEntity
import com.example.data.local.entity.ProgressLogEntity
import com.example.data.local.entity.SensorySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildProfileDao {
    @Query("SELECT * FROM child_profiles WHERE id = :id LIMIT 1")
    fun getProfileFlow(id: Long = 1): Flow<ChildProfileEntity?>

    @Query("SELECT * FROM child_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileDirect(id: Long = 1): ChildProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: ChildProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: ChildProfileEntity)
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
        LessonRecordEntity::class,
        ProgressLogEntity::class,
        SensorySessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun childProfileDao(): ChildProfileDao
    abstract fun lessonRecordDao(): LessonRecordDao
    abstract fun progressLogDao(): ProgressLogDao
    abstract fun sensorySessionDao(): SensorySessionDao
}
