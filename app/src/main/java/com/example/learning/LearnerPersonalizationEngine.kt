package com.example.learning

import android.content.Context
import com.example.data.local.entity.ChildProfileEntity
import java.util.Locale

/**
 * Builds a private, needs-led learner fingerprint for tutoring and adaption.
 * The exact profile stays on-device unless a caller explicitly requests sensitive
 * diagnosis context for an offline/local model.
 */
object LearnerPersonalizationEngine {
    private const val PREFS = "learner_personalization"
    private const val KEY_ATTEMPTS_PREFIX = "attempts_"
    private const val KEY_CORRECT_PREFIX = "correct_"
    private const val KEY_MISSED_PREFIX = "missed_"
    private const val KEY_PREFERRED_STYLE_PREFIX = "style_"

    fun buildPrompt(
        context: Context,
        profile: ChildProfileEntity,
        subject: String = "GENERAL",
        includeSensitiveDiagnosis: Boolean = false
    ): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val id = profile.id
        val attempts = prefs.getInt(KEY_ATTEMPTS_PREFIX + id, 0)
        val correct = prefs.getInt(KEY_CORRECT_PREFIX + id, 0)
        val accuracy = if (attempts == 0) "not enough data yet" else "${(correct * 100) / attempts}%"
        val preferredStyle = prefs.getString(KEY_PREFERRED_STYLE_PREFIX + id, "adaptive") ?: "adaptive"
        val missed = prefs.getString(KEY_MISSED_PREFIX + id, "") ?: ""

        val diagnosis = csv(profile.neurodivergentTypesCsv)
        val strengths = csv(profile.strengthsCsv)
        val challenges = csv(profile.strugglesCsv)
        val interests = csv(profile.hyperFixationsCsv)
        val dislikes = if (challenges.isNotEmpty()) challenges else listOf("No explicit dislikes recorded")
        val diagnosisLine = if (includeSensitiveDiagnosis) {
            "Diagnosis or declared learning differences: ${diagnosis.ifEmpty { listOf("None provided") }.joinToString(", ")}"
        } else {
            "Declared learning-difference details: kept on-device by default; use the needs, strengths and accessibility signals below."
        }

        return """
            LEARNER-CENTRED PERSONALIZATION PROFILE
            Treat this as a living learner profile, not a label. Do not assume every trait applies all the time.
            Learner: ${profile.name.ifBlank { "Student" }}
            Age/grade: ${profile.age} / ${profile.gradeLevel}
            $diagnosisLine
            Strengths/superpowers: ${strengths.ifEmpty { listOf("Discover these through interaction") }.joinToString(", ")}
            Current challenges/weaknesses: ${challenges.ifEmpty { listOf("Discover these through interaction") }.joinToString(", ")}
            Interests, likes and motivating topics: ${interests.ifEmpty { listOf("Discover these through interaction") }.joinToString(", ")}
            Likely friction/dislike areas: ${dislikes.joinToString(", ")}
            Active theme: ${profile.activeThemeId}
            Accessibility: dyslexiaFont=${profile.dyslexiaFontEnabled}; highContrast=${profile.highContrastMode}; readAloud=${profile.readAnswersAloud}; ambientSound=${profile.ambientSound}
            Current subject: $subject
            Observed answer accuracy across this profile: $accuracy
            Recent missed topics/signals: ${missed.ifBlank { "none recorded" }}
            Learned preferred explanation style: $preferredStyle

            PERSONALIZATION RULES:
            1. Adapt the explanation to this individual, not merely their age or diagnosis.
            2. Lead with strengths and interests when introducing difficult concepts.
            3. Break work into smaller steps when the learner's challenge profile suggests executive-function, attention, working-memory, reading, or task-initiation friction.
            4. Offer choices of modality when useful: visual, verbal, example-first, hands-on, or step-by-step.
            5. Do not repeatedly use a style that appears ineffective. Change strategy after repeated misses or frustration.
            6. Increase challenge after demonstrated mastery; reduce complexity after repeated errors without shaming the learner.
            7. Never diagnose, reinterpret, or medically infer a condition. Use declared information only as an accommodation signal.
            8. Preserve the learner's agency: ask what they prefer when there is genuine uncertainty.
            9. Avoid infantilizing older learners and avoid making younger learners feel behind.
            10. Keep personalization focused on education, accessibility, engagement, and wellbeing—not advertising or manipulation.
        """.trimIndent()
    }

    fun recordAnswer(context: Context, profileId: Long, subject: String, correct: Boolean, topic: String? = null) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val attemptsKey = KEY_ATTEMPTS_PREFIX + profileId
        val correctKey = KEY_CORRECT_PREFIX + profileId
        val missedKey = KEY_MISSED_PREFIX + profileId
        val attempts = prefs.getInt(attemptsKey, 0) + 1
        val correctCount = prefs.getInt(correctKey, 0) + if (correct) 1 else 0
        var missed = prefs.getString(missedKey, "") ?: ""
        if (!correct && !topic.isNullOrBlank()) {
            val entries = missed.split("|").filter { it.isNotBlank() }.toMutableList()
            entries.remove(topic)
            entries.add(topic)
            missed = entries.takeLast(8).joinToString("|")
        }
        prefs.edit()
            .putInt(attemptsKey, attempts)
            .putInt(correctKey, correctCount)
            .putString(missedKey, missed)
            .apply()
    }

    fun recordPreferredStyle(context: Context, profileId: Long, style: String) {
        if (style.isBlank()) return
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_PREFERRED_STYLE_PREFIX + profileId, style.lowercase(Locale.US)).apply()
    }

    private fun csv(value: String): List<String> = value.split(",").map { it.trim() }.filter { it.isNotBlank() }
}
