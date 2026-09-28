package com.fourgeailabs.neuropath.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.fourgeailabs.neuropath.data.local.entity.ChildProfileEntity
import com.fourgeailabs.neuropath.data.local.entity.OfflinePackStatus
import com.fourgeailabs.neuropath.data.model.EducationalSubject
import com.fourgeailabs.neuropath.data.model.GradeLevel
import com.fourgeailabs.neuropath.data.repository.NeuroPathRepository
import com.fourgeailabs.neuropath.data.repository.OfflinePackManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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

/**
 * Per-child-profile offline material packs (parent-gated, on-device built):
 * - downloadPack produces a READY pack whose lesson count and byte totals are
 *   the real serialized payload sizes;
 * - pack lessons round-trip through JSON identical to the bundled catalog;
 * - teaching resolves pack-first, falling back to the catalog when stale;
 * - cancelling a re-download restores the previous READY pack (row + lessons);
 * - deletePack removes everything; profile deletion cascades through pack
 *   and lessons (no orphaned child material).
 */
@RunWith(RobolectricTestRunner::class)
class OfflinePackManagerTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var db: AppDatabase
    private lateinit var repo: NeuroPathRepository
    private lateinit var manager: OfflinePackManager

    private fun testProfile() = ChildProfileEntity(
        name = "Test Kid",
        country = "United States",
        stateOrProvince = "Arizona",
        schoolDistrict = "Test District",
        stateStandard = "Arizona Academic Standards",
        activeThemeId = "dino",
        gradeLevel = GradeLevel.KINDERGARTEN.name
    )

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = NeuroPathRepository(db)
        manager = repo.offlinePackManager
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun downloadPack_readyPackHasRealLessonsAndByteTotals() = runBlocking {
        val profileId = db.childProfileDao().insertProfile(testProfile())
        val profile = db.childProfileDao().getProfileDirect(profileId)!!

        val result = manager.downloadPack(profile) { _, _ -> }
        assertTrue(result.isSuccess)

        val pack = db.offlineMaterialPackDao().getPackDirect(profileId)!!
        assertEquals(OfflinePackStatus.READY.name, pack.status)
        assertTrue("pack should contain lessons", pack.totalLessons > 0)
        assertTrue("pack should report real bytes", pack.totalBytes > 0)

        // The pack row's totals must match the actual lesson rows on disk.
        val dao = db.offlineMaterialPackDao()
        assertEquals(dao.getLessonCount(profileId), pack.totalLessons)
        assertEquals(dao.getTotalBytes(profileId), pack.totalBytes)

        // The size estimate must equal what actually gets stored.
        assertEquals(pack.totalBytes, manager.estimatePackSizeBytes(profile))
    }

    @Test
    fun packLesson_serializationRoundTrip_matchesCatalog() = runBlocking {
        val profileId = db.childProfileDao().insertProfile(testProfile())
        val profile = db.childProfileDao().getProfileDirect(profileId)!!
        assertTrue(manager.downloadPack(profile) { _, _ -> }.isSuccess)

        val row = db.offlineMaterialPackDao().getLessonsForProfile(profileId).first()
        val decoded = manager.getPackLesson(profile, row.lessonId)!!
        val catalog = manager.lessonsForProfile(profile).first { it.id == row.lessonId }
        assertEquals("pack lesson must equal the bundled catalog lesson", catalog, decoded)
    }

    @Test
    fun getTeachingLessons_packFirstThenCatalogFallback() = runBlocking {
        val profileId = db.childProfileDao().insertProfile(testProfile())
        val profile = db.childProfileDao().getProfileDirect(profileId)!!

        // No pack yet: falls back to the bundled catalog (today's behavior).
        val fallback = manager.getTeachingLessons(profile, EducationalSubject.MATH, GradeLevel.KINDERGARTEN)
        assertTrue(fallback.isNotEmpty())

        // With a READY pack: resolves from the pack.
        assertTrue(manager.downloadPack(profile) { _, _ -> }.isSuccess)
        assertTrue(manager.isPackUsableFor(profile))
        val fromPack = manager.getTeachingLessons(profile, EducationalSubject.MATH, GradeLevel.KINDERGARTEN)
        assertTrue(fromPack.isNotEmpty())

        // Framework changed: pack is stale -> catalog fallback, never the stale pack.
        val changed = profile.copy(stateStandard = "Some Other Standard")
        assertFalse(manager.isPackUsableFor(changed))
        val afterChange = manager.getTeachingLessons(changed, EducationalSubject.MATH, GradeLevel.KINDERGARTEN)
        assertTrue(afterChange.isNotEmpty())

        // Theme changed: same staleness rule.
        val themeChanged = profile.copy(activeThemeId = "space")
        assertFalse(manager.isPackUsableFor(themeChanged))
    }

    @Test
    fun cancelRedownload_restoresPreviousReadyPackWithLessons() = runBlocking {
        val profileId = db.childProfileDao().insertProfile(testProfile())
        val profile = db.childProfileDao().getProfileDirect(profileId)!!
        assertTrue(manager.downloadPack(profile) { _, _ -> }.isSuccess)

        val dao = db.offlineMaterialPackDao()
        val lessonsBefore = dao.getLessonsForProfile(profileId).map { it.lessonId }.toSet()
        assertTrue(lessonsBefore.isNotEmpty())

        // Re-download, but cancel on the first progress callback (fires while
        // lessons are still being serialized, before anything is written).
        var job: Job? = null
        job = launch {
            manager.downloadPack(profile) { _, _ -> job?.cancel() }
        }
        job.join()

        // The previous READY pack must be fully intact: row AND lessons.
        val pack = dao.getPackDirect(profileId)!!
        assertEquals(OfflinePackStatus.READY.name, pack.status)
        val lessonsAfter = dao.getLessonsForProfile(profileId).map { it.lessonId }.toSet()
        assertEquals(lessonsBefore, lessonsAfter)
        assertTrue(manager.isPackUsableFor(profile))
    }

    @Test
    fun deletePack_removesPackAndAllLessons() = runBlocking {
        val profileId = db.childProfileDao().insertProfile(testProfile())
        val profile = db.childProfileDao().getProfileDirect(profileId)!!
        assertTrue(manager.downloadPack(profile) { _, _ -> }.isSuccess)

        manager.deletePack(profileId)

        val dao = db.offlineMaterialPackDao()
        assertNull(dao.getPackDirect(profileId))
        assertEquals(0, dao.getLessonCount(profileId))
        assertEquals(0L, dao.getTotalBytes(profileId))
    }

    @Test
    fun deleteProfile_cascadesThroughPackAndLessons() = runBlocking {
        val profileId = db.childProfileDao().insertProfile(testProfile())
        val profile = db.childProfileDao().getProfileDirect(profileId)!!
        assertTrue(manager.downloadPack(profile) { _, _ -> }.isSuccess)
        assertTrue(db.offlineMaterialPackDao().getLessonCount(profileId) > 0)

        repo.deleteProfile(profileId)

        // No orphaned child material: pack row and every lesson row are gone.
        assertNull(db.offlineMaterialPackDao().getPackDirect(profileId))
        assertEquals(0, db.offlineMaterialPackDao().getLessonCount(profileId))
    }

    @Test
    fun frameworkKey_isStableAndDistinct() {
        val a = testProfile()
        val b = testProfile().copy(schoolDistrict = "Other District")
        assertEquals(
            OfflinePackManager.frameworkKeyFor(a),
            OfflinePackManager.frameworkKeyFor(a.copy(name = "Different Name"))
        )
        assertFalse(OfflinePackManager.frameworkKeyFor(a) == OfflinePackManager.frameworkKeyFor(b))
    }
}
