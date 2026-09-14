package com.example.data.curriculum.oer

import android.util.Log
import com.example.data.curriculum.CurriculumResolver
import com.example.data.local.AppDatabase
import com.example.data.local.entity.OerCurriculumEntity
import com.example.data.model.EducationalSubject
import com.example.data.model.GradeLevel
import com.example.data.curriculum.uk.UkNationalCurriculumCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class OerCommonsCurriculumService(private val db: AppDatabase) {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    @Volatile
    private var memoryCache: List<OerCommonsCurriculumItem> = emptyList()

    suspend fun initializeAndSeed(): List<OerCommonsCurriculumItem> = withContext(Dispatchers.IO) {
        val dao = db.oerCurriculumDao()
        val count = dao.getCurriculumCount()
        if (count == 0) {
            val preinstalled = PreinstalledOerCurriculumCatalog.getAllPreinstalledCurriculum()
            val entities = preinstalled.map { OerCurriculumEntity.fromDomainModel(it, isPreinstalled = true) }
            dao.insertUnits(entities)
            memoryCache = preinstalled
            Log.d("OerCurriculumService", "Pre-installed ${preinstalled.size} OER Commons K-12 curriculum units into Room database.")
        } else {
            val entities = dao.getAllCurriculumUnitsDirect()
            memoryCache = entities.map { it.toDomainModel() }
            Log.d("OerCurriculumService", "Loaded ${memoryCache.size} OER Commons curriculum units from Room database.")
        }
        if (memoryCache.isEmpty()) {
            memoryCache = PreinstalledOerCurriculumCatalog.getAllPreinstalledCurriculum()
        }
        memoryCache
    }

    suspend fun getAllUnits(): List<OerCommonsCurriculumItem> = withContext(Dispatchers.IO) {
        if (memoryCache.isNotEmpty()) return@withContext memoryCache
        initializeAndSeed()
    }

    suspend fun searchCurriculum(
        query: String,
        subject: EducationalSubject? = null,
        gradeLevel: GradeLevel? = null
    ): List<OerCommonsCurriculumItem> = withContext(Dispatchers.IO) {
        val all = getAllUnits()
        val q = query.trim().lowercase()

        all.filter { item ->
            val matchesSubject = subject == null || item.subject == subject
            val matchesGrade = gradeLevel == null || item.gradeLevel == gradeLevel || item.gradeBand == when (gradeLevel) {
                GradeLevel.PRE_K, GradeLevel.KINDERGARTEN -> OerGradeBand.EARLY_CHILDHOOD
                GradeLevel.GRADE_1, GradeLevel.GRADE_2, GradeLevel.GRADE_3, GradeLevel.GRADE_4, GradeLevel.GRADE_5 -> OerGradeBand.ELEMENTARY
                GradeLevel.GRADE_6, GradeLevel.GRADE_7, GradeLevel.GRADE_8 -> OerGradeBand.MIDDLE_SCHOOL
                GradeLevel.HIGH_SCHOOL -> OerGradeBand.HIGH_SCHOOL
            }

            val matchesQuery = if (q.isBlank()) true else {
                item.unitTitle.lowercase().contains(q) ||
                        item.collectionTitle.lowercase().contains(q) ||
                        item.summary.lowercase().contains(q) ||
                        item.standardCode.lowercase().contains(q) ||
                        item.keyConcepts.any { it.lowercase().contains(q) } ||
                        item.vocabulary.any { it.lowercase().contains(q) } ||
                        item.learningObjectives.any { it.lowercase().contains(q) }
            }

            matchesSubject && matchesGrade && matchesQuery
        }
    }

    suspend fun fetchAndParseOnlineCollection(): OerSyncResult = withContext(Dispatchers.IO) {
        val preinstalled = PreinstalledOerCurriculumCatalog.getAllPreinstalledCurriculum()
        val entities = preinstalled.map { OerCurriculumEntity.fromDomainModel(it, isPreinstalled = true) }

        try {
            val request = Request.Builder()
                .url(PreinstalledOerCurriculumCatalog.OER_COMMONS_BASE_URL)
                .header("User-Agent", "NeuroPath-K12-Educational-App/1.25.00")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val isOnlineLive = response.isSuccessful
                val responseCode = response.code
                db.oerCurriculumDao().insertUnits(entities)
                memoryCache = preinstalled

                if (isOnlineLive) {
                    OerSyncResult(
                        isSuccess = true,
                        sourceTitle = "OER Commons Curated Collections (Online Reachable)",
                        totalUnitsCount = preinstalled.size,
                        message = "OER Commons is reachable (HTTP $responseCode). ${preinstalled.size} pre-installed curriculum units remain available offline."
                    )
                } else {
                    OerSyncResult(
                        isSuccess = false,
                        sourceTitle = "OER Commons Curated Collections (Offline Cache)",
                        totalUnitsCount = preinstalled.size,
                        message = "OER Commons returned HTTP $responseCode. Using ${preinstalled.size} pre-installed curriculum units offline."
                    )
                }
            }
        } catch (e: Exception) {
            db.oerCurriculumDao().insertUnits(entities)
            memoryCache = preinstalled
            Log.w("OerCurriculumService", "OER Commons live probe failed; using offline catalog", e)
            OerSyncResult(
                isSuccess = false,
                sourceTitle = "OER Commons Curated Collections (Offline Cache)",
                totalUnitsCount = preinstalled.size,
                message = "OER Commons could not be reached. Using ${preinstalled.size} pre-installed curriculum units offline."
            )
        }
    }

    suspend fun getCuratedCollectionsDirectoryForGemini(): String = withContext(Dispatchers.IO) {
        val all = getAllUnits()
        val builder = StringBuilder()
        builder.append("OER COMMONS CURATED COLLECTIONS DIRECTORY (https://oercommons.org/curated-collections):\n")
        val grouped = all.groupBy { it.subject }
        grouped.forEach { (subject, items) ->
            builder.append("\n• Subject Domain: ${subject.title} (${items.size} Curated Units):\n")
            items.forEach { item ->
                builder.append("  - [${item.gradeBand.title}] ${item.unitTitle} | Code: ${item.standardCode} | Collection: ${item.collectionTitle} | URL: ${item.oerCommonsUrl}\n")
                builder.append("    Key Concepts: ${item.keyConcepts.joinToString(", ")}\n")
            }
        }
        builder.toString()
    }

    suspend fun retrieveCurriculumContextForTutor(
        query: String,
        studentGrade: GradeLevel,
        studentSubject: EducationalSubject? = null,
        schoolDistrict: String = "",
        country: String = ""
    ): OerTutorCurriculumContext = withContext(Dispatchers.IO) {
        val all = getAllUnits()
        val q = query.trim().lowercase()
        val ukDirectory = if (UkNationalCurriculumCatalog.isUnitedKingdom(country)) {
            UkNationalCurriculumCatalog.directoryText(country, schoolDistrict)
        } else ""

        val inferredSubject = studentSubject ?: when {
            q.contains("math") || q.contains("count") || q.contains("add") || q.contains("algebra") || q.contains("quad") || q.contains("fraction") || q.contains("geometry") || q.contains("trig") || q.contains("number") -> EducationalSubject.MATH
            q.contains("read") || q.contains("phon") || q.contains("spell") || q.contains("word") || q.contains("rhetoric") || q.contains("essay") || q.contains("lit") || q.contains("cer") || q.contains("claim") -> EducationalSubject.READING
            q.contains("sci") || q.contains("bio") || q.contains("cell") || q.contains("physic") || q.contains("dna") || q.contains("force") || q.contains("nature") || q.contains("chem") || q.contains("plate") || q.contains("earth") -> EducationalSubject.SCIENCE
            q.contains("civic") || q.contains("gov") || q.contains("history") || q.contains("court") || q.contains("amend") || q.contains("ecom") || q.contains("geo") || q.contains("map") || q.contains("equator") -> EducationalSubject.SOCIAL_STUDIES
            q.contains("sel") || q.contains("finance") || q.contains("budget") || q.contains("calm") || q.contains("emotion") || q.contains("breath") || q.contains("credit") || q.contains("interest") -> EducationalSubject.LIFE_SKILLS
            else -> null
        }

        val gradeBand = when (studentGrade) {
            GradeLevel.PRE_K, GradeLevel.KINDERGARTEN -> OerGradeBand.EARLY_CHILDHOOD
            GradeLevel.GRADE_1, GradeLevel.GRADE_2, GradeLevel.GRADE_3, GradeLevel.GRADE_4, GradeLevel.GRADE_5 -> OerGradeBand.ELEMENTARY
            GradeLevel.GRADE_6, GradeLevel.GRADE_7, GradeLevel.GRADE_8 -> OerGradeBand.MIDDLE_SCHOOL
            GradeLevel.HIGH_SCHOOL -> OerGradeBand.HIGH_SCHOOL
        }

        val curriculumResolution = CurriculumResolver.resolve(
            country = country,
            stateOrProvince = "",
            schoolDistrict = schoolDistrict,
            stageOrGrade = studentGrade.displayName,
            subject = inferredSubject?.title ?: "GENERAL"
        )

        fun score(item: OerCommonsCurriculumItem): Int {
            if (inferredSubject != null && item.subject != inferredSubject) return Int.MIN_VALUE
            val searchable = buildString {
                append(item.unitTitle.lowercase()).append(' ')
                append(item.collectionTitle.lowercase()).append(' ')
                append(item.summary.lowercase()).append(' ')
                append(item.standardCode.lowercase()).append(' ')
                append(item.keyConcepts.joinToString(" ").lowercase()).append(' ')
                append(item.vocabulary.joinToString(" ").lowercase()).append(' ')
                append(item.learningObjectives.joinToString(" ").lowercase())
            }
            val queryTerms = q.split(Regex("[^a-z0-9]+"))
                .filter { it.length >= 3 }
                .distinct()
            val keywordScore = queryTerms.sumOf { term -> if (searchable.contains(term)) 3 else 0 }
            val exactTitleScore = if (q.isNotBlank() && item.unitTitle.lowercase().contains(q)) 12 else 0
            val gradeScore = when {
                item.gradeLevel == studentGrade -> 8
                item.gradeBand == gradeBand -> 4
                else -> 0
            }
            return keywordScore + exactTitleScore + gradeScore
        }

        val rankedUnits = all.map { it to score(it) }
            .filter { it.second != Int.MIN_VALUE && it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }

        val matchedUnit = rankedUnits.firstOrNull()
            ?: all.firstOrNull { it.gradeLevel == studentGrade }
            ?: all.firstOrNull { it.gradeBand == gradeBand }
            ?: all.firstOrNull()

        if (matchedUnit != null) {
            val objectivesStr = matchedUnit.learningObjectives.joinToString("\n- ")
            val conceptsStr = matchedUnit.keyConcepts.joinToString(", ")
            val socraticStr = matchedUnit.socraticGuidingQuestions.joinToString("\n- ")
            val misconceptionsStr = matchedUnit.commonMisconceptions.joinToString("\n- ")
            val sampleProblem = matchedUnit.practiceProblems.firstOrNull()

            val relatedUnits = all.filter { it.id != matchedUnit.id && it.subject == matchedUnit.subject }.take(2)
            val relatedSummary = if (relatedUnits.isNotEmpty()) {
                "\n\nConnected OER Curated Units (https://oercommons.org/curated-collections):\n" +
                        relatedUnits.joinToString("\n") { "• ${it.unitTitle} (${it.standardCode}) - ${it.oerCommonsUrl}" }
            } else ""

            val formatted = """
                [CURRICULUM ALIGNMENT REPOSITORIES]
                Official jurisdiction resolution: ${curriculumResolution.contextText()}
                ${if (ukDirectory.isNotBlank()) ukDirectory + "\n" else ""}
                [OER COMMONS CURATED COLLECTIONS REPOSITORY BENCHMARK]
                Source Database: OER Commons Curated Collections (https://oercommons.org/curated-collections)
                Collection: ${matchedUnit.collectionTitle}
                Unit: ${matchedUnit.unitTitle}
                Standard Code: ${matchedUnit.standardCode}
                Grade Band: ${matchedUnit.gradeBand.title} (Grade: ${matchedUnit.gradeLevel.displayName})
                Direct Resource URL: ${matchedUnit.oerCommonsUrl}
                License: ${matchedUnit.openLicense}

                Key Concepts & Vocabulary:
                - Concepts: $conceptsStr
                - Terms: ${matchedUnit.vocabulary.joinToString(", ")}

                Learning Objectives:
                - $objectivesStr

                Socratic Inquiries & Guiding Scaffolding:
                - $socraticStr

                Common Student Misconceptions to Address:
                - $misconceptionsStr
                ${if (sampleProblem != null) "\nSample OER Practice Problem: ${sampleProblem.questionPrompt}\nCorrect Solution: ${sampleProblem.correctAnswer} (${sampleProblem.stepByStepExplanation})\nSocratic Hint: ${sampleProblem.socraticClue}" else ""}$relatedSummary
            """.trimIndent()

            val citation = buildString {
                append("Official curriculum authority: ${curriculumResolution.educationAuthority}; ")
                if (curriculumResolution.officialSourceUrl.isNotBlank()) {
                    append("${curriculumResolution.officialSourceUrl}; ")
                }
                if (ukDirectory.isNotBlank()) append("UK official curriculum registry; ")
                append("OER Commons Curated Collections: ${matchedUnit.unitTitle} (${matchedUnit.standardCode}) [https://oercommons.org/curated-collections]")
            }
            val inquiry = matchedUnit.socraticGuidingQuestions.firstOrNull()
                ?: "What part of ${matchedUnit.unitTitle} would you like to explore step-by-step?"

            OerTutorCurriculumContext(
                matchedUnit = matchedUnit,
                formattedContextPrompt = formatted,
                citationSource = citation,
                standardCode = matchedUnit.standardCode,
                inquiryPrompt = inquiry
            )
        } else {
            OerTutorCurriculumContext(
                matchedUnit = null,
                formattedContextPrompt = if (ukDirectory.isNotBlank()) ukDirectory else "OER Commons K-12 Curated Collections standard alignment active (https://oercommons.org/curated-collections).",
                citationSource = if (curriculumResolution.officialSourceUrl.isNotBlank()) curriculumResolution.officialSourceUrl else "OER Commons Curated Collections (https://oercommons.org/curated-collections)",
                standardCode = if (curriculumResolution.educationAuthority.isNotBlank()) "${curriculumResolution.jurisdictionId}.STANDARD" else "OER.K12.STANDARD",
                inquiryPrompt = "What curriculum topic would you like to explore?"
            )
        }
    }
}
