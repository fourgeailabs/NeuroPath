package com.example.learning

import android.content.Context
import com.example.data.curriculum.CurriculumResolver
import com.example.data.local.entity.ChildProfileEntity
import java.util.Locale

/**
 * Builds a private, needs-led learner fingerprint for tutoring and adaptation.
 * Sensitive diagnosis labels remain on-device by default. Cloud tutoring receives
 * educational signals rather than a child's identity or diagnosis labels.
 */
object LearnerPersonalizationEngine {
    private const val PREFS = "learner_personalization"
    private const val KEY_ATTEMPTS_PREFIX = "attempts_"
    private const val KEY_CORRECT_PREFIX = "correct_"
    private const val KEY_MISSED_PREFIX = "missed_"
    private const val KEY_PREFERRED_STYLE_PREFIX = "style_"
    private const val KEY_SUBJECT_ATTEMPTS_PREFIX = "subject_attempts_"
    private const val KEY_SUBJECT_CORRECT_PREFIX = "subject_correct_"
    private const val KEY_TOPIC_ATTEMPTS_PREFIX = "topic_attempts_"
    private const val KEY_TOPIC_CORRECT_PREFIX = "topic_correct_"
    private const val KEY_TOPIC_LAST_RESULT_PREFIX = "topic_last_"

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
        val subjectKey = subject.trim().uppercase(Locale.US).ifBlank { "GENERAL" }
        val subjectAttempts = prefs.getInt(KEY_SUBJECT_ATTEMPTS_PREFIX + id + "_" + subjectKey, 0)
        val subjectCorrect = prefs.getInt(KEY_SUBJECT_CORRECT_PREFIX + id + "_" + subjectKey, 0)
        val subjectAccuracy = if (subjectAttempts == 0) "not enough data yet" else "${(subjectCorrect * 100) / subjectAttempts}%"
        val preferredStyle = prefs.getString(KEY_PREFERRED_STYLE_PREFIX + id, "adaptive") ?: "adaptive"
        val missed = prefs.getString(KEY_MISSED_PREFIX + id, "") ?: ""

        val diagnosis = csv(profile.neurodivergentTypesCsv)
        val strengths = csv(profile.strengthsCsv)
        val challenges = csv(profile.strugglesCsv)
        val interests = csv(profile.hyperFixationsCsv)
        val diagnosisLine = if (includeSensitiveDiagnosis) {
            "Diagnosis or declared learning differences: ${diagnosis.ifEmpty { listOf("None provided") }.joinToString(", ")}"
        } else {
            "Declared learning-difference details: kept on-device by default; use needs, strengths and accessibility signals below."
        }

        val learnerLabel = "Student"
        val accessibility = buildString {
            append("dyslexiaFont=${profile.dyslexiaFontEnabled}; ")
            append("highContrast=${profile.highContrastMode}; ")
            append("readAloud=${profile.readAnswersAloud}; ")
            append("autoHighlight=${profile.autoHighlightWords}")
        }

        val curriculumResolution = CurriculumResolver.resolve(
            country = profile.country,
            stateOrProvince = profile.stateOrProvince,
            schoolDistrict = profile.schoolDistrict,
            stageOrGrade = profile.gradeLevel,
            subject = subjectKey
        )
        val topicMastery = topicMasterySummary(context, id)
        val strategy = recommendedInstructionalStrategy(context, id)

