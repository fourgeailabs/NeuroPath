package com.example.data.curriculum

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurriculumResolverTest {
    @Test
    fun normalizesCommonCountryAliases() {
        val us = CurriculumResolver.resolve(
            country = "USA",
            stateOrProvince = "Arizona",
            stageOrGrade = "Grade 5",
            subject = "MATH"
        )
        assertEquals("United States", us.countryOrTerritory)
        assertEquals("US", us.jurisdictionId)
        assertEquals("Arizona", us.subnationalJurisdiction)
    }

    @Test
    fun decentralizedJurisdictionWithoutSubnationalDataIsNotClaimedAsFullyResolved() {
        val canada = CurriculumResolver.resolve(
            country = "Canada",
            stageOrGrade = "Grade 4",
            subject = "SCIENCE"
        )
        assertEquals("CA", canada.jurisdictionId)
        assertEquals(CurriculumResolver.Resolution.Confidence.ROUTING_ONLY, canada.confidence)
        assertTrue(canada.sourceType.contains("DECENTRALIZED"))
    }

    @Test
    fun ukResolutionUsesDevolvedAuthorityWhenNationIsKnown() {
        val england = CurriculumResolver.resolve(
            country = "UK",
            schoolDistrict = "England",
            stageOrGrade = "Key Stage 2",
            subject = "MATH"
        )
        assertEquals("United Kingdom", england.countryOrTerritory)
        assertTrue(england.jurisdictionId?.startsWith("UK_") == true)
        assertEquals(CurriculumResolver.Resolution.Confidence.HIGH, england.confidence)
    }
}
