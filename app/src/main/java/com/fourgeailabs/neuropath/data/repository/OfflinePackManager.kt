package com.fourgeailabs.neuropath.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.fourgeailabs.neuropath.BuildConfig
import com.fourgeailabs.neuropath.data.curriculum.CurriculumCatalog
import com.fourgeailabs.neuropath.data.local.AppDatabase
import com.fourgeailabs.neuropath.data.local.entity.ChildProfileEntity
import com.fourgeailabs.neuropath.data.local.entity.OfflineMaterialLessonEntity
import com.fourgeailabs.neuropath.data.local.entity.OfflineMaterialPackEntity
import com.fourgeailabs.neuropath.data.local.entity.OfflinePackStatus
import com.fourgeailabs.neuropath.data.model.EducationalSubject
import com.fourgeailabs.neuropath.data.model.FullLesson
import com.fourgeailabs.neuropath.data.model.GradeLevel
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.security.MessageDigest

/**
 * Thrown (as a failed [Result], not a crash) when the device does not have
 * enough free storage to materialize an offline pack. The message carries the
 * real numbers so the UI can tell the parent exactly how much space is needed.
 */
class InsufficientStorageException(message: String) : IOException(message)

/**
 * Builds, stores, and serves per-child-profile offline material packs.
 *
 * A pack is the FULL built-in lesson library for the child's education
 * framework, materialized into Room: one [OfflineMaterialPackEntity] row plus
 * one [OfflineMaterialLessonEntity] row per lesson holding the complete
 * serialized [FullLesson] (teach steps + questions).
 *
 * Honesty contract: every byte of pack content already ships inside the app
 * (the bundled curriculum library). Building a pack transfers nothing over
 * the network, so it is never gated on Wi-Fi — [isActiveNetworkUnmetered]
 * exists (same pattern as the model-download gate) for any future
 * network-backed pack content and for UI copy, and the reported sizes are
 * the real serialized payload sizes.
 */
class OfflinePackManager(private val db: AppDatabase) {

    private val moshi: Moshi = Moshi.Builder().build()
    private val lessonAdapter = moshi.adapter(FullLesson::class.java)

    private val dao get() = db.offlineMaterialPackDao()

    companion object {
        private const val TAG = "OfflinePackManager"

        /** Stable key binding a pack to the framework it was built for. */
        fun frameworkKeyFor(profile: ChildProfileEntity): String =
            listOf(profile.country, profile.stateOrProvince, profile.schoolDistrict, profile.stateStandard)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .joinToString("|")
                .ifBlank { "UNKNOWN_FRAMEWORK" }

        fun frameworkLabelFor(profile: ChildProfileEntity): String =
            listOf(profile.schoolDistrict, profile.stateOrProvince, profile.country)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .joinToString(", ")
                .ifBlank { "the selected framework" }

        fun formatBytes(bytes: Long): String = when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        }

        /**
         * SHA-256 over the pack's lesson payloads (lessonId + payloadJson,
         * sorted by lessonId). Recorded on the pack row when the build
         * finishes so later reads can prove the stored lessons were not
         * corrupted or half-written.
         */
        fun packSha256Hex(lessonRows: List<OfflineMaterialLessonEntity>): String {
            val digest = MessageDigest.getInstance("SHA-256")
            for (row in lessonRows.sortedBy { it.lessonId }) {
                digest.update(row.lessonId.toByteArray(Charsets.UTF_8))
                digest.update(0)
                digest.update(row.payloadJson.toByteArray(Charsets.UTF_8))
                digest.update(0)
            }
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }

    /**
     * What a pack-integrity check concluded. UNVERIFIED_LEGACY covers packs
     * built before the hash was recorded: they are usable but their bytes
     * were never checksummed, and that is reported honestly instead of
     * silently treating them as verified.
     */
    enum class PackIntegrity {
        VERIFIED,
        UNVERIFIED_LEGACY,
        CORRUPT
    }

    /**
     * Headroom kept free on the storage volume beyond the pack's own bytes:
     * Room writes journals and the database file itself alongside the rows.
     */
    private val freeSpaceHeadroomBytes: Long = 32L * 1024L * 1024L

