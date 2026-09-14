package com.example.learning

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.min

/** Durable, on-device instructional mastery signals. */
object MasteryRepository {
    private const val PREFS = "learner_mastery_v2"

    data class TopicMastery(
        val topic: String,
        val attempts: Int,
        val correct: Int,
        val streak: Int,
        val masteryPercent: Int,
        val lastUpdated: Long
    )

    fun record(context: Context, profileId: Long, topic: String, correct: Boolean) {
        val key = "profile_$profileId"
        val all = load(context, key).toMutableMap()
        val normalized = topic.trim().ifBlank { "GENERAL" }
        val previous = all[normalized]
        val attempts = (previous?.attempts ?: 0) + 1
        val correctCount = (previous?.correct ?: 0) + if (correct) 1 else 0
        val streak = if (correct) (previous?.streak ?: 0) + 1 else 0
        val accuracy = correctCount * 100 / attempts
        val mastery = min(100, (accuracy * 70 / 100) + min(30, streak * 5))
        all[normalized] = TopicMastery(normalized, attempts, correctCount, streak, mastery, System.currentTimeMillis())
        save(context, key, all.values)
    }

    fun snapshot(context: Context, profileId: Long): List<TopicMastery> =
        load(context, "profile_$profileId").values.sortedByDescending { it.lastUpdated }

    fun overallMastery(context: Context, profileId: Long): Int {
        val values = snapshot(context, profileId)
        return if (values.isEmpty()) 0 else values.map { it.masteryPercent }.average().toInt()
    }

    private fun load(context: Context, key: String): Map<String, TopicMastery> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(key, null) ?: return emptyMap()
        return runCatching {
            val array = JSONArray(raw)
            buildMap {
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val topic = item.optString("topic")
                    if (topic.isNotBlank()) put(topic, TopicMastery(topic, item.optInt("attempts"), item.optInt("correct"), item.optInt("streak"), item.optInt("masteryPercent"), item.optLong("lastUpdated")))
                }
            }
        }.getOrDefault(emptyMap())
    }

    private fun save(context: Context, key: String, values: Collection<TopicMastery>) {
        val array = JSONArray()
        values.sortedBy { it.topic }.forEach { item ->
            array.put(JSONObject().apply {
                put("topic", item.topic)
                put("attempts", item.attempts)
                put("correct", item.correct)
                put("streak", item.streak)
                put("masteryPercent", item.masteryPercent)
                put("lastUpdated", item.lastUpdated)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(key, array.toString()).apply()
    }
}
