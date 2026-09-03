package com.example.data.curriculum.oer

import com.example.data.model.EducationalSubject

/**
 * Preinstalled Multimedia Video and Audio Curriculum Catalog for OER Commons Curated Collections.
 * Provides high quality video lessons, audio lectures, read-aloud podcasts, synchronized transcripts,
 * and in-video Socratic checkpoints for every course across K-12 subjects.
 */
object PreinstalledOerMediaCatalog {

    fun getMediaForUnit(unitId: String, subject: EducationalSubject): List<OerMediaResource> {
        val list = mutableListOf<OerMediaResource>()

        when (unitId) {
            "oer_k_math_counting_clusters" -> {
                list.add(
                    OerMediaResource(
                        id = "media_k_math_vid",
                        title = "Counting Clusters & 10-Frame Visualizer",
                        mediaType = OerMediaType.VIDEO_LESSON,
                        durationSeconds = 120,
                        description = "Watch how five dots on the top row and two dots on the bottom row combine to make seven without recount.",
                        creatorOrSource = "OER Commons Early Numeracy Initiative",
                        visualSceneKey = "DEFAULT",
                        transcript = listOf(
                            OerTranscriptLine(0, "Teacher Leo", "Welcome to the ten-frame garden! Let's count our friendly ladybugs."),
                            OerTranscriptLine(15, "Teacher Leo", "Look at the top row. It has 1, 2, 3, 4, 5 spots. When the top row is full, you always know it is five!"),
                            OerTranscriptLine(45, "Teacher Leo", "Now we add 2 more ladybugs in the bottom row. 5 plus 2 makes seven!"),
                            OerTranscriptLine(75, "Teacher Leo", "Notice how we didn't have to count from one again. We anchored to five and counted on."),
                            OerTranscriptLine(105, "Teacher Leo", "Great job! Keep practicing your quick ten-frame recognition.")
                        ),
                        checkpoints = listOf(
                            OerPlaybackCheckpoint(
                                timestampSeconds = 50,
                                title = "Ten-Frame Checkpoint",
                                questionPrompt = "If the top row of a 10-frame is full (5) and there is 1 in the bottom row, how many are there?",
                                options = listOf("5", "6", "7", "10"),
                                correctAnswer = "6",
                                explanation = "5 in the top row plus 1 more equals 6!"
                            )
                        ),
                        keyTakeaways = listOf(
                            "Top row of a ten-frame always holds 5 units.",
                            "Subitizing means seeing a small quantity instantly without counting 1 by 1.",
                            "Anchor to 5 to quickly count larger quantities up to 10."
                        )
                    )
                )
                list.add(
                    OerMediaResource(
                        id = "media_k_math_audio",
                        title = "Subitizing Songs & Rhymes Podcast",
                        mediaType = OerMediaType.AUDIO_LECTURE,
                        durationSeconds = 90,
                        description = "Catchy rhythmic audio podcast guiding little learners through quick visual counting games.",
                        creatorOrSource = "OER Commons Early Audio Stories",
                        transcript = listOf(
                            OerTranscriptLine(0, "Audio Buddy", "Clap your hands and count with me! 1, 2, 3, 4, 5!"),
                            OerTranscriptLine(30, "Audio Buddy", "Close your eyes, open them wide! Look at the three stars smiling in the sky!"),
                            OerTranscriptLine(60, "Audio Buddy", "Three stars in a row, shining nice and bright!")
                        ),
                        keyTakeaways = listOf(
                            "Numbers represent exact quantities in rhythm.",
                            "Audio rhyme builds number recall memory."
                        )
                    )
                )
            }

            "oer_gr1_math_addition_bonds" -> {
                list.add(
                    OerMediaResource(
                        id = "media_gr1_math_vid",
                        title = "Making Ten & Number Bonds Video",
                        mediaType = OerMediaType.VIDEO_LESSON,
                        durationSeconds = 140,
                        description = "Decomposing numbers into friendly tens to make mental addition easy and fast.",
                        creatorOrSource = "OER Commons Elementary Math Project",
                        visualSceneKey = "DEFAULT",
                        transcript = listOf(
                            OerTranscriptLine(0, "Instructor Maya", "Today we unlock the superpower of the number 10!"),
                            OerTranscriptLine(20, "Instructor Maya", "When we add 8 + 5, think: What does 8 need to make 10? It needs 2!"),
                            OerTranscriptLine(60, "Instructor Maya", "So we break 5 apart into 2 and 3. 8 + 2 makes 10, plus 3 makes 13!"),
                            OerTranscriptLine(110, "Instructor Maya", "Number bonds let you solve tricky addition problems in your head.")
                        ),
                        checkpoints = listOf(
                            OerPlaybackCheckpoint(
                                timestampSeconds = 65,
                                title = "Make a Ten Checkpoint",
                                questionPrompt = "How would you decompose 6 to add with 9 (9 + 6)?",
                                options = listOf("Split 6 into 1 and 5", "Split 6 into 3 and 3", "Split 6 into 2 and 4", "Split 6 into 0 and 6"),
                                correctAnswer = "Split 6 into 1 and 5",
                                explanation = "9 needs 1 to make 10! So 9 + 1 = 10, plus the remaining 5 gives 15."
                            )
                        ),
                        keyTakeaways = listOf(
                            "Number bonds show how numbers split into parts and whole.",
                            "Making a ten is the fastest mental addition strategy.",
                            "8 + 5 = (8 + 2) + 3 = 10 + 3 = 13."
                        )
                    )
                )
            }

            "oer_gr8_sci_plate_tectonics" -> {
                list.add(
                    OerMediaResource(
                        id = "media_gr8_sci_vid",
                        title = "Plate Tectonics & Mantle Convection Simulation",
                        mediaType = OerMediaType.VIDEO_LESSON,
                        durationSeconds = 180,
                        description = "Deep dive into Earth's lithospheric plates, mantle convection currents, subduction zones, and rift valleys.",
                        creatorOrSource = "OER Earth Science Open Initiative",
                        visualSceneKey = "PLATE_TECTONICS",
                        transcript = listOf(
                            OerTranscriptLine(0, "Dr. Alvarez", "Welcome beneath Earth's crust! Today we investigate the heat engine driving our continents."),
                            OerTranscriptLine(25, "Dr. Alvarez", "Deep within the asthenosphere, intense radioactive decay creates convection currents."),
                            OerTranscriptLine(60, "Dr. Alvarez", "Hot magma rises at mid-ocean ridges (divergent boundaries), creating brand new seafloor."),
                            OerTranscriptLine(95, "Dr. Alvarez", "Meanwhile, colder, denser oceanic crust sinks beneath continental plates at subduction zones."),
                            OerTranscriptLine(140, "Dr. Alvarez", "This dynamic conveyor belt causes earthquakes, volcanic arcs, and mountain building!")
                        ),
                        checkpoints = listOf(
                            OerPlaybackCheckpoint(
                                timestampSeconds = 100,
                                title = "Tectonic Boundary Checkpoint",
                                questionPrompt = "What type of boundary occurs where two plates slide past each other horizontally?",
                                options = listOf("Convergent Boundary", "Divergent Boundary", "Transform Fault", "Subduction Zone"),
                                correctAnswer = "Transform Fault",
                                explanation = "Transform boundaries (like the San Andreas Fault) slide past each other without creating or destroying crust."
                            )
                        ),
                        keyTakeaways = listOf(
                            "Mantle convection currents drive tectonic plate motion.",
                            "Convergent: colliding plates; Divergent: separating plates; Transform: sliding plates.",
                            "Subduction recycles old oceanic crust back into the mantle."
                        )
                    )
                )
                list.add(
                    OerMediaResource(
                        id = "media_gr8_sci_audio",
                        title = "Earthquakes & Ring of Fire Audio Lecture",
                        mediaType = OerMediaType.AUDIO_LECTURE,
                        durationSeconds = 150,
                        description = "Audio documentary exploring the Pacific Ring of Fire, seismic wave detection, and volcanic eruption forecasting.",
                        creatorOrSource = "OER Commons Geosciences Audio",
                        transcript = listOf(
                            OerTranscriptLine(0, "Host Sarah", "Beneath the Pacific Ocean lies the most seismically active zone on our planet: The Ring of Fire."),
                            OerTranscriptLine(45, "Host Sarah", "Seismographs measure P-waves (primary compressional waves) and S-waves (secondary shear waves)."),
                            OerTranscriptLine(90, "Host Sarah", "By measuring the arrival time between P and S waves at 3 stations, seismologists triangulate the exact earthquake epicenter.")
                        ),
                        keyTakeaways = listOf(
                            "Triangulation requires data from at least 3 seismic monitoring stations.",
                            "P-waves travel fastest and move through solids, liquids, and gases."
                        )
                    )
                )
            }

            "oer_hs_math_quadratic_functions" -> {
                list.add(
                    OerMediaResource(
                        id = "media_hs_math_vid",
                        title = "Quadratic Graphs, Vertex & Parabolas",
                        mediaType = OerMediaType.VIDEO_LESSON,
                        durationSeconds = 200,
                        description = "Visual exploration of quadratic parabolic trajectories, vertex coordinates (-b/2a), and real-world projectile physics.",
                        creatorOrSource = "OER OpenStax Math & Khan Open Curriculum",
                        visualSceneKey = "QUADRATIC_PARABOLA",
                        transcript = listOf(
                            OerTranscriptLine(0, "Prof. Jenkins", "Welcome to High School Algebra! Today we explore parabolic curves."),
                            OerTranscriptLine(30, "Prof. Jenkins", "Standard form is f(x) = ax^2 + bx + c. The leading coefficient 'a' dictates whether the parabola opens up or down."),
                            OerTranscriptLine(75, "Prof. Jenkins", "The axis of symmetry runs through x = -b / (2a). This is where the maximum or minimum vertex is located!"),
                            OerTranscriptLine(130, "Prof. Jenkins", "From basketball trajectories to satellite dishes, quadratics model real-world parabolic motion.")
                        ),
                        checkpoints = listOf(
                            OerPlaybackCheckpoint(
                                timestampSeconds = 80,
                                title = "Vertex Formula Checkpoint",
                                questionPrompt = "For f(x) = x^2 - 6x + 8, what is the x-coordinate of the vertex?",
                                options = listOf("x = 3", "x = -3", "x = 6", "x = -6"),
                                correctAnswer = "x = 3",
                                explanation = "Using x = -b / (2a): -(-6) / (2 * 1) = 6 / 2 = 3."
                            )
                        ),
                        keyTakeaways = listOf(
                            "Vertex x-coordinate is given by x = -b / (2a).",
                            "If a > 0, parabola opens upward with a minimum; if a < 0, it opens downward with a maximum.",
                            "Discriminant (b^2 - 4ac) reveals the number of real roots (x-intercepts)."
                        )
                    )
                )
            }

            "oer_hs_sci_chemical_bonding" -> {
                list.add(
                    OerMediaResource(
                        id = "media_hs_sci_vid",
                        title = "Ionic, Covalent & Metallic Bonding Video",
                        mediaType = OerMediaType.VIDEO_LESSON,
                        durationSeconds = 190,
                        description = "Interactive visualization of electronegativity differences, valence octet rules, and Lewis electron dot sharing.",
                        creatorOrSource = "OER Commons Physical Chemistry Lab",
                        visualSceneKey = "CHEMISTRY_BONDING",
                        transcript = listOf(
                            OerTranscriptLine(0, "Dr. Vance", "Why do atoms bond? They bond to achieve stable full valence electron shells!"),
                            OerTranscriptLine(35, "Dr. Vance", "In ionic bonding (large electronegativity gap), metals transfer electrons to nonmetals, forming crystalline lattices."),
                            OerTranscriptLine(80, "Dr. Vance", "In covalent bonding (small electronegativity gap), nonmetals share electron pairs between orbitals."),
                            OerTranscriptLine(130, "Dr. Vance", "Polar covalent bonds arise when electrons are shared unequally, giving molecules partial charges (like water!).")
                        ),
                        checkpoints = listOf(
                            OerPlaybackCheckpoint(
                                timestampSeconds = 85,
                                title = "Bonding Type Checkpoint",
                                questionPrompt = "What type of bond forms between Sodium (Na, metal) and Chlorine (Cl, nonmetal)?",
                                options = listOf("Ionic Bond", "Nonpolar Covalent Bond", "Polar Covalent Bond", "Hydrogen Bond"),
                                correctAnswer = "Ionic Bond",
                                explanation = "Sodium transfers 1 valence electron to Chlorine, creating Na+ and Cl- ions held by electrostatic attraction."
                            )
                        ),
                        keyTakeaways = listOf(
                            "Ionic: electron transfer between metal & nonmetal.",
                            "Covalent: electron sharing between nonmetals.",
                            "Electronegativity difference determines bond character."
                        )
                    )
                )
            }

            else -> {
                // Generate standard subject-grounded video & audio lessons
                list.add(
                    OerMediaResource(
                        id = "media_${unitId}_vid",
                        title = "OER Concept Video Lesson",
                        mediaType = OerMediaType.VIDEO_LESSON,
                        durationSeconds = 150,
                        description = "Interactive OER Commons curated video lesson with animated visual diagrams and step-by-step concept walkthroughs.",
                        creatorOrSource = "OER Commons Curated Video Lab",
                        visualSceneKey = "DEFAULT",
                        transcript = listOf(
                            OerTranscriptLine(0, "Instructor", "Welcome to this OER Commons Curated Lesson! Let's explore the core concepts step-by-step."),
                            OerTranscriptLine(30, "Instructor", "Notice how breaking down the standard into smaller pieces makes mastery intuitive and clear."),
                            OerTranscriptLine(75, "Instructor", "Let's connect this concept with a real-world example you see in everyday life."),
                            OerTranscriptLine(120, "Instructor", "Excellent job! You are ready to apply this concept in your practice problems.")
                        ),
                        checkpoints = listOf(
                            OerPlaybackCheckpoint(
                                timestampSeconds = 80,
                                title = "Quick Concept Check",
                                questionPrompt = "Why is grounding knowledge in real-world examples helpful?",
                                options = listOf("It makes concepts easier to remember and apply", "It has no purpose", "It replaces practice", "It only works for math"),
                                correctAnswer = "It makes concepts easier to remember and apply",
                                explanation = "Real-world connections create neural pathways that reinforce long-term understanding!"
                            )
                        ),
                        keyTakeaways = listOf(
                            "Master key definitions and core relationships.",
                            "Apply step-by-step problem solving strategies.",
                            "Review Socratic feedback to clarify any misconceptions."
                        )
                    )
                )
                list.add(
                    OerMediaResource(
                        id = "media_${unitId}_audio",
                        title = "OER Audio Lecture & Socratic Guide",
                        mediaType = OerMediaType.AUDIO_LECTURE,
                        durationSeconds = 120,
                        description = "Auditory curriculum guide with clear vocal explanations, acoustic pacing, and synchronized read-along transcript.",
                        creatorOrSource = "OER Commons Auditory Curriculum Series",
                        transcript = listOf(
                            OerTranscriptLine(0, "Auditory Narrator", "Hello learner! Relax, take a deep breath, and listen to today's core learning summary."),
                            OerTranscriptLine(35, "Auditory Narrator", "As we explore this subject, consider how each key concept fits into the bigger picture."),
                            OerTranscriptLine(80, "Auditory Narrator", "Great listening! Take these key takeaways with you into your practice session.")
                        ),
                        keyTakeaways = listOf(
                            "Auditory reinforcement enhances conceptual retention.",
                            "Follow along with synchronized closed captions."
                        )
                    )
                )
            }
        }

        return list
    }
}
