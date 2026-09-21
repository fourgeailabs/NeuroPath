package com.fourgeailabs.neuropath.data.curriculum.uk

/**
 * Official UK curriculum registry.
 *
 * The UK does not have one single national school curriculum: education is devolved.
 * NeuroPath therefore treats England, Scotland, Wales and Northern Ireland as
 * separate official curriculum authorities while exposing one UK directory to the UI.
 * This registry contains authoritative source links and high-level statutory structure;
 * it is not a replacement for the full legal curriculum documents.
 */
object UkNationalCurriculumCatalog {
    enum class Nation(val displayName: String, val authority: String, val officialUrl: String) {
        ENGLAND(
            "England",
            "Department for Education",
            "https://www.gov.uk/national-curriculum"
        ),
        SCOTLAND(
            "Scotland",
            "Education Scotland",
            "https://education.gov.scot/curriculum-for-excellence/"
        ),
        WALES(
            "Wales",
            "Welsh Government / Hwb",
            "https://hwb.gov.wales/curriculum-for-wales"
        ),
        NORTHERN_IRELAND(
            "Northern Ireland",
            "Department of Education / CCEA",
            "https://www.education-ni.gov.uk/articles/statutory-curriculum"
        )
    }

    data class Framework(
        val nation: Nation,
        val stage: String,
        val ageRange: String,
        val subjects: List<String>,
        val standardsDescription: String,
        val officialUrl: String
    )

    val frameworks: List<Framework> = listOf(
        Framework(
            Nation.ENGLAND,
            "Key Stages 1-2",
            "5-11",
            listOf("English", "Mathematics", "Science", "Computing", "History", "Geography", "Art and Design", "Music", "PE", "Design and Technology", "Languages"),
            "Statutory National Curriculum programmes of study and attainment targets.",
            "https://www.gov.uk/government/publications/national-curriculum-in-england-framework-for-key-stages-1-to-4"
        ),
        Framework(
            Nation.ENGLAND,
            "Key Stages 3-4",
            "11-16",
            listOf("English", "Mathematics", "Science", "Computing", "History", "Geography", "Languages", "Citizenship", "Art and Design", "Music", "PE", "Design and Technology"),
            "Statutory National Curriculum programmes of study; KS4 also connects to approved national qualifications.",
            "https://www.gov.uk/government/publications/national-curriculum-in-england-secondary-curriculum"
        ),
        Framework(
            Nation.SCOTLAND,
            "Curriculum for Excellence: Early-First-Second",
            "3-12",
            listOf("Literacy and English", "Numeracy and Mathematics", "Health and Wellbeing", "Sciences", "Social Studies", "Technologies", "Expressive Arts", "Languages", "Religious and Moral Education"),
            "Experiences and Outcomes and Curriculum for Excellence Benchmarks within the broad general education.",
            "https://education.gov.scot/curriculum-for-excellence/"
        ),
        Framework(
            Nation.SCOTLAND,
            "Curriculum for Excellence: Third-Fourth / Senior Phase",
            "12-18",
            listOf("Literacy", "Numeracy", "Sciences", "Social Studies", "Technologies", "Languages", "Expressive Arts", "Health and Wellbeing"),
            "Curriculum for Excellence Benchmarks, Experiences and Outcomes, and senior-phase qualifications/pathways.",
            "https://education.gov.scot/curriculum-for-excellence/about-curriculum-for-excellence/"
        ),
        Framework(
            Nation.WALES,
            "Curriculum for Wales: Ages 3-16",
            "3-16",
            listOf("Languages, Literacy and Communication", "Mathematics and Numeracy", "Science and Technology", "Health and Well-being", "Humanities", "Expressive Arts"),
            "Statutory Curriculum for Wales framework with four purposes, Areas of Learning and Experience, Statements of What Matters and Principles of Progression.",
            "https://hwb.gov.wales/curriculum-for-wales"
        ),
        Framework(
            Nation.NORTHERN_IRELAND,
            "Foundation Stage and Key Stages 1-2",
            "4-11",
            listOf("Language and Literacy", "Mathematics and Numeracy", "The Arts", "The World Around Us", "Personal Development and Mutual Understanding", "Physical Education", "Religious Education"),
            "Current statutory curriculum minimum content and cross-curricular skills for primary education.",
            "https://www.education-ni.gov.uk/articles/statutory-curriculum"
        ),
        Framework(
            Nation.NORTHERN_IRELAND,
            "Key Stages 3-4",
            "11-16",
            listOf("Language and Literacy", "Mathematics and Numeracy", "Science and Technology", "Environment and Society", "Arts", "Modern Languages", "PE", "Learning for Life and Work", "Religious Education"),
            "Current statutory curriculum, with Key Stage 4 entitlement and national qualification pathways. A new Curriculum 2028 is currently under consultation and is not treated as current law.",
            "https://www.education-ni.gov.uk/articles/statutory-curriculum"
        )
    )

    fun isUnitedKingdom(country: String): Boolean {
        val normalized = country.trim().lowercase()
        return normalized in setOf(
            "uk", "u.k.", "united kingdom", "great britain", "britain",
            "england", "scotland", "wales", "northern ireland"
        )
    }

    fun nationFor(country: String, schoolDistrict: String = ""): Nation? {
        val normalized = "$country $schoolDistrict".lowercase()
        return when {
            normalized.contains("northern ireland") || normalized.contains("belfast") -> Nation.NORTHERN_IRELAND
            normalized.contains("scotland") || normalized.contains("edinburgh") || normalized.contains("glasgow") -> Nation.SCOTLAND
            normalized.contains("wales") || normalized.contains("cardiff") || normalized.contains("swansea") -> Nation.WALES
            normalized.contains("england") || normalized.contains("london") || normalized.contains("manchester") || normalized.contains("birmingham") -> Nation.ENGLAND
            country.trim().equals("uk", ignoreCase = true) || country.trim().equals("united kingdom", ignoreCase = true) -> null
            else -> null
        }
    }

    fun directoryText(country: String, schoolDistrict: String = ""): String {
        val nation = nationFor(country, schoolDistrict)
        val selected = if (nation == null) frameworks else frameworks.filter { it.nation == nation }
        val heading = if (nation == null) "UNITED KINGDOM — DEVOLVED NATIONAL CURRICULUM DIRECTORY" else "${nation.displayName.uppercase()} — OFFICIAL CURRICULUM DIRECTORY"
        return buildString {
            appendLine(heading)
            appendLine("The UK has devolved education systems; do not present these as one identical curriculum.")
            selected.forEach { framework ->
                appendLine("• ${framework.nation.displayName} | ${framework.stage} | Ages ${framework.ageRange}")
                appendLine("  Authority: ${framework.nation.authority}")
                appendLine("  Subjects: ${framework.subjects.joinToString(", ")}")
                appendLine("  Alignment: ${framework.standardsDescription}")
                appendLine("  Official source: ${framework.officialUrl}")
            }
        }
    }
}
