package com.example.data.curriculum.oer

import com.example.data.model.EducationalSubject
import com.example.data.model.GradeLevel

enum class OerGradeBand(val title: String, val description: String) {
    EARLY_CHILDHOOD("Early Childhood (Pre-K & Kindergarten)", "Foundational sensory learning, phonological awareness, and early numeracy"),
    ELEMENTARY("Elementary School (Grades 1-5)", "Core literacy, arithmetic operations, ecosystems, and community concepts"),
    MIDDLE_SCHOOL("Middle School (Grades 6-8)", "Pre-algebra, cellular biology, physical forces, and civics"),
    HIGH_SCHOOL("High School (Grades 9-12)", "Algebra I/II, Geometry, Molecular Genetics, Physics, Rhetoric, Government & Personal Finance")
}

data class OerPracticeProblem(
    val id: String,
    val questionPrompt: String,
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val stepByStepExplanation: String,
    val socraticClue: String
)

data class OerCommonsCurriculumItem(
    val id: String,
    val subject: EducationalSubject,
    val gradeLevel: GradeLevel,
    val gradeBand: OerGradeBand,
    val collectionTitle: String,
    val unitTitle: String,
    val standardCode: String,
    val oerCommonsUrl: String = "https://oercommons.org/curated-collections",
    val openLicense: String = "Creative Commons Attribution 4.0 International (CC BY 4.0)",
    val summary: String,
    val learningObjectives: List<String>,
    val keyConcepts: List<String>,
    val vocabulary: List<String> = emptyList(),
    val essentialQuestions: List<String>,
    val socraticGuidingQuestions: List<String>,
    val commonMisconceptions: List<String>,
    val practiceProblems: List<OerPracticeProblem> = emptyList(),
    val accessibilityAccommodations: List<String> = emptyList()
)

data class OerTutorCurriculumContext(
    val matchedUnit: OerCommonsCurriculumItem?,
    val formattedContextPrompt: String,
    val citationSource: String,
    val standardCode: String,
    val inquiryPrompt: String
)

data class OerSyncResult(
    val isSuccess: Boolean,
    val sourceTitle: String,
    val totalUnitsCount: Int,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
