package com.fourgeailabs.neuropath.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fourgeailabs.neuropath.data.curriculum.oer.OerCommonsCurriculumItem
import com.fourgeailabs.neuropath.data.curriculum.oer.OerGradeBand
import com.fourgeailabs.neuropath.data.curriculum.oer.OerPracticeProblem
import com.fourgeailabs.neuropath.data.model.EducationalSubject
import com.fourgeailabs.neuropath.data.model.GradeLevel
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "oer_curriculum_units")
data class OerCurriculumEntity(
    @PrimaryKey
    val id: String,
    val subjectName: String,
    val gradeLevelCode: String,
    val gradeBandName: String,
    val collectionTitle: String,
    val unitTitle: String,
    val standardCode: String,
    val oerCommonsUrl: String,
    val openLicense: String,
    val summary: String,
    val learningObjectivesCsv: String,
    val keyConceptsCsv: String,
    val vocabularyCsv: String,
    val essentialQuestionsCsv: String,
    val socraticGuidingQuestionsCsv: String,
    val commonMisconceptionsCsv: String,
    val practiceProblemsJson: String = "",
    val accommodationsCsv: String = "",
    val isPreinstalled: Boolean = true,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
) {
    /**
     * Maps back to the domain model. Returns null when the stored subject/grade/band codes
     * are not recognized — callers skip such rows instead of silently mislabeling them
     * (e.g. as Math / Grade 1), which would corrupt the tutor's curriculum context.
     */
    fun toDomainModelOrNull(): OerCommonsCurriculumItem? {
        val subject = try {
            EducationalSubject.valueOf(subjectName)
        } catch (_: Exception) {
            null
        }
        val grade = GradeLevel.entries.find { it.code == gradeLevelCode }
        val gradeBand = try {
            OerGradeBand.valueOf(gradeBandName)
        } catch (_: Exception) {
            null
        }
        if (subject == null || grade == null || gradeBand == null) return null

        val objectives = learningObjectivesCsv.split("||").filter { it.isNotBlank() }
        val concepts = keyConceptsCsv.split("||").filter { it.isNotBlank() }
        val vocab = vocabularyCsv.split("||").filter { it.isNotBlank() }
        val essential = essentialQuestionsCsv.split("||").filter { it.isNotBlank() }
        val socratic = socraticGuidingQuestionsCsv.split("||").filter { it.isNotBlank() }
        val misconceptions = commonMisconceptionsCsv.split("||").filter { it.isNotBlank() }
        val accommodations = accommodationsCsv.split("||").filter { it.isNotBlank() }

        return OerCommonsCurriculumItem(
            id = id,
            subject = subject,
            gradeLevel = grade,
            gradeBand = gradeBand,
            collectionTitle = collectionTitle,
            unitTitle = unitTitle,
            standardCode = standardCode,
            oerCommonsUrl = oerCommonsUrl,
            openLicense = openLicense,
            summary = summary,
            learningObjectives = objectives,
            keyConcepts = concepts,
            vocabulary = vocab,
            essentialQuestions = essential,
            socraticGuidingQuestions = socratic,
            commonMisconceptions = misconceptions,
            practiceProblems = decodePracticeProblems(practiceProblemsJson),
            accessibilityAccommodations = accommodations
        )
    }

    companion object {
        fun fromDomainModel(item: OerCommonsCurriculumItem, isPreinstalled: Boolean = true): OerCurriculumEntity {
            return OerCurriculumEntity(
                id = item.id,
                subjectName = item.subject.name,
                gradeLevelCode = item.gradeLevel.code,
                gradeBandName = item.gradeBand.name,
                collectionTitle = item.collectionTitle,
                unitTitle = item.unitTitle,
                standardCode = item.standardCode,
                oerCommonsUrl = item.oerCommonsUrl,
                openLicense = item.openLicense,
                summary = item.summary,
                learningObjectivesCsv = item.learningObjectives.joinToString("||"),
                keyConceptsCsv = item.keyConcepts.joinToString("||"),
                vocabularyCsv = item.vocabulary.joinToString("||"),
                essentialQuestionsCsv = item.essentialQuestions.joinToString("||"),
                socraticGuidingQuestionsCsv = item.socraticGuidingQuestions.joinToString("||"),
                commonMisconceptionsCsv = item.commonMisconceptions.joinToString("||"),
                practiceProblemsJson = encodePracticeProblems(item.practiceProblems),
                accommodationsCsv = item.accessibilityAccommodations.joinToString("||"),
                isPreinstalled = isPreinstalled,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
        }

        /** Serializes practice problems to JSON so they survive the Room round-trip. */
        fun encodePracticeProblems(problems: List<OerPracticeProblem>): String {
            if (problems.isEmpty()) return ""
            return try {
                val arr = JSONArray()
                problems.forEach { p ->
                    arr.put(
                        JSONObject()
                            .put("id", p.id)
                            .put("q", p.questionPrompt)
                            .put("opts", JSONArray(p.options))
                            .put("a", p.correctAnswer)
                            .put("expl", p.stepByStepExplanation)
                            .put("clue", p.socraticClue)
                    )
                }
                arr.toString()
            } catch (_: Exception) {
                ""
            }
        }

        /** Parses [encodePracticeProblems] output; corrupt payloads yield an empty list. */
        fun decodePracticeProblems(json: String): List<OerPracticeProblem> {
            if (json.isBlank()) return emptyList()
            return runCatching {
                val arr = JSONArray(json)
                List(arr.length()) { i ->
                    val o = arr.getJSONObject(i)
                    val opts = o.optJSONArray("opts")
                    OerPracticeProblem(
                        id = o.optString("id"),
                        questionPrompt = o.optString("q"),
                        options = List(opts?.length() ?: 0) { j -> opts!!.optString(j) },
                        correctAnswer = o.optString("a"),
                        stepByStepExplanation = o.optString("expl"),
                        socraticClue = o.optString("clue")
                    )
                }
            }.getOrDefault(emptyList())
        }
    }
}
