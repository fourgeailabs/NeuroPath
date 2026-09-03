package com.example.data.curriculum.oer

import com.example.data.model.EducationalSubject
import com.example.data.model.GradeLevel

enum class OerGradeBand(val title: String, val description: String) {
    EARLY_CHILDHOOD("Early Childhood (Pre-K & Kindergarten)", "Foundational sensory learning, phonological awareness, and early numeracy"),
    ELEMENTARY("Elementary School (Grades 1-5)", "Core literacy, arithmetic operations, ecosystems, and community concepts"),
    MIDDLE_SCHOOL("Middle School (Grades 6-8)", "Pre-algebra, cellular biology, physical forces, and civics"),
    HIGH_SCHOOL("High School (Grades 9-12)", "Algebra I/II, Geometry, Molecular Genetics, Physics, Rhetoric, Government & Personal Finance")
}

enum class OerMediaType(val label: String, val emoji: String) {
    VIDEO_LESSON("Video Lesson", "🎬"),
    AUDIO_LECTURE("Audio Lecture", "🎧"),
    READ_ALOUD("Read-Aloud Story", "📖"),
    SCIENCE_SIMULATION("Interactive Simulation", "🔬"),
    PHONICS_SOUNDBOARD("Phonics & Pronunciation", "🗣️")
}

data class OerTranscriptLine(
    val timestampSeconds: Int,
    val speaker: String,
    val text: String
)

data class OerPlaybackCheckpoint(
    val timestampSeconds: Int,
    val title: String,
    val questionPrompt: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)

data class OerMediaResource(
    val id: String,
    val title: String,
    val mediaType: OerMediaType,
    val durationSeconds: Int,
    val description: String,
    val creatorOrSource: String = "OER Commons Curated Collections",
    val videoUrl: String? = null,
    val audioUrl: String? = null,
    val visualSceneKey: String = "DEFAULT",
    val transcript: List<OerTranscriptLine> = emptyList(),
    val checkpoints: List<OerPlaybackCheckpoint> = emptyList(),
    val keyTakeaways: List<String> = emptyList(),
    val sourceUrl: String = "https://oercommons.org/curated-collections"
)

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
    val mediaResources: List<OerMediaResource> = emptyList(),
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

