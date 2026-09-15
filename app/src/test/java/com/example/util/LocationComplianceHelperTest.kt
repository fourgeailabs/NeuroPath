package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Test

class LocationComplianceHelperTest {
    @Test
    fun knownSurpriseZipDoesNotDriftToPhoenix() {
        val method = LocationComplianceHelper::class.java.getDeclaredMethod("mapKnownUsZip", String::class.java)
        method.isAccessible = true
        val result = method.invoke(LocationComplianceHelper, "85379") as Pair<*, *>?
        assertEquals("Arizona", result?.first)
        assertEquals("Surprise", result?.second)
    }

    @Test
    fun unknownZipHasNoKnownCityMapping() {
        val method = LocationComplianceHelper::class.java.getDeclaredMethod("mapKnownUsZip", String::class.java)
        method.isAccessible = true
        assertEquals(null, method.invoke(LocationComplianceHelper, "99999"))
    }
}
