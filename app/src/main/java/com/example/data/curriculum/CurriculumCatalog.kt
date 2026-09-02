package com.example.data.curriculum

import com.example.data.model.ConceptStep
import com.example.data.model.EducationalSubject
import com.example.data.model.FullLesson
import com.example.data.model.GradeLevel
import com.example.data.model.QuestionItem

object CurriculumCatalog {

    const val OER_COMMONS_COLLECTIONS_URL = "https://oercommons.org/curated-collections"

    fun getLessonsForSubjectAndGrade(
        subject: EducationalSubject,
        gradeLevel: GradeLevel,
        stateStandardCode: String,
        themeWorldId: String = "dino",
        country: String = "United States"
    ): List<FullLesson> {
        val all = getMasterCurriculum(themeWorldId, country)
        val exactMatches = all.filter { it.subject == subject && it.gradeLevel == gradeLevel }
        if (exactMatches.isNotEmpty()) return exactMatches

        val isSecondary = gradeLevel == GradeLevel.HIGH_SCHOOL ||
                gradeLevel == GradeLevel.GRADE_8 ||
                gradeLevel == GradeLevel.GRADE_7 ||
                gradeLevel == GradeLevel.GRADE_6

        val tierMatches = all.filter {
            it.subject == subject && if (isSecondary) {
                it.gradeLevel == GradeLevel.HIGH_SCHOOL || it.gradeLevel == GradeLevel.GRADE_8
            } else {
                it.gradeLevel == GradeLevel.KINDERGARTEN || it.gradeLevel == GradeLevel.GRADE_1
            }
        }
        if (tierMatches.isNotEmpty()) return tierMatches

        return all.filter { it.subject == subject }
    }

    fun getLessonById(id: String, themeWorldId: String = "dino", country: String = "United States"): FullLesson? {
        return getMasterCurriculum(themeWorldId, country).find { it.id == id }
    }

