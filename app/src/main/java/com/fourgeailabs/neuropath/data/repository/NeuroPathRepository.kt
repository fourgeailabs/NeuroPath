package com.fourgeailabs.neuropath.data.repository

import com.fourgeailabs.neuropath.data.curriculum.CurriculumCatalog
import com.fourgeailabs.neuropath.data.curriculum.oer.OerCommonsCurriculumItem
import com.fourgeailabs.neuropath.data.curriculum.oer.OerCommonsCurriculumService
import com.fourgeailabs.neuropath.data.curriculum.oer.OerSyncResult
import com.fourgeailabs.neuropath.data.curriculum.oer.OerTutorCurriculumContext
import com.fourgeailabs.neuropath.data.local.AppDatabase
import com.fourgeailabs.neuropath.data.local.entity.ChildProfileEntity
import com.fourgeailabs.neuropath.data.local.entity.LessonRecordEntity
import com.fourgeailabs.neuropath.data.local.entity.OerCurriculumEntity
import com.fourgeailabs.neuropath.data.local.entity.ProgressLogEntity
import com.fourgeailabs.neuropath.data.local.entity.SensorySessionEntity
import com.fourgeailabs.neuropath.data.model.EducationalSubject
import com.fourgeailabs.neuropath.data.model.FullLesson
import com.fourgeailabs.neuropath.data.model.GradeLevel
import kotlinx.coroutines.flow.Flow

class NeuroPathRepository(private val db: AppDatabase) {

    val oerCurriculumService: OerCommonsCurriculumService = OerCommonsCurriculumService(db)

    val allProfilesFlow: Flow<List<ChildProfileEntity>> = db.childProfileDao().getAllProfilesFlow()
    val allProgressLogs: Flow<List<ProgressLogEntity>> = db.progressLogDao().getAllProgressLogs()
    val allSensorySessions: Flow<List<SensorySessionEntity>> = db.sensorySessionDao().getAllSensorySessions()
    val lessonRecords: Flow<List<LessonRecordEntity>> = db.lessonRecordDao().getAllLessonRecords()
    val latestCurriculumFlow: Flow<com.fourgeailabs.neuropath.data.local.entity.DownloadedCurriculumEntity?> = db.curriculumDao().getLatestCurriculumFlow()
    val oerCurriculumUnitsFlow: Flow<List<OerCurriculumEntity>> = db.oerCurriculumDao().getAllCurriculumUnitsFlow()

    suspend fun initializeOerCurriculum(): List<OerCommonsCurriculumItem> {
        return oerCurriculumService.initializeAndSeed()
    }

    suspend fun verifyOerCurriculumLibrary(): OerSyncResult {
        return oerCurriculumService.verifyLocalCatalog()
    }

    suspend fun searchOerCurriculum(
        query: String,
        subject: EducationalSubject? = null,
        gradeLevel: GradeLevel? = null
    ): List<OerCommonsCurriculumItem> {
        return oerCurriculumService.searchCurriculum(query, subject, gradeLevel)
    }

    suspend fun retrieveOerTutorContext(
        query: String,
        studentGrade: GradeLevel,
        studentSubject: EducationalSubject? = null,
        schoolDistrict: String = "",
        country: String = "",
        stateOrProvince: String = ""
    ): OerTutorCurriculumContext {
        return oerCurriculumService.retrieveCurriculumContextForTutor(
            query = query,
            studentGrade = studentGrade,
            studentSubject = studentSubject,
            schoolDistrict = schoolDistrict,
            country = country,
            stateOrProvince = stateOrProvince
        )
    }

    suspend fun getLatestCurriculum(): com.fourgeailabs.neuropath.data.local.entity.DownloadedCurriculumEntity? {
        return db.curriculumDao().getLatestCurriculumDirect()
    }

    suspend fun saveDownloadedCurriculum(curriculum: com.fourgeailabs.neuropath.data.local.entity.DownloadedCurriculumEntity) {
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

        // A brand-new profile starts blank: no assumed location, district, diagnoses,
        // strengths, or interests. The setup flow fills these in from the parent.
        val newId = db.childProfileDao().insertOrUpdateProfile(ChildProfileEntity())
        val insertedId = if (newId > 0) newId else 1L
        return db.childProfileDao().getProfileDirect(insertedId) ?: ChildProfileEntity(id = insertedId)
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

    suspend fun clearAllCustomApiKeys() {
        db.childProfileDao().clearAllCustomApiKeys()
    }

    suspend fun awardStarsAndGems(profileId: Long, stars: Int, gems: Int) {
        db.childProfileDao().addStarsAndGems(profileId, stars, gems)
    }

    suspend fun unlockItem(profileId: Long, itemId: String, starCost: Int, gemCost: Int): Boolean {
        return db.childProfileDao().tryUnlockItem(profileId, itemId, starCost, gemCost) > 0
    }

    suspend fun updateStreak(profileId: Long, streakDays: Int, lastActiveDate: String) {
        db.childProfileDao().updateStreak(profileId, streakDays, lastActiveDate)
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

    fun getChatMessagesForSessionFlow(profileId: Long, sessionId: String): Flow<List<com.fourgeailabs.neuropath.data.local.entity.ChatMessageEntity>> {
        return db.chatMessageDao().getMessagesForSession(profileId, sessionId)
    }

    fun getAllChatMessagesFlow(profileId: Long): Flow<List<com.fourgeailabs.neuropath.data.local.entity.ChatMessageEntity>> {
        return db.chatMessageDao().getAllMessagesForProfile(profileId)
    }

    fun getBookmarkedChatMessagesFlow(profileId: Long): Flow<List<com.fourgeailabs.neuropath.data.local.entity.ChatMessageEntity>> {
        return db.chatMessageDao().getBookmarkedMessages(profileId)
    }

    fun searchChatMessagesFlow(profileId: Long, query: String): Flow<List<com.fourgeailabs.neuropath.data.local.entity.ChatMessageEntity>> {
        return db.chatMessageDao().searchMessages(profileId, query)
    }

    fun getChatSessionSummariesFlow(profileId: Long): Flow<List<com.fourgeailabs.neuropath.data.local.ChatSessionSummary>> {
        return db.chatMessageDao().getSessionSummaries(profileId)
    }

    suspend fun saveChatMessage(message: com.fourgeailabs.neuropath.data.local.entity.ChatMessageEntity): Long {
        return db.chatMessageDao().insertMessage(message)
    }

    suspend fun toggleChatBookmark(messageId: Long, isBookmarked: Boolean) {
        db.chatMessageDao().updateBookmark(messageId, isBookmarked)
    }

    suspend fun deleteChatSession(profileId: Long, sessionId: String) {
        db.chatMessageDao().deleteSession(profileId, sessionId)
    }

    suspend fun clearAllChatHistory(profileId: Long) {
        db.chatMessageDao().clearAllMessages(profileId)
    }
}
