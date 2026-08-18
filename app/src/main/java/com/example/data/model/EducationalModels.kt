package com.example.data.model

enum class NeurodivergentType(val title: String, val description: String, val icon: String) {
    ADHD("ADHD", "Micro-rewards, visual milestones, pacing resets", "⚡"),
    AUTISM_ASD("Autism / ASD", "Predictable structure, low stimulation, special interest themes", "🧩"),
    DYSLEXIA("Dyslexia", "High-legibility fonts, word spacing, read-aloud TTS", "📖"),
    SENSORY_SENSITIVITY("Sensory Sensitivity", "Pastel tones, muted sounds, fidget & breathing tools", "🌿"),
    AUDITORY_PROCESSING("Auditory Processing (APD)", "Visual captions, highlight-as-it-reads, pace control", "🎧"),
    NEUROTYPICAL("Standard Learner", "Engaging structured mastery & adaptive AI pacing", "🌟")
}

enum class WorldTheme(
    val id: String,
    val title: String,
    val emoji: String,
    val buddyName: String,
    val buddyRole: String,
    val greeting: String,
    val primaryHex: Long,
    val secondaryHex: Long,
    val surfaceHex: Long,
    val cardHex: Long
) {
    DINOSAURS(
        id = "dino",
        title = "Dinosaur Safari",
        emoji = "🦖",
        buddyName = "Professor Rex",
        buddyRole = "Prehistoric Paleontologist Buddy",
        greeting = "Rawr! Ready for a prehistoric discovery?",
        primaryHex = 0xFF2D6A4F,
        secondaryHex = 0xFF52B788,
        surfaceHex = 0xFFF1F8F4,
        cardHex = 0xFFD8F3DC
    ),
    SPACE(
        id = "space",
        title = "Cosmic Galaxy",
        emoji = "🚀",
        buddyName = "Commander Nova",
        buddyRole = "Stellar Astro-Explorer",
        greeting = "3... 2... 1... Blast off into learning!",
        primaryHex = 0xFF3D348B,
        secondaryHex = 0xFF7678ED,
        surfaceHex = 0xFFF4F3FA,
        cardHex = 0xFFE2E1F8
    ),
    SUPERHERO(
        id = "superhero",
        title = "Hero League",
        emoji = "🦸‍♂️",
        buddyName = "Captain Courage",
        buddyRole = "Knowledge Guardian",
        greeting = "Your superpower today is curiosity!",
        primaryHex = 0xFF9E2A2B,
        secondaryHex = 0xFFE09F3E,
        surfaceHex = 0xFFFFF8F0,
        cardHex = 0xFFFFE8D6
    ),
    OCEAN(
        id = "ocean",
        title = "Deep Blue Ocean",
        emoji = "🐬",
        buddyName = "Captain Splash",
        buddyRole = "Marine Explorer",
        greeting = "Let's dive deep into exciting adventures!",
        primaryHex = 0xFF0077B6,
        secondaryHex = 0xFF48CAE4,
        surfaceHex = 0xFFF0F9FD,
        cardHex = 0xFFCAF0F8
    ),
    KNIGHTS(
        id = "knights",
        title = "Kingdom Quest",
        emoji = "🏰",
        buddyName = "Sir Lancelot Jr.",
        buddyRole = "Royal Quest Guide",
        greeting = "Welcome, brave scholar, to your noble quest!",
        primaryHex = 0xFF6B4C9A,
        secondaryHex = 0xFFC9A227,
        surfaceHex = 0xFFFBF8FF,
        cardHex = 0xFFEFE8FF
    ),
    MAGIC_ANIMALS(
        id = "magic",
        title = "Enchanted Meadow",
        emoji = "🦄",
        buddyName = "Starla the Unicorn",
        buddyRole = "Magical Guide",
        greeting = "Sprinkling magical sparks of wisdom!",
        primaryHex = 0xFFB5179E,
        secondaryHex = 0xFFF72585,
        surfaceHex = 0xFFFFF0F8,
        cardHex = 0xFFFFD6F0
    ),
    VIDEO_GAMES(
        id = "games",
        title = "Pixel Arcade",
        emoji = "🎮",
        buddyName = "Pixel Bot",
        buddyRole = "Arcade Coach",
        greeting = "Level 1 Start! Power up your brain!",
        primaryHex = 0xFF1B4965,
        secondaryHex = 0xFF62B6CB,
        surfaceHex = 0xFFF2F9FA,
        cardHex = 0xFFBEE9E8
    ),
    MYTHICAL_CREATURES(
        id = "mythical",
        title = "Mythical Creatures & Folklore",
        emoji = "🐉",
        buddyName = "Ignis the Drake",
        buddyRole = "Folklore Lorekeeper & Dragon Guide",
        greeting = "Hail, myth-seeker! Ready to explore legendary tales and ancient wonders?",
        primaryHex = 0xFF581845,
        secondaryHex = 0xFFC70039,
        surfaceHex = 0xFFFFF5F8,
        cardHex = 0xFFFFE3EC
    )
}

