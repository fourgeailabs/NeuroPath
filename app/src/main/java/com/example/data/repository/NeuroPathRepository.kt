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

    val allProfilesFlow: Flow<List<ChildProfileEntity>> = db.childProfileDao().getAllProfilesFlow()
    val allProgressLogs: Flow<List<ProgressLogEntity>> = db.progressLogDao().getAllProgressLogs()
    val allSensorySessions: Flow<List<SensorySessionEntity>> = db.sensorySessionDao().getAllSensorySessions()
    val lessonRecords: Flow<List<LessonRecordEntity>> = db.lessonRecordDao().getAllLessonRecords()
    val latestCurriculumFlow: Flow<com.example.data.local.entity.DownloadedCurriculumEntity?> = db.curriculumDao().getLatestCurriculumFlow()

    suspend fun getLatestCurriculum(): com.example.data.local.entity.DownloadedCurriculumEntity? {
        return db.curriculumDao().getLatestCurriculumDirect()
    }

    suspend fun saveDownloadedCurriculum(curriculum: com.example.data.local.entity.DownloadedCurriculumEntity) {
        db.curriculumDao().insertCurriculum(curriculum)
    }

    fun getProfileFlow(id: Long): Flow<ChildProfileEntity?> {
        return db.childProfileDao().getProfileFlow(id)
    }

    suspend fun getProfileDirect(id: Long): ChildProfileEntity? {
        return db.childProfileDao().getProfileDirect(id)
    }

    suspend fun getAllProfilesDirect(): List<ChildProfileEntity> {
        return db.childProfileDao().getAllProfilesDirect()
    }

    suspend fun getOrCreateProfile(): ChildProfileEntity {
        val existing = db.childProfileDao().getAllProfilesDirect().firstOrNull()
        if (existing != null) return existing

        // Fresh install state - Square one! No pre-installed mock profiles.
        // TTS is strictly OFF by default (readAnswersAloud = false)
        val freshProfile = ChildProfileEntity(
            id = 1,
            name = "",
            age = 6,
            gradeLevel = "KINDERGARTEN",
            ageGroupTier = "ELEMENTARY",
            stateStandard = "CA",
            activeThemeId = "dino",
            neurodivergentTypesCsv = "ADHD,AUTISM_ASD",
            strugglesCsv = "Focus & Staying On Task, Task Initiation & Procrastination",
            strengthsCsv = "Visual Pattern Recognition, Deep Passion & Hyperfocus",
            hyperFixationsCsv = "Dinosaurs & Prehistoric Safari, Space Exploration & Astronomy",
            totalStars = 0,
            totalGems = 0,
            currentStreakDays = 0,
            currentAvatarId = "av_robot",
            unlockedItemIdsCsv = "av_robot",
            appLanguageCode = "en-US",
            country = "United States",
            stateOrProvince = "California",
            city = "Los Angeles",
            schoolDistrict = "Los Angeles Unified School District (LAUSD)",
            readAnswersAloud = false, // OFF by default
            isInitialSetupComplete = false // Forces fresh setup screen on first launch
        )
        val newId = db.childProfileDao().insertOrUpdateProfile(freshProfile)
        return freshProfile.copy(id = if (newId > 0) newId else 1)
    }

    suspend fun insertProfile(profile: ChildProfileEntity): Long {
        return db.childProfileDao().insertProfile(profile)
    }

    suspend fun updateProfile(profile: ChildProfileEntity) {
        db.childProfileDao().updateProfile(profile)
    }

    suspend fun deleteProfile(id: Long) {
        db.childProfileDao().deleteProfileById(id)
    }

    suspend fun updateParentPinForAll(pin: String) {
        db.childProfileDao().updateParentPinForAll(pin)
    }

    suspend fun awardStarsAndGems(profileId: Long, stars: Int, gems: Int) {
        val profile = getProfileDirect(profileId) ?: return
        val updated = profile.copy(
            totalStars = profile.totalStars + stars,
            totalGems = profile.totalGems + gems
        )
        db.childProfileDao().updateProfile(updated)
    }

    suspend fun unlockItem(profileId: Long, itemId: String, starCost: Int, gemCost: Int): Boolean {
        val profile = getProfileDirect(profileId) ?: return false
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

    suspend fun equipItem(profileId: Long, category: String, itemId: String) {
        val profile = getProfileDirect(profileId) ?: return
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
        profileId: Long,
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
                profileId = profileId,
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
        awardStarsAndGems(profileId, starsToAward, gemsToAward)
    }

    suspend fun logSensorySession(profileId: Long, type: String, durationSeconds: Int, countAction: Int) {
        db.sensorySessionDao().insertSensorySession(
            SensorySessionEntity(
                profileId = profileId,
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
