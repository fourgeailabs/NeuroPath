package com.example.network

import com.example.data.model.AppLanguage
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiClientTest {
    
    @Test
    fun parseAndEvaluateMath_handlesSimpleAddition() = runBlocking {
        val result = GeminiClient.parseAndEvaluateMath("2 + 3")
        assertNotNull(result)
        val res = checkNotNull(result)
        assertEquals("2 + 3", res.originalDisplay)
        assertEquals(5L, res.finalResult)
        assertEquals(1, res.steps.size)
        assertEquals("2 + 3 = 5", res.steps.first())
    }
    
    @Test
    fun parseAndEvaluateMath_handlesSubtraction() = runBlocking {
        val result = GeminiClient.parseAndEvaluateMath("10 - 4")
        assertNotNull(result)
        val res = checkNotNull(result)
        assertEquals(6L, res.finalResult)
        assertEquals("10 - 4 = 6", res.steps.first())
    }
    
    @Test
    fun parseAndEvaluateMath_handlesMultiplication() = runBlocking {
        val result = GeminiClient.parseAndEvaluateMath("6 * 7")
        assertNotNull(result)
        val res = checkNotNull(result)
        assertEquals(42L, res.finalResult)
        assertEquals("6 × 7 = 42", res.steps.first())
    }
    
    @Test
    fun parseAndEvaluateMath_handlesDivision() = runBlocking {
        val result = GeminiClient.parseAndEvaluateMath("20 / 4")
        assertNotNull(result)
        val res = checkNotNull(result)
        assertEquals(5L, res.finalResult)
        assertEquals("20 ÷ 4 = 5", res.steps.first())
    }
    
    @Test
    fun parseAndEvaluateMath_handlesOrderOfOperations() = runBlocking {
        val result = GeminiClient.parseAndEvaluateMath("2 + 3 * 4")
        assertNotNull(result)
        val res = checkNotNull(result)
        assertEquals(14L, res.finalResult)
        assertEquals(2, res.steps.size)
        assertEquals("3 × 4 = 12", res.steps[0])
        assertEquals("2 + 12 = 14", res.steps[1])
    }
    
    @Test
    fun parseAndEvaluateMath_handlesNaturalLanguage() = runBlocking {
        val result = GeminiClient.parseAndEvaluateMath("5 plus 3")
        assertNotNull(result)
        val res = checkNotNull(result)
        assertEquals(8L, res.finalResult)
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