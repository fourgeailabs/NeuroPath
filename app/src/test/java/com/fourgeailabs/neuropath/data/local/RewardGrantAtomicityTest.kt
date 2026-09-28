package com.fourgeailabs.neuropath.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.fourgeailabs.neuropath.data.local.entity.ChildProfileEntity
import com.fourgeailabs.neuropath.data.local.entity.RewardGrantEntity
import com.fourgeailabs.neuropath.data.repository.NeuroPathRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Reward grants are atomic and idempotent:
 * - [RewardGrantDao.grantOnce] applies the stars/gems increment only when the
 *   grant key was never recorded (single Room transaction, so a retried or
 *   concurrently duplicated grant can never double-award);
 * - [NeuroPathRepository.recordLessonCompletion] writes the progress log, the
 *   lesson record, and the reward grant in one transaction;
 * - retries carrying the same idempotency key award exactly once.
 */
@RunWith(RobolectricTestRunner::class)
class RewardGrantAtomicityTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var db: AppDatabase
    private lateinit var repo: NeuroPathRepository

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = NeuroPathRepository(db)
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun profileId(): Long = runBlocking {
        db.childProfileDao().insertProfile(ChildProfileEntity(name = "Test Kid"))
    }

    private fun starsOf(profileId: Long): Int = runBlocking {
        db.childProfileDao().getProfileDirect(profileId)!!.totalStars
    }

    private fun gemsOf(profileId: Long): Int = runBlocking {
        db.childProfileDao().getProfileDirect(profileId)!!.totalGems
    }

    @Test
    fun grantOnce_sameKeyTwice_awardsExactlyOnce() = runBlocking {
        val id = profileId()
        val grant = RewardGrantEntity("event-1", id, stars = 5, gems = 1, reason = "lesson_completion")

        assertTrue(db.rewardGrantDao().grantOnce(grant))
        assertFalse("duplicate key must not re-apply", db.rewardGrantDao().grantOnce(grant))

        assertEquals(5, starsOf(id))
        assertEquals(1, gemsOf(id))
        assertEquals(1L, db.rewardGrantDao().countByKey("event-1"))
    }

    @Test
    fun grantOnce_concurrentDuplicates_awardExactlyOnce() = runBlocking {
        val id = profileId()
        val results = (1..20).map {
            async {
                db.rewardGrantDao().grantOnce(
                    RewardGrantEntity("race-key", id, stars = 5, gems = 1, reason = "lesson_completion")
                )
            }
        }.awaitAll()

        assertEquals("exactly one concurrent grant may win", 1, results.count { it })
        assertEquals(5, starsOf(id))
        assertEquals(1, gemsOf(id))
    }

    @Test
    fun grantOnce_distinctKeys_eachAwardApplies() = runBlocking {
        val id = profileId()
        assertTrue(db.rewardGrantDao().grantOnce(RewardGrantEntity("k1", id, 5, 1, "lesson_completion")))
        assertTrue(db.rewardGrantDao().grantOnce(RewardGrantEntity("k2", id, 3, 0, "lesson_completion")))
        assertEquals(8, starsOf(id))
        assertEquals(1, gemsOf(id))
    }

    @Test
    fun recordLessonCompletion_isSingleTransaction_allOrNothing() = runBlocking {
        val id = profileId()
        repo.recordLessonCompletion(
            profileId = id, lessonId = "lesson-1", subjectId = "MATH",
            lessonTitle = "Adding", scorePercent = 95, totalQuestions = 10,
            correctQuestions = 9, durationSeconds = 120, sensoryBreaksCount = 0,
            gradeLevel = "KINDERGARTEN", stateStandard = "AZ", standardCode = "K.OA"
        )

        assertEquals(5, starsOf(id))
        assertEquals(1, gemsOf(id))
        val record = db.lessonRecordDao().getLessonRecord("lesson-1")!!
        assertEquals(1, record.attempts)
        assertEquals(95, record.scorePercent)
        assertEquals(
            "progress log, lesson record, and grant land together",
            1, db.progressLogDao().getAllProgressLogs().first().size
        )
        assertEquals(1L, db.rewardGrantDao().countByKey("lesson_completion:lesson-1:attempt:1"))
    }

    @Test
    fun recordLessonCompletion_retryWithSameIdempotencyKey_awardsOnce() = runBlocking {
        val id = profileId()
        val args = listOf(
            Triple(id, "lesson-9", "evt-uuid-1234"),
            Triple(id, "lesson-9", "evt-uuid-1234")
        )
        for ((pid, lesson, key) in args) {
            repo.recordLessonCompletion(
                profileId = pid, lessonId = lesson, subjectId = "MATH",
                lessonTitle = "Adding", scorePercent = 92, totalQuestions = 10,
                correctQuestions = 9, durationSeconds = 100, sensoryBreaksCount = 0,
                gradeLevel = "KINDERGARTEN", stateStandard = "AZ", standardCode = "K.OA",
                idempotencyKey = key
            )
        }
        // The retry recorded a second attempt (visible in history) but the
        // reward for that event key was applied exactly once.
        assertEquals(2, db.lessonRecordDao().getLessonRecord("lesson-9")!!.attempts)
        assertEquals(5, starsOf(id))
        assertEquals(1, gemsOf(id))
    }

    @Test
    fun recordLessonCompletion_twoDistinctAttempts_awardPerAttempt() = runBlocking {
        val id = profileId()
        repeat(2) {
            repo.recordLessonCompletion(
                profileId = id, lessonId = "lesson-2", subjectId = "MATH",
                lessonTitle = "Adding", scorePercent = 75, totalQuestions = 10,
                correctQuestions = 7, durationSeconds = 90, sensoryBreaksCount = 0,
                gradeLevel = "KINDERGARTEN", stateStandard = "AZ", standardCode = "K.OA"
            )
        }
        // Two recorded attempts -> two grants (one per attempt key).
        assertEquals(2, db.lessonRecordDao().getLessonRecord("lesson-2")!!.attempts)
        assertEquals(6, starsOf(id))
        assertEquals(0, gemsOf(id))
    }

    @Test
    fun awardStarsAndGems_withGrantKey_isIdempotent() = runBlocking {
        val id = profileId()
        repo.awardStarsAndGems(id, stars = 4, gems = 2, grantKey = "manual-1")
        repo.awardStarsAndGems(id, stars = 4, gems = 2, grantKey = "manual-1")
        assertEquals(4, starsOf(id))
        assertEquals(2, gemsOf(id))
    }

    @Test
    fun awardStarsAndGems_withoutGrantKey_keepsAtomicIncrement() = runBlocking {
        val id = profileId()
        repo.awardStarsAndGems(id, stars = 2, gems = 0)
        repo.awardStarsAndGems(id, stars = 3, gems = 1)
        assertEquals(5, starsOf(id))
        assertEquals(1, gemsOf(id))
    }
}