    fun getMasterCurriculum(themeId: String = "dino", country: String = "United States"): List<FullLesson> {
        val isUk = country.contains("United Kingdom", ignoreCase = true) || country.equals("GB", ignoreCase = true) || country.equals("UK", ignoreCase = true)
        val isCa = country.contains("Canada", ignoreCase = true)
        val isAu = country.contains("Australia", ignoreCase = true)
        val isIndia = country.contains("India", ignoreCase = true)
        val isDe = country.contains("Germany", ignoreCase = true)
        val isFr = country.contains("France", ignoreCase = true)
        val isJp = country.contains("Japan", ignoreCase = true)

        val mathStandard = when {
            isUk -> "DfE.KS1.MATH.01 (UK Key Stage 1)"
            isCa -> "ONTARIO.MATH.GR.1 (Ontario Ministry Curriculum)"
            isAu -> "ACARA.AC9M1N01 (Australian Curriculum F-10)"
            isIndia -> "NCERT.MATH.P1 (NEP 2020 Foundational Stage)"
            isDe -> "KMK.MATH.PR1 (KMK Bildungsstandards)"
            isFr -> "SOCLE.FR.MATH.C2 (Cycle 2 Éducation Nationale)"
            isJp -> "MEXT.MATH.E1 (文部科学省 学習指導要領)"
            else -> "OER.COMMONS.K12.MATH / CCSS.MATH.CONTENT.K.OA.A.1 (OER Commons Curated Collections)"
        }

        val readingStandard = when {
            isUk -> "DfE.KS1.ENG.READ (UK Letters and Sounds Phonics)"
            isCa -> "ONTARIO.LANG.GR.1 (Ontario Language Curriculum)"
            isAu -> "ACARA.AC9E1LY01 (Australian English Literacy)"
            isIndia -> "NCERT.ENG.LANG.01 (NCERT English Foundational)"
            isDe -> "KMK.DEU.PR1 (KMK Grundschul-Lehrplan)"
            isFr -> "SOCLE.FR.FRANCAIS (Cycle 2 Français)"
            isJp -> "MEXT.KOKUGO.E1 (文部科学省 国語科指導要領)"
            else -> "OER.COMMONS.K12.ELA / CCSS.ELA-LITERACY.RF.K.1 (OER Commons Curated Collections)"
        }

        val scienceStandard = when {
            isUk -> "DfE.KS1.SCI.01 (UK National Curriculum Science)"
            isCa -> "ONTARIO.SCI.GR.1 (Ontario Science & Technology)"
            isAu -> "ACARA.AC9S1U01 (Australian Science Understanding)"
            isIndia -> "NCERT.EVS.P1 (NCERT Environmental Studies)"
            isDe -> "KMK.SU.PR1 (Sachunterricht Bildungsplan)"
            isFr -> "SOCLE.FR.SCI.C2 (Questionner le monde)"
            isJp -> "MEXT.RIKA.E1 (文部科学省 生活科・理科)"
            else -> "OER.COMMONS.K12.SCI / NGSS.K-LS1-1 (OER Commons Curated Collections)"
        }

        val socialStandard = when {
            isUk -> "DfE.KS1.HIST.GEO (UK History & Geography)"
            isCa -> "ONTARIO.SS.GR.1 (Social Studies Heritage & Community)"
            isAu -> "ACARA.AC9HS1K01 (Australian HASS Framework)"
            isIndia -> "NCERT.SOC.P1 (NCERT Social Science & Civics)"
            isDe -> "KMK.SU.GEMEIN (Gesellschaftliches Lernen)"
            isFr -> "SOCLE.FR.EMC (Enseignement moral et civique)"
            isJp -> "MEXT.SHAKAI.E1 (文部科学省 生活科・社会科)"
            else -> "OER.COMMONS.K12.SOC / NCSS.D2.Civ.1.K-2 (OER Commons Curated Collections)"
        }

        // High School Standards (OER Commons Curated Collections https://oercommons.org/curated-collections)
        val hsMathStandard = "OER.COMMONS.HS.MATH.ALG1 / CCSS.MATH.HSA.REI.B.4 (OER Commons High School Algebra Collection)"
        val hsReadingStandard = "OER.COMMONS.HS.ELA.LIT / CCSS.ELA-LITERACY.RL.9-10.1 (OER Commons High School ELA Collection)"
        val hsScienceStandard = "OER.COMMONS.HS.SCI.BIO / NGSS.HS-LS1-1 (OER Commons High School Science Collection)"
        val hsSocialStandard = "OER.COMMONS.HS.SOC.CIVICS / NCSS.D2.Civ.2.9-12 (OER Commons High School Civics Collection)"
        val hsLifeSkillsStandard = "OER.COMMONS.HS.LIFESKILLS / CASEL.SEL.RESPONSIBLE_DECISION (OER Commons High School Readiness)"

        return listOf(
            // ==========================================
            // ELEMENTARY / FOUNDATIONAL (K-5)
            // ==========================================

            // 1. MATHEMATICS: Addition & Counting Patterns (20 Questions Adaptive)
            FullLesson(
                id = "math_counting_patterns",
                subject = EducationalSubject.MATH,
                gradeLevel = GradeLevel.KINDERGARTEN,
                stateStandardCode = mathStandard,
                standardDescription = "Master foundational number sense, addition models, skip counting, and geometry in strict alignment with OER Commons Curated Collections ($OER_COMMONS_COLLECTIONS_URL).",
                title = "Counting Clusters & Stepping Patterns",
                summary = "Discover how combining smaller groups makes bigger numbers using visual stepping stones!",
                themeWorldId = themeId,
                teachSteps = listOf(
                    ConceptStep(
                        stepNumber = 1,
                        title = "What is Adding?",
                        text = "Adding means putting groups together to find the total! Imagine having 2 items in one hand, and finding 3 more.",
                        visualEmoji = "🦖 + 🦖🦖 = 🦖🦖🦖",
                        tipOrFunFact = "Fun Tip: You can count on your fingers or tap each item one by one!",
                        interactivePrompt = "If we have 2 stars and find 2 more, how many do we have?",
                        interactiveAnswers = listOf("3 Stars", "4 Stars", "5 Stars"),
                        interactiveCorrectIndex = 1
                    ),
                    ConceptStep(
                        stepNumber = 2,
                        title = "Counting On Strategy",
                        text = "Instead of starting from 1 every time, start with the bigger number in your mind and count forward!",
                        visualEmoji = "5 ➡️ (6, 7, 8) 🎯",
                        tipOrFunFact = "Neuro-Tip: Keeping the big number in your head saves your brain energy!",
                        interactivePrompt = "Start at 6 and count 2 more steps: 6... 7...",
                        interactiveAnswers = listOf("8", "9", "7"),
                        interactiveCorrectIndex = 0
                    ),
                    ConceptStep(
                        stepNumber = 3,
                        title = "Number Pairs to Ten (Friends of 10)",
                        text = "Certain number pairs always fit together like puzzle pieces to make 10! Like 5+5, 8+2, 7+3, and 9+1.",
                        visualEmoji = "🧩 7 + 3 = 10 🧩",
                        tipOrFunFact = "Making 10 makes mental math quick and super smooth!",
                        interactivePrompt = "What number is the puzzle buddy for 8 to make 10?",
                        interactiveAnswers = listOf("1", "2", "3"),
                        interactiveCorrectIndex = 1
                    )
                ),
                questions = generate20MathQuestions(themeId)
            ),

            // 2. READING & PHONICS: Word Families & Phonics
            FullLesson(
                id = "reading_phonemic_awareness",
                subject = EducationalSubject.READING,
                gradeLevel = GradeLevel.KINDERGARTEN,
                stateStandardCode = readingStandard,
                standardDescription = "Demonstrate phonological awareness, sound blending, and reading comprehension aligned with OER Commons Curated Collections ($OER_COMMONS_COLLECTIONS_URL).",
                title = "Phoneme Blends & Sound Builders",
                summary = "Unlock secret words by listening to starting, middle, and ending vowel sounds!",
                themeWorldId = themeId,
                teachSteps = listOf(
                    ConceptStep(
                        stepNumber = 1,
                        title = "The Power of Letter Sounds",
                        text = "Every letter makes its own special vibration or sound. When we snap them together like train cars, we read real words!",
                        visualEmoji = "🚂 [C] + [A] + [T] = CAT 🐱",
                        tipOrFunFact = "Saying each sound out loud while tapping your arm helps memory stick!",
                        interactivePrompt = "Which sound does 'S' make?",
                        practiceOptions = listOf("Ssss like a snake", "Buh like a ball", "Mmm like yummy"),
                        correctOptionIndex = 0
                    ),
                    ConceptStep(
                        stepNumber = 2,
                        title = "Rhyming Word Families",
                        text = "Words that rhyme share the exact same ending sound! For example: CAT, BAT, HAT, and MAT all belong to the '-AT' family.",
                        visualEmoji = "🎩 🦇 🐱 🚪",
                        tipOrFunFact = "Listen to the end of the word — it bounces the same way!",
                        interactivePrompt = "Which word rhymes with SUN?",
                        practiceOptions = listOf("RUN", "CAR", "DOG"),
                        correctOptionIndex = 0
                    ),
                    ConceptStep(
                        stepNumber = 3,
                        title = "Sight Words: Super Fast Helpers",
                        text = "Sight words are common words like 'THE', 'AND', 'IS', and 'YOU' that appear everywhere in stories. Spot them instantly!",
                        visualEmoji = "👀 'THE' 'AND' 'CAN' 🚀",
                        tipOrFunFact = "Recognizing sight words helps you read whole adventure stories without stopping!",
                        interactivePrompt = "Choose the sight word spelled correctly:",
                        practiceOptions = listOf("THE", "TEH", "HET"),
                        correctOptionIndex = 0
                    )
                ),
                questions = generate20ReadingQuestions(themeId)
            ),

            // 3. SCIENCE & NATURE: Solar System, Habitats & Forces
            FullLesson(
                id = "science_ecosystems_forces",
                subject = EducationalSubject.SCIENCE,
                gradeLevel = GradeLevel.GRADE_1,
                stateStandardCode = scienceStandard,
                standardDescription = "Observe patterns, living organism requirements, forces, and environments aligned with OER Commons Curated Collections ($OER_COMMONS_COLLECTIONS_URL).",
                title = "Habitats, Gravity & Living Wonders",
                summary = "Explore how animals adapt, how gravity pulls objects, and why our planet is unique!",
                themeWorldId = themeId,
                teachSteps = listOf(
                    ConceptStep(
                        stepNumber = 1,
                        title = "What is an Ecosystem Habitat?",
                        text = "A habitat is a natural home where a plant or animal finds food, fresh water, and cozy shelter to grow.",
                        visualEmoji = "🌲 🌊 🏜️ ❄️",
                        tipOrFunFact = "A camel's long eyelashes protect it from blowing desert sand!",
                        interactivePrompt = "Where does a dolphin feel most at home?",
                        practiceOptions = listOf("The Ocean", "A Pine Forest", "A Sandy Desert"),
                        correctOptionIndex = 0
                    ),
                    ConceptStep(
                        stepNumber = 2,
                        title = "Pushes, Pulls & Gravity",
                        text = "Forces make things move! A push moves an object away, a pull brings it closer, and gravity pulls everything down toward Earth.",
                        visualEmoji = "🧲 ⬇️ 🍎 🚀",
                        tipOrFunFact = "Without gravity, we would float right up into the clouds like balloons!",
                        interactivePrompt = "When you drop a bouncy ball, what pulls it back to the ground?",
                        practiceOptions = listOf("Gravity", "Electricity", "Wind"),
                        correctOptionIndex = 0
                    ),
                    ConceptStep(
                        stepNumber = 3,
                        title = "Day, Night & Sun Energy",
                        text = "Earth spins around like a gentle merry-go-round! When your side faces the Sun, it is bright daytime.",
                        visualEmoji = "☀️ 🌍 🌙",
                        tipOrFunFact = "Plants use sunlight to make delicious food through photosynthesis!",
                        interactivePrompt = "What provides warm light and energy to all Earth's plants?",
                        practiceOptions = listOf("The Sun", "The Moon", "Flashlights"),
                        correctOptionIndex = 0
                    )
                ),
                questions = generate20ScienceQuestions(themeId)
            ),

            // 4. SOCIAL STUDIES & GEOGRAPHY
            FullLesson(
                id = "social_community_helpers",
                subject = EducationalSubject.SOCIAL_STUDIES,
                gradeLevel = GradeLevel.GRADE_1,
                stateStandardCode = socialStandard,
                standardDescription = "Explain how community helpers, rules, and geography contribute to a thriving society in alignment with OER Commons Curated Collections ($OER_COMMONS_COLLECTIONS_URL).",
                title = "Community Helpers, Maps & Traditions",
                summary = "Discover how diverse helpers, kindness rules, and maps connect our vibrant neighborhoods!",
                themeWorldId = themeId,
                teachSteps = listOf(
                    ConceptStep(
                        stepNumber = 1,
                        title = "Who are Community Helpers?",
                        text = "Community helpers are people who work together to keep us safe, healthy, educated, and well-fed!",
                        visualEmoji = "👩‍⚕️ 🚒 🧑‍🏫 👮‍♂️",
                        tipOrFunFact = "Doctors, teachers, firefighters, and sanitation workers all work as a team!",
                        interactivePrompt = "Who helps extinguish fires and rescue pets?",
                        interactiveAnswers = listOf("Firefighters", "Librarians", "Chefs"),
                        interactiveCorrectIndex = 0
                    ),
                    ConceptStep(
                        stepNumber = 2,
                        title = "Reading Simple Maps & Compasses",
                        text = "A map is a bird's-eye view drawing of a place. A compass rose shows four main directions: North, South, East, and West.",
                        visualEmoji = "🗺️ 🧭 ⬆️ North",
                        tipOrFunFact = "Remember: Never Eat Soggy Waffles = North, East, South, West!",
                        interactivePrompt = "What direction points straight up on most world maps?",
                        interactiveAnswers = listOf("North", "South", "East"),
                        interactiveCorrectIndex = 0
                    ),
                    ConceptStep(
                        stepNumber = 3,
                        title = "Respect, Diversity & Rules",
                        text = "Rules help everyone stay safe and fair. Everyone has unique talents and traditions that make our world wonderful.",
                        visualEmoji = "🤝 🌍 🌈",
                        tipOrFunFact = "Listening to friends helps us understand different viewpoints!",
                        interactivePrompt = "Why do classrooms have rules?",
                        interactiveAnswers = listOf("To keep everyone safe and happy", "To make tests harder", "To stop playing"),
                        interactiveCorrectIndex = 0
                    )
                ),
                questions = generate20SocialStudiesQuestions(themeId)
            ),

            // 5. LIFE SKILLS & SEL (Social Emotional Learning)
            FullLesson(
                id = "sel_feelings_regulation",
                subject = EducationalSubject.LIFE_SKILLS,
                gradeLevel = GradeLevel.KINDERGARTEN,
                stateStandardCode = "OER.COMMONS.SEL.AWARENESS / CASEL.SEL.SELF_AWARENESS",
                standardDescription = "Recognize one's own emotions, thoughts, and values and how they influence behavior.",
                title = "Emotional Thermometer & Calm Reset Tools",
                summary = "Learn to identify internal feelings, recognize sensory overload, and use calming self-soothing superpowers!",
                themeWorldId = themeId,
                teachSteps = listOf(
                    ConceptStep(
                        stepNumber = 1,
                        title = "All Feelings are Normal & OK!",
                        text = "Sometimes we feel joyful and energetic, sometimes tired, frustrated, or overwhelmed. All emotions are valid signals from your brain!",
                        visualEmoji = "😊 😔 😡 😮",
                        tipOrFunFact = "Emotions are like weather clouds — they roll in, teach us something, and gently pass by!",
                        interactivePrompt = "What should you do when you feel frustrated?",
                        interactiveAnswers = listOf("Take a slow breath or sensory break", "Scream loudly", "Give up completely"),
                        interactiveCorrectIndex = 0
                    ),
                    ConceptStep(
                        stepNumber = 2,
                        title = "The 4-7-8 Reset Superpower",
                        text = "When your heart beats fast or sensory inputs feel too loud: Inhale through nose for 4s, Hold gently for 7s, Exhale slow for 8s.",
                        visualEmoji = "🌬️ 4s Inhale ➡️ 7s Hold ➡️ 8s Exhale 🧘",
                        tipOrFunFact = "Long exhales tell your nervous system that you are 100% safe right now.",
                        interactivePrompt = "How does deep, slow breathing help your body?",
                        interactiveAnswers = listOf("It calms down heart rate and nervous system", "It makes you run faster", "It turns off the lights"),
                        interactiveCorrectIndex = 0
                    ),
                    ConceptStep(
                        stepNumber = 3,
                        title = "Asking for Space & Communicating Needs",
                        text = "If sounds are too noisy or a task is hard, you can say: 'I need a sensory break' or 'Can I have some quiet time please?'",
                        visualEmoji = "🎧 💬 🛑",
                        tipOrFunFact = "Advocating for your sensory needs is a true sign of strength and emotional intelligence!",
                        interactivePrompt = "What is a kind way to ask for quiet time?",
                        interactiveAnswers = listOf("'May I please have a quiet sensory break?'", "'Leave me alone!'", "Throwing pencils"),
                        interactiveCorrectIndex = 0
                    )
                ),
                questions = generate20LifeSkillsQuestions(themeId)
            ),

            // ==========================================
            // HIGH SCHOOL (GRADES 9-12) CURRICULUM
            // Pulling from OER Commons Curated Collections
            // (https://oercommons.org/curated-collections)
            // ==========================================

            // 6. HIGH SCHOOL MATHEMATICS: Algebra I & II, Quadratics & Functions
            FullLesson(
                id = "math_highschool_algebra",
                subject = EducationalSubject.MATH,
                gradeLevel = GradeLevel.HIGH_SCHOOL,
                stateStandardCode = hsMathStandard,
                standardDescription = "Solve quadratic equations by factoring, completing the square, and using the quadratic formula. Model exponential and polynomial systems from OER Commons Curated Collections ($OER_COMMONS_COLLECTIONS_URL).",
                title = "Quadratic Equations, Systems & Function Models",
                summary = "Master quadratic relationships, parabolic trajectories, and multi-variable equation systems with high school rigor.",
                themeWorldId = themeId,
                teachSteps = listOf(
                    ConceptStep(
                        stepNumber = 1,
                        title = "Anatomy of a Quadratic Function",
                        text = "A quadratic equation has the standard form ax² + bx + c = 0 (where a ≠ 0). Its graphical representation is a symmetric parabola with a vertex (maximum or minimum) and line of symmetry at x = -b/(2a).",
                        visualEmoji = "📈 f(x) = ax² + bx + c 🎯",
                        tipOrFunFact = "Physics Connection: The trajectory of any thrown projectile or orbiting satellite follows a quadratic parabolic curve!",
                        interactivePrompt = "For the quadratic equation y = x² - 6x + 8, what is the x-coordinate of the vertex (-b/2a)?",
                        interactiveAnswers = listOf("x = 3", "x = -3", "x = 6"),
                        interactiveCorrectIndex = 0
                    ),
                    ConceptStep(
                        stepNumber = 2,
                        title = "Factoring & The Zero Product Property",
                        text = "When ax² + bx + c factors into (x - p)(x - q) = 0, the solutions (roots or x-intercepts) are x = p and x = q because if either factor is 0, their product is 0.",
                        visualEmoji = "🧩 (x - 4)(x - 2) = 0 ➡️ x = 4, 2",
                        tipOrFunFact = "Mental Math Tip: To factor x² + bx + c, look for two numbers that multiply to 'c' and add up to 'b'.",
                        interactivePrompt = "What are the solutions to x² - 5x + 6 = 0? (Find numbers that multiply to 6 and add to -5)",
                        interactiveAnswers = listOf("x = 2 and x = 3", "x = -2 and x = -3", "x = 1 and x = 6"),
                        interactiveCorrectIndex = 0
                    ),
                    ConceptStep(
                        stepNumber = 3,
                        title = "The Quadratic Formula & Discriminant",
                        text = "For any quadratic, x = (-b ± √(b² - 4ac)) / (2a). The discriminant Δ = b² - 4ac reveals the nature of roots: Δ > 0 (two real roots), Δ = 0 (one repeated root), Δ < 0 (two complex/imaginary roots).",
                        visualEmoji = "📐 Δ = b² - 4ac ⚡",
                        tipOrFunFact = "Neuro-Tip: If Δ is a perfect square (like 1, 4, 9, 16, 25), the quadratic factors neatly with rational numbers!",
                        interactivePrompt = "If a quadratic has a discriminant of Δ = 0, how many unique real roots does it possess?",
                        interactiveAnswers = listOf("Exactly 1 real root", "2 distinct real roots", "No real roots"),
                        interactiveCorrectIndex = 0
                    )
                ),
                questions = generate20HighSchoolMathQuestions(themeId)
            ),

            // 7. HIGH SCHOOL READING & ELA: Rhetorical Analysis & Synthesis
            FullLesson(
                id = "reading_highschool_rhetoric",
                subject = EducationalSubject.READING,
                gradeLevel = GradeLevel.HIGH_SCHOOL,
                stateStandardCode = hsReadingStandard,
                standardDescription = "Cite strong and thorough textual evidence, analyze author's rhetoric (Ethos, Pathos, Logos), and evaluate central themes across complex literary texts from OER Commons Curated Collections ($OER_COMMONS_COLLECTIONS_URL).",
                title = "Critical Rhetoric, Literary Analysis & Synthesis",
                summary = "Deconstruct classical and modern arguments, evaluate rhetorical appeals, and synthesize evidence for academic essays.",
                themeWorldId = themeId,
                teachSteps = listOf(
                    ConceptStep(
                        stepNumber = 1,
                        title = "The Aristotelian Rhetorical Appeals",
                        text = "Authors persuade audiences through three core pillars: Ethos (establishing authority and ethical credibility), Logos (logical reasoning, statistics, and syllogisms), and Pathos (appealing to emotional and visceral human empathy).",
                        visualEmoji = "⚖️ Ethos (Credibility) | Logos (Logic) | Pathos (Emotion)",
                        tipOrFunFact = "Critical Reading Tip: Strong academic arguments rely primarily on Logos supported by Ethos, rather than purely emotional Pathos.",
                        interactivePrompt = "A scientist citing 15 peer-reviewed clinical trials and statistical data is primarily employing which rhetorical appeal?",
                        practiceOptions = listOf("Logos (Logic & Evidence)", "Pathos (Emotion)", "Hyperbole (Exaggeration)"),
                        correctOptionIndex = 0
                    ),
                    ConceptStep(
                        stepNumber = 2,
                        title = "Tone, Diction & Subtext",
                        text = "Diction (word choice) determines the tone and nuance of a passage. Authors deliberately choose words with specific connotations (emotional weight) to guide the reader's interpretation beyond the literal denotation.",
                        visualEmoji = "🔍 Connotation vs. Denotation 📖",
                        tipOrFunFact = "Notice how describing a room as 'serene' vs 'vacant' creates entirely opposite moods despite both meaning empty!",
                        interactivePrompt = "Which word possesses the most positive connotation for someone who spends money carefully?",
                        practiceOptions = listOf("Frugal / Economical", "Stingy / Cheap", "Careless"),
                        correctOptionIndex = 0
                    ),
                    ConceptStep(
                        stepNumber = 3,
                        title = "Synthesizing Counterarguments",
                        text = "A sophisticated thesis acknowledges and refutes opposing perspectives through concession and rebuttal, demonstrating intellectual depth and balanced critical analysis.",
                        visualEmoji = "💡 Concession ➡️ Rebuttal ➡️ Strong Synthesis 🛡️",
                        tipOrFunFact = "Using phrases like 'Although proponents argue X, recent empirical evidence indicates Y' strengthens your analytical authority.",
                        interactivePrompt = "What is the purpose of addressing a counterargument in an analytical essay?",
                        practiceOptions = listOf("To demonstrate thorough research and strengthen the central claim", "To confuse the reader", "To make the paper twice as long"),
                        correctOptionIndex = 0
                    )
                ),
                questions = generate20HighSchoolReadingQuestions(themeId)
            ),

            // 8. HIGH SCHOOL SCIENCE: Cellular Biology, Genetics & Newtonian Mechanics
            FullLesson(
                id = "science_highschool_biology_physics",
                subject = EducationalSubject.SCIENCE,
                gradeLevel = GradeLevel.HIGH_SCHOOL,
                stateStandardCode = hsScienceStandard,
                standardDescription = "Construct explanations for cellular respiration, DNA transcription/translation, Mendelian inheritance, and Newtonian mechanics from OER Commons Curated Collections ($OER_COMMONS_COLLECTIONS_URL).",
                title = "Cellular Biology, DNA Genetics & Physical Dynamics",
                summary = "Explore molecular biochemistry, genetic inheritance, chemical thermodynamics, and universal physical laws.",
                themeWorldId = themeId,
                teachSteps = listOf(
                    ConceptStep(
                        stepNumber = 1,
                        title = "The Central Dogma of Molecular Biology",
                        text = "Genetic information flows in a universal direction: DNA (transcription inside nucleus) ➡️ mRNA ➡️ Ribosome (translation) ➡️ Amino Acid Polypeptide Chain (Protein). Proteins execute almost all biochemical cell functions.",
                        visualEmoji = "🧬 DNA ➡️ 📜 mRNA ➡️ ⚙️ Functional Protein",
                        tipOrFunFact = "The human genome contains approximately 3 billion base pairs of DNA packed into the nucleus of almost every cell!",
                        interactivePrompt = "During transcription, which nitrogenous base in RNA pairs with Adenine (A) on the DNA template?",
                        practiceOptions = listOf("Uracil (U)", "Thymine (T)", "Cytosine (C)"),
                        correctOptionIndex = 0
                    ),
                    ConceptStep(
                        stepNumber = 2,
                        title = "Cellular Respiration & ATP Energy Currency",
                        text = "Cells convert glucose and oxygen into cellular energy: C₆H₁₂O₆ + 6O₂ ➡️ 6CO₂ + 6H₂O + ~36 ATP. The process occurs in three stages: Glycolysis (cytoplasm), Krebs Cycle, and the Electron Transport Chain (mitochondria).",
                        visualEmoji = "⚡ Glucose + O₂ ➡️ CO₂ + H₂O + ATP 🔋",
                        tipOrFunFact = "ATP (Adenosine Triphosphate) releases energy when its high-energy terminal phosphate bond is hydrolyzed into ADP + Pi.",
                        interactivePrompt = "Which cellular organelle is the primary powerhouse site for the Electron Transport Chain and ATP synthesis?",
                        practiceOptions = listOf("Mitochondrion", "Endoplasmic Reticulum", "Golgi Apparatus"),
                        correctOptionIndex = 0
                    ),
                    ConceptStep(
                        stepNumber = 3,
                        title = "Newton's Second Law & Conservation of Momentum",
                        text = "Force equals mass times acceleration (F = ma). In any closed physical system, total momentum (p = mv) and total energy are conserved, transforming between kinetic (½mv²) and potential (mgh) states.",
                        visualEmoji = "🚀 F = ma | p = mv | E_k = ½mv² 🌌",
                        tipOrFunFact = "Momentum conservation explains why rockets accelerate in the vacuum of space by expelling high-velocity exhaust gases backward!",
                        interactivePrompt = "If you double the velocity of a moving object, by what factor does its kinetic energy (½mv²) increase?",
                        practiceOptions = listOf("4 times (Quadrupled)", "2 times (Doubled)", "Remains identical"),
                        correctOptionIndex = 0
                    )
                ),
                questions = generate20HighSchoolScienceQuestions(themeId)
            ),

            // 9. HIGH SCHOOL SOCIAL STUDIES: Civics, Government & Macroeconomics
            FullLesson(
                id = "social_highschool_civics_econ",
                subject = EducationalSubject.SOCIAL_STUDIES,
                gradeLevel = GradeLevel.HIGH_SCHOOL,
                stateStandardCode = hsSocialStandard,
                standardDescription = "Analyze foundational constitutional principles, the tripartite system of government, civil liberties, and macroeconomic indicators from OER Commons Curated Collections ($OER_COMMONS_COLLECTIONS_URL).",
                title = "Constitutional Law, Global Economics & Civic Institutions",
                summary = "Analyze constitutional checks and balances, the Bill of Rights, fiscal/monetary policy, and global macroeconomic principles.",
                themeWorldId = themeId,
                teachSteps = listOf(
                    ConceptStep(
                        stepNumber = 1,
                        title = "Constitutional Checks & Balances",
                        text = "The US Constitution divides power across three co-equal branches: Legislative (Congress creates laws), Executive (President/Cabinet enforces laws), and Judicial (Supreme Court interprets constitutionality). Each branch holds veto/oversight checks on the others.",
                        visualEmoji = "🏛️ Legislative ⚖️ Judicial 🏢 Executive",
                        tipOrFunFact = "The Supreme Court case Marbury v. Madison (1803) established the principle of Judicial Review, allowing courts to strike down unconstitutional laws.",
                        interactivePrompt = "Which constitutional mechanism allows the Executive branch to check a bill passed by Congress?",
                        interactiveAnswers = listOf("Presidential Veto", "Judicial Review", "Filibuster"),
                        interactiveCorrectIndex = 0
                    ),
                    ConceptStep(
                        stepNumber = 2,
                        title = "Monetary Policy vs. Fiscal Policy",
                        text = "Fiscal Policy is managed by Congress and the President through taxation and government spending. Monetary Policy is managed by the Central Bank (Federal Reserve) by adjusting interest rates and bank reserve requirements to control inflation and employment.",
                        visualEmoji = "💵 Fiscal (Tax/Spend) 🔄 Monetary (Interest/Fed)",
                        tipOrFunFact = "When inflation is high, the Federal Reserve typically raises benchmark interest rates to cool aggregate borrowing and demand.",
                        interactivePrompt = "Who conducts monetary policy by adjusting benchmark interest rates in the United States?",
                        interactiveAnswers = listOf("The Federal Reserve (Central Bank)", "The Department of the Treasury", "State Governors"),
                        interactiveCorrectIndex = 0
                    ),
                    ConceptStep(
                        stepNumber = 3,
                        title = "The Bill of Rights & Landmark Precedents",
                        text = "The first 10 Amendments safeguard fundamental liberties: 1st (Speech, Religion, Press, Assembly), 4th (Protection against unreasonable searches), 5th (Due process, protection against self-incrimination), and 14th (Equal Protection under the law).",
                        visualEmoji = "📜 1st, 4th, 5th & 14th Amendments 🛡️",
                        tipOrFunFact = "The 14th Amendment's Equal Protection Clause was the constitutional foundation for landmark civil rights rulings like Brown v. Board of Education (1954).",
                        interactivePrompt = "Which constitutional amendment guarantees freedom of speech, the press, and peaceful assembly?",
                        interactiveAnswers = listOf("First Amendment", "Fourth Amendment", "Eighth Amendment"),
                        interactiveCorrectIndex = 0
                    )
                ),
                questions = generate20HighSchoolSocialStudiesQuestions(themeId)
            ),

            // 10. HIGH SCHOOL LIFE SKILLS & CAREER: Personal Finance & Executive Functioning
            FullLesson(
                id = "lifeskills_highschool_finance_career",
                subject = EducationalSubject.LIFE_SKILLS,
                gradeLevel = GradeLevel.HIGH_SCHOOL,
                stateStandardCode = hsLifeSkillsStandard,
                standardDescription = "Develop lifelong competencies in financial literacy, compound interest, executive function organization, stress regulation, and career transition planning from OER Commons Curated Collections ($OER_COMMONS_COLLECTIONS_URL).",
                title = "Personal Finance, Executive Mastery & Career Readiness",
                summary = "Master budgeting, credit scores, compound interest investing, time management, and neurodivergent executive strategies.",
                themeWorldId = themeId,
                teachSteps = listOf(
                    ConceptStep(
                        stepNumber = 1,
                        title = "The Power of Compound Interest & 50/30/20 Budgeting",
                        text = "Compound interest (A = P(1 + r/n)^(nt)) allows investments to grow exponentially over decades. The 50/30/20 budgeting rule allocates 50% of income to Needs (housing, groceries), 30% to Wants, and 20% to Savings/Investments and debt elimination.",
                        visualEmoji = "📈 50% Needs | 30% Wants | 20% Wealth & Savings 💰",
                        tipOrFunFact = "The Rule of 72: Divide 72 by your annual interest rate to estimate how many years it takes for your invested money to double!",
                        interactivePrompt = "According to the 50/30/20 budget framework, what percentage of net income should be directed to savings, investments, and debt reduction?",
                        interactiveAnswers = listOf("20%", "50%", "5%"),
                        interactiveCorrectIndex = 0
                    ),
                    ConceptStep(
                        stepNumber = 2,
                        title = "Credit Scores & Debt Management",
                        text = "A credit score (300-850) measures financial reliability. It is calculated by Payment History (35%), Credit Utilization (30%), Length of History (15%), Credit Mix (10%), and New Inquiries (10%). Maintaining low utilization (<30%) builds top-tier credit.",
                        visualEmoji = "💳 On-Time Payments + Low Utilization = 750+ Score 🎯",
                        tipOrFunFact = "Paying off credit card balances in full every month allows you to build a top credit score without ever paying a single cent of interest!",
                        interactivePrompt = "What factor has the highest weighting (35%) in determining your personal credit score?",
                        interactiveAnswers = listOf("On-time payment history", "How many cards you carry", "Your job title"),
                        interactiveCorrectIndex = 0
                    ),
                    ConceptStep(
                        stepNumber = 3,
                        title = "Executive Functioning & Cognitive Energy Management",
                        text = "Neurodivergent brains thrive with external scaffolding: Time Blocking (assigning task blocks), Body Doubling (working alongside others), reducing cognitive friction through visual task trackers, and honoring energy rhythms.",
                        visualEmoji = "🧠 Time Blocking + Sensory Breaks = Sustainable Focus ⚡",
                        tipOrFunFact = "The Pomodoro Technique (25m focused work + 5m sensory reset) prevents hyperfocus burnout and maintains peak executive clarity.",
                        interactivePrompt = "What is an evidence-based strategy to prevent cognitive burnout during multi-hour study sessions?",
                        interactiveAnswers = listOf("Scheduled focus intervals with structured sensory/movement breaks", "Studying for 6 hours without drinking water", "Multitasking on 5 screens at once"),
                        interactiveCorrectIndex = 0
                    )
                ),
                questions = generate20HighSchoolLifeSkillsQuestions(themeId)
            )
        )
    }

