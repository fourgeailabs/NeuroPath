package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "child_profiles")
data class ChildProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "Alex",
    val gradeLevel: String = "KINDERGARTEN",
    val stateStandard: String = "CA",
    val activeThemeId: String = "dino",
    val neurodivergentTypesCsv: String = "ADHD,AUTISM_ASD,DYSLEXIA",
    val dyslexiaFontEnabled: Boolean = false,
    val highContrastMode: String = "PASTEL", // PASTEL, TWILIGHT_DARK, BUTTERCREAM, HIGH_CONTRAST, MINT
    val ttsSpeed: Float = 0.9f,
    val ttsVoicePitch: Float = 1.0f,
    val autoHighlightWords: Boolean = true,
    val readAnswersAloud: Boolean = true,
    val ambientSound: String = "OFF", // RAIN, WAVES, FOREST, WHITE_NOISE, CHIMES, OFF
    val totalStars: Int = 18,
    val totalGems: Int = 5,
    val currentStreakDays: Int = 3,
    val currentAvatarId: String = "av_robot",
    val equippedHatId: String? = null,
    val equippedPetId: String? = null,
    val equippedBadgeId: String? = "badge_mindful",
    val unlockedItemIdsCsv: String = "av_robot,badge_mindful",
    val parentPin: String = "1234",
    val dailyGoalMinutes: Int = 15,
    val isCoppaConsented: Boolean = true
)

@Entity(tableName = "lesson_records")
data class LessonRecordEntity(
    @PrimaryKey
    val lessonId: String,
    val subjectId: String,
    val gradeLevel: String,
    val stateStandard: String,
    val title: String,
    val standardCode: String,
    val completed: Boolean = false,
    val scorePercent: Int = 0,
    val attempts: Int = 0,
    val lastAttemptTimestamp: Long = 0L,
    val customThemeId: String? = null
)

@Entity(tableName = "progress_logs")
data class ProgressLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long = 1,
    val subjectId: String,
    val lessonId: String,
    val lessonTitle: String,
    val scorePercent: Int,
    val totalQuestions: Int,
    val correctQuestions: Int,
    val durationSeconds: Int,
    val sensoryBreaksTaken: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sensory_sessions")
data class SensorySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long = 1,
    val activityType: String, // POP_IT, BREATHING, SOUNDSCAPE
    val durationSeconds: Int,
    val countAction: Int, // e.g. bubbles popped or breath cycles
    val timestamp: Long = System.currentTimeMillis()
)
