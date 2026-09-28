package com.fourgeailabs.neuropath.network

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Slice 1 honesty tests: a dead or unavailable AI path must never pretend to be
 * Llama 3.2. The Socratic fallback is the "Socratic Teacher" — explicitly labelled,
 * never stored under a Llama model name.
 */
class LlamaReplyHonestyTest {

    @Test
    fun socraticStatusReply_identifiesAsSocraticTeacher() {
        val reply = LlamaClient.generateLocalSocraticReply(
            lastUserMessage = "are you working?",
            schoolDistrict = "Test District"
        )
        assertTrue("Status reply must name the Socratic Teacher", reply.contains("Socratic Teacher"))
    }

    @Test
    fun socraticStatusReply_neverClaimsToBePoweredByLlama() {
        val reply = LlamaClient.generateLocalSocraticReply(
            lastUserMessage = "are you working?",
            schoolDistrict = "Test District"
        )
        assertFalse(
            "Status reply must not claim Llama provenance",
            reply.contains("powered by Llama 3.2 3B", ignoreCase = true)
        )
    }

    @Test
    fun generateChatReply_noApiKey_returnsSocraticFallback() = runBlocking {
        val previousOverride = LlamaClient.customApiKeyOverride
        LlamaClient.customApiKeyOverride = ""
        try {
            val reply = LlamaClient.generateChatReply(
                conversationHistory = listOf("user" to "What is 2 + 2?"),
                systemPrompt = "Be helpful.",
                modelMode = ChatModelMode.GENERAL,
                customApiKey = "",
                appContext = null
            )
            assertEquals(ChatReplySource.SOCRATIC_FALLBACK, reply.source)
            assertTrue(reply.text.isNotBlank())
        } finally {
            LlamaClient.customApiKeyOverride = previousOverride
        }
    }

    @Test
    fun localEngineChatReply_marksEngineErrorBannerAsError() {
        val reply = LlamaClient.localEngineChatReply(
            "🦙 [Llama 3.2 3B Local Engine]: model failed to load"
        )
        assertEquals(ChatReplySource.ERROR, reply.source)
    }

    @Test
    fun localEngineChatReply_marksRealOutputAsLocalModel() {
        val reply = LlamaClient.localEngineChatReply("Here is a real on-device answer.")
        assertEquals(ChatReplySource.LOCAL_MODEL, reply.source)
    }

    @Test
    fun recordedModel_socraticFallbackMapsToOfflineSocratic() {
        val model = ChatReplySource.SOCRATIC_FALLBACK.recordedModel(ChatModelMode.GENERAL)
        assertEquals(ChatModelMode.OFFLINE, model)
        assertEquals("offline-socratic", model.modelName)
    }

    @Test
    fun recordedModel_localModelStaysLocal() {
        val model = ChatReplySource.LOCAL_MODEL.recordedModel(ChatModelMode.GENERAL)
        assertEquals(ChatModelMode.LLAMA_LOCAL, model)
    }

    @Test
    fun isUsableModelAnswer_rejectsSocraticEchoAndErrors() {
        assertFalse(
            ChatReply("💡 Exploring \"Give a 1-sentence...\"", ChatReplySource.SOCRATIC_FALLBACK)
                .isUsableModelAnswer()
        )
        assertFalse(
            ChatReply("The cloud tutor ran into a problem.", ChatReplySource.ERROR)
                .isUsableModelAnswer()
        )
        assertFalse(ChatReply("", ChatReplySource.CLOUD).isUsableModelAnswer())
        assertTrue(ChatReply("A real model answer.", ChatReplySource.CLOUD).isUsableModelAnswer())
        assertTrue(
            ChatReply("A real on-device answer.", ChatReplySource.LOCAL_MODEL).isUsableModelAnswer()
        )
    }

    @Test
    fun socraticChatReply_labelsFallbackSource() {
        val reply = LlamaClient.socraticChatReply(lastUserMessage = "hello")
        assertEquals(ChatReplySource.SOCRATIC_FALLBACK, reply.source)
        assertTrue(reply.text.isNotBlank())
    }

    // --- Lesson-grounded Socratic teaching (scope extension) ---

    private fun sampleLesson() = LessonContext(
        lessonTitle = "The Water Cycle",
        subject = "Science & Nature",
        keyPoints = listOf(
            "Evaporation turns liquid water into vapor when heated by the sun",
            "Condensation forms clouds as vapor cools high in the sky"
        )
    )

    @Test
    fun socraticReply_referencesSuppliedLessonFacts() {
        val reply = LlamaClient.generateLocalSocraticReply(
            lastUserMessage = "tell me more about this",
            lessonContext = sampleLesson()
        )
        assertTrue(
            "Grounded reply must reference a real lesson fact",
            reply.contains("Evaporation turns liquid water into vapor") ||
                reply.contains("Condensation forms clouds")
        )
        assertTrue(
            "Grounded reply must name the lesson",
            reply.contains("The Water Cycle")
        )
    }

    @Test
    fun socraticQuiz_usesRealLessonMaterial() {
        val reply = LlamaClient.generateLocalSocraticReply(
            lastUserMessage = "quiz me please",
            lessonContext = sampleLesson()
        )
        assertTrue(
            "Quiz must be built on lesson facts, not a generic placeholder",
            reply.contains("Evaporation turns liquid water into vapor") ||
                reply.contains("Condensation forms clouds")
        )
    }

    @Test
    fun socraticRecap_listsLessonKeyPoints() {
        val reply = LlamaClient.generateLocalSocraticReply(
            lastUserMessage = "can you recap the lesson?",
            lessonContext = sampleLesson()
        )
        assertTrue(reply.contains("The Water Cycle"))
        assertTrue(reply.contains("Evaporation turns liquid water into vapor"))
        assertTrue(reply.contains("Condensation forms clouds"))
    }

    @Test
    fun socraticReply_withoutLessonContext_stillAnswers() {
        val reply = LlamaClient.generateLocalSocraticReply(
            lastUserMessage = "quiz me please",
            lessonContext = LessonContext()
        )
        assertTrue("Empty lesson context must still produce an answer", reply.isNotBlank())
    }

    @Test
    fun socraticReply_withLessonContext_staysHonestlyLabeled() {
        val reply = LlamaClient.socraticChatReply(
            lastUserMessage = "explain this",
            lessonContext = sampleLesson()
        )
        assertEquals(ChatReplySource.SOCRATIC_FALLBACK, reply.source)
        assertFalse(
            "Grounded fallback must never claim Llama provenance",
            reply.text.contains("powered by Llama 3.2 3B", ignoreCase = true)
        )
        assertFalse(
            "Grounded fallback must never be usable as a model answer",
            reply.isUsableModelAnswer()
        )
    }

    @Test
    fun lessonContext_hasContent_detectsRealMaterial() {
        assertTrue(sampleLesson().hasContent())
        assertFalse(LessonContext().hasContent())
        assertFalse(LessonContext(keyPoints = listOf("", "  ")).hasContent())
    }
}