    // ==========================================
    // ELEMENTARY QUESTIONS (K-5)
    // ==========================================

    private fun generate20MathQuestions(themeId: String): List<QuestionItem> {
        val themePrefix = when (themeId) {
            "dino" -> "🦖 Dino Explorer: "
            "space" -> "🚀 Astro Command: "
            "ocean" -> "🐬 Sea Explorer: "
            "superhero" -> "🦸‍♂️ Hero Quest: "
            "mythical" -> "🐉 Mythical Quest: "
            "magic" -> "🦄 Magic Spark: "
            "knights" -> "🏰 Castle Quest: "
            else -> "✨ "
        }
        return listOf(
            QuestionItem(1, "${themePrefix}If there are 3 items on a ledge and you find 2 more, how many are there in total?", listOf("4", "5", "6", "3"), 1, "Count forward from 3: 4, 5!", "Awesome! 3 + 2 equals 5.", visualAidEmoji = "➕"),
            QuestionItem(2, "${themePrefix}What number comes right after 7?", listOf("6", "8", "9", "10"), 1, "Think: 5, 6, 7, ...", "Super! 8 comes right after 7.", visualAidEmoji = "🔢"),
            QuestionItem(3, "${themePrefix}Which two numbers make a perfect 10?", listOf("5 + 5", "3 + 4", "2 + 6", "1 + 7"), 0, "Hold up both hands with 5 fingers each!", "Brilliant! 5 and 5 always make 10.", visualAidEmoji = "🖐️"),
            QuestionItem(4, "${themePrefix}If you have 6 energy crystals and lose 1, how many are left?", listOf("5", "6", "7", "4"), 0, "Count one step backwards from 6.", "Great job! 6 minus 1 leaves 5.", visualAidEmoji = "💎"),
            QuestionItem(5, "${themePrefix}What shape has 3 straight sides and 3 corners?", listOf("Triangle", "Square", "Circle", "Rectangle"), 0, "Tri- means three, like a tricycle!", "Spot on! A triangle always has 3 sides.", visualAidEmoji = "📐"),
            QuestionItem(6, "${themePrefix}Count by tens: 10, 20, 30, ... what comes next?", listOf("35", "40", "50", "45"), 1, "Add another 10 to 30.", "Fantastic! 40 comes after 30.", visualAidEmoji = "🔟"),
            QuestionItem(7, "${themePrefix}Which number is the greatest (biggest)?", listOf("14", "19", "9", "11"), 1, "Look at the tens and ones place.", "Terrific! 19 is greater than 14, 11, and 9.", visualAidEmoji = "👑"),
            QuestionItem(8, "${themePrefix}What is 4 + 4?", listOf("7", "8", "9", "10"), 1, "Double 4 equals...", "Awesome work! 4 + 4 = 8.", visualAidEmoji = "🎯"),
            QuestionItem(9, "${themePrefix}If you have 10 coins and give away 0 coins, how many do you have?", listOf("0", "10", "9", "1"), 1, "Subtracting zero leaves the amount unchanged!", "Spot on! 10 - 0 is still 10.", visualAidEmoji = "🪙"),
            QuestionItem(10, "${themePrefix}Which shape has NO straight edges and rolls smoothly?", listOf("Circle", "Square", "Hexagon", "Cube"), 0, "Think of a wheel or planetary orbit.", "Perfect! Circles have continuous curves.", visualAidEmoji = "⚪"),
            QuestionItem(11, "${themePrefix}What is 10 + 3?", listOf("12", "13", "14", "15"), 1, "One ten and three ones makes...", "Great! 10 + 3 = 13.", visualAidEmoji = "⭐"),
            QuestionItem(12, "${themePrefix}What number is 1 less than 15?", listOf("13", "14", "16", "17"), 1, "Hop 1 step back from 15.", "Super! 14 is one less than 15.", visualAidEmoji = "📉"),
            QuestionItem(13, "${themePrefix}If a team has 4 members and 3 new members join, how many in all?", listOf("6", "7", "8", "5"), 1, "Add: 4 + 3.", "Well done! 4 + 3 = 7 total members.", visualAidEmoji = "👥"),
            QuestionItem(14, "${themePrefix}Which is an EVEN number (can be split equally in 2)?", listOf("3", "5", "6", "7"), 2, "Can you split it into two equal whole groups?", "Spot on! 6 splits evenly into 3 and 3.", visualAidEmoji = "⚖️"),
            QuestionItem(15, "${themePrefix}What is 9 + 1?", listOf("8", "9", "10", "11"), 2, "One more than 9.", "Boom! 9 + 1 = 10.", visualAidEmoji = "🚀"),
            QuestionItem(16, "${themePrefix}Complete the pattern: 2, 4, 6, ...", listOf("7", "8", "9", "10"), 1, "We are skip counting by 2!", "High five! 8 comes next in the 2s pattern.", visualAidEmoji = "⚡"),
            QuestionItem(17, "${themePrefix}If a box has 8 apples and 4 are eaten, how many remain?", listOf("3", "4", "5", "6"), 1, "8 minus 4 equals...", "Correct! 8 - 4 = 4 apples left.", visualAidEmoji = "🍎"),
            QuestionItem(18, "${themePrefix}Which number is between 11 and 13?", listOf("10", "12", "14", "15"), 1, "11, ___, 13.", "Exactly! 12 sits between 11 and 13.", visualAidEmoji = "📍"),
            QuestionItem(19, "${themePrefix}What is 7 + 3?", listOf("9", "10", "11", "8"), 1, "Remember the Friends of 10!", "Excellent! 7 and 3 make 10.", visualAidEmoji = "🔟"),
            QuestionItem(20, "${themePrefix}Mastery Challenge: What is 5 + 5 + 5?", listOf("10", "15", "20", "25"), 1, "Count by 5s: 5, 10, ...", "Grand Champion! 5 + 5 + 5 = 15!", visualAidEmoji = "🏆")
        )
    }

