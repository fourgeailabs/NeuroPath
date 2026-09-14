package com.example.data.curriculum

import com.example.data.curriculum.uk.UkNationalCurriculumCatalog

/**
 * Resolves the curriculum context that should be used before selecting lessons.
 * Age/grade is intentionally a stage selector, not a curriculum authority.
 *
 * This resolver returns routing/provenance metadata. It does not pretend that a
 * registry entry means the full official curriculum has already been downloaded.
 */
object CurriculumResolver {
    data class Resolution(
        val jurisdictionId: String?,
        val countryOrTerritory: String,
        val subnationalJurisdiction: String,
        val educationAuthority: String,
        val officialSourceUrl: String,
        val stageOrGrade: String,
        val subject: String,
        val confidence: Confidence,
        val sourceType: String = "OFFICIAL_AUTHORITY_ENTRY_POINT"
    ) {
        enum class Confidence { HIGH, MEDIUM, ROUTING_ONLY }

        fun contextText(): String = buildString {
            appendLine("CURRICULUM JURISDICTION RESOLUTION")
            appendLine("Country/Territory: $countryOrTerritory")
            if (subnationalJurisdiction.isNotBlank()) appendLine("Subnational jurisdiction: $subnationalJurisdiction")
            appendLine("Education authority: $educationAuthority")
            appendLine("Stage/Grade: $stageOrGrade")
            appendLine("Subject: $subject")
            appendLine("Authority source: $officialSourceUrl")
            appendLine("Resolution confidence: ${confidence.name}")
            appendLine("Source type: $sourceType")
            appendLine("Use the official authority as the source of truth for the current statutory curriculum.")
        }
    }

    fun resolve(
        country: String,
        stateOrProvince: String = "",
        schoolDistrict: String = "",
        stageOrGrade: String,
        subject: String
    ): Resolution {
        val normalizedCountry = country.trim()
        val normalizedSubnational = listOf(stateOrProvince.trim(), schoolDistrict.trim())
            .filter { it.isNotBlank() }
            .joinToString(" / ")
        val registry = CurriculumJurisdictionRegistry.find(normalizedCountry)

        if (UkNationalCurriculumCatalog.isUnitedKingdom(normalizedCountry)) {
            val nation = UkNationalCurriculumCatalog.nationFor(normalizedCountry, schoolDistrict)
            val authority = nation?.authority ?: "Devolved national education authorities"
            val url = nation?.officialUrl ?: "https://www.gov.uk/national-curriculum"
            return Resolution(
                jurisdictionId = "UK${nation?.name?.let { "_${it}" } ?: ""}",
                countryOrTerritory = normalizedCountry,
                subnationalJurisdiction = nation?.displayName ?: normalizedSubnational,
                educationAuthority = authority,
                officialSourceUrl = url,
                stageOrGrade = stageOrGrade,
                subject = subject,
                confidence = if (nation != null) Resolution.Confidence.HIGH else Resolution.Confidence.MEDIUM
            )
        }

        if (registry == null) {
            return Resolution(
                jurisdictionId = null,
                countryOrTerritory = normalizedCountry.ifBlank { "Unknown" },
                subnationalJurisdiction = normalizedSubnational,
                educationAuthority = "Official local education authority — jurisdiction not yet seeded",
                officialSourceUrl = "",
                stageOrGrade = stageOrGrade,
                subject = subject,
                confidence = Resolution.Confidence.ROUTING_ONLY,
                sourceType = "UNRESOLVED_JURISDICTION"
            )
        }

        val hasSubnationalSignal = stateOrProvince.isNotBlank() || schoolDistrict.isNotBlank()
        val confidence = when {
            hasSubnationalSignal && registry.educationAuthority.contains("state", ignoreCase = true) -> Resolution.Confidence.MEDIUM
            registry.educationAuthority.contains("national", ignoreCase = true) ||
                registry.educationAuthority.contains("ministry", ignoreCase = true) ||
                registry.educationAuthority.contains("agency", ignoreCase = true) -> Resolution.Confidence.HIGH
            else -> Resolution.Confidence.MEDIUM
        }

        return Resolution(
            jurisdictionId = registry.id,
            countryOrTerritory = registry.countryOrTerritory,
            subnationalJurisdiction = normalizedSubnational,
            educationAuthority = registry.educationAuthority,
            officialSourceUrl = registry.officialCurriculumUrl,
            stageOrGrade = stageOrGrade,
            subject = subject,
            confidence = confidence
        )
    }
}