        return """
            LEARNER-CENTRED PERSONALIZATION PROFILE
            Treat this as a living learner profile, not a label. Do not assume every trait applies all the time.
            Learner: $learnerLabel
            Age/grade: ${profile.age} / ${profile.gradeLevel}
            $diagnosisLine
            Strengths/superpowers: ${strengths.ifEmpty { listOf("Discover these through interaction") }.joinToString(", ")}
            Current challenges/weaknesses: ${challenges.ifEmpty { listOf("Discover these through interaction") }.joinToString(", ")}
            Interests, likes and motivating topics: ${interests.ifEmpty { listOf("Discover these through interaction") }.joinToString(", ")}
            Active theme: ${profile.activeThemeId}
            Accessibility: $accessibility
            Current subject: $subjectKey
            Observed answer accuracy across this profile: $accuracy
            Observed answer accuracy in current subject: $subjectAccuracy
            Topic mastery signals: $topicMastery
            Recent missed topics/signals: ${missed.ifBlank { "none recorded" }}
            Learned preferred explanation style: $preferredStyle
            Current instructional strategy: $strategy

            ${curriculumResolution.contextText()}

            PERSONALIZATION RULES:
            1. Adapt the explanation to this individual, not merely their age or diagnosis.
            2. Lead with strengths and interests when introducing difficult concepts.
            3. Break work into smaller steps when the learner's challenge profile suggests executive-function, attention, working-memory, reading, or task-initiation friction.
            4. Offer choices of modality when useful: visual, verbal, example-first, hands-on, or step-by-step.
            5. Do not repeatedly use a style that appears ineffective. Change strategy after repeated misses or frustration.
            6. Increase challenge after demonstrated mastery; reduce complexity after repeated errors without shaming the learner.
            7. Treat topic mastery as an evidence signal, not a permanent judgment. More attempts with consistent success increase confidence; recent errors lower confidence and should trigger scaffolding.
            8. Never diagnose, reinterpret, or medically infer a condition. Use declared information only as an accommodation signal.
            9. Preserve the learner's agency: ask what they prefer when there is genuine uncertainty.
            10. Avoid infantilizing older learners and avoid making younger learners feel behind.
            11. Keep personalization focused on education, accessibility, engagement, and wellbeing—not advertising or manipulation.
        """.trimIndent()
    }

    fun recordAnswer(context: Context, profileId: Long, subject: String, correct: Boolean, topic: String? = null) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val attemptsKey = KEY_ATTEMPTS_PREFIX + profileId
        val correctKey = KEY_CORRECT_PREFIX + profileId
        val missedKey = KEY_MISSED_PREFIX + profileId
        val subjectKey = subject.trim().uppercase(Locale.US).ifBlank { "GENERAL" }
        val subjectAttemptsKey = KEY_SUBJECT_ATTEMPTS_PREFIX + profileId + "_" + subjectKey
        val subjectCorrectKey = KEY_SUBJECT_CORRECT_PREFIX + profileId + "_" + subjectKey
        val attempts = prefs.getInt(attemptsKey, 0) + 1
        val correctCount = prefs.getInt(correctKey, 0) + if (correct) 1 else 0
        val subjectAttempts = prefs.getInt(subjectAttemptsKey, 0) + 1
        val subjectCorrect = prefs.getInt(subjectCorrectKey, 0) + if (correct) 1 else 0

        val editor = prefs.edit()
            .putInt(attemptsKey, attempts)
            .putInt(correctKey, correctCount)
            .putInt(subjectAttemptsKey, subjectAttempts)
            .putInt(subjectCorrectKey, subjectCorrect)

        var missed = prefs.getString(missedKey, "") ?: ""
        if (!correct && !topic.isNullOrBlank()) {
            val entries = missed.split("|").filter { it.isNotBlank() }.toMutableList()
            entries.remove(topic)
            entries.add(topic)
            missed = entries.takeLast(8).joinToString("|")
        }

        if (!topic.isNullOrBlank()) {
            val topicKey = normalizeTopic(topic)
            val topicAttemptsKey = KEY_TOPIC_ATTEMPTS_PREFIX + profileId + "_" + topicKey
            val topicCorrectKey = KEY_TOPIC_CORRECT_PREFIX + profileId + "_" + topicKey
            val topicLastResultKey = KEY_TOPIC_LAST_RESULT_PREFIX + profileId + "_" + topicKey
            editor.putInt(topicAttemptsKey, prefs.getInt(topicAttemptsKey, 0) + 1)
                .putInt(topicCorrectKey, prefs.getInt(topicCorrectKey, 0) + if (correct) 1 else 0)
                .putBoolean(topicLastResultKey, correct)
        }

        editor.putString(missedKey, missed).apply()
    }

    fun recordPreferredStyle(context: Context, profileId: Long, style: String) {
        if (style.isBlank()) return
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_PREFERRED_STYLE_PREFIX + profileId, style.lowercase(Locale.US)).apply()
    }

    /** Returns compact topic evidence suitable for a tutoring prompt. */
    fun topicMasterySummary(context: Context, profileId: Long): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val missed = prefs.getString(KEY_MISSED_PREFIX + profileId, "") ?: ""
        val topics = missed.split("|").filter { it.isNotBlank() }.takeLast(8)
        if (topics.isEmpty()) return "none recorded"

        return topics.joinToString("; ") { topic ->
            val key = normalizeTopic(topic)
            val attempts = prefs.getInt(KEY_TOPIC_ATTEMPTS_PREFIX + profileId + "_" + key, 0)
            val correct = prefs.getInt(KEY_TOPIC_CORRECT_PREFIX + profileId + "_" + key, 0)
            val lastCorrect = prefs.getBoolean(KEY_TOPIC_LAST_RESULT_PREFIX + profileId + "_" + key, false)
            val mastery = if (attempts == 0) 0 else (correct * 100) / attempts
            val confidence = confidenceFor(mastery, attempts, lastCorrect)
            "$topic=${mastery}% mastery, ${confidence}% confidence signal over $attempts attempts"
        }
    }

    /**
     * Turns the observed learner evidence into an explicit next-step teaching strategy.
     * This is deterministic, local, and deliberately conservative: the learner model
     * changes the path to mastery, never the curriculum objective itself.
     */
    fun recommendedInstructionalStrategy(context: Context, profileId: Long): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val attempts = prefs.getInt(KEY_ATTEMPTS_PREFIX + profileId, 0)
        val correct = prefs.getInt(KEY_CORRECT_PREFIX + profileId, 0)
        val accuracy = if (attempts == 0) null else (correct * 100) / attempts
        val missed = prefs.getString(KEY_MISSED_PREFIX + profileId, "") ?: ""
        val recentMisses = missed.split("|").filter { it.isNotBlank() }.takeLast(3)

        return when {
            attempts == 0 -> "Start with a short diagnostic-friendly explanation, one concrete example, and a low-pressure check for understanding."
            accuracy != null && accuracy < 55 -> "Use high scaffolding: one concept at a time, worked example, short steps, frequent checks, and an alternate explanation if the first attempt does not land."
            recentMisses.size >= 2 -> "Target recent struggle topics first; use retrieval practice, misconception checks, and smaller steps before increasing difficulty."
            accuracy != null && accuracy < 75 -> "Use moderate scaffolding: explain, model, then ask the learner to complete the next step with a hint available."
            accuracy >= 90 -> "Use mastery progression: reduce scaffolding, increase transfer/application, and introduce a slightly harder problem while checking for durable understanding."
            else -> "Use adaptive instruction: concise explanation, concrete example, guided practice, then a quick retrieval check."
        }
    }

    private fun confidenceFor(mastery: Int, attempts: Int, lastCorrect: Boolean): Int = when {
        attempts == 0 -> 0
        attempts < 3 -> mastery
        lastCorrect && mastery >= 70 -> (mastery + 10).coerceAtMost(100)
        !lastCorrect -> (mastery - 10).coerceAtLeast(0)
        else -> mastery
    }

    private fun normalizeTopic(value: String): String = value.trim().lowercase(Locale.US)
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')
        .take(80)
        .ifBlank { "general" }

    private fun csv(value: String): List<String> = value.split(",").map { it.trim() }.filter { it.isNotBlank() }
}