    private fun generate20ReadingQuestions(themeId: String): List<QuestionItem> {
        val themePrefix = when (themeId) {
            "dino" -> "📖 Dino Library: "
            "space" -> "📖 Star Archives: "
            else -> "📖 "
        }
        return listOf(
            QuestionItem(1, "${themePrefix}What sound does the word 'SUN' start with?", listOf("/s/", "/m/", "/b/", "/t/"), 0, "Listen to the very beginning: Sss-un.", "Terrific! 'SUN' starts with the /s/ sound.", visualAidEmoji = "☀️"),
            QuestionItem(2, "${themePrefix}Which word rhymes with 'STAR'?", listOf("FAR", "MOON", "SUN", "SKY"), 0, "Listen for the -AR ending sound.", "Great job! STAR and FAR rhyme.", visualAidEmoji = "⭐"),
            QuestionItem(3, "${themePrefix}What word is formed by blending /b/ + /a/ + /t/?", listOf("BAT", "CAT", "TAB", "BET"), 0, "Slide the sounds together: b-a-t.", "Awesome! /b/ /a/ /t/ blends into BAT.", visualAidEmoji = "🦇"),
            QuestionItem(4, "${themePrefix}Which of these is a high-frequency sight word?", listOf("THE", "ZYX", "QUUX", "PLOP"), 0, "Look for the familiar word you see in every book.", "Spot on! 'THE' is a key sight word.", visualAidEmoji = "👀"),
            QuestionItem(5, "${themePrefix}What is the opposite of 'UP'?", listOf("HIGH", "DOWN", "TOP", "ABOVE"), 1, "Think about looking up at the sky vs looking at feet.", "Perfect! DOWN is the opposite of UP.", visualAidEmoji = "⬇️"),
            QuestionItem(6, "${themePrefix}Which letter is a vowel?", listOf("B", "T", "A", "K"), 2, "Vowels are A, E, I, O, U!", "Well done! 'A' is one of the 5 main vowels.", visualAidEmoji = "🔤"),
            QuestionItem(7, "${themePrefix}Which word rhymes with 'TREE'?", listOf("BEE", "BRANCH", "LEAF", "WOOD"), 0, "Listen to that long /ee/ ending sound.", "Sweet! TREE and BEE rhyme.", visualAidEmoji = "🐝"),
            QuestionItem(8, "${themePrefix}What is the ending sound in the word 'DOG'?", listOf("/g/", "/d/", "/o/", "/t/"), 0, "Listen to the very last pop: Do-g.", "Correct! 'DOG' ends with the /g/ sound.", visualAidEmoji = "🐕"),
            QuestionItem(9, "${themePrefix}Which word describes something HUGE?", listOf("Tiny", "Gigantic", "Small", "Short"), 1, "Gigantic means giant-sized!", "Super! Gigantic means very big.", visualAidEmoji = "🐘"),
            QuestionItem(10, "${themePrefix}How many syllables (beats) are in 'DI-NO-SAUR'?", listOf("1", "2", "3", "4"), 2, "Clap each beat: Di (1) - no (2) - saur (3).", "Excellent! Dinosaur has 3 syllables.", visualAidEmoji = "👏"),
            QuestionItem(11, "${themePrefix}Which word belongs to the '-OP' family?", listOf("HOP", "HAT", "HIT", "HUT"), 0, "Look for -op at the end.", "Yes! HOP ends in -op.", visualAidEmoji = "🐰"),
            QuestionItem(12, "${themePrefix}What punctuation mark shows a question?", listOf("?", "!", ".", ","), 0, "Look for the curvy mark with a dot under it.", "Spot on! The question mark '?' asks something.", visualAidEmoji = "❓"),
            QuestionItem(13, "${themePrefix}Which word rhymes with 'PLAY'?", listOf("DAY", "DOG", "PIG", "PAN"), 0, "Listen to the -AY sound.", "Great! PLAY and DAY rhyme.", visualAidEmoji = "🎨"),
            QuestionItem(14, "${themePrefix}What sound does 'CH' make in 'CHIP'?", listOf("/ch/", "/sh/", "/th/", "/wh/"), 0, "Think of a choo-choo train!", "Terrific! CH makes the /ch/ sound.", visualAidEmoji = "🚂"),
            QuestionItem(15, "${themePrefix}Which word is an action verb?", listOf("RUN", "DESK", "PURPLE", "CLOUD"), 0, "Verbs are things your body can do!", "Awesome! RUN is an action verb.", visualAidEmoji = "🏃"),
            QuestionItem(16, "${themePrefix}What is the middle vowel sound in 'PEN'?", listOf("Short e", "Short a", "Short o", "Short u"), 0, "Say: P-Eh-n.", "Correct! 'PEN' has the short /e/ vowel.", visualAidEmoji = "🖊️"),
            QuestionItem(17, "${themePrefix}Which word rhymes with 'NIGHT'?", listOf("LIGHT", "DARK", "NOON", "DAWN"), 0, "Listen to the -IGHT rhyme.", "Brilliant! NIGHT and LIGHT rhyme.", visualAidEmoji = "💡"),
            QuestionItem(18, "${themePrefix}What does an author do?", listOf("Writes the words in a book", "Draws pictures only", "Binds the paper", "Sells the toys"), 0, "Authors craft the story text!", "Spot on! The author writes the story.", visualAidEmoji = "✍️"),
            QuestionItem(19, "${themePrefix}Which sentence has correct capitalization?", listOf("The cat is orange.", "the Cat Is orange.", "THE CAT is Orange.", "the cat is orange"), 0, "Sentences start with a capital letter and end with a period.", "Wonderful! First letter capitalized, period at end.", visualAidEmoji = "📝"),
            QuestionItem(20, "${themePrefix}Mastery Challenge: Read and complete: 'A kind friend will ____ you.'", listOf("help", "hop", "harm", "hiss"), 0, "Think about what a good friend does when you need a hand.", "Grand Champion! A kind friend will help you.", visualAidEmoji = "🏆")
        )
    }

    private fun generate20ScienceQuestions(themeId: String): List<QuestionItem> {
        return listOf(
            QuestionItem(1, "What do all plants need to grow healthy and green?", listOf("Sunlight & Water", "Ice cream", "Moonlight only", "Dark closets"), 0, "Plants use light to make food.", "Great! Plants need sunlight, soil, and water.", visualAidEmoji = "🌱"),
            QuestionItem(2, "Which planet do we live on?", listOf("Earth", "Mars", "Jupiter", "Venus"), 0, "The blue-and-green water planet!", "Spot on! We live on planet Earth.", visualAidEmoji = "🌍"),
            QuestionItem(3, "What state of matter is water when frozen into ice cubes?", listOf("Solid", "Liquid", "Gas", "Plasma"), 0, "Ice holds its shape firmly.", "Awesome! Ice is a solid.", visualAidEmoji = "🧊"),
            QuestionItem(4, "What natural force pulls objects towards the center of Earth?", listOf("Gravity", "Magnetism", "Electricity", "Sound"), 0, "It keeps your feet on the ground!", "Correct! Gravity pulls us down.", visualAidEmoji = "🍎"),
            QuestionItem(5, "What do birds have that helps them fly through the air?", listOf("Feathers & Wings", "Gills", "Scales", "Shells"), 0, "Lightweight wings and soft feathers.", "Super! Feathers and hollow bones enable flight.", visualAidEmoji = "🦅"),
            QuestionItem(6, "Which organ inside your chest pumps oxygen-rich blood?", listOf("Heart", "Lungs", "Stomach", "Brain"), 0, "Listen to the rhythmic thump-thump!", "Terrific! The heart pumps blood all over your body.", visualAidEmoji = "❤️"),
            QuestionItem(7, "What season comes right after Winter when flowers bloom?", listOf("Spring", "Summer", "Autumn", "Winter"), 0, "Baby animals are born and leaves bud!", "Lovely! Spring brings flowers and warmth.", visualAidEmoji = "🌸"),
            QuestionItem(8, "What do bees collect from flowers to make sweet honey?", listOf("Nectar", "Sand", "Salt", "Leaves"), 0, "Sweet liquid inside blossoms.", "Spot on! Bees gather sweet flower nectar.", visualAidEmoji = "🐝"),
            QuestionItem(9, "What type of animal lives in water and breathes with gills?", listOf("Fish", "Dog", "Bird", "Squirrel"), 0, "Gills extract oxygen from water.", "Yes! Fish breathe underwater using gills.", visualAidEmoji = "🐟"),
            QuestionItem(10, "What is the closest star to our planet Earth?", listOf("The Sun", "Polaris", "Sirius", "Alpha Centauri"), 0, "The giant glowing sphere in our daytime sky.", "Brilliant! The Sun is our nearest star.", visualAidEmoji = "☀️"),
            QuestionItem(11, "Which of these is a nocturnal animal (awake at night)?", listOf("Owl", "Rooster", "Butterfly", "Cow"), 0, "Owls have big eyes to see in dark forests.", "Great! Owls hunt and hoot at night.", visualAidEmoji = "🦉"),
            QuestionItem(12, "What happens to water when it boils in a kettle?", listOf("Turns to Steam (Gas)", "Turns to Ice", "Disappears forever", "Freezes"), 0, "Hot steam rises into the air.", "Correct! Water evaporates into steam gas.", visualAidEmoji = "♨️"),
            QuestionItem(13, "What covers a reptile's skin for protection?", listOf("Scales", "Fur", "Feathers", "Wool"), 0, "Lizards and snakes have scaly skin.", "Super! Reptiles have tough scales.", visualAidEmoji = "🦎"),
            QuestionItem(14, "What tool helps scientists see tiny bacteria that human eyes cannot?", listOf("Microscope", "Telescope", "Binoculars", "Sunglasses"), 0, "Micro- means super small.", "Spot on! Microscopes magnify microscopic worlds.", visualAidEmoji = "🔬"),
            QuestionItem(15, "Why do some trees drop their leaves in Autumn?", listOf("To save water & energy for winter", "Because they are sleepy", "To feed birds", "They blow away for fun"), 0, "Shedding leaves protects trees from freezing cold.", "Awesome! Deciduous trees conserve energy.", visualAidEmoji = "🍂"),
            QuestionItem(16, "What is the main source of light and heat for Earth?", listOf("The Sun", "The Moon", "Campfires", "Flashlights"), 0, "Our solar system's star.", "Correct! The Sun provides warmth and light.", visualAidEmoji = "☀️"),
            QuestionItem(17, "What is a baby frog called when it hatches from an egg in water?", listOf("Tadpole", "Puppy", "Caterpillar", "Calf"), 0, "It swims with a tail before growing legs!", "Wonderful! Tadpoles grow into frogs.", visualAidEmoji = "🐸"),
            QuestionItem(18, "Which metal is attracted to a magnet?", listOf("Iron / Steel", "Wood", "Plastic", "Glass"), 0, "Magnets pull magnetic metals.", "Spot on! Iron sticks to magnets.", visualAidEmoji = "🧲"),
            QuestionItem(19, "What is the protective outer shell of a tree called?", listOf("Bark", "Skin", "Crust", "Peel"), 0, "The rough layer on tree trunks.", "Yes! Bark protects the tree inside.", visualAidEmoji = "🌳"),
            QuestionItem(20, "Mastery Challenge: What do we call an animal that eats only plants?", listOf("Herbivore", "Carnivore", "Omnivore", "Insectivore"), 0, "Herb- comes from herbs/plants.", "Grand Champion! Herbivores eat plants and greenery.", visualAidEmoji = "🏆")
        )
    }

