package com.fourgeailabs.neuropath.network

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Permanent automated audit of the on-device AI engine's honesty: the prompt
 * the local model actually receives must carry the Llama 3.2 chat template,
 * the system grounding, the curriculum context, and the conversation history.
 * A prompt that silently dropped any of these would make the "AI" dumber than
 * advertised without any visible error — this test pins the contract.
 */
class ChatAIEngineAuditTest {

    @Test
    fun promptUsesLlama32ChatTemplate() {
        val prompt = LlamaLocalManager.buildLocalLlamaPrompt(userPrompt = "What is 2 + 3?")
        assertTrue(prompt.startsWith("<|begin_of_text|>"))
        assertTrue(prompt.contains("<|start_header_id|>system<|end_header_id|>"))
        assertTrue(prompt.contains("<|start_header_id|>user<|end_header_id|>"))
        assertTrue(prompt.contains("<|start_header_id|>assistant<|end_header_id|>"))
        // The assistant header must be the final turn opener, not closed mid-prompt.
        assertTrue(prompt.trimEnd().endsWith("<|start_header_id|>assistant<|end_header_id|>"))
    }

    @Test
    fun promptIncludesUserQuestionAndSystemGrounding() {
        val prompt = LlamaLocalManager.buildLocalLlamaPrompt(
            userPrompt = "Why is the sky blue?",
            systemPrompt = "Be extra encouraging today."
        )
        assertTrue(prompt.contains("Why is the sky blue?"))
        assertTrue(prompt.contains("Be extra encouraging today."))
        assertTrue(prompt.contains("Socratic teaching style"))
    }

    @Test
    fun promptIncludesCurriculumAndDistrictGrounding() {
        val prompt = LlamaLocalManager.buildLocalLlamaPrompt(
            userPrompt = "Explain fractions.",
            schoolDistrict = "Dysart Unified",
            stateOrProvince = "Arizona",
            country = "USA",
            standardTitle = "Arizona Academic Standards",
            curriculumContext = "Fractions as parts of a whole."
        )
        assertTrue(prompt.contains("Dysart Unified"))
        assertTrue(prompt.contains("Arizona"))
        assertTrue(prompt.contains("Arizona Academic Standards"))
        assertTrue(prompt.contains("Fractions as parts of a whole."))
    }

    @Test
    fun promptIncludesRecentConversationHistory() {
        val prompt = LlamaLocalManager.buildLocalLlamaPrompt(
            userPrompt = "And what about division?",
            conversationHistory = listOf(
                "user" to "What is multiplication?",
                "model" to "Multiplication is repeated addition."
            )
        )
        assertTrue(prompt.contains("Student: What is multiplication?"))
        assertTrue(prompt.contains("Learning Buddy: Multiplication is repeated addition."))
    }

    @Test
    fun promptOmitsEmptySectionsWithoutBreakingTemplate() {
        val prompt = LlamaLocalManager.buildLocalLlamaPrompt(userPrompt = "Hi")
        // No history block when there is no history.
        assertFalse(prompt.contains("Recent conversation"))
        // Template still intact.
        assertTrue(prompt.contains("<|begin_of_text|>"))
        assertTrue(prompt.contains("<|eot_id|>"))
    }

    @Test
    fun promptCapsHistoryLength() {
        val longHistory = (1..20).map { "user" to "Question number $it" }
        val prompt = LlamaLocalManager.buildLocalLlamaPrompt(
            userPrompt = "Done?",
            conversationHistory = longHistory
        )
        // Only the most recent turns survive; the oldest must be dropped.
        assertFalse(prompt.contains("Question number 1\n"))
        assertTrue(prompt.contains("Question number 20"))
    }
}
