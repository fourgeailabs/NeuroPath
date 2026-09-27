package com.fourgeailabs.neuropath.ui.components

import com.fourgeailabs.neuropath.data.model.WorldTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoadingFactsTest {

    @Test
    fun `rotation interval starts at six seconds`() {
        assertEquals(6_000L, LOADING_FACT_ROTATION_MS)
    }

    @Test
    fun `every theme resolves to a non-empty fact list`() {
        WorldTheme.entries.forEach { theme ->
            val facts = loadingFactsForTheme(theme.id)
            assertTrue("No facts for theme ${theme.id}", facts.isNotEmpty())
        }
    }

    @Test
    fun `all facts have text and a module tag`() {
        val all = LOADING_FACTS_BY_THEME.values.flatten()
        assertTrue(all.isNotEmpty())
        all.forEach { fact ->
            assertTrue("Blank fact text", fact.text.isNotBlank())
            assertTrue("Blank module for: ${fact.text}", fact.module.isNotBlank())
        }
    }

    @Test
    fun `facts are bite-size nibbles`() {
        LOADING_FACTS_BY_THEME.values.flatten().forEach { fact ->
            assertTrue(
                "Fact too long for a loading nibble (${fact.text.length} chars): ${fact.text}",
                fact.text.length <= 220
            )
        }
    }

    @Test
    fun `unknown theme falls back to general facts`() {
        val facts = loadingFactsForTheme("no_such_theme")
        assertTrue(facts.isNotEmpty())
    }
}