    private fun generate20SocialStudiesQuestions(themeId: String): List<QuestionItem> {
        return listOf(
            QuestionItem(1, "Who helps guide and teach children in school every day?", listOf("Teachers", "Astronauts", "Divers", "Pilots"), 0, "They share knowledge and support students.", "Great job! Teachers inspire and educate.", visualAidEmoji = "🧑‍🏫"),
            QuestionItem(2, "What is a drawing that shows streets, rivers, and cities from above?", listOf("A Map", "A Comic Book", "A Painting", "A Mirror"), 0, "Use it when exploring or traveling.", "Spot on! Maps help us navigate places.", visualAidEmoji = "🗺️"),
            QuestionItem(3, "What symbol represents freedom in New York Harbor?", listOf("Statue of Liberty", "Golden Gate Bridge", "Eiffel Tower", "Mount Rushmore"), 0, "Lady Liberty holds a glowing torch.", "Terrific! The Statue of Liberty welcomes people.", visualAidEmoji = "🗽"),
            QuestionItem(4, "What are the colors of the United States flag?", listOf("Red, White, and Blue", "Green and Yellow", "Purple and Orange", "Black and Gold"), 0, "Stars and stripes in three colors.", "Super! Red, White, and Blue with 50 stars.", visualAidEmoji = "🇺🇸"),
            QuestionItem(5, "Who helps deliver mail, letters, and packages to our homes?", listOf("Mail Carrier / Postal Worker", "Lifeguard", "Baker", "Mechanic"), 0, "They visit our mailboxes in uniforms.", "Awesome! Postal workers deliver mail.", visualAidEmoji = "📬"),
            QuestionItem(6, "What direction is opposite of North on a map?", listOf("South", "East", "West", "Up"), 0, "Pointing down toward the South pole.", "Correct! South is opposite of North.", visualAidEmoji = "🧭"),
            QuestionItem(7, "Why do communities build libraries?", listOf("So anyone can borrow books and learn for free", "To sell expensive cars", "To play loud music", "To sleep overnight"), 0, "Libraries share knowledge with all neighbors.", "Wonderful! Public libraries provide free books and community spaces.", visualAidEmoji = "📚"),
            QuestionItem(8, "What do we use money (coins and dollars) for?", listOf("To buy goods, food, and services", "To draw on walls", "To build houses out of coins", "To throw away"), 0, "Money trades value for needed items.", "Spot on! Money is used to exchange goods and services.", visualAidEmoji = "💵"),
            QuestionItem(9, "What emergency number connects you to police, fire, or ambulance in the US?", listOf("911", "411", "000", "123"), 0, "Three numbers to call for immediate help.", "Crucial knowledge! 911 is for emergency help.", visualAidEmoji = "🚨"),
            QuestionItem(10, "What is a tradition?", listOf("A special custom passed down through families & cultures", "A new pair of shoes", "A math test", "A broken clock"), 0, "Things families celebrate year after year.", "Yes! Traditions celebrate shared history and culture.", visualAidEmoji = "🎉"),
            QuestionItem(11, "Which leader lives and works in the White House?", listOf("The President of the United States", "The Mayor of Town", "The School Principal", "The Bus Driver"), 0, "The head of the national executive branch.", "Brilliant! The President lives in the White House.", visualAidEmoji = "🏛️"),
            QuestionItem(12, "What is a globe?", listOf("A round 3D model of planet Earth", "A flat poster", "A square board game", "A lamp shade"), 0, "Spin it to see all oceans and continents!", "Correct! A globe is a spherical model of Earth.", visualAidEmoji = "🌐"),
            QuestionItem(13, "How can citizens help keep their neighborhoods clean and beautiful?", listOf("Picking up litter and recycling", "Throwing trash on sidewalks", "Breaking streetlights", "Ignoring leaks"), 0, "Taking care of shared parks and streets.", "Terrific! Recycling and cleaning up helps everyone.", visualAidEmoji = "♻️"),
            QuestionItem(14, "What continent do the USA, Canada, and Mexico belong to?", listOf("North America", "Europe", "Asia", "Africa"), 0, "Our home continent.", "Spot on! North America is our continent.", visualAidEmoji = "🌎"),
            QuestionItem(15, "What do firefighters wear to protect themselves from heat and smoke?", listOf("Special insulated helmets & fireproof jackets", "Swimsuits", "Paper hats", "T-shirts"), 0, "Heavy duty protective turnout gear.", "Super! Firefighters wear heat-resistant protective gear.", visualAidEmoji = "🚒"),
            QuestionItem(16, "What is voting?", listOf("Making a group choice by casting a secret ballot", "Shouting the loudest", "Rolling dice", "Flipping a coin"), 0, "Everyone gets a fair say in elections.", "Great! Voting lets citizens choose leaders fairly.", visualAidEmoji = "🗳️"),
            QuestionItem(17, "What holiday celebrates gratitude, harvest, and family meals in November?", listOf("Thanksgiving", "Valentine's Day", "Halloween", "Labor Day"), 0, "Giving thanks for good food and friends.", "Lovely! Thanksgiving is a time of gratitude.", visualAidEmoji = "🦃"),
            QuestionItem(18, "What does a dentist take care of?", listOf("Our teeth and gums", "Our cars", "Our bicycles", "Our gardens"), 0, "They check for clean, healthy smiles!", "Spot on! Dentists keep teeth strong and clean.", visualAidEmoji = "🦷"),
            QuestionItem(19, "What is a neighbor?", listOf("A person who lives near your home", "A wild jungle animal", "A planet in space", "A fictional robot"), 0, "People who share your street or apartment building.", "Yes! Neighbors live nearby in our community.", visualAidEmoji = "🏡"),
            QuestionItem(20, "Mastery Challenge: Why is showing empathy (understanding how others feel) important?", listOf("It builds kindness, trust, and peaceful friendships", "It wins prizes", "It makes you boss of everyone", "It is required for exams"), 0, "Putting yourself in someone else's shoes.", "Grand Champion! Empathy connects us and spreads kindness!", visualAidEmoji = "🏆")
        )
    }

    private fun generate20LifeSkillsQuestions(themeId: String): List<QuestionItem> {
        return listOf(
            QuestionItem(1, "When you feel overwhelmed or sensory overload, what is a great first step?", listOf("Take a slow 4-7-8 breath or step into a calm zone", "Scream loudly", "Throw things", "Bottle it up inside"), 0, "Slow deep breathing resets your nervous system.", "Excellent choice! Taking a calm reset helps your brain reset.", visualAidEmoji = "🧘"),
            QuestionItem(2, "How long should you wash your hands with soap and water?", listOf("20 seconds (Sing Happy Birthday twice)", "2 seconds", "1 minute without soap", "Only with dry towel"), 0, "Singing a short song helps time 20 seconds.", "Great hygiene! 20 seconds cleans away germs thoroughly.", visualAidEmoji = "🧼"),
            QuestionItem(3, "What should you do if the lights or sounds in a room feel too loud/bright?", listOf("Ask kindly for headphones or dimmed lights", "Suffer quietly without telling anyone", "Run away without looking", "Cry silently"), 0, "Communicating sensory needs helps adults support you.", "Super! Asking for sensory tools (like noise-cancelling headphones) is smart.", visualAidEmoji = "🎧"),
            QuestionItem(4, "Why is getting 8-10 hours of sleep each night important?", listOf("It recharges your brain and body for learning", "It makes you forget everything", "It slows down growth", "It turns off dreams"), 0, "Your brain organizes memories while sleeping.", "Spot on! Sleep powers your focus, mood, and memory.", visualAidEmoji = "😴"),
            QuestionItem(5, "If a friend looks sad on the playground, what is a thoughtful thing to do?", listOf("Ask gently: 'Are you okay? Do you want company?'", "Laugh at them", "Ignore them completely", "Tell everyone secrets"), 0, "Checking in shows empathy and warmth.", "Kind heart! A gentle check-in shows true friendship.", visualAidEmoji = "🤝"),
            QuestionItem(6, "When crossing a street, what must you always do before stepping off the curb?", listOf("Look Left, Right, and Left again, holding an adult's hand", "Run across without looking", "Look only at shoes", "Close your eyes"), 0, "Always check for vehicles in all directions.", "Crucial safety! Always look both ways and hold hands.", visualAidEmoji = "🚦"),
            QuestionItem(7, "What is a healthy routine to start your school morning?", listOf("Eat breakfast, brush teeth, and pack backpack", "Play video games until late for school", "Skip breakfast and run", "Stay in pajamas"), 0, "Predictable morning steps make mornings smooth.", "Great routine! Consistent morning routines reduce morning stress.", visualAidEmoji = "🥞"),
            QuestionItem(8, "If you don't understand a lesson question, what should you do?", listOf("Raise your hand or ask: 'Can you explain in another way?'", "Rip the paper", "Pretend you know it", "Give up forever"), 0, "Asking questions helps your brain grow.", "Awesome growth mindset! Asking questions is how we master new skills.", visualAidEmoji = "🙋"),
            QuestionItem(9, "What does the 4-7-8 breathing method do for your heartbeat?", listOf("Slows down racing heartbeat and brings calm", "Makes you dizzy", "Speeds up heart rate", "Stops you from thinking"), 0, "Deep diaphragmatic breathing relaxes the vagus nerve.", "Terrific! Long exhales signal safety and relaxation to your body.", visualAidEmoji = "🌬️"),
            QuestionItem(10, "Why is drinking fresh water throughout the day good for you?", listOf("It keeps brain cells hydrated and energized", "It tastes like candy", "It turns hair blue", "It stops sleep"), 0, "Hydration keeps thinking sharp and bodies strong.", "Spot on! Water is essential for high brain energy.", visualAidEmoji = "💧"),
            QuestionItem(11, "What can you use if your fingers need to fidget during quiet listening?", listOf("A discreet sensory pop-it or stress squishy", "Tapping desks loudly", "Poking neighbors", "Snapping pens"), 0, "Tactile sensory tools satisfy motor needs quietly.", "Super! Quiet fidgets channel energy into better focus.", visualAidEmoji = "🫧"),
            QuestionItem(12, "What should you do after playing with pet animals?", listOf("Wash your hands thoroughly with soap", "Rub eyes immediately", "Put hands in mouth", "Wipe hands on pants"), 0, "Washing stops animal dander and germs from spreading.", "Great habit! Always wash hands after pet cuddles.", visualAidEmoji = "🐾"),
            QuestionItem(13, "What is positive self-talk?", listOf("Saying: 'I can try my best and learn from mistakes!'", "Saying: 'I am terrible at this.'", "Saying: 'I will never get it.'", "Ignoring all feedback"), 0, "Encouraging words you say inside your own head.", "Brilliant! Positive self-talk boosts confidence and resilience.", visualAidEmoji = "💪"),
            QuestionItem(14, "If someone accidentally bumps into you, what is a calm response?", listOf("Say: 'It's okay, accidents happen!'", "Push them back hard", "Yell at them", "Cry for an hour"), 0, "Recognizing unintentional accidents avoids conflict.", "Spot on! Recognizing accidents keeps the peace.", visualAidEmoji = "🕊️"),
            QuestionItem(15, "Why is brushing your teeth twice every day important?", listOf("Prevents cavities and keeps gums healthy and fresh", "Makes teeth turn green", "Is only for adults", "Replaces washing hands"), 0, "Two minutes morning and night cleans plaque.", "Terrific! Brushing protects your bright smile.", visualAidEmoji = "🪥"),
            QuestionItem(16, "When you finish using toys or art supplies, what is the best next step?", listOf("Put them back in their bins so you can find them next time", "Leave them scattered on the floor", "Throw them under the bed", "Hide them outside"), 0, "Clean spaces reduce visual sensory clutter.", "Super! Tidying up keeps spaces calm and organized.", visualAidEmoji = "📦"),
            QuestionItem(17, "What is a healthy way to express feeling angry?", listOf("Drawing feelings on paper or squeezing a plushie", "Kicking furniture", "Calling people names", "Throwing food"), 0, "Physical expression without hurting people or objects.", "Wonderful! Creative outlets safely discharge anger.", visualAidEmoji = "🖍️"),
            QuestionItem(18, "Why is it important to take turns when playing a game?", listOf("It makes games fun and fair for all players", "It lets only one person win forever", "It takes too long", "It stops the game"), 0, "Turn-taking builds social harmony.", "High five! Taking turns shows respect and fairness.", visualAidEmoji = "🎲"),
            QuestionItem(19, "What should you do before eating fresh fruit like apples or berries?", listOf("Rinse them under clean running water", "Bury them in dirt", "Paint them", "Leave them in the sun"), 0, "Rinsing removes dirt and dust.", "Spot on! Washing fruit makes it clean and ready to enjoy.", visualAidEmoji = "🍎"),
            QuestionItem(20, "Mastery Challenge: What is the most important thing to remember when learning something difficult?", listOf("Mistakes help my brain make new connections and grow!", "I should only try things I'm already perfect at", "Giving up is fastest", "Never ask for assistance"), 0, "Every challenge builds stronger neural pathways!", "Grand Champion! Growth mindset turns 'I can't' into 'I can't YET!'", visualAidEmoji = "🏆")
        )
    }

