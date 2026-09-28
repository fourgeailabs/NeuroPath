package com.fourgeailabs.neuropath.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fourgeailabs.neuropath.data.local.entity.OfflineMaterialLessonEntity
import com.fourgeailabs.neuropath.data.local.entity.OfflineMaterialPackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineMaterialPackDao {

    @Query("SELECT * FROM offline_material_packs WHERE profileId = :profileId LIMIT 1")
    fun getPackFlow(profileId: Long): Flow<OfflineMaterialPackEntity?>

    @Query("SELECT * FROM offline_material_packs WHERE profileId = :profileId LIMIT 1")
    suspend fun getPackDirect(profileId: Long): OfflineMaterialPackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPack(pack: OfflineMaterialPackEntity)

    /**
     * Inserts the pack row only when the profile has none. Used when marking a
     * build DOWNLOADING: unlike [upsertPack] (REPLACE), this never deletes the
     * existing row, so the previous pack's lesson rows survive until the new
     * pack is fully built and swapped in by [replacePack].
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPackIfMissing(pack: OfflineMaterialPackEntity): Long

    /**
     * Status-only update: flips the pack's build state without touching the
     * pack row's other columns or its lesson rows. A cancelled, failed, or
     * process-killed build can therefore restore the previous state without
     * ever destroying the lessons that were already on disk.
     */
    @Query("UPDATE offline_material_packs SET status = :status WHERE profileId = :profileId")
    suspend fun updatePackStatus(profileId: Long, status: String)

    @Query("SELECT * FROM offline_material_packs WHERE status = :status")
    suspend fun getPacksByStatus(status: String): List<OfflineMaterialPackEntity>

    @Update
    suspend fun updatePack(pack: OfflineMaterialPackEntity)

    @Query("DELETE FROM offline_material_packs WHERE profileId = :profileId")
    suspend fun deletePack(profileId: Long)

    @Query("SELECT * FROM offline_material_lessons WHERE profileId = :profileId AND lessonId = :lessonId LIMIT 1")
    suspend fun getLesson(profileId: Long, lessonId: String): OfflineMaterialLessonEntity?

    @Query("SELECT * FROM offline_material_lessons WHERE profileId = :profileId ORDER BY lessonId ASC")
    suspend fun getLessonsForProfile(profileId: Long): List<OfflineMaterialLessonEntity>

    @Query("SELECT COUNT(*) FROM offline_material_lessons WHERE profileId = :profileId")
    suspend fun getLessonCount(profileId: Long): Int

    @Query("SELECT COALESCE(SUM(byteSize), 0) FROM offline_material_lessons WHERE profileId = :profileId")
    suspend fun getTotalBytes(profileId: Long): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<OfflineMaterialLessonEntity>)

    @Query("DELETE FROM offline_material_lessons WHERE profileId = :profileId")
    suspend fun deleteLessonsForProfile(profileId: Long)

    /**
     * Atomically replaces a profile's pack: pack row + all lesson rows in one
     * transaction, so readers never see a half-written pack.
     */
    @Transaction
    suspend fun replacePack(pack: OfflineMaterialPackEntity, lessons: List<OfflineMaterialLessonEntity>) {
        deleteLessonsForProfile(pack.profileId)
        upsertPack(pack)
        if (lessons.isNotEmpty()) insertLessons(lessons)
    }

    /**
     * Atomically removes a profile's pack and every lesson row. (The FK cascade
     * would handle this too; the explicit delete keeps it deterministic and
     * testable even with foreign_keys pragmas off.)
     */
    @Transaction
    suspend fun deletePackAndLessons(profileId: Long) {
        deleteLessonsForProfile(profileId)
        deletePack(profileId)
    }
}
