package com.example.network

import com.example.data.model.AppLanguage
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class GeminiClientTest {
    
    @Test
    fun parseAndEvaluateMath_handlesSimpleAddition() = runBlocking {
        val result = GeminiClient.parseAndEvaluateMath("2 + 3")
        assertNotNull(result)
        assertEquals("2 + 3", result.originalDisplay)
        assertEquals(5L, result.finalResult)
        assertEquals(1, result.steps.size)
        assertEquals("2 + 3 = 5", result.steps.first())
    }
    
    @Test
    fun parseAndEvaluateMath_handlesSubtraction() = runBlocking {
        val result = GeminiClient.parseAndEvaluateMath("10 - 4")
        assertNotNull(result)
        assertEquals(6L, result.finalResult)
        assertEquals("10 - 4 = 6", result.steps.first())
    }
    
    @Test
    fun parseAndEvaluateMath_handlesMultiplication() = runBlocking {
        val result = GeminiClient.parseAndEvaluateMath("6 * 7")
        assertNotNull(result)
        assertEquals(42L, result.finalResult)
        assertEquals("6 × 7 = 42", result.steps.first())
    }
    
    @Test
    fun parseAndEvaluateMath_handlesDivision() = runBlocking {
        val result = GeminiClient.parseAndEvaluateMath("20 / 4")
        assertNotNull(result)
        assertEquals(5L, result.finalResult)
        assertEquals("20 ÷ 4 = 5", result.steps.first())
    }
    
    @Test
    fun parseAndEvaluateMath_handlesOrderOfOperations() = runBlocking {
        val result = GeminiClient.parseAndEvaluateMath("2 + 3 * 4")
        assertNotNull(result)
        assertEquals(14L, result.finalResult)
        assertEquals(2, result.steps.size)
        assertEquals("3 × 4 = 12", result.steps[0])
        assertEquals("2 + 12 = 14", result.steps[1])
    }
    
    @Test
    fun parseAndEvaluateMath_handlesNaturalLanguage() = runBlocking {
        val result = GeminiClient.parseAndEvaluateMath("5 plus 3")
        assertNotNull(result)
        assertEquals(8L, result.finalResult)
    }
    
    @Test
    fun parseAndEvaluateMath_returnsNullForInvalidInput() = runBlocking {
        val result = GeminiClient.parseAndEvaluateMath("hello world")
        assertNull(result)
    }
    
    @Test
    fun generateLocalSocraticReply_handlesMath() = runBlocking {
        val reply = GeminiClient.generateLocalSocraticReply(
            lastUserMessage = "What is 5 + 3?",
            schoolDistrict = "LAUSD",
            stateOrProvince = "California",
            country = "United States",
            standardTitle = "CA-CCSS",
            languageCode = "en-US"
        )
        assertTrue(reply.contains("8"))
    }
    
    @Test
    fun generateLocalSocraticReply_handlesGreeting() = runBlocking {
        val reply = GeminiClient.generateLocalSocraticReply(
            lastUserMessage = "hello",
            schoolDistrict = "LAUSD",
            stateOrProvince = "California",
            country = "United States",
            standardTitle = "CA-CCSS",
            languageCode = "en-US"
        )
        assertTrue(reply.isNotBlank())
    }
    
    @Test
    fun appLanguageFromCode() {
        assertEquals(AppLanguage.ENGLISH_US, AppLanguage.fromCode("en-US"))
        assertEquals(AppLanguage.ENGLISH_UK, AppLanguage.fromCode("en-GB"))
        assertEquals(AppLanguage.SPANISH, AppLanguage.fromCode("es"))
        assertEquals(AppLanguage.FRENCH, AppLanguage.fromCode("fr"))
    }
}