package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "child_profiles")
data class ChildProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val age: Int = 6,
    val gradeLevel: String = "KINDERGARTEN",
    val ageGroupTier: String = "ELEMENTARY", // ELEMENTARY, MIDDLE_SCHOOL, HIGH_SCHOOL
    val stateStandard: String = "CA",
    val country: String = "United States",
    val stateOrProvince: String = "California",
    val city: String = "Los Angeles",
    val schoolDistrict: String = "Los Angeles Unified School District (LAUSD)",
    val appLanguageCode: String = "en-US",
    val activeThemeId: String = "dino",
    val neurodivergentTypesCsv: String = "ADHD,AUTISM_ASD",
    val strugglesCsv: String = "Focus & Staying On Task, Task Initiation & Procrastination",
    val strengthsCsv: String = "Visual Pattern Recognition, Deep Passion & Hyperfocus",
    val hyperFixationsCsv: String = "Dinosaurs & Prehistoric Safari, Space Exploration & Astronomy",
    val customAccentColorHex: String? = null,
    val dyslexiaFontEnabled: Boolean = false,
    val highContrastMode: String = "PASTEL", // PASTEL, TWILIGHT_DARK, BUTTERCREAM, HIGH_CONTRAST, MINT
    val ttsSpeed: Float = 0.88f,
    val ttsVoicePitch: Float = 1.05f,
    val autoHighlightWords: Boolean = true,
    val readAnswersAloud: Boolean = false, // OFF by default
    val ambientSound: String = "OFF", // RAIN, WAVES, FOREST, WHITE_NOISE, CHIMES, OFF
    val totalStars: Int = 0,
    val totalGems: Int = 0,
    val currentStreakDays: Int = 0,
    val currentAvatarId: String = "av_robot",
    val equippedHatId: String? = null,
    val equippedPetId: String? = null,
    val equippedBadgeId: String? = null,
    val unlockedItemIdsCsv: String = "av_robot",
    val parentPin: String = "",
    val dailyGoalMinutes: Int = 15,
    val isCoppaConsented: Boolean = true,
    val isInitialSetupComplete: Boolean = false,
    val zipOrPostalCodeOverride: String = "",
    val customAiPlatform: String = "Google Gemini",
    val customApiKey: String = ""
)

@Entity(tableName = "downloaded_curriculum")
data class DownloadedCurriculumEntity(
    @PrimaryKey
    val id: String, // e.g. "curriculum_US_California_LAUSD"
    val country: String,
    val stateOrProvince: String,
    val schoolDistrict: String,
    val postalCode: String,
    val standardTitle: String,
    val officialSourceAgency: String, // e.g. "California Department of Education (CDE) / CCSS & NGSS"
    val officialSourceUrl: String,
    val gradesCoveredSummary: String, // "Pre-K through 12th Grade"
    val rawCurriculumJson: String,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val lastSyncDateFormatted: String, // "2026-08-29"
    val syncStatus: String = "UP_TO_DATE"
) {
    val curriculumSummary: String
        get() = "$standardTitle issued by $officialSourceAgency ($gradesCoveredSummary). Core standards summary: ${rawCurriculumJson.take(1500)}"
}

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
