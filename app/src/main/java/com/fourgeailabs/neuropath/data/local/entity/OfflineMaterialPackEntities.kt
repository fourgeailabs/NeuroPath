package com.fourgeailabs.neuropath.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Per-child-profile offline material pack.
 *
 * A pack is a materialized, verifiable snapshot of the FULL built-in lesson
 * library for the child's education framework (country / state / district /
 * standard), stored on-device so lessons and teaching work with no connection.
 * The source content is the app's bundled curriculum library, so building a
 * pack transfers no bytes over the network; the stored byte counts are the
 * real serialized sizes of the lesson payloads.
 *
 * One row per child profile (profileId is the primary key). Deleting the
 * profile cascades to the pack and its lessons — no orphaned rows.
 */
@Entity(
    tableName = "offline_material_packs",
    foreignKeys = [
        ForeignKey(
            entity = ChildProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId"], unique = true)]
)
data class OfflineMaterialPackEntity(
    @PrimaryKey
    val profileId: Long,
    /** Stable framework key, e.g. "US/Arizona/Surprise/CCSS". */
    val frameworkKey: String,
    val country: String,
    val stateOrProvince: String,
    val schoolDistrict: String,
    val standardTitle: String,
    val themeWorldId: String,
    /** NOT_DOWNLOADED, DOWNLOADING, READY, FAILED */
    val status: String = OfflinePackStatus.NOT_DOWNLOADED.name,
    val totalLessons: Int = 0,
    /** Real serialized payload bytes on disk (sum of lesson rows). */
    val totalBytes: Long = 0L,
    val downloadedAt: Long = 0L,
    /** Bumped when the bundled curriculum changes so stale packs can be flagged. */
    val packVersion: Int = OfflinePackStatus.CURRENT_PACK_VERSION,
    /**
     * SHA-256 over the pack's lesson payloads (lessonId + payloadJson, sorted by
     * lessonId), recorded when the pack is built. NULL for packs built before
     * the integrity check existed — those are reported as unverified, never
     * silently treated as verified.
     */
    val contentSha256: String? = null
)

object OfflinePackStatus {
    /** Increment when the bundled curriculum content changes shape. */
    const val CURRENT_PACK_VERSION = 1
    val NOT_DOWNLOADED = PackState.NOT_DOWNLOADED
    val DOWNLOADING = PackState.DOWNLOADING
    val READY = PackState.READY
    val FAILED = PackState.FAILED

    enum class PackState {
        NOT_DOWNLOADED, DOWNLOADING, READY, FAILED
    }

    fun isUsable(status: String, packVersion: Int): Boolean =
        status == READY.name && packVersion == CURRENT_PACK_VERSION
}

/**
 * One fully serialized lesson belonging to a profile's offline pack.
 * The JSON payload is the complete [com.fourgeailabs.neuropath.data.model.FullLesson]
 * (teach steps + questions), so teaching can run straight from the pack.
 */
@Entity(
    tableName = "offline_material_lessons",
    foreignKeys = [
        ForeignKey(
            entity = OfflineMaterialPackEntity::class,
            parentColumns = ["profileId"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profileId"]),
        Index(value = ["profileId", "lessonId"], unique = true)
    ]
)
data class OfflineMaterialLessonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val lessonId: String,
    val subjectName: String,
    val gradeLevelCode: String,
    val payloadJson: String,
    val byteSize: Long = 0L
)
