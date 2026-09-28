package com.fourgeailabs.neuropath.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.fourgeailabs.neuropath.data.repository.OfflinePackManager.PackIntegrity
import com.fourgeailabs.neuropath.data.local.entity.ChildProfileEntity
import com.fourgeailabs.neuropath.data.local.entity.OfflineMaterialPackEntity
import com.fourgeailabs.neuropath.data.local.entity.OfflinePackStatus
import com.fourgeailabs.neuropath.data.model.GradeLevel
import com.fourgeailabs.neuropath.data.repository.InsufficientStorageException
import com.fourgeailabs.neuropath.data.repository.NeuroPathRepository
import com.fourgeailabs.neuropath.data.repository.OfflinePackManager
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Download hardening for [OfflinePackManager]:
 * - the free-space pre-check refuses a build with an honest
 *   [InsufficientStorageException] instead of dying mid-write;
 * - every finished pack records a SHA-256 over its lesson payloads and
 *   [OfflinePackManager.verifyPackIntegrity] detects tampering;
 * - [OfflinePackManager.recoverInterruptedPackBuilds] cleans up packs left in
 *   DOWNLOADING by a process death: complete packs are restored to READY,
 *   incomplete shells are deleted — never a half-state.
 */
@RunWith(RobolectricTestRunner::class)
class PackHardeningTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var db: AppDatabase
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
        manager = NeuroPathRepository(db).offlinePackManager
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun profile() = runBlocking {
        val id = db.childProfileDao().insertProfile(testProfile())
        db.childProfileDao().getProfileDirect(id)!!
    }

    @Test
    fun downloadPack_recordsSha256_verifyPasses() = runBlocking {
        val p = profile()
        val result = manager.downloadPack(p) { _, _ -> }
        assertTrue(result.isSuccess)

        val pack = db.offlineMaterialPackDao().getPackDirect(p.id)!!
        assertNotNull("finished pack must record its payload hash", pack.contentSha256)
        assertEquals(PackIntegrity.VERIFIED, manager.verifyPackIntegrity(p.id))
    }

    @Test
    fun verifyPackIntegrity_detectsTamperedLesson() = runBlocking {
        val p = profile()
        assertTrue(manager.downloadPack(p) { _, _ -> }.isSuccess)

        // Corrupt one stored lesson payload in place.
        val dao = db.offlineMaterialPackDao()
        val row = dao.getLessonsForProfile(p.id).first()
        dao.insertLessons(listOf(row.copy(payloadJson = row.payloadJson + "tampered")))

        assertEquals(PackIntegrity.CORRUPT, manager.verifyPackIntegrity(p.id))
    }

    @Test
    fun verifyPackIntegrity_legacyNullHash_isUnverifiedNotCorrupt() = runBlocking {
        val p = profile()
        assertTrue(manager.downloadPack(p) { _, _ -> }.isSuccess)

        // Simulate a pack built before the hash existed.
        db.openHelper.writableDatabase.execSQL(
            "UPDATE offline_material_packs SET contentSha256 = NULL WHERE profileId = ${p.id}"
        )

        assertEquals(PackIntegrity.UNVERIFIED_LEGACY, manager.verifyPackIntegrity(p.id))
    }

    @Test
    fun verifyPackIntegrity_missingPack_isCorrupt() = runBlocking {
        assertEquals(PackIntegrity.CORRUPT, manager.verifyPackIntegrity(999_999L))
    }

    @Test
    fun downloadPack_fullDisk_failsHonestlyWithoutWriting() = runBlocking {
        val p = profile()
        manager.freeSpaceBytesProvider = { 0L }

        val result = manager.downloadPack(p) { _, _ -> }

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue("must be the honest storage error, was $error", error is InsufficientStorageException)
        assertTrue(
            "message must carry the real numbers, was: ${error?.message}",
            error?.message?.contains("Not enough storage space") == true
        )
        // Nothing half-written: no pack shell left behind.
        assertNull(db.offlineMaterialPackDao().getPackDirect(p.id))
    }

    @Test
    fun hasEnoughFreeSpace_respectsHeadroom() {
        assertTrue(manager.hasEnoughFreeSpace(requiredBytes = 1_000L, freeBytes = 100L * 1024L * 1024L))
        assertFalse(manager.hasEnoughFreeSpace(requiredBytes = 1_000L, freeBytes = 1_000L))
    }

    @Test
    fun recoverInterruptedBuilds_completePack_restoredReady() = runBlocking {
        val p = profile()
        assertTrue(manager.downloadPack(p) { _, _ -> }.isSuccess)

        // Simulate process death mid re-download: status stuck at DOWNLOADING
        // while the previous pack's lessons are still on disk (status-only mark).
        val dao = db.offlineMaterialPackDao()
        dao.updatePackStatus(p.id, OfflinePackStatus.DOWNLOADING.name)

        manager.recoverInterruptedPackBuilds()

        val pack = dao.getPackDirect(p.id)!!
        assertEquals(OfflinePackStatus.READY.name, pack.status)
        assertTrue(manager.isPackUsableFor(p))
    }

    @Test
    fun recoverInterruptedBuilds_incompleteShell_isDeleted() = runBlocking {
        val p = profile()
        val dao = db.offlineMaterialPackDao()
        // Simulate process death during a first-ever build: DOWNLOADING shell,
        // zero lessons.
        dao.upsertPack(
            OfflineMaterialPackEntity(
                profileId = p.id,
                frameworkKey = "k",
                country = "c",
                stateOrProvince = "s",
                schoolDistrict = "d",
                standardTitle = "std",
                themeWorldId = "dino",
                status = OfflinePackStatus.DOWNLOADING.name
            )
        )

        manager.recoverInterruptedPackBuilds()

        assertNull("incomplete shell must be removed, not left half-built", dao.getPackDirect(p.id))
    }

    @Test
    fun recoverInterruptedBuilds_partialLessons_shellIsDeleted() = runBlocking {
        val p = profile()
        assertTrue(manager.downloadPack(p) { _, _ -> }.isSuccess)
        val dao = db.offlineMaterialPackDao()

        // Simulate death after some (not all) new lessons landed: shrink the
        // stored rows so the pack no longer matches its recorded totals.
        val rows = dao.getLessonsForProfile(p.id)
        assertTrue(rows.size > 1)
        dao.deleteLessonsForProfile(p.id)
        dao.insertLessons(rows.take(1))
        dao.updatePackStatus(p.id, OfflinePackStatus.DOWNLOADING.name)

        manager.recoverInterruptedPackBuilds()

        assertNull("partial pack must never be served as complete", dao.getPackDirect(p.id))
        assertEquals(0, dao.getLessonCount(p.id))
    }
}