enum class EducationalSubject(val id: String, val title: String, val emoji: String, val description: String) {
    MATH("math", "Mathematics", "🔢", "Numbers, counting, patterns, geometry, & logic"),
    READING("reading", "Reading & Phonics", "📖", "Phonemic awareness, vocabulary, & comprehension"),
    SCIENCE("science", "Science & Nature", "🔬", "Living things, planets, physics, & ecosystems"),
    SOCIAL_STUDIES("social", "Social Studies & History", "🌍", "Communities, geography, leaders, & cultures"),
    LIFE_SKILLS("lifeskills", "Life Skills & SEL", "❤️", "Emotions, daily routines, hygiene, & decision making")
}

enum class GradeLevel(val code: String, val displayName: String, val ageRange: String) {
    PRE_K("pre_k", "Pre-K", "Ages 3-4"),
    KINDERGARTEN("k", "Kindergarten", "Ages 5-6"),
    GRADE_1("1", "1st Grade", "Ages 6-7"),
    GRADE_2("2", "2nd Grade", "Ages 7-8"),
    GRADE_3("3", "3rd Grade", "Ages 8-9"),
    GRADE_4("4", "4th Grade", "Ages 9-10"),
    GRADE_5("5", "5th Grade", "Ages 10-11"),
    GRADE_6("6", "6th Grade", "Ages 11-12"),
    GRADE_7("7", "7th Grade", "Ages 12-13"),
    GRADE_8("8", "8th Grade", "Ages 13-14"),
    HIGH_SCHOOL("9_12", "High School (9-12)", "Ages 14-18")
}

data class StateCurriculum(
    val code: String,
    val name: String,
    val standardTitle: String,
    val focusDescription: String
)

val US_STATE_CURRICULA = listOf(
    StateCurriculum("CA", "California", "CA-CCSS & NGSS", "California Common Core & Next Gen Science Standards"),
    StateCurriculum("TX", "Texas", "TEKS (Texas Essential Knowledge)", "Texas Essential Knowledge and Skills Benchmarks"),
    StateCurriculum("NY", "New York", "NYS Next Generation Standards", "New York State P-12 Learning Standards"),
    StateCurriculum("FL", "Florida", "B.E.S.T. Standards", "Florida Benchmarks for Excellent Student Thinking"),
    StateCurriculum("IL", "Illinois", "Illinois Learning Standards", "Aligned to CCSS with Social-Emotional Focus"),
    StateCurriculum("PA", "Pennsylvania", "PA Core Standards", "Pennsylvania Academic & Core Standards"),
    StateCurriculum("OH", "Ohio", "Ohio's Learning Standards", "Ohio State Board of Education Guidelines"),
    StateCurriculum("GA", "Georgia", "GSE (Georgia Standards of Excellence)", "Georgia Standards of Excellence Framework"),
    StateCurriculum("NC", "North Carolina", "NC Standard Course of Study", "NCDPI Standard Course of Study"),
    StateCurriculum("MI", "Michigan", "Michigan Academic Standards", "MDE Grade Level Content Expectations"),
    StateCurriculum("COMMON_CORE", "National Common Core", "CCSS & NGSS National", "United States National Model Standards")
)
