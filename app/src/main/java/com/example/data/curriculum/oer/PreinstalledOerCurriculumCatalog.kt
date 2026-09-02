package com.example.data.curriculum.oer

import com.example.data.model.EducationalSubject
import com.example.data.model.GradeLevel

object PreinstalledOerCurriculumCatalog {

    const val OER_COMMONS_BASE_URL = "https://oercommons.org/curated-collections"

    fun getAllPreinstalledCurriculum(): List<OerCommonsCurriculumItem> {
        return listOf(
            // ==========================================
            // 1. MATHEMATICS (K-12 FULL SPECTRUM)
            // ==========================================
            OerCommonsCurriculumItem(
                id = "oer_k_math_counting_clusters",
                subject = EducationalSubject.MATH,
                gradeLevel = GradeLevel.KINDERGARTEN,
                gradeBand = OerGradeBand.EARLY_CHILDHOOD,
                collectionTitle = "OER Commons Early Math: Counting & Cardinality",
                unitTitle = "Counting Clusters, Subitizing & Ten-Frames",
                standardCode = "CCSS.MATH.CONTENT.K.CC.B.4 / OER.K.MATH.01",
                oerCommonsUrl = "$OER_COMMONS_BASE_URL/early-math-cardinality",
                summary = "Master foundational one-to-one correspondence, visual subitizing up to 5, and ten-frame spatial number structures.",
                learningObjectives = listOf(
                    "Count objects in a group up to 20 with 1-to-1 correspondence.",
                    "Instantly recognize quantities up to 5 without counting (perceptual subitizing).",
                    "Represent quantities on a 10-frame visual grid."
                ),
                keyConcepts = listOf("One-to-One Matching", "Cardinality (The last number counted is the total)", "Ten-Frame Anchors"),
                vocabulary = listOf("Count", "Set", "Ten-Frame", "More", "Fewer", "Equal"),
                essentialQuestions = listOf(
                    "How does knowing the count of a small group help us build bigger numbers?",
                    "Why do numbers stay the same amount no matter how they are arranged?"
                ),
                socraticGuidingQuestions = listOf(
                    "If you have 4 stepping stones and place 1 more, what is the new total without recounting from one?",
                    "Can you see how 5 on top and 2 on the bottom fill the 10-frame?"
                ),
                commonMisconceptions = listOf(
                    "Recounting all objects from 1 instead of counting on from a known group.",
                    "Skipping objects when counting irregular clusters."
                ),
                practiceProblems = listOf(
                    OerPracticeProblem(
                        id = "p_k_math_1",
                        questionPrompt = "There are 5 leaves on the branch. A dinosaur eats 2 leaves. How many leaves are left?",
                        options = listOf("2", "3", "4", "7"),
                        correctAnswer = "3",
                        stepByStepExplanation = "Start at 5. Count backward 2 steps: 4, 3. There are 3 leaves remaining.",
                        socraticClue = "Hold 5 in your hand and take away 2."
                    )
                ),
                accessibilityAccommodations = listOf("High visual contrast ten-frames", "Tactile counter haptics", "Bite-sized question chunking")
            ),

            OerCommonsCurriculumItem(
                id = "oer_gr1_math_addition_bonds",
                subject = EducationalSubject.MATH,
                gradeLevel = GradeLevel.GRADE_1,
                gradeBand = OerGradeBand.ELEMENTARY,
                collectionTitle = "OER Commons Elementary: Operations & Algebraic Thinking",
                unitTitle = "Addition Strategies, Friends of 10 & Number Bonds",
                standardCode = "CCSS.MATH.CONTENT.1.OA.C.6 / OER.1.MATH.02",
                oerCommonsUrl = "$OER_COMMONS_BASE_URL/grade-1-operations",
                summary = "Decompose numbers within 20, utilize commutative property, and anchor sums to make-a-ten.",
                learningObjectives = listOf(
                    "Fluently add and subtract within 10 and apply make-ten strategies within 20.",
                    "Understand that 4 + 6 is the exact same sum as 6 + 4 (Commutative Property).",
                    "Break apart addends to simplify mental math (e.g., 8 + 5 = 8 + 2 + 3 = 13)."
                ),
                keyConcepts = listOf("Number Bonds (Part-Part-Whole)", "Making Ten Anchor", "Commutative Addition"),
                vocabulary = listOf("Addend", "Sum", "Difference", "Equation", "Decompose"),
                essentialQuestions = listOf(
                    "How can grouping numbers into tens make mental math faster and easier?",
                    "Why can we switch the order of numbers when adding but not when subtracting?"
                ),
                socraticGuidingQuestions = listOf(
                    "What number needs to be added to 7 to make a friendly 10?",
                    "If 8 + 2 = 10, how can you use that to quickly solve 8 + 6?"
                ),
                commonMisconceptions = listOf(
                    "Believing the equal sign means 'the answer goes here' rather than balance between two sides."
                ),
                practiceProblems = listOf(
                    OerPracticeProblem(
                        id = "p_gr1_math_1",
                        questionPrompt = "Solve: 7 + 8 = ?",
                        options = listOf("14", "15", "16", "17"),
                        correctAnswer = "15",
                        stepByStepExplanation = "Take 3 from 8 to give to 7 to make 10. You have 5 left. 10 + 5 = 15.",
                        socraticClue = "Think: What does 7 need to reach 10? Take that from 8!"
                    )
                ),
                accessibilityAccommodations = listOf("Visual split-arrow number bonds", "Audio read-aloud support", "No time limits")
            ),

            OerCommonsCurriculumItem(
                id = "oer_gr3_math_multiplication_arrays",
                subject = EducationalSubject.MATH,
                gradeLevel = GradeLevel.GRADE_3,
                gradeBand = OerGradeBand.ELEMENTARY,
                collectionTitle = "OER Commons Grade 3: Multiplication & Area Arrays",
                unitTitle = "Multiplication as Equal Groups, Arrays & Area Modeling",
                standardCode = "CCSS.MATH.CONTENT.3.OA.A.1 / OER.3.MATH.03",
                oerCommonsUrl = "$OER_COMMONS_BASE_URL/grade-3-multiplication-arrays",
                summary = "Connect repeated addition to equal rectangular arrays, area grid models, and the distributive property.",
                learningObjectives = listOf(
                    "Interpret products of whole numbers as equal groups of items.",
                    "Construct rectangular arrays with rows and columns to represent multiplication.",
                    "Apply the distributive property to decompose tough facts (e.g., 7 × 8 = (5 × 8) + (2 × 8))."
                ),
                keyConcepts = listOf("Equal Groups", "Rows & Columns", "Area Tile Model", "Distributive Property"),
                vocabulary = listOf("Factor", "Product", "Array", "Dimension", "Area"),
                essentialQuestions = listOf(
                    "How is multiplication related to repeated addition and physical grid area?",
                    "How does breaking a difficult factor into friendly numbers help us solve big facts?"
                ),
                socraticGuidingQuestions = listOf(
                    "If you have 4 rows with 6 stars in each row, how many equal groups is that?",
                    "How can you break 7 × 6 into two smaller, easier multiplication facts?"
                ),
                commonMisconceptions = listOf(
                    "Confusing 3 × 4 (3 groups of 4) with 3 + 4 (adding 3 and 4)."
                ),
                practiceProblems = listOf(
                    OerPracticeProblem(
                        id = "p_gr3_math_1",
                        questionPrompt = "A garden has 6 rows of carrots with 4 carrots in each row. How many carrots are in the garden?",
                        options = listOf("10", "20", "24", "28"),
                        correctAnswer = "24",
                        stepByStepExplanation = "Multiply rows by items per row: 6 rows × 4 carrots = 24 total carrots.",
                        socraticClue = "Count 6 groups of 4 or add 4 six times."
                    )
                ),
                accessibilityAccommodations = listOf("Grid highlighting", "Color-coded factor blocks")
            ),

            OerCommonsCurriculumItem(
                id = "oer_gr5_math_fractions_operations",
                subject = EducationalSubject.MATH,
                gradeLevel = GradeLevel.GRADE_5,
                gradeBand = OerGradeBand.ELEMENTARY,
                collectionTitle = "OER Commons Grade 5: Number & Operations - Fractions",
                unitTitle = "Fraction Equivalence, Unlike Denominators & Visual Models",
                standardCode = "CCSS.MATH.CONTENT.5.NF.A.1 / OER.5.MATH.04",
                oerCommonsUrl = "$OER_COMMONS_BASE_URL/grade-5-fractions",
                summary = "Add and subtract fractions with unlike denominators by finding common multiples and equivalent fraction bars.",
                learningObjectives = listOf(
                    "Find common denominators using least common multiples (LCM).",
                    "Add and subtract fractions and mixed numbers with unlike denominators.",
                    "Use visual fraction bar models and number lines to verify reasonableness."
                ),
                keyConcepts = listOf("Numerator & Denominator", "Equivalent Fractions", "Common Denominator", "Benchmark Fractions"),
                vocabulary = listOf("Numerator", "Denominator", "Least Common Denominator", "Improper Fraction", "Mixed Number"),
                essentialQuestions = listOf(
                    "Why must fractions have the same denominator before we can add or subtract them?",
                    "How do benchmark fractions like 1/2 help us estimate whether our answer makes sense?"
                ),
                socraticGuidingQuestions = listOf(
                    "If we are adding 1/2 and 1/4, what common denominator can both 2 and 4 share?",
                    "What happens to the size of each slice when the denominator gets larger?"
                ),
                commonMisconceptions = listOf(
                    "Adding numerators and denominators together across (e.g. 1/2 + 1/3 = 2/5 ❌)."
                ),
                practiceProblems = listOf(
                    OerPracticeProblem(
                        id = "p_gr5_math_1",
                        questionPrompt = "What is 1/3 + 1/6 in simplest form?",
                        options = listOf("2/9", "1/2", "3/6", "2/6"),
                        correctAnswer = "1/2",
                        stepByStepExplanation = "Convert 1/3 to 2/6. Then 2/6 + 1/6 = 3/6. Simplify 3/6 by dividing numerator and denominator by 3 = 1/2.",
                        socraticClue = "First find a common denominator for 3 and 6, then add only the top numerators."
                    )
                ),
                accessibilityAccommodations = listOf("Fraction bar visualizers", "Step-by-step denominator guides")
            ),

            OerCommonsCurriculumItem(
                id = "oer_mid_math_linear_equations",
                subject = EducationalSubject.MATH,
                gradeLevel = GradeLevel.GRADE_8,
                gradeBand = OerGradeBand.MIDDLE_SCHOOL,
                collectionTitle = "OER Commons Middle School: Linear Equations & Functions",
                unitTitle = "Solving Multi-Step Linear Equations & Slope-Intercept Form",
                standardCode = "CCSS.MATH.CONTENT.8.EE.C.7 / OER.8.MATH.05",
                oerCommonsUrl = "$OER_COMMONS_BASE_URL/grade-8-linear-equations",
                summary = "Solve linear equations with variables on both sides, expand using the distributive property, and graph y = mx + b.",
                learningObjectives = listOf(
                    "Solve linear equations with rational number coefficients involving distributive property.",
                    "Identify equations that have one solution, infinitely many solutions, or no solution.",
                    "Interpret rate of change (slope m) and initial value (y-intercept b) from tables and graphs."
                ),
                keyConcepts = listOf("Inverse Operations", "Balance Principle", "Slope as Rate of Change (Δy/Δx)", "Y-Intercept"),
                vocabulary = listOf("Variable", "Coefficient", "Constant", "Slope", "Intercept", "Solution Set"),
                essentialQuestions = listOf(
                    "Why must an operation performed on one side of an equation be matched on the other?",
                    "How does slope describe the steepness and direction of a linear relationship?"
                ),
                socraticGuidingQuestions = listOf(
                    "What inverse operation undoes multiplication? What undoes subtraction?",
                    "If an equation simplifies to 5 = 5, what does that tell you about the number of solutions?"
                ),
                commonMisconceptions = listOf(
                    "Forgetting to distribute negative signs across parentheses.",
                    "Confusing the coordinates (x, y) with slope m."
                ),
                practiceProblems = listOf(
                    OerPracticeProblem(
                        id = "p_mid_math_1",
                        questionPrompt = "Solve for x: 3(x + 4) = 24",
                        options = listOf("x = 4", "x = 8", "x = 12", "x = 20"),
                        correctAnswer = "x = 4",
                        stepByStepExplanation = "Step 1: Divide both sides by 3 to get x + 4 = 8. Step 2: Subtract 4 from both sides: x = 4.",
                        socraticClue = "Either distribute 3 first (3x + 12 = 24) or divide both sides by 3."
                    )
                ),
                accessibilityAccommodations = listOf("Algebra balance visual simulator", "Highlighted inverse operation steps")
            ),

            OerCommonsCurriculumItem(
                id = "oer_hs_math_algebra1_quadratics",
                subject = EducationalSubject.MATH,
                gradeLevel = GradeLevel.HIGH_SCHOOL,
                gradeBand = OerGradeBand.HIGH_SCHOOL,
                collectionTitle = "OER Commons High School Mathematics: Algebra I Curated Collection",
                unitTitle = "Quadratic Functions, Factoring & Quadratic Formula",
                standardCode = "CCSS.MATH.CONTENT.HSA.REI.B.4 / OER.HS.MATH.01",
                oerCommonsUrl = "$OER_COMMONS_BASE_URL/hs-algebra-1-quadratics",
                summary = "Comprehensive exploration of quadratic functions (ax² + bx + c = 0), factoring trinomials, vertex form, and applying the quadratic formula.",
                learningObjectives = listOf(
                    "Solve quadratic equations by factoring, completing the square, and using the quadratic formula x = (-b ± √(b² - 4ac)) / (2a).",
                    "Analyze the discriminant (b² - 4ac) to determine the number and nature of real solutions.",
                    "Graph parabolas identifying the vertex (-b/(2a), f(-b/(2a))), axis of symmetry, and x/y intercepts.",
                    "Model real-world projectile motion (h(t) = -16t² + v₀t + h₀)."
                ),
                keyConcepts = listOf("Parabola", "Zero Product Property", "Vertex Form y = a(x-h)² + k", "Discriminant", "Quadratic Formula"),
                vocabulary = listOf("Quadratic", "Parabola", "Vertex", "Axis of Symmetry", "Roots/Zeros", "Discriminant", "Trinomial"),
                essentialQuestions = listOf(
                    "How do quadratic relationships model physical phenomena like gravity and satellite dish reflections?",
                    "When is factoring faster than using the quadratic formula, and vice-versa?"
                ),
                socraticGuidingQuestions = listOf(
                    "If (x - 3)(x + 5) = 0, what values of x make each factor zero?",
                    "What does a negative discriminant (b² - 4ac < 0) tell you about where the parabola touches the x-axis?"
                ),
                commonMisconceptions = listOf(
                    "Forgetting the ± symbol when taking square roots or in the quadratic formula.",
                    "Applying zero product property when the other side is not equal to zero."
                ),
                practiceProblems = listOf(
                    OerPracticeProblem(
                        id = "p_hs_math_1",
                        questionPrompt = "Find the roots of x² - 5x + 6 = 0 by factoring.",
                        options = listOf("x = 2 and x = 3", "x = -2 and x = -3", "x = 1 and x = 6", "x = -1 and x = 6"),
                        correctAnswer = "x = 2 and x = 3",
                        stepByStepExplanation = "Find two numbers that multiply to +6 and add to -5: they are -2 and -3. Factored: (x - 2)(x - 3) = 0. Set each factor to 0: x = 2, x = 3.",
                        socraticClue = "Look for two numbers whose product is 6 and whose sum is -5."
                    )
                ),
                accessibilityAccommodations = listOf("Step-by-step formula breakdowns", "Color-coded coefficients a, b, c", "Dyscalculia number support")
            ),

            OerCommonsCurriculumItem(
                id = "oer_hs_math_geometry_trig",
                subject = EducationalSubject.MATH,
                gradeLevel = GradeLevel.HIGH_SCHOOL,
                gradeBand = OerGradeBand.HIGH_SCHOOL,
                collectionTitle = "OER Commons High School: Geometry & Trigonometry",
                unitTitle = "Right Triangle Trigonometry, Similarity & Pythagorean Theorem",
                standardCode = "CCSS.MATH.CONTENT.HSG.SRT.C.8 / OER.HS.MATH.02",
                oerCommonsUrl = "$OER_COMMONS_BASE_URL/hs-geometry-trigonometry",
                summary = "Apply trigonometric ratios (sine, cosine, tangent) and the Pythagorean theorem to solve right triangles in applied contexts.",
                learningObjectives = listOf(
                    "Define trigonometric ratios using SOH-CAH-TOA for acute angles in right triangles.",
                    "Use inverse trig functions (arcsin, arccos, arctan) to find unknown angle measures.",
                    "Solve multi-step real-world problems involving angles of elevation and depression."
                ),
                keyConcepts = listOf("SOH-CAH-TOA", "Sine = Opposite/Hypotenuse", "Cosine = Adjacent/Hypotenuse", "Tangent = Opposite/Adjacent"),
                vocabulary = listOf("Hypotenuse", "Adjacent", "Opposite", "Sine", "Cosine", "Tangent", "Angle of Elevation"),
                essentialQuestions = listOf(
                    "Why are trigonometric ratios constant for all similar right triangles with the same acute angle?",
                    "How can indirect measurement be used to calculate heights of tall structures?"
                ),
                socraticGuidingQuestions = listOf(
                    "Which side is across from the right angle? Which side is adjacent to angle θ?",
                    "If you know the opposite side and hypotenuse, which trig function relates them?"
                ),
                commonMisconceptions = listOf(
                    "Using degrees mode vs radians mode incorrectly on calculators.",
                    "Mixing up the adjacent side with the hypotenuse."
                ),
                practiceProblems = listOf(
                    OerPracticeProblem(
                        id = "p_hs_geom_1",
                        questionPrompt = "In a right triangle, the side opposite angle θ is 3 and the hypotenuse is 5. What is sin(θ)?",
                        options = listOf("3/5", "4/5", "3/4", "5/3"),
                        correctAnswer = "3/5",
                        stepByStepExplanation = "By definition, sin(θ) = Opposite / Hypotenuse. Since Opposite = 3 and Hypotenuse = 5, sin(θ) = 3/5 = 0.6.",
                        socraticClue = "Remember SOH: Sine is Opposite over Hypotenuse."
                    )
                ),
                accessibilityAccommodations = listOf("Visual triangle diagram labels", "Mnemonic audio cues")
            ),

            // ==========================================
            // 2. ENGLISH LANGUAGE ARTS & READING
            // ==========================================
            OerCommonsCurriculumItem(
                id = "oer_k_reading_phonics_cvc",
                subject = EducationalSubject.READING,
                gradeLevel = GradeLevel.KINDERGARTEN,
                gradeBand = OerGradeBand.EARLY_CHILDHOOD,
                collectionTitle = "OER Commons Early Literacy: Phonics & Phonemic Awareness",
                unitTitle = "Phoneme Blending, CVC Word Families & Rhyming",
                standardCode = "CCSS.ELA-LITERACY.RF.K.2 / OER.K.ELA.01",
                oerCommonsUrl = "$OER_COMMONS_BASE_URL/early-phonics-cvc",
                summary = "Segment and blend individual letter sounds in Consonant-Vowel-Consonant (CVC) words and identify rhyming patterns.",
                learningObjectives = listOf(
                    "Isolate initial, medial vowel, and final phonemes in three-phoneme words.",
                    "Blend sounds together smoothly to read CVC words (e.g., /b/ + /a/ + /t/ = BAT).",
                    "Recognize and produce rhyming word pairs."
                ),
                keyConcepts = listOf("Phoneme Segmentation", "Blending", "Word Families (-at, -op, -un)", "Short Vowels"),
                vocabulary = listOf("Letter", "Sound", "Vowel", "Consonant", "Rhyme", "Blend"),
                essentialQuestions = listOf(
                    "How does hearing individual sounds in a word help us read and spell?",
                    "Why do words that rhyme sound the same at the end?"
                ),
                socraticGuidingQuestions = listOf(
                    "What sound do you hear at the very beginning of 'SUN'? What sound is in the middle?",
                    "If you change the /b/ in 'BAT' to /c/, what new word do you make?"
                ),
                commonMisconceptions = listOf(
                    "Adding an extra 'uh' sound to consonants (saying 'buh' instead of a crisp /b/)."
                ),
                practiceProblems = listOf(
                    OerPracticeProblem(
                        id = "p_k_ela_1",
                        questionPrompt = "Blend these sounds together: /f/ + /o/ + /x/. What word does it make?",
                        options = listOf("BOX", "FOX", "FIX", "FOG"),
                        correctAnswer = "FOX",
                        stepByStepExplanation = "Slide the sounds together: /f/ -> /fo/ -> /fox/ makes the word FOX.",
                        socraticClue = "Start with the /f/ sound and slide to /ox/."
                    )
                ),
                accessibilityAccommodations = listOf("Phoneme highlight boxes", "Slow synthesized speech playback", "Dyslexia-spaced typography")
            ),

            OerCommonsCurriculumItem(
                id = "oer_hs_ela_rhetorical_analysis",
                subject = EducationalSubject.READING,
                gradeLevel = GradeLevel.HIGH_SCHOOL,
                gradeBand = OerGradeBand.HIGH_SCHOOL,
                collectionTitle = "OER Commons High School ELA: Rhetoric & Composition",
                unitTitle = "Rhetorical Analysis: Aristotelian Appeals & Argumentation",
                standardCode = "CCSS.ELA-LITERACY.RL.9-10.1 / CCSS.ELA-LITERACY.RI.11-12.6 / OER.HS.ELA.01",
                oerCommonsUrl = "$OER_COMMONS_BASE_URL/hs-rhetoric-composition",
                summary = "Analyze authorial craft, evaluate rhetorical appeals (Ethos, Pathos, Logos), syntax, tone, and construct rigorous analytical essays.",
                learningObjectives = listOf(
                    "Identify and evaluate Aristotelian rhetorical appeals: Ethos (credibility), Pathos (emotion), and Logos (logic/evidence).",
                    "Analyze how an author's diction, syntax, figurative language, and structural choices establish tone and advance their central thesis.",
                    "Synthesize multiple complex non-fiction and literary texts into a cohesive, evidence-backed argumentative essay."
                ),
                keyConcepts = listOf("Ethos, Pathos, Logos", "Rhetorical Triangle", "Tone vs. Mood", "Authorial Purpose", "Counter-Argument & Rebuttal"),
                vocabulary = listOf("Rhetoric", "Ethos", "Pathos", "Logos", "Diction", "Syntax", "Concession", "Rebuttal", "Fallacy"),
                essentialQuestions = listOf(
                    "How do authors use language strategically to persuade, inform, or evoke emotional resonance in diverse audiences?",
                    "How does evaluating rhetorical strategies make us critical consumers of media and public discourse?"
                ),
                socraticGuidingQuestions = listOf(
                    "How does the speaker establish their authority and credibility (Ethos) with a skeptical audience?",
                    "Where does the author acknowledge an opposing viewpoint, and what evidence do they use to refute it?"
                ),
                commonMisconceptions = listOf(
                    "Labeling an appeal as 'using Pathos' without explaining the specific emotional effect and why it persuades the reader."
                ),
                practiceProblems = listOf(
                    OerPracticeProblem(
                        id = "p_hs_ela_1",
                        questionPrompt = "A speaker cites peer-reviewed clinical research and statistical data from 10,000 participants. Which rhetorical appeal is primarily being utilized?",
                        options = listOf("Pathos", "Logos", "Ethos", "Kairos"),
                        correctAnswer = "Logos",
                        stepByStepExplanation = "Logos appeals to logic, reason, empirical evidence, and statistics to support a claim.",
                        socraticClue = "Think: Which appeal is rooted in logic, numbers, facts, and rational proofs?"
                    )
                ),
                accessibilityAccommodations = listOf("Graphic organizers for essay synthesis", "Text-to-speech audio highlighting")
            ),

            // ==========================================
            // 3. SCIENCE & NATURE
            // ==========================================
            OerCommonsCurriculumItem(
                id = "oer_k_sci_living_ecosystems",
                subject = EducationalSubject.SCIENCE,
                gradeLevel = GradeLevel.KINDERGARTEN,
                gradeBand = OerGradeBand.EARLY_CHILDHOOD,
                collectionTitle = "OER Commons Primary Science: Life Science & Earth",
                unitTitle = "Living Things, Plant Needs, Animal Habitats & Weather",
                standardCode = "NGSS.K-LS1-1 / OER.K.SCI.01",
                oerCommonsUrl = "$OER_COMMONS_BASE_URL/k-life-science",
                summary = "Observe patterns in what plants and animals need to survive, explore habitats, and observe daily weather cycles.",
                learningObjectives = listOf(
                    "Identify the basic survival needs of plants (sunlight, water, soil) and animals (food, water, shelter).",
                    "Classify living versus non-living things based on growth and needs.",
                    "Describe weather patterns and seasonal shifts."
                ),
                keyConcepts = listOf("Living vs Non-Living", "Habitat Needs", "Sunlight & Water", "Seasonal Cycles"),
                vocabulary = listOf("Plant", "Animal", "Habitat", "Sunlight", "Survival", "Weather"),
                essentialQuestions = listOf(
                    "What makes a living thing different from a rock or a toy?",
                    "How does an animal's home provide what it needs to grow?"
                ),
                socraticGuidingQuestions = listOf(
                    "What happens to a plant if we keep it in a dark closet without any sunshine?",
                    "Why do fish need gills instead of lungs to live underwater?"
                ),
                commonMisconceptions = listOf(
                    "Believing plants 'eat' soil rather than making energy using sunlight and water through photosynthesis."
                ),
                practiceProblems = listOf(
                    OerPracticeProblem(
                        id = "p_k_sci_1",
                        questionPrompt = "What two essential things does a green plant need from its environment to make food?",
                        options = listOf("Sunlight and Water", "Juice and Milk", "Darkness and Cold", "Rocks and Wind"),
                        correctAnswer = "Sunlight and Water",
                        stepByStepExplanation = "Plants use energy from sunlight and water absorbed through their roots to grow strong.",
                        socraticClue = "Think about what you give a houseplant on a sunny windowsill."
                    )
                ),
                accessibilityAccommodations = listOf("Visual nature diagrams", "Tactile sensory interaction")
            ),

            OerCommonsCurriculumItem(
                id = "oer_hs_sci_molecular_biology",
                subject = EducationalSubject.SCIENCE,
                gradeLevel = GradeLevel.HIGH_SCHOOL,
                gradeBand = OerGradeBand.HIGH_SCHOOL,
                collectionTitle = "OER Commons High School Science: Biology Curated Collection",
                unitTitle = "Cellular Biology, DNA Replication, Protein Synthesis & Genetics",
                standardCode = "NGSS.HS-LS1-1 / NGSS.HS-LS3-1 / OER.HS.SCI.01",
                oerCommonsUrl = "$OER_COMMONS_BASE_URL/hs-biology-genetics",
                summary = "Examine DNA double helix structure, transcription (DNA ➡️ mRNA), translation at ribosomes, Mendelian genetics, and cellular respiration.",
                learningObjectives = listOf(
                    "Explain how DNA's nucleotide base-pairing code (A-T, C-G) stores genetic instructions for protein synthesis.",
                    "Trace the Central Dogma of Molecular Biology: DNA Transcription ➡️ mRNA ➡️ Ribosomal Translation ➡️ Amino Acid Polypeptide Chain.",
                    "Construct Punnett squares to predict phenotypic and genotypic ratios in monohybrid and dihybrid crosses.",
                    "Compare cellular respiration (C₆H₁₂O₆ + 6O₂ ➡️ 6CO₂ + 6H₂O + 36 ATP) with photosynthetic bioenergetics."
                ),
                keyConcepts = listOf("Central Dogma", "Nucleotide Complementarity", "Punnett Squares (3:1 Phenotype)", "Cellular Respiration ATP Yield"),
                vocabulary = listOf("DNA", "RNA", "Transcription", "Translation", "Codon", "Allele", "Homozygous", "Heterozygous", "Mitochondria", "ATP"),
                essentialQuestions = listOf(
                    "How does a sequence of just 4 nucleotide bases create the immense biodiversity of life on Earth?",
                    "How do cellular bioenergetic systems convert radiant solar energy into chemical ATP fuel?"
                ),
                socraticGuidingQuestions = listOf(
                    "If a DNA template strand has the sequence TAC GGC, what mRNA codon sequence is transcribed?",
                    "In a cross between two heterozygous parents (Bb × Bb), what is the probability of an offspring having the recessive phenotype?"
                ),
                commonMisconceptions = listOf(
                    "Thinking translation happens inside the nucleus rather than at the ribosomes in the cytoplasm.",
                    "Confusing mitosis (identical somatic cell division) with meiosis (gamete haploid reduction division)."
                ),
                practiceProblems = listOf(
                    OerPracticeProblem(
                        id = "p_hs_bio_1",
                        questionPrompt = "If adenine (A) makes up 30% of the bases in a double-stranded DNA sample, what percentage is cytosine (C)?",
                        options = listOf("20%", "30%", "40%", "70%"),
                        correctAnswer = "20%",
                        stepByStepExplanation = "According to Chargaff's rules: A = T = 30% (totaling 60%). The remaining 40% is split equally between G and C (40% / 2 = 20% Cytosine).",
                        socraticClue = "Remember Chargaff's rule: %A = %T and %G = %C. All four must sum to 100%."
                    )
                ),
                accessibilityAccommodations = listOf("Color-coded base pair animations", "Interactive Punnett square builder")
            ),

            OerCommonsCurriculumItem(
                id = "oer_hs_sci_newtonian_physics",
                subject = EducationalSubject.SCIENCE,
                gradeLevel = GradeLevel.HIGH_SCHOOL,
                gradeBand = OerGradeBand.HIGH_SCHOOL,
                collectionTitle = "OER Commons High School Physics: Mechanics & Energy",
                unitTitle = "Newtonian Mechanics, Kinematics & Conservation of Energy",
                standardCode = "NGSS.HS-PS2-1 / OER.HS.SCI.02",
                oerCommonsUrl = "$OER_COMMONS_BASE_URL/hs-physics-mechanics",
                summary = "Master Newton's three laws of motion (F = ma), vector kinematic equations, momentum, work, and conservation of mechanical energy.",
                learningObjectives = listOf(
                    "Analyze forces using free-body diagrams and calculate net force, mass, and acceleration (F_net = ma).",
                    "Apply kinematic equations to predict velocity, displacement, and acceleration in 1D and projectile motion.",
                    "Demonstrate conservation of mechanical energy (KE + PE = constant, where KE = ½mv² and PE = mgh)."
                ),
                keyConcepts = listOf("Inertia", "Net Force F=ma", "Action-Reaction", "Kinetic & Potential Energy", "Momentum Conservation"),
                vocabulary = listOf("Kinematics", "Acceleration", "Velocity", "Newton", "Joule", "Inertia", "Friction", "Conservation"),
                essentialQuestions = listOf(
                    "Why does an object in outer space continue moving at constant speed without any engine running?",
                    "How does total energy remain constant during a roller coaster's climb and descent?"
                ),
                socraticGuidingQuestions = listOf(
                    "If the net force on an object is zero, does that mean it must be at rest, or could it be moving at constant velocity?",
                    "When mass is doubled while keeping force constant, what happens to the acceleration?"
                ),
                commonMisconceptions = listOf(
                    "Believing that motion requires a continuous forward force (Aristotelian view vs. Newton's 1st Law)."
                ),
                practiceProblems = listOf(
                    OerPracticeProblem(
                        id = "p_hs_phys_1",
                        questionPrompt = "A 5 kg crate is pushed with a net force of 20 N. What is the acceleration of the crate?",
                        options = listOf("2 m/s²", "4 m/s²", "10 m/s²", "100 m/s²"),
                        correctAnswer = "4 m/s²",
                        stepByStepExplanation = "Using Newton's Second Law: a = F / m = 20 N / 5 kg = 4 m/s².",
                        socraticClue = "Use F = m × a. Rearrange to solve for acceleration: a = F / m."
                    )
                ),
                accessibilityAccommodations = listOf("Physics variable legend", "Interactive force vector sliders")
            ),

            // ==========================================
            // 4. SOCIAL STUDIES & CIVICS
            // ==========================================
            OerCommonsCurriculumItem(
                id = "oer_hs_soc_civics_government",
                subject = EducationalSubject.SOCIAL_STUDIES,
                gradeLevel = GradeLevel.HIGH_SCHOOL,
                gradeBand = OerGradeBand.HIGH_SCHOOL,
                collectionTitle = "OER Commons High School: US Government, Civics & Economics",
                unitTitle = "US Constitutional Principles, Separation of Powers & Judicial Review",
                standardCode = "NCSS.D2.Civ.2.9-12 / OER.HS.SOC.01",
                oerCommonsUrl = "$OER_COMMONS_BASE_URL/hs-civics-government",
                summary = "Examine Constitutional foundations, tripartite branches (Legislative, Executive, Judicial), checks and balances, federalism, and landmark Supreme Court precedents.",
                learningObjectives = listOf(
                    "Analyze Constitutional structures of government: Articles I, II, and III.",
                    "Explain the system of checks and balances and the power of Judicial Review established in Marbury v. Madison (1803).",
                    "Evaluate civil rights and constitutional amendments (1st, 4th, 5th, 14th Amendments) through landmark court cases.",
                    "Examine macroeconomic fiscal and monetary policies (GDP, Federal Reserve interest rates, inflation management)."
                ),
                keyConcepts = listOf("Separation of Powers", "Checks & Balances", "Federalism", "Judicial Review", "Bill of Rights", "Monetary Policy"),
                vocabulary = listOf("Constitution", "Legislative", "Executive", "Judicial", "Precedent", "Due Process", "Federalism", "Macroeconomics"),
                essentialQuestions = listOf(
                    "How does the separation of powers prevent tyranny and protect individual civil liberties?",
                    "How do Constitutional interpretations evolve over time to address modern societal challenges?"
                ),
                socraticGuidingQuestions = listOf(
                    "How can the President check the power of Congress? How can Congress check the President?",
                    "What fundamental principle did Marbury v. Madison establish regarding unconstitutional laws?"
                ),
                commonMisconceptions = listOf(
                    "Confusing the roles of the Federal Reserve (monetary policy) with the US Congress (fiscal tax/spend policy)."
                ),
                practiceProblems = listOf(
                    OerPracticeProblem(
                        id = "p_hs_civ_1",
                        questionPrompt = "Which landmark Supreme Court case established the principle of Judicial Review?",
                        options = listOf("Marbury v. Madison (1803)", "Brown v. Board of Education (1954)", "McCulloch v. Maryland (1819)", "Gibbons v. Ogden (1824)"),
                        correctAnswer = "Marbury v. Madison (1803)",
                        stepByStepExplanation = "In Marbury v. Madison (1803), Chief Justice John Marshall established the power of the Supreme Court to declare acts of Congress unconstitutional.",
                        socraticClue = "Think of the 1803 case where the Supreme Court asserted its power to strike down unconstitutional legislation."
                    )
                ),
                accessibilityAccommodations = listOf("Government branch branch chart diagrams", "Simplified legal precedent summaries")
            ),

            // ==========================================
            // 5. LIFE SKILLS & SEL
            // ==========================================
            OerCommonsCurriculumItem(
                id = "oer_hs_sel_personal_finance",
                subject = EducationalSubject.LIFE_SKILLS,
                gradeLevel = GradeLevel.HIGH_SCHOOL,
                gradeBand = OerGradeBand.HIGH_SCHOOL,
                collectionTitle = "OER Commons High School: Financial Literacy & Executive Functioning",
                unitTitle = "Personal Finance, Budgeting, Compound Interest & Executive Pacing",
                standardCode = "CASEL.SEL.RESPONSIBLE_DECISION / OER.HS.SEL.01",
                oerCommonsUrl = "$OER_COMMONS_BASE_URL/hs-financial-literacy",
                summary = "Equip students with life-ready personal finance skills: the 50/30/20 budget, compound interest formulas, credit score mastery, and neurodivergent executive pacing.",
                learningObjectives = listOf(
                    "Construct a balanced monthly budget utilizing the 50/30/20 framework (50% Needs, 30% Wants, 20% Savings/Debt).",
                    "Calculate compound interest growth (A = P(1 + r/n)^(nt)) and estimate doubling time using the Rule of 72.",
                    "Understand credit scores (300-850), credit utilization ratios (<30%), and the risks of high-interest revolving debt.",
                    "Implement neurodivergent executive strategies: task chunking, time-blocking, and sensory reset breaks."
                ),
                keyConcepts = listOf("50/30/20 Budgeting", "Compound Interest", "Rule of 72", "Credit Utilization", "Executive Task Chunking"),
                vocabulary = listOf("Budget", "Principal", "Interest Rate", "Compound Growth", "Credit Score", "Asset", "Liability", "Executive Function"),
                essentialQuestions = listOf(
                    "How does starting to save and invest early leverage exponential compound growth over time?",
                    "How can self-awareness of our sensory energy and focus cycles help us organize demanding study schedules?"
                ),
                socraticGuidingQuestions = listOf(
                    "If an investment yields an 8% annual return, about how many years will it take to double using the Rule of 72?",
                    "What is the difference between a 'Need' (e.g. rent, groceries) and a 'Want' (e.g. dining out, video games) in a 50/30/20 budget?"
                ),
                commonMisconceptions = listOf(
                    "Thinking simple interest and compound interest grow at the same linear rate.",
                    "Believing carrying a credit card balance from month to month improves your credit score (it only generates interest fees!)."
                ),
                practiceProblems = listOf(
                    OerPracticeProblem(
                        id = "p_hs_fin_1",
                        questionPrompt = "Using the Rule of 72, approximately how many years will it take for an investment to double at a 6% annual return?",
                        options = listOf("6 years", "12 years", "18 years", "72 years"),
                        correctAnswer = "12 years",
                        stepByStepExplanation = "Rule of 72: Divide 72 by the annual interest rate (72 / 6 = 12 years).",
                        socraticClue = "Divide 72 by the rate of return (6)."
                    )
                ),
                accessibilityAccommodations = listOf("Interactive budget sliders", "Sensory break reminders", "Bite-sized step calculators")
            )
        )
    }

    fun getUnitsForGradeAndSubject(grade: GradeLevel, subject: EducationalSubject): List<OerCommonsCurriculumItem> {
        val all = getAllPreinstalledCurriculum()
        val exact = all.filter { it.gradeLevel == grade && it.subject == subject }
        if (exact.isNotEmpty()) return exact

        val band = when (grade) {
            GradeLevel.PRE_K, GradeLevel.KINDERGARTEN -> OerGradeBand.EARLY_CHILDHOOD
            GradeLevel.GRADE_1, GradeLevel.GRADE_2, GradeLevel.GRADE_3, GradeLevel.GRADE_4, GradeLevel.GRADE_5 -> OerGradeBand.ELEMENTARY
            GradeLevel.GRADE_6, GradeLevel.GRADE_7, GradeLevel.GRADE_8 -> OerGradeBand.MIDDLE_SCHOOL
            GradeLevel.HIGH_SCHOOL -> OerGradeBand.HIGH_SCHOOL
        }
        val bandMatches = all.filter { it.gradeBand == band && it.subject == subject }
        if (bandMatches.isNotEmpty()) return bandMatches

        return all.filter { it.subject == subject }
    }
}