    /**
     * Usable bytes on the volume holding the Room database. An internal var
     * (not a const) so tests can simulate a full disk without touching the
     * real filesystem.
     */
    internal var freeSpaceBytesProvider: () -> Long = {
        runCatching {
            val dbPath = db.openHelper.writableDatabase.path
            val dir: File? = if (dbPath != null) File(dbPath).parentFile else null
            dir?.usableSpace ?: Long.MAX_VALUE
        }.getOrDefault(Long.MAX_VALUE)
    }

    internal fun hasEnoughFreeSpace(requiredBytes: Long, freeBytes: Long = freeSpaceBytesProvider()): Boolean =
        freeBytes >= requiredBytes + freeSpaceHeadroomBytes

    fun getPackFlow(profileId: Long): Flow<OfflineMaterialPackEntity?> =
        dao.getPackFlow(profileId)

    suspend fun getPackDirect(profileId: Long): OfflineMaterialPackEntity? =
        dao.getPackDirect(profileId)

    /**
     * True when a pack exists, is READY, matches the current pack version, and
     * still matches the profile's framework and theme. Anything else means the
     * caller must fall back to the bundled catalog (current behavior).
     */
    suspend fun isPackUsableFor(profile: ChildProfileEntity): Boolean {
        val pack = dao.getPackDirect(profile.id) ?: return false
        if (!OfflinePackStatus.isUsable(pack.status, pack.packVersion)) return false
        if (pack.frameworkKey != frameworkKeyFor(profile)) return false
        if (pack.themeWorldId != profile.activeThemeId) return false
        return true
    }

    /** Lessons the pack would contain, without writing anything. */
    fun lessonsForProfile(profile: ChildProfileEntity): List<FullLesson> =
        CurriculumCatalog.getMasterCurriculum(
            themeId = profile.activeThemeId.ifBlank { "dino" },
            country = profile.country
        )

    /** Real size estimate: serializes exactly what would be stored. */
    suspend fun estimatePackSizeBytes(profile: ChildProfileEntity): Long =
        withContext(Dispatchers.Default) {
            var total = 0L
            for (lesson in lessonsForProfile(profile)) {
                currentCoroutineContext().ensureActive()
                total += lessonAdapter.toJson(lesson).toByteArray(Charsets.UTF_8).size
            }
            total
        }

    private fun draftPack(profile: ChildProfileEntity, status: String): OfflineMaterialPackEntity =
        OfflineMaterialPackEntity(
            profileId = profile.id,
            frameworkKey = frameworkKeyFor(profile),
            country = profile.country,
            stateOrProvince = profile.stateOrProvince,
            schoolDistrict = profile.schoolDistrict,
            standardTitle = profile.stateStandard,
            themeWorldId = profile.activeThemeId.ifBlank { "dino" },
            status = status
        )

