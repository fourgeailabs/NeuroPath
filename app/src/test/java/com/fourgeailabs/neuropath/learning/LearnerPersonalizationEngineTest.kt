package com.fourgeailabs.neuropath.learning

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LearnerPersonalizationEngineTest {
    
    private val context: Context get() = ApplicationProvider.getApplicationContext<Context>()
    private val testProfileId = 999999L
    
    @Test
    fun recordAnswer_tracksAttemptsAndCorrectness() = runBlocking {
        // Reset any existing data
        val prefs = context.getSharedPreferences("learner_personalization", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        
        // Record some answers
        LearnerPersonalizationEngine.recordAnswer(context, testProfileId, "MATH", true, "addition")
        LearnerPersonalizationEngine.recordAnswer(context, testProfileId, "MATH", true, "addition")
        LearnerPersonalizationEngine.recordAnswer(context, testProfileId, "MATH", false, "subtraction")
        
        // Verify data was recorded
        val attempts = prefs.getInt("attempts_$testProfileId", 0)
        val correct = prefs.getInt("correct_$testProfileId", 0)
        
        assertEquals(3, attempts)
        assertEquals(2, correct)
        
        // Check subject-specific
        val mathAttempts = prefs.getInt("subject_attempts_${testProfileId}_MATH", 0)
        val mathCorrect = prefs.getInt("subject_correct_${testProfileId}_MATH", 0)
        assertEquals(3, mathAttempts)
        assertEquals(2, mathCorrect)
    }
    
    @Test
    fun recordAnswer_tracksMissedTopics() = runBlocking {
        val prefs = context.getSharedPreferences("learner_personalization", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        
        LearnerPersonalizationEngine.recordAnswer(context, testProfileId, "MATH", false, "fractions")
        LearnerPersonalizationEngine.recordAnswer(context, testProfileId, "MATH", false, "multiplication")
        LearnerPersonalizationEngine.recordAnswer(context, testProfileId, "MATH", true, "addition")
        
        val missed = prefs.getString("missed_$testProfileId", "") ?: ""
        assertTrue(missed.contains("fractions"))
        assertTrue(missed.contains("multiplication"))
        assertFalse(missed.contains("addition"))
    }
    
    @Test
    fun recommendedInstructionalStrategy_basedOnAccuracy() = runBlocking {
        val prefs = context.getSharedPreferences("learner_personalization", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        
        // No attempts - diagnostic friendly
        var strategy = LearnerPersonalizationEngine.recommendedInstructionalStrategy(context, testProfileId)
        assertTrue(strategy.contains("diagnostic-friendly"))
        
        // Low accuracy - high scaffolding
        prefs.edit()
            .putInt("attempts_$testProfileId", 10)
            .putInt("correct_$testProfileId", 4)
            .apply()
        strategy = LearnerPersonalizationEngine.recommendedInstructionalStrategy(context, testProfileId)
        assertTrue(strategy.contains("high scaffolding"))
        
        // High accuracy - mastery progression
        prefs.edit()
            .putInt("attempts_$testProfileId", 10)
            .putInt("correct_$testProfileId", 9)
            .apply()
        strategy = LearnerPersonalizationEngine.recommendedInstructionalStrategy(context, testProfileId)
        assertTrue(strategy.contains("mastery progression"))
    }
    
    @Test
    fun topicMasterySummary_tracksPerTopic() = runBlocking {
        val prefs = context.getSharedPreferences("learner_personalization", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        
        // Record multiple attempts on same topic
        LearnerPersonalizationEngine.recordAnswer(context, testProfileId, "MATH", true, "fractions")
        LearnerPersonalizationEngine.recordAnswer(context, testProfileId, "MATH", true, "fractions")
        LearnerPersonalizationEngine.recordAnswer(context, testProfileId, "MATH", false, "fractions")
        
        val summary = LearnerPersonalizationEngine.topicMasterySummary(context, testProfileId)
        assertTrue(summary.contains("fractions"))
        assertTrue(summary.contains("66%")) // 2/3 = 66%
    }
    
    @Test
    fun buildPrompt_excludesDiagnosisByDefault() = runBlocking {
        val profile = com.fourgeailabs.neuropath.data.local.entity.ChildProfileEntity(
            id = testProfileId,
            name = "Test",
            age = 7,
            gradeLevel = "GRADE_1",
            ageGroupTier = "ELEMENTARY",
            stateStandard = "CA",
            country = "United States",
            stateOrProvince = "California",
            city = "Los Angeles",
            schoolDistrict = "LAUSD",
            appLanguageCode = "en-US",
            activeThemeId = "dino",
            neurodivergentTypesCsv = "ADHD,AUTISM_ASD",
            strugglesCsv = "Focus",
            strengthsCsv = "Visual",
            hyperFixationsCsv = "Dinosaurs"
        )
        
        val prompt = LearnerPersonalizationEngine.buildPrompt(context, profile, "MATH", false)
        assertTrue(prompt.contains("kept on-device by default"))
        assertFalse(prompt.contains("ADHD"))
        assertFalse(prompt.contains("AUTISM_ASD"))
    }
}