    // ==========================================
    // HIGH SCHOOL QUESTIONS (GRADES 9-12)
    // ==========================================

    private fun generate20HighSchoolMathQuestions(themeId: String): List<QuestionItem> {
        val p = "📐 HS Algebra: "
        return listOf(
            QuestionItem(1, "${p}What are the roots of the quadratic equation x² - 9 = 0?", listOf("x = 3 and x = -3", "x = 9 and x = -9", "x = 3 only", "x = 0"), 0, "Factor the difference of squares: (x-3)(x+3) = 0.", "Exact! (x-3)(x+3) = 0 gives roots ±3.", visualAidEmoji = "🔢"),
            QuestionItem(2, "${p}In the standard form ax² + bx + c = 0, what does the discriminant b² - 4ac indicate when it is negative (< 0)?", listOf("Two complex (non-real) roots", "Two distinct real roots", "One rational real root", "An infinite number of roots"), 0, "The square root of a negative number yields imaginary terms (i).", "Correct! Δ < 0 produces complex conjugate solutions.", visualAidEmoji = "⚡"),
            QuestionItem(3, "${p}Solve for x: 2x + 5 = 17.", listOf("x = 6", "x = 11", "x = 7", "x = 5"), 0, "Subtract 5: 2x = 12, then divide by 2.", "Great! x = 6 satisfies 2(6) + 5 = 17.", visualAidEmoji = "🎯"),
            QuestionItem(4, "${p}What is the vertex of the parabola f(x) = (x - 4)² + 7?", listOf("(4, 7)", "(-4, 7)", "(4, -7)", "(-4, -7)"), 0, "In vertex form a(x-h)² + k, the vertex is (h, k).", "Spot on! The vertex is at (4, 7).", visualAidEmoji = "📍"),
            QuestionItem(5, "${p}Which function models exponential growth?", listOf("f(x) = 3(1.08)^x", "f(x) = 3x + 8", "f(x) = x² + 3", "f(x) = 3(0.5)^x"), 0, "Growth occurs when the base b in f(x) = a(b)^x is greater than 1.", "Awesome! 1.08 > 1 represents 8% continuous growth.", visualAidEmoji = "📈"),
            QuestionItem(6, "${p}What is the slope of a line perpendicular to y = (2/3)x + 4?", listOf("-3/2", "3/2", "2/3", "-2/3"), 0, "Perpendicular slopes are negative reciprocals: m_perp = -1/m.", "Brilliant! The negative reciprocal of 2/3 is -3/2.", visualAidEmoji = "📐"),
            QuestionItem(7, "${p}Factor completely: x² - 7x + 12.", listOf("(x - 3)(x - 4)", "(x + 3)(x + 4)", "(x - 2)(x - 6)", "(x - 1)(x - 12)"), 0, "Find two numbers multiplying to +12 and adding to -7: -3 and -4.", "Correct! (x - 3)(x - 4) = x² - 7x + 12.", visualAidEmoji = "🧩"),
            QuestionItem(8, "${p}What is log₁₀(1000)?", listOf("3", "100", "10", "30"), 0, "Ask: 10 raised to what power equals 1000? 10³ = 1000.", "Perfect! log₁₀(1000) = 3.", visualAidEmoji = "💡"),
            QuestionItem(9, "${p}Solve the system: x + y = 10 and x - y = 4.", listOf("x = 7, y = 3", "x = 6, y = 4", "x = 8, y = 2", "x = 5, y = 5"), 0, "Add both equations: 2x = 14 ➡️ x = 7, then y = 3.", "Terrific! (7, 3) satisfies both linear equations.", visualAidEmoji = "⚖️"),
            QuestionItem(10, "${p}According to the Pythagorean theorem, what is the hypotenuse of a right triangle with legs a = 6 and b = 8?", listOf("10", "14", "12", "100"), 0, "c = √(6² + 8²) = √(36 + 64) = √100 = 10.", "Spot on! 6-8-10 is a classic Pythagorean triple.", visualAidEmoji = "🔺"),
            QuestionItem(11, "${p}What is the value of 5! (5 factorial)?", listOf("120", "25", "60", "720"), 0, "Multiply: 5 × 4 × 3 × 2 × 1 = 120.", "Super! 5! = 120.", visualAidEmoji = "❗"),
            QuestionItem(12, "${p}Simplify: (x³)(x⁵).", listOf("x⁸", "x¹⁵", "x²", "2x⁸"), 0, "When multiplying terms with the same base, add exponents: 3 + 5 = 8.", "Well done! Exponent product rule gives x⁸.", visualAidEmoji = "⚡"),
            QuestionItem(13, "${p}What is the domain of the function f(x) = √(x - 2)?", listOf("x ≥ 2", "x > 0", "All real numbers", "x ≤ 2"), 0, "The radicand under a square root must be non-negative: x - 2 ≥ 0.", "Exact! The domain is [2, ∞).", visualAidEmoji = "🛡️"),
            QuestionItem(14, "${p}What is the sum of interior angles in a 5-sided pentagon?", listOf("540°", "360°", "180°", "720°"), 0, "Formula: (n - 2) × 180° = (5 - 2) × 180° = 540°.", "Great geometry! A pentagon has 540° interior sum.", visualAidEmoji = "⬡"),
            QuestionItem(15, "${p}If f(x) = 2x + 3 and g(x) = x², what is the composite function f(g(3))?", listOf("21", "81", "15", "18"), 0, "g(3) = 3² = 9. Then f(9) = 2(9) + 3 = 21.", "Awesome! f(g(3)) = 21.", visualAidEmoji = "🔄"),
            QuestionItem(16, "${p}What is the probability of flipping two fair coins and getting two heads (HH)?", listOf("1/4 (25%)", "1/2 (50%)", "1/3 (33%)", "1/8 (12.5%)"), 0, "Multiply independent probabilities: 1/2 × 1/2 = 1/4.", "Correct! Probability of HH is 0.25 or 1/4.", visualAidEmoji = "🪙"),
            QuestionItem(17, "${p}Which angle has sin(θ) = 1/2 in the first quadrant?", listOf("30° (π/6)", "45° (π/4)", "60° (π/3)", "90° (π/2)"), 0, "On the unit circle, sin(30°) = 1/2.", "Spot on! sin(30°) = 0.5.", visualAidEmoji = "📐"),
            QuestionItem(18, "${p}Solve for x: |2x - 4| = 10.", listOf("x = 7 and x = -3", "x = 7 only", "x = 3 and x = -7", "x = 5 and x = -5"), 0, "Case 1: 2x - 4 = 10 ➡️ x = 7. Case 2: 2x - 4 = -10 ➡️ x = -3.", "Terrific absolute value solving!", visualAidEmoji = "📏"),
            QuestionItem(19, "${p}What is the median of the dataset: 4, 8, 12, 16, 20?", listOf("12", "16", "10", "14"), 0, "The middle value in an ordered set of 5 numbers is the 3rd term.", "Yes! 12 is the median.", visualAidEmoji = "📊"),
            QuestionItem(20, "${p}Mastery Challenge: What is the derivative of f(x) = 3x² + 5x - 7 with respect to x?", listOf("6x + 5", "3x + 5", "6x² + 5", "6x"), 0, "Apply power rule: d/dx(3x²) = 6x, d/dx(5x) = 5, d/dx(-7) = 0.", "Grand Champion! d/dx(3x² + 5x - 7) = 6x + 5.", visualAidEmoji = "🏆")
        )
    }

    private fun generate20HighSchoolReadingQuestions(themeId: String): List<QuestionItem> {
        val p = "📖 HS Literature: "
        return listOf(
            QuestionItem(1, "${p}Which rhetorical device involves placing two contrasting ideas side by side for dramatic emphasis?", listOf("Antithesis", "Alliteration", "Onomatopoeia", "Hyperbole"), 0, "Example: 'Speech is silver, but silence is golden.'", "Spot on! Antithesis juxtaposes contrasting concepts.", visualAidEmoji = "⚖️"),
            QuestionItem(2, "${p}What is the primary function of dramatic irony in literature?", listOf("The audience knows crucial information that the characters do not", "The ending is universally happy", "Characters speak exclusively in rhyming couplets", "The narrator is unreliable"), 0, "Dramatic irony creates suspense and tension.", "Correct! Dramatic irony builds profound narrative suspense.", visualAidEmoji = "🎭"),
            QuestionItem(3, "${p}In George Orwell's 'Animal Farm', what literary form represents a deeper political and historical reality through symbolic characters?", listOf("Allegory", "Soliloquy", "Sonnet", "Epilogue"), 0, "An allegory uses surface narrative to represent broader political critique.", "Brilliant! Animal Farm is an allegory for totalitarianism.", visualAidEmoji = "📖"),
            QuestionItem(4, "${p}What is the term for an author's distinct attitude or emotional stance toward their subject matter?", listOf("Tone", "Meter", "Syntax", "Rhyme scheme"), 0, "Tone conveys whether the writing is solemn, sarcastic, satirical, or reverent.", "Yes! Tone reveals the writer's attitude.", visualAidEmoji = "🔍"),
            QuestionItem(5, "${p}Which statement represents an appeal to Ethos?", listOf("'As a board-certified neurologist with 20 years of research, I confirm this methodology.'", "'If you don't act today, innocent animals will suffer.'", "'Data demonstrates an 84% reduction in errors.'", "'This is the newest trend.'"), 0, "Ethos relies on credibility, credentials, and ethics.", "Spot on! Credentialing builds ethos.", visualAidEmoji = "🎓"),
            QuestionItem(6, "${p}What figure of speech is: 'The classroom was a pressure cooker during final exams'?", listOf("Metaphor", "Simile", "Personification", "Understatement"), 0, "A direct comparison stating one thing IS another without 'like' or 'as'.", "Exact! Direct comparison makes it a metaphor.", visualAidEmoji = "♨️"),
            QuestionItem(7, "${p}What is an overarching central idea or universal insight into human nature explored across a literary work?", listOf("Theme", "Plot point", "Setting", "Protagonist"), 0, "Theme expresses the deeper thematic message of a text.", "Super! Theme is the universal takeaway.", visualAidEmoji = "🌟"),
            QuestionItem(8, "${p}In Shakespearean drama, what is a speech where a character speaks their innermost private thoughts aloud alone on stage?", listOf("Soliloquy", "Dialogue", "Aside", "Prologue"), 0, "Examples include Hamlet's 'To be, or not to be'.", "Terrific! Soliloquies voice internal psychological reflection.", visualAidEmoji = "🗣️"),
            QuestionItem(9, "${p}What logical fallacy occurs when someone attacks their opponent's character rather than addressing their actual argument?", listOf("Ad Hominem", "Straw Man", "Post Hoc Ergo Propter Hoc", "Bandwagon"), 0, "Latin for 'to the person'.", "Correct! Ad hominem targets the person instead of the claim.", visualAidEmoji = "🛑"),
            QuestionItem(10, "${p}Which narrative point of view utilizes an all-knowing narrator who reveals the internal thoughts of all characters?", listOf("Third-Person Omniscient", "First-Person Protagonist", "Third-Person Limited", "Second-Person Direct"), 0, "Omniscient means all-knowing.", "Well done! Third-person omniscient accesses all minds.", visualAidEmoji = "👁️"),
            QuestionItem(11, "${p}What is the term for the repetition of initial consonant sounds across successive words?", listOf("Alliteration", "Assonance", "Consonance", "Hyperbole"), 0, "Example: 'Peter Piper picked a peck of pickled peppers.'", "Spot on! Initial consonant repetition is alliteration.", visualAidEmoji = "🔤"),
            QuestionItem(12, "${p}What is a thesis statement?", listOf("A concise claim that states the central argument of an academic essay", "A dictionary definition", "A summary of the conclusion", "A list of sources"), 0, "A thesis guides the entire essay's analytical trajectory.", "Yes! A thesis asserts the core arguable claim.", visualAidEmoji = "📝"),
            QuestionItem(13, "${p}Which literary period emphasized emotion, individualism, the sublime in nature, and reaction against industrial rationalism?", listOf("Romanticism", "Enlightenment", "Realism", "Post-Modernism"), 0, "Writers included Wordsworth, Shelley, and Keats.", "Super! Romanticism celebrated intuition and nature.", visualAidEmoji = "🌄"),
            QuestionItem(14, "${p}What does the root word 'chrono' mean in words like chronology, chronicle, and synchronize?", listOf("Time", "Space", "Sound", "Color"), 0, "From Greek 'chronos' meaning time.", "Correct! Chrono refers to time.", visualAidEmoji = "⏱️"),
            QuestionItem(15, "${p}What is the climax in classic dramatic narrative structure (Freytag's Pyramid)?", listOf("The peak turning point and highest emotional tension of the conflict", "The initial exposition", "The resolution and aftermath", "The opening dialogue"), 0, "The climax resolves the primary crisis.", "Terrific! The climax is the critical turning point.", visualAidEmoji = "⛰️"),
            QuestionItem(16, "${p}Which word is the best synonym for 'meticulous'?", listOf("Painstakingly precise and thorough", "Careless", "Hasty", "Vague"), 0, "Meticulous denotes extreme attention to detail.", "Spot on! Meticulous means thorough and precise.", visualAidEmoji = "🔬"),
            QuestionItem(17, "${p}In argumentation, what is the term for acknowledging valid opposing points before refuting them?", listOf("Concession", "Fallacy", "Dogmatism", "Circular reasoning"), 0, "Concession shows intellectual honesty and analytical rigor.", "Awesome! Concession validates nuance before rebuttal.", visualAidEmoji = "🤝"),
            QuestionItem(18, "${p}What is a motif in literature?", listOf("A recurring image, symbol, or concept that develops theme throughout a work", "A one-time spelling error", "The publisher's logo", "The page margin"), 0, "Motifs recur across chapters to reinforce meaning.", "Great! Motifs are recurring thematic symbols.", visualAidEmoji = "🔁"),
            QuestionItem(19, "${p}Which sentence demonstrates correct parallel grammatical structure?", listOf("She loves swimming, running, and reading.", "She loves swimming, to run, and reads.", "She loves to swim, running, and read.", "She loves swimming, run, and to read."), 0, "All items in the series must share the same grammatical form (gerunds: -ing).", "Exact! Parallel structure ensures grammatical harmony.", visualAidEmoji = "📐"),
            QuestionItem(20, "${p}Mastery Challenge: In rhetoric, what is 'Kairos'?", listOf("The opportune, opportune moment and timeliness for delivering an argument", "The author's handwriting", "The length of a speech", "The number of citations"), 0, "Ancient Greek concept of the critical, opportune window of time.", "Grand Champion! Kairos is the timeliness of rhetorical delivery!", visualAidEmoji = "🏆")
        )
    }

