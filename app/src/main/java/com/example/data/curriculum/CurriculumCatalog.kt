package com.example.data.curriculum

import com.example.data.model.ConceptStep
import com.example.data.model.EducationalSubject
import com.example.data.model.FullLesson
import com.example.data.model.GradeLevel
import com.example.data.model.QuestionItem

object CurriculumCatalog {

    fun getLessonsForSubjectAndGrade(
        subject: EducationalSubject,
        gradeLevel: GradeLevel,
        stateStandardCode: String,
        themeWorldId: String = "dino",
        country: String = "United States"
    ): List<FullLesson> {
        val all = getMasterCurriculum(themeWorldId, country)
        val filtered = all.filter { it.subject == subject }
        return if (filtered.isNotEmpty()) filtered else getMasterCurriculum(themeWorldId, country).filter { it.subject == subject }
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
            else -> "CCSS.MATH.CONTENT.K.OA.A.1 (US Common Core & State Standards)"
        }

        val readingStandard = when {
            isUk -> "DfE.KS1.ENG.READ (UK Letters and Sounds Phonics)"
            isCa -> "ONTARIO.LANG.GR.1 (Ontario Language Curriculum)"
            isAu -> "ACARA.AC9E1LY01 (Australian English Literacy)"
            isIndia -> "NCERT.ENG.LANG.01 (NCERT English Foundational)"
            isDe -> "KMK.DEU.PR1 (KMK Grundschul-Lehrplan)"
            isFr -> "SOCLE.FR.FRANCAIS (Cycle 2 Français)"
            isJp -> "MEXT.KOKUGO.E1 (文部科学省 国語科指導要領)"
            else -> "CCSS.ELA-LITERACY.RF.K.1 (US Common Core ELA Standards)"
        }

        val scienceStandard = when {
            isUk -> "DfE.KS1.SCI.01 (UK National Curriculum Science)"
            isCa -> "ONTARIO.SCI.GR.1 (Ontario Science & Technology)"
            isAu -> "ACARA.AC9S1U01 (Australian Science Understanding)"
            isIndia -> "NCERT.EVS.P1 (NCERT Environmental Studies)"
            isDe -> "KMK.SU.PR1 (Sachunterricht Bildungsplan)"
            isFr -> "SOCLE.FR.SCI.C2 (Questionner le monde)"
            isJp -> "MEXT.RIKA.E1 (文部科学省 生活科・理科)"
            else -> "NGSS.K-LS1-1 (Next Generation Science Standards)"
        }

        val socialStandard = when {
            isUk -> "DfE.KS1.HIST.GEO (UK History & Geography)"
            isCa -> "ONTARIO.SS.GR.1 (Social Studies Heritage & Community)"
            isAu -> "ACARA.AC9HS1K01 (Australian HASS Framework)"
            isIndia -> "NCERT.SOC.P1 (NCERT Social Science & Civics)"
            isDe -> "KMK.SU.GEMEIN (Gesellschaftliches Lernen)"
            isFr -> "SOCLE.FR.EMC (Enseignement moral et civique)"
            isJp -> "MEXT.SHAKAI.E1 (文部科学省 生活科・社会科)"
            else -> "NCSS.D2.Civ.1.K-2 (National Social Studies Framework)"
        }

        return listOf(
            // 1. MATHEMATICS: Addition & Counting Patterns (20 Questions Adaptive)
            FullLesson(
                id = "math_counting_patterns",
                subject = EducationalSubject.MATH,
                gradeLevel = GradeLevel.KINDERGARTEN,
                stateStandardCode = mathStandard,
                standardDescription = "Master foundational number sense, addition models, skip counting, and geometry in strict alignment with accredited $country educational standards.",
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
                standardDescription = "Demonstrate phonological awareness, sound blending, and reading comprehension aligned with $country educational benchmarks.",
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
                standardDescription = "Observe patterns, living organism requirements, forces, and environments aligned with accredited $country science standards.",
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
                standardDescription = "Explain how community helpers, rules, and geography contribute to a thriving society in $country.",
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
                stateStandardCode = "CASEL.SEL.SELF_AWARENESS",
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
            )
        )
    }

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
}