    /**
     * Materializes the full pack for [profile]. Reports (done, total) as each
     * lesson is serialized; cancellation is honored between lessons and leaves
     * any previously READY pack untouched. All-or-nothing: the pack row and
     * its lesson rows are written in a single transaction, and a SHA-256 over
     * the lesson payloads is recorded so later reads can verify integrity.
     *
     * Hardening:
     * - a free-space pre-check refuses the build with an honest
     *   [InsufficientStorageException] instead of dying mid-write;
     * - the DOWNLOADING mark is a status-only update: unlike the old
     *   REPLACE upsert it never deletes the previous pack's lesson rows, so a
     *   cancelled, failed, or process-killed build can never strand a
     *   DOWNLOADING shell with zero lessons.
     */
    suspend fun downloadPack(
        profile: ChildProfileEntity,
        onProgress: (done: Int, total: Int) -> Unit = { _, _ -> }
    ): Result<OfflineMaterialPackEntity> {
        val estimatedBytes = estimatePackSizeBytes(profile)
        if (!hasEnoughFreeSpace(estimatedBytes)) {
            val free = freeSpaceBytesProvider()
            return Result.failure(
                InsufficientStorageException(
                    "Not enough storage space to build the offline pack: it needs about " +
                        "${formatBytes(estimatedBytes)} but only ${formatBytes(free.coerceAtLeast(0))} " +
                        "is free. Free up space and try again."
                )
            )
        }
        val previous = dao.getPackDirect(profile.id)
        val previousStatus = previous?.status
        // Status-only: the previous pack's lesson rows stay on disk until the
        // new pack is fully built and swapped in by replacePack below.
        dao.insertPackIfMissing(draftPack(profile, OfflinePackStatus.DOWNLOADING.name))
        dao.updatePackStatus(profile.id, OfflinePackStatus.DOWNLOADING.name)
        return try {
            val lessons = lessonsForProfile(profile)
            if (lessons.isEmpty()) {
                throw IllegalStateException("The bundled curriculum library contains no lessons for this framework.")
            }
            val lessonRows = lessons.mapIndexed { index, lesson ->
                currentCoroutineContext().ensureActive()
                val json = lessonAdapter.toJson(lesson)
                onProgress(index + 1, lessons.size)
                OfflineMaterialLessonEntity(
                    profileId = profile.id,
                    lessonId = lesson.id,
                    subjectName = lesson.subject.name,
                    gradeLevelCode = lesson.gradeLevel.code,
                    payloadJson = json,
                    byteSize = json.toByteArray(Charsets.UTF_8).size.toLong()
                )
            }
            currentCoroutineContext().ensureActive()
            val totalBytes = lessonRows.sumOf { it.byteSize }
            val ready = draftPack(profile, OfflinePackStatus.READY.name).copy(
                totalLessons = lessonRows.size,
                totalBytes = totalBytes,
                downloadedAt = System.currentTimeMillis(),
                packVersion = OfflinePackStatus.CURRENT_PACK_VERSION,
                contentSha256 = packSha256Hex(lessonRows)
            )
            dao.replacePack(ready, lessonRows)
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Pack ready for profile ${profile.id}: ${lessonRows.size} lessons, $totalBytes bytes")
            }
            Result.success(ready)
        } catch (ce: CancellationException) {
            // Restore whatever was there before; a cancelled re-download must
            // never destroy a previously READY pack. The restore MUST run in
            // NonCancellable: this coroutine is already cancelled, and Room's
            // suspend DAO calls would otherwise throw immediately without
            // executing. Because the DOWNLOADING mark was status-only, the
            // previous lesson rows are still on disk — only the status flips.
            withContext(NonCancellable) {
                runCatching {
                    if (previousStatus != null) {
                        dao.updatePackStatus(profile.id, previousStatus)
                    } else {
                        dao.deletePackAndLessons(profile.id)
                    }
                }
            }
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Pack download cancelled for profile ${profile.id}")
            }
            throw ce
        } catch (e: Exception) {
            Log.e(TAG, "Pack download failed for profile ${profile.id}", e)
            withContext(NonCancellable) {
                runCatching {
                    if (previousStatus != null) {
                        dao.updatePackStatus(profile.id, previousStatus)
                    } else {
                        dao.updatePackStatus(profile.id, OfflinePackStatus.FAILED.name)
                    }
                }
            }
            Result.failure(e)
        }
    }

    /**
     * Recomputes the SHA-256 over the stored lesson payloads and compares it
     * with the hash recorded when the pack was built.
     */
    suspend fun verifyPackIntegrity(profileId: Long): PackIntegrity = withContext(Dispatchers.Default) {
        val pack = dao.getPackDirect(profileId) ?: return@withContext PackIntegrity.CORRUPT
        val recorded = pack.contentSha256 ?: return@withContext PackIntegrity.UNVERIFIED_LEGACY
        val lessons = dao.getLessonsForProfile(profileId)
        if (lessons.size != pack.totalLessons || lessons.sumOf { it.byteSize } != pack.totalBytes) {
            return@withContext PackIntegrity.CORRUPT
        }
        currentCoroutineContext().ensureActive()
        if (packSha256Hex(lessons) == recorded) PackIntegrity.VERIFIED else PackIntegrity.CORRUPT
    }

    /**
     * Process-death recovery: any pack left in DOWNLOADING never finished its
     * build. Because the DOWNLOADING mark is status-only, the previous pack's
     * lessons are still on disk, so recovery is honest:
     * - rows that form a complete, current-version pack -> restored to READY;
     * - anything else -> the shell is deleted so the UI offers a clean rebuild.
     * Call once at app startup, before packs are served to teaching.
     */
    suspend fun recoverInterruptedPackBuilds() {
        for (pack in dao.getPacksByStatus(OfflinePackStatus.DOWNLOADING.name)) {
            val lessons = dao.getLessonsForProfile(pack.profileId)
            val complete = pack.packVersion == OfflinePackStatus.CURRENT_PACK_VERSION &&
                pack.totalLessons > 0 &&
                lessons.size == pack.totalLessons &&
                lessons.sumOf { it.byteSize } == pack.totalBytes
            if (complete) {
                dao.updatePackStatus(pack.profileId, OfflinePackStatus.READY.name)
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Recovered interrupted pack build for profile ${pack.profileId}: previous pack intact, restored READY")
                }
            } else {
                dao.deletePackAndLessons(pack.profileId)
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Recovered interrupted pack build for profile ${pack.profileId}: incomplete shell deleted")
                }
            }
        }
    }

    /**
     * Deletes the pack and every lesson row for [profileId]. Complete removal:
     * packs live entirely in Room (no files), so nothing is left behind.
     */
    suspend fun deletePack(profileId: Long) {
        dao.deletePackAndLessons(profileId)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Pack deleted for profile $profileId")
        }
    }

    /** Full deserialized lesson list for Socratic/teaching use; empty when unusable. */
    suspend fun getPackLessons(profile: ChildProfileEntity): List<FullLesson> {
        if (!isPackUsableFor(profile)) return emptyList()
        return dao.getLessonsForProfile(profile.id).mapNotNull { row ->
            runCatching { lessonAdapter.fromJson(row.payloadJson) }.getOrNull()
        }
    }

    /** Single lesson from the pack; null when the pack is unusable or lacks it. */
    suspend fun getPackLesson(profile: ChildProfileEntity, lessonId: String): FullLesson? {
        if (!isPackUsableFor(profile)) return null
        val row = dao.getLesson(profile.id, lessonId) ?: return null
        return runCatching { lessonAdapter.fromJson(row.payloadJson) }.getOrNull()
    }

    /**
     * Pack-first lesson list for teaching: the pack's framework-bound copy when
     * usable, otherwise the bundled catalog (today's behavior, already offline).
     */
    suspend fun getTeachingLessons(
        profile: ChildProfileEntity,
        subject: EducationalSubject,
        gradeLevel: GradeLevel
    ): List<FullLesson> {
        if (isPackUsableFor(profile)) {
            val packLessons = dao.getLessonsForProfile(profile.id).mapNotNull { row ->
                runCatching { lessonAdapter.fromJson(row.payloadJson) }.getOrNull()
            }
            if (packLessons.isNotEmpty()) {
                return CurriculumCatalog.getLessonsForSubjectAndGrade(packLessons, subject, gradeLevel)
            }
        }
        return CurriculumCatalog.getLessonsForSubjectAndGrade(
            subject = subject,
            gradeLevel = gradeLevel,
            stateStandardCode = profile.stateStandard,
            themeWorldId = profile.activeThemeId.ifBlank { "dino" },
            country = profile.country
        )
    }

    /**
     * Pack-first single lesson for teaching; null only when neither the pack
     * nor the catalog has it (callers keep today's null-handling).
     */
    suspend fun getTeachingLesson(profile: ChildProfileEntity, lessonId: String): FullLesson? {
        getPackLesson(profile, lessonId)?.let { return it }
        return CurriculumCatalog.getLessonById(
            lessonId,
            profile.activeThemeId.ifBlank { "dino" },
            profile.country
        )
    }

    /**
     * Same unmetered-network pattern as the model-download gate. Pack content
     * is on-device so downloads are never blocked on metered networks; this is
     * exposed for UI copy and for any future network-backed pack content.
     */
    fun isActiveNetworkUnmetered(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
    }
}
