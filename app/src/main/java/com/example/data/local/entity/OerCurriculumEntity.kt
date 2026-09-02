package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.curriculum.oer.OerCommonsCurriculumItem
import com.example.data.curriculum.oer.OerGradeBand
import com.example.data.curriculum.oer.OerPracticeProblem
import com.example.data.model.EducationalSubject
import com.example.data.model.GradeLevel

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
    fun toDomainModel(): OerCommonsCurriculumItem {
        val subject = try {
            EducationalSubject.valueOf(subjectName)
        } catch (_: Exception) {
            EducationalSubject.MATH
        }

        val grade = GradeLevel.values().find { it.code == gradeLevelCode } ?: GradeLevel.GRADE_1

        val gradeBand = try {
            OerGradeBand.valueOf(gradeBandName)
        } catch (_: Exception) {
            OerGradeBand.ELEMENTARY
        }

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
            practiceProblems = emptyList(), // Parsed when needed
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
                practiceProblemsJson = "",
                accommodationsCsv = item.accessibilityAccommodations.joinToString("||"),
                isPreinstalled = isPreinstalled,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
        }
    }
}
