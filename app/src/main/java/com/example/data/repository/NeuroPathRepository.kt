package com.example.data.repository

import com.example.data.curriculum.CurriculumCatalog
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ChildProfileEntity
import com.example.data.local.entity.LessonRecordEntity
import com.example.data.local.entity.ProgressLogEntity
import com.example.data.local.entity.SensorySessionEntity
import com.example.data.model.EducationalSubject
import com.example.data.model.FullLesson
import com.example.data.model.GradeLevel
import kotlinx.coroutines.flow.Flow

class NeuroPathRepository(private val db: AppDatabase) {

    val profileFlow: Flow<ChildProfileEntity?> = db.childProfileDao().getProfileFlow(1)
    val allProgressLogs: Flow<List<ProgressLogEntity>> = db.progressLogDao().getAllProgressLogs()
    val allSensorySessions: Flow<List<SensorySessionEntity>> = db.sensorySessionDao().getAllSensorySessions()
    val lessonRecords: Flow<List<LessonRecordEntity>> = db.lessonRecordDao().getAllLessonRecords()

    suspend fun getOrCreateProfile(): ChildProfileEntity {
        val existing = db.childProfileDao().getProfileDirect(1)
        if (existing != null) return existing

        val defaultProfile = ChildProfileEntity(
            id = 1,
            name = "Alex",
            gradeLevel = "KINDERGARTEN",
            stateStandard = "CA",
            activeThemeId = "dino",
            neurodivergentTypesCsv = "ADHD,AUTISM_ASD,DYSLEXIA,SENSORY_SENSITIVITY",
            totalStars = 20,
            totalGems = 6,
            currentStreakDays = 4,
            currentAvatarId = "av_robot",
            unlockedItemIdsCsv = "av_robot,badge_mindful"
        )
        db.childProfileDao().insertOrUpdateProfile(defaultProfile)
        return defaultProfile
    }

    suspend fun updateProfile(profile: ChildProfileEntity) {
        db.childProfileDao().updateProfile(profile)
    }

    suspend fun awardStarsAndGems(stars: Int, gems: Int) {
        val profile = getOrCreateProfile()
        val updated = profile.copy(
            totalStars = profile.totalStars + stars,
            totalGems = profile.totalGems + gems
        )
        db.childProfileDao().updateProfile(updated)
    }

    suspend fun unlockItem(itemId: String, starCost: Int, gemCost: Int): Boolean {
        val profile = getOrCreateProfile()
        if (profile.totalStars < starCost || profile.totalGems < gemCost) return false
        val currentItems = profile.unlockedItemIdsCsv.split(",").filter { it.isNotBlank() }.toMutableSet()
        currentItems.add(itemId)
        val updated = profile.copy(
            totalStars = profile.totalStars - starCost,
            totalGems = profile.totalGems - gemCost,
            unlockedItemIdsCsv = currentItems.joinToString(",")
        )
        db.childProfileDao().updateProfile(updated)
        return true
    }

    suspend fun equipItem(category: String, itemId: String) {
        val profile = getOrCreateProfile()
        val updated = when (category) {
            "AVATAR" -> profile.copy(currentAvatarId = itemId)
            "HAT" -> profile.copy(equippedHatId = if (profile.equippedHatId == itemId) null else itemId)
            "PET" -> profile.copy(equippedPetId = if (profile.equippedPetId == itemId) null else itemId)
            "BADGE" -> profile.copy(equippedBadgeId = if (profile.equippedBadgeId == itemId) null else itemId)
            else -> profile
        }
        db.childProfileDao().updateProfile(updated)
    }

    suspend fun recordLessonCompletion(
        lessonId: String,
        subjectId: String,
        lessonTitle: String,
        scorePercent: Int,
        totalQuestions: Int,
        correctQuestions: Int,
        durationSeconds: Int,
        sensoryBreaksCount: Int,
        gradeLevel: String,
        stateStandard: String,
        standardCode: String
    ) {
        // Record in progress log
        db.progressLogDao().insertLog(
            ProgressLogEntity(
                subjectId = subjectId,
                lessonId = lessonId,
                lessonTitle = lessonTitle,
                scorePercent = scorePercent,
                totalQuestions = totalQuestions,
                correctQuestions = correctQuestions,
                durationSeconds = durationSeconds,
                sensoryBreaksTaken = sensoryBreaksCount
            )
        )

        // Update or insert lesson record
        val existingRecord = db.lessonRecordDao().getLessonRecord(lessonId)
        val newRecord = LessonRecordEntity(
            lessonId = lessonId,
            subjectId = subjectId,
            gradeLevel = gradeLevel,
            stateStandard = stateStandard,
            title = lessonTitle,
            standardCode = standardCode,
            completed = scorePercent >= 60,
            scorePercent = maxOf(scorePercent, existingRecord?.scorePercent ?: 0),
            attempts = (existingRecord?.attempts ?: 0) + 1,
            lastAttemptTimestamp = System.currentTimeMillis()
        )
        db.lessonRecordDao().insertLessonRecord(newRecord)

        // Award stars based on score
        val starsToAward = when {
            scorePercent >= 90 -> 5
            scorePercent >= 70 -> 3
            else -> 2
        }
        val gemsToAward = if (scorePercent >= 80) 1 else 0
        awardStarsAndGems(starsToAward, gemsToAward)
    }

    suspend fun logSensorySession(type: String, durationSeconds: Int, countAction: Int) {
        db.sensorySessionDao().insertSensorySession(
            SensorySessionEntity(
                activityType = type,
                durationSeconds = durationSeconds,
                countAction = countAction
            )
        )
    }

    fun getLesson(id: String, themeId: String): FullLesson? {
        return CurriculumCatalog.getLessonById(id, themeId)
    }

    fun getLessonsForSubject(subject: EducationalSubject, gradeLevel: GradeLevel, state: String, themeId: String): List<FullLesson> {
        return CurriculumCatalog.getLessonsForSubjectAndGrade(subject, gradeLevel, state, themeId)
    }
}
