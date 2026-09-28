package com.fourgeailabs.neuropath.data.repository

import androidx.room.withTransaction
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
import com.fourgeailabs.neuropath.data.local.entity.RewardGrantEntity
import com.fourgeailabs.neuropath.data.local.entity.SensorySessionEntity
import com.fourgeailabs.neuropath.data.model.EducationalSubject
import com.fourgeailabs.neuropath.data.model.FullLesson
import com.fourgeailabs.neuropath.data.model.GradeLevel
import kotlinx.coroutines.flow.Flow

class NeuroPathRepository(private val db: AppDatabase) {

    companion object {
        /**
         * Retention cap: how many non-bookmarked chat messages are kept per child.
         * Bookmarked messages are the parent's explicit "keep this" signal and are
         * never pruned. Enforced in [saveChatMessage] after every insert.
         */
        const val MAX_STORED_CHAT_MESSAGES_PER_CHILD = 1000
    }

    val oerCurriculumService: OerCommonsCurriculumService = OerCommonsCurriculumService(db)

    /**
     * Per-child-profile offline material packs: the full built-in lesson
     * library for the child's framework, materialized on-device (see
     * [OfflinePackManager] for the honesty contract — no network transfer).
     */
    val offlinePackManager: OfflinePackManager = OfflinePackManager(db)

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

    /**
     * How a child profile's data is treated on deletion. HARD_DELETE removes the
     * profile row and — via ON DELETE CASCADE foreign keys plus
     * [LearnerPersonalizationEngine.clearProfileData] (called by the ViewModel) —
     * every child-owned row and preference. ANONYMIZE is reserved for a future
     * keep-the-progress-without-the-identity mode and is not implemented yet.
     */
    enum class ProfileDeletionMode {
        HARD_DELETE,
        ANONYMIZE
    }

    suspend fun deleteProfile(id: Long, mode: ProfileDeletionMode = ProfileDeletionMode.HARD_DELETE) {
        when (mode) {
            ProfileDeletionMode.HARD_DELETE -> db.childProfileDao().deleteProfileById(id)
            // The SQLite ON DELETE CASCADE constraints on progress_logs, sensory_sessions
            // and chat_messages remove the child's rows atomically with the profile row.
            ProfileDeletionMode.ANONYMIZE -> TODO("Anonymize-instead profile deletion is not implemented yet")
        }
    }

    suspend fun updateParentPinForAll(pin: String) {
        db.childProfileDao().updateParentPinForAll(pin)
    }

    suspend fun clearAllCustomApiKeys() {
        db.childProfileDao().clearAllCustomApiKeys()
    }

    /**
     * Adds stars/gems with a single atomic UPDATE (no read-modify-write race).
     * When [grantKey] is supplied the award is idempotent: a retried or
     * duplicated call carrying the same key awards exactly once.
     */
    suspend fun awardStarsAndGems(profileId: Long, stars: Int, gems: Int, grantKey: String? = null) {
        if (grantKey != null) {
            db.rewardGrantDao().grantOnce(
                RewardGrantEntity(
                    grantKey = grantKey,
                    profileId = profileId,
                    stars = stars,
                    gems = gems,
                    reason = "manual_award"
                )
            )
        } else {
            db.childProfileDao().addStarsAndGems(profileId, stars, gems)
        }
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

    /**
     * Records a lesson completion and its stars/gems reward as one atomic unit:
     * the progress log, the lesson record (attempt counter), and the reward
     * grant are written in a single Room transaction, so a crash or retry can
     * never leave a recorded attempt without its reward or a reward without
     * its attempt — and concurrent completions serialize instead of
     * double-counting the same attempt.
     *
     * The reward is idempotent per attempt: pass a stable [idempotencyKey] for
     * the completion event and retries of that same event award exactly once.
     * Without one, the key falls back to "lesson_completion:<lessonId>:attempt:<n>"
     * (one grant per recorded attempt).
     */
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
        standardCode: String,
        idempotencyKey: String? = null
    ) {
        db.withTransaction {
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
            val attempt = (existingRecord?.attempts ?: 0) + 1
            val newRecord = LessonRecordEntity(
                lessonId = lessonId,
                subjectId = subjectId,
                gradeLevel = gradeLevel,
                stateStandard = stateStandard,
                title = lessonTitle,
                standardCode = standardCode,
                completed = scorePercent >= 60,
                scorePercent = maxOf(scorePercent, existingRecord?.scorePercent ?: 0),
                attempts = attempt,
                lastAttemptTimestamp = System.currentTimeMillis()
            )
            db.lessonRecordDao().insertLessonRecord(newRecord)

            val starsToAward = when {
                scorePercent >= 90 -> 5
                scorePercent >= 70 -> 3
                else -> 2
            }
            val gemsToAward = if (scorePercent >= 80) 1 else 0
            val grantKey = idempotencyKey ?: "lesson_completion:$lessonId:attempt:$attempt"
            db.rewardGrantDao().grantOnce(
                RewardGrantEntity(
                    grantKey = grantKey,
                    profileId = profileId,
                    stars = starsToAward,
                    gems = gemsToAward,
                    reason = "lesson_completion"
                )
            )
        }
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

    /**
     * Pack-first lesson list for teaching: the profile's offline pack when it
     * is READY and still matches the profile's framework/theme, otherwise the
     * bundled catalog (today's behavior — already fully offline).
     */
    suspend fun getTeachingLessons(
        profile: com.fourgeailabs.neuropath.data.local.entity.ChildProfileEntity,
        subject: EducationalSubject,
        gradeLevel: GradeLevel
    ): List<FullLesson> = offlinePackManager.getTeachingLessons(profile, subject, gradeLevel)

    /**
     * Pack-first single lesson for teaching; null only when neither the pack
     * nor the catalog has it.
     */
    suspend fun getTeachingLesson(
        profile: com.fourgeailabs.neuropath.data.local.entity.ChildProfileEntity,
        lessonId: String
    ): FullLesson? = offlinePackManager.getTeachingLesson(profile, lessonId)

    fun getOfflinePackFlow(profileId: Long): kotlinx.coroutines.flow.Flow<com.fourgeailabs.neuropath.data.local.entity.OfflineMaterialPackEntity?> =
        offlinePackManager.getPackFlow(profileId)

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
        val id = db.chatMessageDao().insertMessage(message)
        // Retention cap: chat history must not grow without bound per child.
        db.chatMessageDao().pruneOldMessages(message.profileId, MAX_STORED_CHAT_MESSAGES_PER_CHILD)
        return id
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