    private fun generate20HighSchoolScienceQuestions(themeId: String): List<QuestionItem> {
        val p = "🔬 HS Science: "
        return listOf(
            QuestionItem(1, "${p}What are the complementary base pairing rules in double-stranded DNA?", listOf("Adenine with Thymine (A-T), Cytosine with Guanine (C-G)", "Adenine with Guanine, Cytosine with Thymine", "Adenine with Uracil, Cytosine with Guanine", "All bases pair randomly"), 0, "Chargaff's rules: A pairs with T (2 hydrogen bonds), C with G (3 bonds).", "Spot on! A-T and C-G form the DNA double helix.", visualAidEmoji = "🧬"),
            QuestionItem(2, "${p}Which law states that an object at rest stays at rest unless acted upon by an unbalanced net external force?", listOf("Newton's First Law (Law of Inertia)", "Newton's Second Law (F=ma)", "Newton's Third Law (Action-Reaction)", "Law of Universal Gravitation"), 0, "Inertia is the tendency of matter to resist acceleration.", "Correct! Newton's 1st Law governs inertia.", visualAidEmoji = "🌌"),
            QuestionItem(3, "${p}What is the pH of a neutral aqueous solution at 25°C?", listOf("7.0", "1.0", "14.0", "0.0"), 0, "Neutral water has equal concentrations of H⁺ and OH⁻ ions (10⁻⁷ M).", "Exact! pH 7 is neutral; <7 is acidic, >7 is basic.", visualAidEmoji = "🧪"),
            QuestionItem(4, "${p}What process in the thylakoid membrane splits water molecules to produce oxygen and protons using light energy?", listOf("Photolysis in Photosystem II", "Calvin Cycle", "Glycolysis", "Fermentation"), 0, "2H₂O + light ➡️ 4H⁺ + 4e⁻ + O₂.", "Brilliant! Photolysis generates Earth's breathable oxygen.", visualAidEmoji = "☀️"),
            QuestionItem(5, "${p}What is the unit of electrical resistance in the SI system?", listOf("Ohm (Ω)", "Ampere (A)", "Volt (V)", "Watt (W)"), 0, "Ohm's Law: V = IR, where resistance R is measured in Ohms.", "Super! Resistance is measured in Ohms.", visualAidEmoji = "⚡"),
            QuestionItem(6, "${p}In Mendelian genetics, crossing two heterozygous parents (Aa x Aa) yields what expected phenotypic ratio for a dominant trait?", listOf("3:1 (75% dominant, 25% recessive)", "1:1 (50% each)", "9:3:3:1", "1:2:1 genotype only"), 0, "Punnett square: AA (1), Aa (2), aa (1) ➡️ 3 dominant : 1 recessive.", "Spot on! The classic monohybrid phenotypic ratio is 3:1.", visualAidEmoji = "🌱"),
            QuestionItem(7, "${p}What organelle contains hydrolytic digestive enzymes that break down cellular waste and macromolecules?", listOf("Lysosome", "Ribosome", "Centrosome", "Vacuole"), 0, "Lysosomes act as the cell's recycling and waste degradation center.", "Correct! Lysosomes hydrolyze cellular debris.", visualAidEmoji = "🧫"),
            QuestionItem(8, "${p}What fundamental subatomic particle determines the atomic number and chemical identity of an element?", listOf("Proton", "Neutron", "Electron", "Positron"), 0, "Carbon always has 6 protons; changing proton count changes the element.", "Yes! Protons define atomic number Z.", visualAidEmoji = "⚛️"),
            QuestionItem(9, "${p}What is the rate of gravitational acceleration at Earth's surface (ignoring air resistance)?", listOf("9.8 m/s²", "3.0 × 10⁸ m/s", "6.67 × 10⁻¹¹ N", "100 m/s²"), 0, "Free-fall acceleration g ≈ 9.80 m/s².", "Terrific! g = 9.8 m/s² on Earth.", visualAidEmoji = "🍎"),
            QuestionItem(10, "${p}Which type of chemical bond involves the equal sharing of electron pairs between two atoms of similar electronegativity?", listOf("Non-polar Covalent Bond", "Ionic Bond", "Hydrogen Bond", "Metallic Bond"), 0, "Molecules like O₂ and CH₄ have non-polar covalent bonds.", "Well done! Shared electron pairs form covalent bonds.", visualAidEmoji = "🔗"),
            QuestionItem(11, "${p}What process creates gametes (sperm and egg cells) with half the somatic chromosome number (haploid n)?", listOf("Meiosis", "Mitosis", "Binary Fission", "Cytokinesis"), 0, "Meiosis involves two consecutive divisions producing 4 haploid daughter cells.", "Super! Meiosis creates genetic diversity in gametes.", visualAidEmoji = "🔄"),
            QuestionItem(12, "${p}What principle states that no two electrons in an atom can have the same set of four quantum numbers?", listOf("Pauli Exclusion Principle", "Heisenberg Uncertainty Principle", "Hund's Rule", "Aufbau Principle"), 0, "Wolfgang Pauli established that opposite spins are required in an orbital.", "Spot on! Pauli Exclusion governs orbital occupancy.", visualAidEmoji = "🔬"),
            QuestionItem(13, "${p}Which organ system is responsible for producing hormones like insulin, thyroid hormone, and cortisol?", listOf("Endocrine System", "Integumentary System", "Lymphatic System", "Skeletal System"), 0, "Endocrine glands release chemical messengers into the bloodstream.", "Exact! The endocrine system regulates hormonal homeostasis.", visualAidEmoji = "🩺"),
            QuestionItem(14, "${p}What is the speed of light in a vacuum (c)?", listOf("Approximately 3.0 × 10⁸ m/s (300,000 km/s)", "343 m/s", "1,000 km/h", "9.8 × 10⁶ m/s"), 0, "c = 299,792,458 m/s.", "Awesome! Light travels at ~300,000 km per second in a vacuum.", visualAidEmoji = "✨"),
            QuestionItem(15, "${p}What thermodynamic law states that the entropy (disorder) of an isolated system always increases over time?", listOf("Second Law of Thermodynamics", "First Law of Thermodynamics", "Third Law of Thermodynamics", "Zeroth Law"), 0, "ΔS_universe ≥ 0.", "Correct! The 2nd Law defines the thermodynamic arrow of time.", visualAidEmoji = "♨️"),
            QuestionItem(16, "${p}What is an enzyme's active site?", listOf("The specific region where substrate molecules bind and undergo chemical catalysis", "The DNA binding domain", "The outer cell membrane", "The energy storage depot"), 0, "Substrates fit into active sites like a lock and key (induced fit).", "Spot on! Active sites catalyze biochemical reactions.", visualAidEmoji = "🧩"),
            QuestionItem(17, "${p}What causes the Coriolis effect on Earth's atmosphere and ocean currents?", listOf("Earth's rotation on its axis", "Ocean tides from the Moon", "Volcanic activity", "Solar flares"), 0, "Earth's rotation deflects winds to the right in the North, left in the South.", "Brilliant! Planetary rotation drives the Coriolis effect.", visualAidEmoji = "🌀"),
            QuestionItem(18, "${p}Which blood type is considered the universal red blood cell donor?", listOf("O negative (O-)", "AB positive (AB+)", "A positive (A+)", "B negative (B-)"), 0, "O- lacks A, B, and Rh antigens, preventing recipient immune rejection.", "Crucial medical knowledge! O- is the universal RBC donor.", visualAidEmoji = "🩸"),
            QuestionItem(19, "${p}What is the relationship between wavelength and frequency for electromagnetic waves (c = λν)?", listOf("Inversely proportional (shorter wavelength = higher frequency)", "Directly proportional", "Unrelated", "Constant sum"), 0, "As wavelength gets shorter, wave frequency and photon energy increase.", "Exact! λ and ν are inversely proportional.", visualAidEmoji = "〰️"),
            QuestionItem(20, "${p}Mastery Challenge: What fundamental equation expresses mass-energy equivalence in Special Relativity?", listOf("E = mc²", "F = ma", "PV = nRT", "V = IR"), 0, "Albert Einstein demonstrated that mass can be converted into energy.", "Grand Champion! E = mc² connects energy, mass, and the speed of light!", visualAidEmoji = "🏆")
        )
    }

    private fun generate20HighSchoolSocialStudiesQuestions(themeId: String): List<QuestionItem> {
        val p = "🌍 HS Civics & Econ: "
        return listOf(
            QuestionItem(1, "${p}What landmark 1803 Supreme Court case established the principle of Judicial Review?", listOf("Marbury v. Madison", "McCulloch v. Maryland", "Brown v. Board of Education", "Gibbons v. Ogden"), 0, "Chief Justice John Marshall declared that courts can invalidate unconstitutional acts.", "Spot on! Marbury v. Madison established judicial review.", visualAidEmoji = "🏛️"),
            QuestionItem(2, "${p}In macroeconomics, what does Gross Domestic Product (GDP) measure?", listOf("The total monetary value of all finished goods and services produced within a country in a year", "The national debt", "The stock market average", "The total paper currency printed"), 0, "GDP = Consumption + Investment + Government Spending + Net Exports (C+I+G+NX).", "Correct! GDP measures total national economic output.", visualAidEmoji = "📊"),
            QuestionItem(3, "${p}Which constitutional amendment granted women the right to vote (women's suffrage) in the United States in 1920?", listOf("19th Amendment", "13th Amendment", "15th Amendment", "26th Amendment"), 0, "Ratified after decades of tireless advocacy by suffragists.", "Super! The 19th Amendment secured women's voting rights.", visualAidEmoji = "🗳️"),
            QuestionItem(4, "${p}What economic term describes a sustained increase in the general price level of goods and services over time?", listOf("Inflation", "Deflation", "Stagnation", "Recession"), 0, "Inflation erodes the purchasing power of currency.", "Spot on! Inflation measures rising price levels.", visualAidEmoji = "📈"),
            QuestionItem(5, "${p}Who is considered the primary author of the Declaration of Independence (1776)?", listOf("Thomas Jefferson", "Benjamin Franklin", "Alexander Hamilton", "George Washington"), 0, "Drafted by the Committee of Five with Jefferson as lead author.", "Exact! Thomas Jefferson drafted the Declaration.", visualAidEmoji = "📜"),
            QuestionItem(6, "${p}What is the role of the Electoral College in US presidential elections?", listOf("An official body of 538 state electors who cast ballots to elect the President and VP", "A college for political science students", "A congressional committee", "The Supreme Court clerk staff"), 0, "A candidate needs 270 electoral votes to win the presidency.", "Terrific! 538 electors compose the Electoral College.", visualAidEmoji = "🏛️"),
            QuestionItem(7, "${p}What economic structure is characterized by private ownership of capital and free-market pricing driven by supply and demand?", listOf("Capitalism / Free Enterprise", "Command Economy", "Feudalism", "Mercantilism"), 0, "Markets allocate resources through voluntary trade and price signals.", "Well done! Capitalism relies on market competition and private enterprise.", visualAidEmoji = "💵"),
            QuestionItem(8, "${p}Which constitutional clause in Article VI establishes that the US Constitution and federal treaties are the 'supreme Law of the Land'?", listOf("The Supremacy Clause", "The Commerce Clause", "The Elastic Clause", "The Establishment Clause"), 0, "Federal constitutional law preempts conflicting state legislation.", "Spot on! The Supremacy Clause establishes federal constitutional primacy.", visualAidEmoji = "⚖️"),
            QuestionItem(9, "${p}What major 1944 international conference established the World Bank and International Monetary Fund (IMF)?", listOf("Bretton Woods Conference", "Treaty of Versailles", "Yalta Conference", "Potsdam Conference"), 0, "Held in New Hampshire to stabilize post-WWII global monetary systems.", "Awesome historical knowledge! Bretton Woods framed modern global finance.", visualAidEmoji = "🌐"),
            QuestionItem(10, "${p}What is Gerrymandering?", listOf("The practice of redrawing legislative electoral district boundaries to give one political party an unfair advantage", "Passing a bill without debate", "A filibuster in the Senate", "Appointing judges for life"), 0, "Named after Governor Elbridge Gerry in 1812.", "Correct! Gerrymandering manipulates district maps.", visualAidEmoji = "🗺️"),
            QuestionItem(11, "${p}In economic theory, what is 'Opportunity Cost'?", listOf("The value of the next best alternative given up when making a choice", "The monetary price on a receipt", "The cost of raw materials", "Shipping and handling fees"), 0, "Every choice means sacrificing the next best option.", "Super! Opportunity cost is the sacrificed alternative value.", visualAidEmoji = "💡"),
            QuestionItem(12, "${p}Which branch of Congress has 100 members (2 per state) serving 6-year staggered terms?", listOf("United States Senate", "House of Representatives", "Supreme Court", "Cabinet"), 0, "The Senate represents states equally under Article I.", "Exact! The US Senate comprises 100 senators.", visualAidEmoji = "🏛️"),
            QuestionItem(13, "${p}What landmark 1954 Supreme Court decision struck down racial segregation in public schools, overturning Plessy v. Ferguson?", listOf("Brown v. Board of Education", "Roe v. Wade", "Miranda v. Arizona", "Gideon v. Wainwright"), 0, "Declared that 'separate educational facilities are inherently unequal.'", "Spot on! Brown v. Board mandated school desegregation.", visualAidEmoji = "🤝"),
            QuestionItem(14, "${p}What is a trade deficit?", listOf("When a country imports more goods and services in value than it exports", "When tariffs are abolished", "When domestic production stops", "When currency is pegged to gold"), 0, "Imports > Exports = Trade Deficit.", "Yes! A trade deficit occurs when imports exceed exports.", visualAidEmoji = "🚢"),
            QuestionItem(15, "${p}How many justices typically serve on the Supreme Court of the United States?", listOf("9", "12", "7", "15"), 0, "One Chief Justice and eight Associate Justices.", "Terrific! The Supreme Court has 9 lifetime-appointed justices.", visualAidEmoji = "⚖️"),
            QuestionItem(16, "${p}What was the primary purpose of the Marshall Plan (1948)?", listOf("To provide economic aid to rebuild war-torn Western Europe and prevent the spread of communism", "To build the Panama Canal", "To explore space", "To lower domestic taxes"), 0, "Named after US Secretary of State George C. Marshall.", "Brilliant! The Marshall Plan revitalized European infrastructure.", visualAidEmoji = "🏗️"),
            QuestionItem(17, "${p}What is the primary function of the Bill of Rights?", listOf("To protect individual civil liberties and limit governmental overreach", "To collect income taxes", "To establish military branches", "To set election dates"), 0, "The first 10 amendments safeguard foundational human rights.", "Super! The Bill of Rights guarantees constitutional protections.", visualAidEmoji = "📜"),
            QuestionItem(18, "${p}Which economic concept describes a market dominated by a single seller with no close substitutes?", listOf("Monopoly", "Oligopoly", "Perfect Competition", "Monopsony"), 0, "Monopolies possess extreme pricing power and high barriers to entry.", "Correct! Monopolies control entire market supply.", visualAidEmoji = "🏢"),
            QuestionItem(19, "${p}What is the minimum voting age guaranteed by the 26th Amendment (1971)?", listOf("18 years old", "21 years old", "16 years old", "25 years old"), 0, "Ratified during the Vietnam War: 'Old enough to fight, old enough to vote.'", "Spot on! The 26th Amendment lowered the voting age to 18.", visualAidEmoji = "🗳️"),
            QuestionItem(20, "${p}Mastery Challenge: What is the concept of 'Popular Sovereignty'?", listOf("The foundational doctrine that governmental legitimacy and authority are created and sustained by the consent of the governed people", "Rule by a monarch", "Military dictatorship", "Rule by artificial intelligence"), 0, "'We the People' establishes the ultimate source of power in a democracy.", "Grand Champion! Popular sovereignty places supreme power in the citizens!", visualAidEmoji = "🏆")
        )
    }

    private fun generate20HighSchoolLifeSkillsQuestions(themeId: String): List<QuestionItem> {
        val p = "💼 HS Life Skills: "
        return listOf(
            QuestionItem(1, "${p}What is the 'Rule of 72' used for in personal finance?", listOf("Estimating the number of years required to double an investment at a given fixed annual interest rate", "Calculating tax deductions", "Determining mortgage terms", "Measuring inflation on groceries"), 0, "Divide 72 by the annual return rate (e.g. 72 / 8% = 9 years).", "Spot on! 72 / rate = years to double capital.", visualAidEmoji = "📈"),
            QuestionItem(2, "${p}What is the fundamental difference between a Roth IRA and a Traditional IRA?", listOf("Roth contributions are made with after-tax money and grow 100% tax-free in retirement, whereas Traditional is pre-tax", "Roth IRAs are only for businesses", "Traditional IRAs cannot invest in stocks", "They have identical tax structures"), 0, "Roth IRA withdrawals in retirement are completely tax-free.", "Correct! Roth IRAs provide tax-free retirement growth.", visualAidEmoji = "💰"),
            QuestionItem(3, "${p}What is the recommended maximum percentage of your credit limit (Credit Utilization) you should use to maintain a high credit score?", listOf("Under 30% (ideally under 10%)", "100% of the limit", "At least 80%", "Credit utilization does not matter"), 0, "High credit utilization signals risk to lenders and lowers credit scores.", "Super! Keeping utilization below 30% boosts credit scores.", visualAidEmoji = "💳"),
            QuestionItem(4, "${p}In executive functioning, what is 'Time Blocking'?", listOf("Scheduling specific, dedicated calendar time blocks for individual tasks rather than relying on an unorganized to-do list", "Ignoring the clock completely", "Stopping work at noon", "Working without breaks"), 0, "Time blocking protects focus and reduces decision fatigue.", "Brilliant! Time blocking structures high-focus productivity.", visualAidEmoji = "🗓️"),
            QuestionItem(5, "${p}What is the purpose of an Emergency Fund in personal finance?", listOf("Maintaining 3 to 6 months of living expenses in an accessible high-yield savings account for unexpected crises", "Buying luxury electronics", "Speculative stock trading", "Paying parking tickets only"), 0, "An emergency fund protects you from relying on high-interest debt.", "Spot on! 3-6 months of living expenses provides financial security.", visualAidEmoji = "🛡️"),
            QuestionItem(6, "${p}What does 'W-2' refer to in United States employment and taxation?", listOf("An official annual tax form provided by employers reporting your earned wages and taxes withheld", "A college application", "A driver's license application", "A retirement account"), 0, "Employers must send W-2 forms by January 31st each year.", "Exact! W-2 reports annual income and tax withholdings.", visualAidEmoji = "📄"),
            QuestionItem(7, "${p}What is the 'Pomodoro Technique' for academic studying and focus?", listOf("Working with 25 minutes of deep focus followed by a 5-minute restorative sensory break", "Studying for 5 hours straight", "Eating pasta while writing essays", "Listening to loud music non-stop"), 0, "Short focus cycles sustain executive energy and prevent mental fatigue.", "Great! 25m work + 5m rest keeps cognitive stamina high.", visualAidEmoji = "🍅"),
            QuestionItem(8, "${p}When signing a residential apartment lease, what is a 'Security Deposit'?", listOf("A refundable deposit held by the landlord to cover potential property damage during your tenancy", "A monthly utility fee", "The broker commission", "A government tax"), 0, "Returned at move-out if the apartment is clean and undamaged.", "Super! Security deposits protect against lease damages.", visualAidEmoji = "🔑"),
            QuestionItem(9, "${p}What is the difference between a subsidized and unsubsidized federal student loan?", listOf("The government pays the interest on subsidized loans while you are enrolled in school, but interest accrues immediately on unsubsidized", "Subsidized loans never have to be repaid", "Unsubsidized loans are only for graduate school", "They have identical terms"), 0, "Subsidized loans are based on demonstrated financial need.", "Crucial college prep knowledge! Subsidized loans save on in-school interest.", visualAidEmoji = "🎓"),
            QuestionItem(10, "${p}What is 'Body Doubling' in neurodivergent productivity and ADHD strategies?", listOf("Working on tasks alongside another person (in person or virtually) to enhance accountability and focus", "Hiring an actor to do your homework", "Cloning a computer file", "Working two jobs at once"), 0, "Having a partner nearby provides quiet executive grounding.", "Awesome! Body doubling creates gentle social accountability.", visualAidEmoji = "👥"),
            QuestionItem(11, "${p}What is a deductible in health or auto insurance?", listOf("The out-of-pocket amount you must pay before the insurance company begins covering expenses", "The monthly cost of the policy (premium)", "The maximum payout limit", "A discount code"), 0, "Higher deductibles usually mean lower monthly premiums.", "Spot on! The deductible is your upfront out-of-pocket threshold.", visualAidEmoji = "🩺"),
            QuestionItem(12, "${p}What is the 50/30/20 budgeting rule?", listOf("50% Needs, 30% Wants, 20% Savings & Debt Repayment", "50% Savings, 30% Housing, 20% Food", "50% Entertainment, 30% Needs, 20% Taxes", "Equal division across all items"), 0, "Popularized by financial experts for balanced money management.", "Yes! 50% Needs, 30% Wants, 20% Wealth Building.", visualAidEmoji = "📊"),
            QuestionItem(13, "${p}What is 'Phishing' in cybersecurity and digital literacy?", listOf("Deceptive emails or text messages designed to trick you into revealing passwords, banking info, or sensitive data", "Catching trout with a rod", "Upgrading computer RAM", "Playing video games online"), 0, "Always verify the sender's actual email address before clicking links.", "Crucial digital safety! Phishing tries to steal login credentials.", visualAidEmoji = "🎣"),
            QuestionItem(14, "${p}When crafting a professional resume, what should bullet points ideally emphasize?", listOf("Measurable achievements, quantifiable results, and specific action verbs", "Personal hobbies and favorite movies only", "Paragraph-long narratives", "List of high school friends"), 0, "Use formulas like 'Accomplished [X] as measured by [Y] by doing [Z]'.", "Terrific! Quantifiable achievements stand out to employers.", visualAidEmoji = "💼"),
            QuestionItem(15, "${p}What is an index fund (such as an S&P 500 ETF)?", listOf("A diversified portfolio of hundreds of stocks that tracks the overall performance of a market index with low fees", "A lottery ticket", "A single risky startup company", "A physical bank vault"), 0, "Index funds provide instant broad diversification and low expense ratios.", "Well done! Index funds are the bedrock of long-term wealth building.", visualAidEmoji = "📈"),
            QuestionItem(16, "${p}What is active listening during interpersonal communication?", listOf("Fully focusing on understanding the speaker, making eye contact, and reflecting back their meaning before responding", "Thinking about what you will say next while they talk", "Interrupting with advice immediately", "Checking your phone while listening"), 0, "Active listening builds trust and emotional intelligence.", "Spot on! Active listening validates the speaker.", visualAidEmoji = "👂"),
            QuestionItem(17, "${p}What does the term 'APR' stand for on credit cards and loan agreements?", listOf("Annual Percentage Rate", "Automatic Payment Receipt", "Account Principal Ratio", "Applied Personal Return"), 0, "APR represents the annual cost of borrowing money including interest fees.", "Exact! APR is the Annual Percentage Rate.", visualAidEmoji = "🏷️"),
            QuestionItem(18, "${p}What is the primary benefit of automating your savings transfers on payday?", listOf("'Paying yourself first' ensures savings happen before discretionary spending occurs", "It makes banks give you free gifts", "It eliminates all income taxes", "It prevents you from checking bank balance"), 0, "Automated habits eliminate reliance on daily willpower.", "Brilliant! Paying yourself first builds wealth effortlessly.", visualAidEmoji = "🔄"),
            QuestionItem(19, "${p}How does cognitive reframing help manage stress and anxiety?", listOf("Identifying irrational negative thought patterns and replacing them with realistic, constructive perspectives", "Ignoring problems until they disappear", "Blaming others for difficulties", "Giving up on goals"), 0, "Reframing shifts 'I can't do this' into 'This is challenging, but I have tools to handle it step by step'.", "Super! Cognitive reframing builds lifelong emotional resilience.", visualAidEmoji = "🧠"),
            QuestionItem(20, "${p}Mastery Challenge: What is the most powerful asset a young high school student has when investing for their future?", listOf("Time and compound growth over decades", "Having millions of dollars immediately", "Insider trading secrets", "Picking volatile individual stocks"), 0, "Starting early allows compound interest to multiply small sums into fortunes.", "Grand Champion! Decades of compound growth is your greatest superpower!", visualAidEmoji = "🏆")
        )
    }
}
