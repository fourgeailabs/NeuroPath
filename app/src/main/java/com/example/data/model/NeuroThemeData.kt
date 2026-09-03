package com.example.data.model

import com.example.data.local.entity.ChildProfileEntity

enum class ThemeRotationSchedule(
    val id: String,
    val title: String,
    val intervalDays: Int,
    val description: String,
    val icon: String
) {
    MANUAL("MANUAL", "Permanent (Manual Change Only)", 0, "Keep selected theme permanently until you choose to change it", "🔒"),
    DAILY("DAILY", "Daily Spark (Every 1 Day)", 1, "Wake up to a fresh new theme world matched to your profile every day", "🌟"),
    EVERY_3_DAYS("EVERY_3_DAYS", "Every 3 Days", 3, "Rotate to a new topic realm every 3 days for dynamic variety", "⚡"),
    WEEKLY("WEEKLY", "Weekly Adventure (Every 7 Days)", 7, "A new themed curriculum voyage every week", "📅"),
    BIWEEKLY("BIWEEKLY", "Bi-Weekly Unit (Every 14 Days)", 14, "Two-week deep dive across subject challenges", "🧭"),
    MONTHLY("MONTHLY", "Monthly Expedition (Every 30 Days)", 30, "Monthly thematic unit study and world exploration", "🗓️");

    companion object {
        fun fromId(id: String?): ThemeRotationSchedule {
            return values().find { it.id.equals(id, ignoreCase = true) } ?: MANUAL
        }
    }
}

enum class NeuroThemeCategory(
    val id: String,
    val title: String,
    val emoji: String,
    val description: String
) {
    HISTORY_CIVILIZATION("HISTORY", "Ancient History & Civilizations", "🏛️", "Pharaohs, empires, dynasties & historical archaeology"),
    AI_ROBOTICS_TECH("TECH", "Robotics, AI & Modern Tech", "🤖", "Mechatronics, artificial intelligence, quantum & code"),
    MYTH_FANTASY("MYTH", "Mythological Creatures & Folklore", "🐉", "Dragons, phoenixes, folklore spirits & enchanted realms"),
    CULINARY_FOOD_SCIENCE("CULINARY", "Culinary Adventures & Kitchen Science", "🍳", "Cooking chemistry, master chef bistros & gastronomy"),
    MUSIC_AUDIO_ARTS("MUSIC", "Musical Journeys & Sound Design", "🎷", "Synthesizers, orchestras, beats, rhythm & acoustics"),
    SPORTS_KINETIC("SPORTS", "Sports Superstars & Kinetic Motion", "🏎️", "Racing physics, athletics, ninja agility & movement"),
    NATURE_ECOLOGY("NATURE", "Environmental Explorers & Ecology", "🌿", "Rainforests, ocean reefs, glaciers & clean energy"),
    ART_DESIGN("ART", "Artistic Expression & Visual Design", "🎨", "Anime manga, architecture, sculpting & animation"),
    TRANSPORT_ENGINEERING("TRANSPORT", "Transportation Tycoons & Transit", "🚂", "Locomotives, aviation, submarines & hyperloop"),
    MYSTERY_SPY("SPY", "Spy Academy & Detective Agency", "🕵️‍♂️", "Secret agents, forensic sleuths, ciphers & escape rooms");

    companion object {
        fun fromId(id: String?): NeuroThemeCategory? {
            return values().find { it.id.equals(id, ignoreCase = true) }
        }
    }
}

data class NeuroThemeData(
    val id: String,
    val title: String,
    val category: NeuroThemeCategory,
    val emoji: String,
    val buddyName: String,
    val buddyRole: String,
    val greeting: String,
    val targetAgeTiers: List<AgeGroupTier> = listOf(AgeGroupTier.ELEMENTARY, AgeGroupTier.MIDDLE_SCHOOL, AgeGroupTier.HIGH_SCHOOL),
    val bestForDiagnoses: List<String>,
    val bestForStrengths: List<String>,
    val remediesStruggles: List<String>,
    val mathIntegration: String,
    val readingIntegration: String,
    val scienceIntegration: String,
    val socialStudiesIntegration: String,
    val interactiveIdea: String,
    val primaryHex: Long,
    val secondaryHex: Long,
    val surfaceHex: Long,
    val cardHex: Long
)

object NeuroThemeCatalog {

    val ALL_100_THEMES: List<NeuroThemeData> = listOf(
        // ==========================================
        // 1. ANCIENT CIVILIZATIONS & WORLD HISTORY (1-10)
        // ==========================================
        NeuroThemeData(
            id = "ancient_egypt",
            title = "Ancient Egypt: Pharaohs & Pyramids",
            category = NeuroThemeCategory.HISTORY_CIVILIZATION,
            emoji = "🏛️",
            buddyName = "Scribe Thoth",
            buddyRole = "Royal Papyrus Archivist",
            greeting = "By the wisdom of the Nile, let's unlock ancient secrets!",
            bestForDiagnoses = listOf("ADHD", "AUTISM_ASD", "DYSLEXIA", "GIFTED_2E"),
            bestForStrengths = listOf("Visual Pattern Recognition", "Spatial & 3D Reasoning", "Deep Passion & Hyperfocus"),
            remediesStruggles = listOf("Focus & Staying On Task", "Reading Fluency & Phonics"),
            mathIntegration = "Calculate pyramid slope geometry, volume of stone blocks, and Nile flood calendar cycles.",
            readingIntegration = "Decode phonetically annotated hieroglyphic papyrus scrolls and decipher royal cartouches.",
            scienceIntegration = "Analyze solar astronomy, salt desiccation in mummification chemistry, and delta silt soil biology.",
            socialStudiesIntegration = "Explore Nile trade routes, dynasty governance, and daily life of artisan builders.",
            interactiveIdea = "Interactive balance scale matching stone weights to align pyramid foundations with haptic snap feedback.",
            primaryHex = 0xFFC59B27,
            secondaryHex = 0xFF8C6200,
            surfaceHex = 0xFFFFFDF5,
            cardHex = 0xFFF9F0D1
        ),
        NeuroThemeData(
            id = "ancient_greece",
            title = "Greek Agora & Mythic Scholars",
            category = NeuroThemeCategory.HISTORY_CIVILIZATION,
            emoji = "🏺",
            buddyName = "Athena's Owl",
            buddyRole = "Philosopher Academy Guide",
            greeting = "Welcome, seeker of truth! Let reason and curiosity illuminate the path!",
            bestForDiagnoses = listOf("ADHD", "DYSLEXIA", "GIFTED_2E", "ANXIETY_STRESS"),
            bestForStrengths = listOf("Logic, Puzzles & Systems", "Creative Storytelling & Art"),
            remediesStruggles = listOf("Multi-Step Instructions", "Emotional Frustration When Stuck"),
            mathIntegration = "Discover the Pythagorean theorem, geometric proofs, and Archimedean water levers.",
            readingIntegration = "Analyze Homeric epic myths, Socratic dialogues, and dramatic theater masks.",
            scienceIntegration = "Study early celestial astronomy, simple machines, and botanical classification by Aristotle.",
            socialStudiesIntegration = "Compare Athenian direct democracy with Spartan civic structures and Mediterranean maritime trade.",
            interactiveIdea = "Geometric sandbox connecting star constellations to build temple columns and water aqueducts.",
            primaryHex = 0xFF1D3557,
            secondaryHex = 0xFF457B9D,
            surfaceHex = 0xFFF1FAEE,
            cardHex = 0xFFA8DADC
        ),
        NeuroThemeData(
            id = "ancient_rome",
            title = "Roman Engineering & Aqueducts",
            category = NeuroThemeCategory.HISTORY_CIVILIZATION,
            emoji = "🏟️",
            buddyName = "Centurion Titus",
            buddyRole = "Imperial Master Architect",
            greeting = "All roads lead to mastery! Let's construct unbreakable knowledge!",
            bestForDiagnoses = listOf("AUTISM_ASD", "DYSCALCULIA", "EXECUTIVE_DYSFUNCTION"),
            bestForStrengths = listOf("Spatial & 3D Reasoning", "Logic, Puzzles & Systems"),
            remediesStruggles = listOf("Math Concepts & Working Memory", "Task Initiation & Procrastination"),
            mathIntegration = "Calculate arch keystone angles, Roman numeral conversions, and road gradient ratios.",
            readingIntegration = "Read civic proclamations, military logistics dispatches, and Roman mythology tales.",
            scienceIntegration = "Examine volcanic pozzolana hydraulic concrete chemistry and gravity-fed water siphons.",
            socialStudiesIntegration = "Study the Roman Senate, Pax Romana trade networks, and urban engineering feats.",
            interactiveIdea = "Drag-and-drop arch builder placing the keystone to distribute pressure and support aqueduct water flow.",
            primaryHex = 0xFF8B0000,
            secondaryHex = 0xFFB22222,
            surfaceHex = 0xFFFFF8F0,
            cardHex = 0xFFFFE4C4
        ),
        NeuroThemeData(
            id = "maya_astronomy",
            title = "Maya & Inca Sky Observers",
            category = NeuroThemeCategory.HISTORY_CIVILIZATION,
            emoji = "🌴",
            buddyName = "Astronomer K'in",
            buddyRole = "Solar Calendar Guardian",
            greeting = "The stars align for your learning journey! Welcome to the sacred canopy!",
            bestForDiagnoses = listOf("ADHD", "AUTISM_ASD", "DYSCALCULIA", "GIFTED_2E"),
            bestForStrengths = listOf("Visual Pattern Recognition", "Logic, Puzzles & Systems"),
            remediesStruggles = listOf("Math Concepts & Working Memory", "Time Blindness & Transitions"),
            mathIntegration = "Master the base-20 vigesimal number system with visual dots, bars, and zero shells.",
            readingIntegration = "Interpret Maya glyph codices, oral folklore legends of the Popol Vuh, and quipu knots.",
            scienceIntegration = "Track Venus planetary orbital mathematics, agricultural rainforest terracing, and obsidian optics.",
            socialStudiesIntegration = "Explore Mesoamerican city-states, trade in jade and cacao, and Andean suspension bridges.",
            interactiveIdea = "Interactive quipu knot simulator and Maya numeral calculator with tactile sliding beads.",
            primaryHex = 0xFF2D6A4F,
            secondaryHex = 0xFF40916C,
            surfaceHex = 0xFFF2F9F4,
            cardHex = 0xFFD8F3DC
        ),
        NeuroThemeData(
            id = "silk_road_merchants",
            title = "Silk Road Caravans & Spice Traders",
            category = NeuroThemeCategory.HISTORY_CIVILIZATION,
            emoji = "🐪",
            buddyName = "Merchant Safir",
            buddyRole = "Oasis Caravanserai Guide",
            greeting = "Unfurl the desert maps! Today we exchange riches of knowledge and culture!",
            bestForDiagnoses = listOf("ADHD", "DYSLEXIA", "SENSORY_PROCESSING"),
            bestForStrengths = listOf("Empathy & Emotional Insight", "Creative Storytelling & Art"),
            remediesStruggles = listOf("Focus & Staying On Task", "Emotional Frustration When Stuck"),
            mathIntegration = "Calculate merchant currency exchange rates, goods weighing with balance scales, and camel cargo capacity.",
            readingIntegration = "Analyze traveler journals from Marco Polo and Ibn Battuta with rich sensory imagery.",
            scienceIntegration = "Investigate silk worm metamorphosis biology, paper-making chemistry, and magnetic lodestone compasses.",
            socialStudiesIntegration = "Understand cultural diffusion, global trade hubs, and religious exchange across Asia, Africa, and Europe.",
            interactiveIdea = "Virtual bazaar trade scale balancing spices, silk bolts, and bronze coins with sound chimes.",
            primaryHex = 0xFF9E2A2B,
            secondaryHex = 0xFFE09F3E,
            surfaceHex = 0xFFFFFDF5,
            cardHex = 0xFFFFE8D6
        ),
        NeuroThemeData(
            id = "viking_voyagers",
            title = "Viking Voyagers & Sunstone Navigators",
            category = NeuroThemeCategory.HISTORY_CIVILIZATION,
            emoji = "⛵",
            buddyName = "Captain Astrid",
            buddyRole = "Longship Wayfinder",
            greeting = "Hoist the striped sails! The northern winds guide us toward grand discoveries!",
            bestForDiagnoses = listOf("ADHD", "DYSLEXIA", "SENSORY_PROCESSING"),
            bestForStrengths = listOf("Spatial & 3D Reasoning", "Deep Passion & Hyperfocus"),
            remediesStruggles = listOf("Reading Fluency & Phonics", "Time Blindness & Transitions"),
            mathIntegration = "Calculate nautical knot speeds, longship displacement, and celestial navigation angles.",
            readingIntegration = "Read Norse sagas, poetic Eddas, and phonetic elder Futhark runestones with syllable highlights.",
            scienceIntegration = "Examine polarizing optical properties of Iceland spar sunstones and North Atlantic ocean currents.",
            socialStudiesIntegration = "Explore Norse settlement across Scandinavia, Iceland, Greenland, and Vinland, and the Thing assemblies.",
            interactiveIdea = "Sunstone navigation compass aligning polarized light rings to steer longships through ocean fog.",
            primaryHex = 0xFF1B4965,
            secondaryHex = 0xFF62B6CB,
            surfaceHex = 0xFFF0F8FF,
            cardHex = 0xFFBEE9E8
        ),
        NeuroThemeData(
            id = "medieval_castles",
            title = "Medieval Castles & Alchemy Keeps",
            category = NeuroThemeCategory.HISTORY_CIVILIZATION,
            emoji = "🏰",
            buddyName = "Master Alchemist Alden",
            buddyRole = "Keep Scholar & Herbalist",
            greeting = "Welcome behind castle ramparts! Let us forge wisdom stronger than dragon-steel!",
            bestForDiagnoses = listOf("AUTISM_ASD", "DYSCALCULIA", "GIFTED_2E"),
            bestForStrengths = listOf("Logic, Puzzles & Systems", "Creative Storytelling & Art"),
            remediesStruggles = listOf("Task Initiation & Procrastination", "Multi-Step Instructions"),
            mathIntegration = "Calculate catapult trajectory parabolas, castle wall perimeters, and drawbridge pulley gear ratios.",
            readingIntegration = "Decode illuminated medieval manuscripts, chivalric codes of honor, and town guild charters.",
            scienceIntegration = "Explore iron metallurgy, herbal plant remedies, medieval water wheels, and simple chemistry.",
            socialStudiesIntegration = "Examine feudal manor hierarchy, craft guilds, medieval agrarian cycles, and siege defense strategies.",
            interactiveIdea = "Physics siege catapult and drawbridge counterweight calibration simulator with real-time arc physics.",
            primaryHex = 0xFF6B4C9A,
            secondaryHex = 0xFFC9A227,
            surfaceHex = 0xFFFBF8FF,
            cardHex = 0xFFEFE8FF
        ),
        NeuroThemeData(
            id = "samurai_zen",
            title = "Feudal Samurai & Zen Garden Sages",
            category = NeuroThemeCategory.HISTORY_CIVILIZATION,
            emoji = "🌸",
            buddyName = "Master Kenshin",
            buddyRole = "Zen Calligrapher & Martial Sage",
            greeting = "With a calm mind and focused breath, every challenge becomes clear.",
            bestForDiagnoses = listOf("ADHD", "ANXIETY_STRESS", "SENSORY_PROCESSING"),
            bestForStrengths = listOf("Empathy & Emotional Insight", "Visual Pattern Recognition"),
            remediesStruggles = listOf("Emotional Frustration When Stuck", "Sensory Overload & Noise"),
            mathIntegration = "Calculate tatami mat room modular geometry, origami folding angles, and abacus soroban sums.",
            readingIntegration = "Compose haiku poetry with 5-7-5 syllabic meters and read classic warrior bushido codes.",
            scienceIntegration = "Analyze folding steel metallurgy, hydraulic bamboo shishi-odoshi fountain physics, and garden botany.",
            socialStudiesIntegration = "Learn about the Edo period, shogunate governance, samurai ethics, and traditional tea ceremony culture.",
            interactiveIdea = "Zen sand garden raking canvas with peaceful soothing ambient sounds and geometry puzzle stones.",
            primaryHex = 0xFF4A5568,
            secondaryHex = 0xFFE53E3E,
            surfaceHex = 0xFFF7FAFC,
            cardHex = 0xFFEDF2F7
        ),
        NeuroThemeData(
            id = "indus_valley",
            title = "Indus Valley Ancient Town Planners",
            category = NeuroThemeCategory.HISTORY_CIVILIZATION,
            emoji = "🧱",
            buddyName = "Planner Maya",
            buddyRole = "Mohenjo-Daro Chief Architect",
            greeting = "Precision and harmony build great communities! Let's lay the bricks of learning!",
            bestForDiagnoses = listOf("AUTISM_ASD", "DYSCALCULIA", "EXECUTIVE_DYSFUNCTION"),
            bestForStrengths = listOf("Spatial & 3D Reasoning", "Visual Pattern Recognition"),
            remediesStruggles = listOf("Math Concepts & Working Memory", "Multi-Step Instructions"),
            mathIntegration = "Measure uniform baked brick ratios (4:2:1), grid city coordinate street layouts, and water capacity.",
            readingIntegration = "Analyze mysterious Indus seal pictographs, archaeological field notes, and trade stamp emblems.",
            scienceIntegration = "Study advanced underground terracotta drainage hydraulics, gravity sewer flows, and bronze casting.",
            socialStudiesIntegration = "Investigate egalitarian bronze age urban planning, public grain silos, and Harappan merchant links.",
            interactiveIdea = "Grid-based urban drainage builder routing water channels through ancient brick street networks.",
            primaryHex = 0xFFB3541E,
            secondaryHex = 0xFFE28743,
            surfaceHex = 0xFFFFF8F2,
            cardHex = 0xFFFFEBD9
        ),
        NeuroThemeData(
            id = "ancient_china_inventions",
            title = "Ancient China: Four Great Inventions",
            category = NeuroThemeCategory.HISTORY_CIVILIZATION,
            emoji = "🏮",
            buddyName = "Scholar Shen",
            buddyRole = "Han Academy Inventor",
            greeting = "From paper to compass, great inventions start with a single observant thought!",
            bestForDiagnoses = listOf("ADHD", "GIFTED_2E", "DYSLEXIA"),
            bestForStrengths = listOf("Technical & Digital Aptitude", "Creative Storytelling & Art"),
            remediesStruggles = listOf("Focus & Staying On Task", "Task Initiation & Procrastination"),
            mathIntegration = "Calculate abacus arithmetic, Grand Canal lock water level rises, and Great Wall masonry volume.",
            readingIntegration = "Explore poetic folklore of the Dragon Boat festival, calligraphy radical strokes, and ancient scrolls.",
            scienceIntegration = "Experiment with magnetic lodestone navigation, woodblock movable type printing, and gunpowder physics.",
            socialStudiesIntegration = "Understand the Silk Road economy, civil service examinations, and dynasties along the Yellow River.",
            interactiveIdea = "Movable woodblock printing press simulator typesetting Chinese characters with tactile alignment.",
            primaryHex = 0xFF800020,
            secondaryHex = 0xFFD4AF37,
            surfaceHex = 0xFFFFF9F5,
            cardHex = 0xFFFFEBD6
        ),

        // ==========================================
        // 2. ROBOTICS, AI & FUTURISTIC TECH (11-20)
        // ==========================================
        NeuroThemeData(
            id = "robotics_ai",
            title = "Robotics & AI Cybernetic Academy",
            category = NeuroThemeCategory.AI_ROBOTICS_TECH,
            emoji = "🤖",
            buddyName = "Byte & Spark",
            buddyRole = "Cybernetic Robotics Mentor",
            greeting = "Beep boop! Neural circuits fully energized! Let's program our next breakthrough!",
            bestForDiagnoses = listOf("ADHD", "AUTISM_ASD", "GIFTED_2E"),
            bestForStrengths = listOf("Technical & Digital Aptitude", "Logic, Puzzles & Systems"),
            remediesStruggles = listOf("Focus & Staying On Task", "Multi-Step Instructions"),
            mathIntegration = "Program servo motor rotation angles, binary logic gates (AND/OR/NOT), and sensor distance vectors.",
            readingIntegration = "Read pseudocode syntax algorithms, robot safety ethics directives, and tech innovation briefs.",
            scienceIntegration = "Analyze closed-loop PID controllers, lithium battery voltage drop, and ultrasonic distance waves.",
            socialStudiesIntegration = "Discuss automation in global manufacturing, ethical AI in society, and the history of computing.",
            interactiveIdea = "Visual block-based logic gate circuit builder that lights up LED indicators and powers robotic arms.",
            primaryHex = 0xFF1F4068,
            secondaryHex = 0xFF162447,
            surfaceHex = 0xFFF0F4F8,
            cardHex = 0xFFD9E2EC
        ),
        NeuroThemeData(
            id = "quantum_supercomputing",
            title = "Quantum Supercomputing Matrix",
            category = NeuroThemeCategory.AI_ROBOTICS_TECH,
            emoji = "⚡",
            buddyName = "Qubit-Zero",
            buddyRole = "Superposition AI Guide",
            greeting = "Entering the quantum realm! Where possibilities compute in parallel!",
            bestForDiagnoses = listOf("AUTISM_ASD", "GIFTED_2E", "DYSCALCULIA"),
            bestForStrengths = listOf("Logic, Puzzles & Systems", "Visual Pattern Recognition"),
            remediesStruggles = listOf("Task Initiation & Procrastination", "Working Memory"),
            mathIntegration = "Explore probability distributions, binary-to-hexadecimal conversions, and matrix coordinate rotations.",
            readingIntegration = "Read futuristic science logs, encrypted cybersecurity transmissions, and sci-fi narrative hooks.",
            scienceIntegration = "Investigate particle wave duality, superconducting cryogenics, and photon laser communications.",
            socialStudiesIntegration = "Explore the global race for supercomputing infrastructure and data privacy protections.",
            interactiveIdea = "Interactive Bloch sphere visualizer rotating qubit states between |0⟩ and |1⟩ with neon wave pulses.",
            primaryHex = 0xFF4A0E4E,
            secondaryHex = 0xFF00ADB5,
            surfaceHex = 0xFFF5F0F6,
            cardHex = 0xFFE8D7EA
        ),
        NeuroThemeData(
            id = "mars_rover_engineers",
            title = "Mars Rover Expedition & Red Planet AI",
            category = NeuroThemeCategory.AI_ROBOTICS_TECH,
            emoji = "🚀",
            buddyName = "Rover Sol-7",
            buddyRole = "Autonomous Rover AI",
            greeting = "Telemetry verified! Wheels spinning across Jezero Crater on the Red Planet!",
            bestForDiagnoses = listOf("ADHD", "AUTISM_ASD", "EXECUTIVE_DYSFUNCTION"),
            bestForStrengths = listOf("Spatial & 3D Reasoning", "Deep Passion & Hyperfocus"),
            remediesStruggles = listOf("Time Blindness & Transitions", "Task Initiation & Procrastination"),
            mathIntegration = "Calculate radio transmission delay times (3 to 22 minutes), solar panel kilowatt hours, and slope angles.",
            readingIntegration = "Analyze Martian soil sample logs, Mission Control mission check-lists, and rover engineering reports.",
            scienceIntegration = "Study atmospheric pressure (0.6% of Earth), iron oxide mineralogy, and robotic spectroscopy lasers.",
            socialStudiesIntegration = "Learn about international space treaty cooperation, NASA/ESA space programs, and human habitats.",
            interactiveIdea = "Waypoint path-planning grid avoiding Martian boulder hazards to drill core mineral samples.",
            primaryHex = 0xFFB33939,
            secondaryHex = 0xFFCD6133,
            surfaceHex = 0xFFFFF5F0,
            cardHex = 0xFFFFDFD3
        ),
        NeuroThemeData(
            id = "cyber_security_hackers",
            title = "Cyber Security & Cipher White-Hats",
            category = NeuroThemeCategory.AI_ROBOTICS_TECH,
            emoji = "🛡️",
            buddyName = "Glitch Guardian",
            buddyRole = "Firewall Security Analyst",
            greeting = "Firewalls active! Encrypting mission data to defend digital freedom!",
            bestForDiagnoses = listOf("ADHD", "AUTISM_ASD", "GIFTED_2E"),
            bestForStrengths = listOf("Logic, Puzzles & Systems", "Technical & Digital Aptitude"),
            remediesStruggles = listOf("Focus & Staying On Task", "Emotional Frustration When Stuck"),
            mathIntegration = "Master modular arithmetic in RSA cryptography, prime number factorization, and hash algorithms.",
            readingIntegration = "Analyze digital incident response case studies, cybersecurity ethics codes, and phishing alerts.",
            scienceIntegration = "Understand fiber optic total internal reflection, electronic packet routing, and network topology.",
            socialStudiesIntegration = "Explore internet history, global digital infrastructure security, and digital citizenship ethics.",
            interactiveIdea = "Caesar cipher and binary hash codecracker wheel with instant decryption visual animations.",
            primaryHex = 0xFF0B3C5D,
            secondaryHex = 0xFF328CC1,
            surfaceHex = 0xFFF0F7FB,
            cardHex = 0xFFD9EBF5
        ),
        NeuroThemeData(
            id = "nanobot_medicine",
            title = "Nano-Bots & Microscopic Cellular Lab",
            category = NeuroThemeCategory.AI_ROBOTICS_TECH,
            emoji = "🔬",
            buddyName = "Dr. Nano",
            buddyRole = "Bio-Nanotechnology Lead",
            greeting = "Shrinking down to the cellular scale! Let's repair biology with micro-bots!",
            bestForDiagnoses = listOf("AUTISM_ASD", "DYSLEXIA", "SENSORY_PROCESSING"),
            bestForStrengths = listOf("Visual Pattern Recognition", "Deep Passion & Hyperfocus"),
            remediesStruggles = listOf("Reading Fluency & Phonics", "Working Memory"),
            mathIntegration = "Calculate metric conversions (meters to nanometers 10^-9), cell surface area to volume ratios.",
            readingIntegration = "Read cellular biology diagrams with dyslexia-friendly phoneme guides and animated labels.",
            scienceIntegration = "Explore ribosome protein synthesis, white blood cell immune responses, and targeted drug delivery.",
            socialStudiesIntegration = "Discuss medical ethics in genetics, global access to nanotechnology healthcare, and lab safety.",
            interactiveIdea = "Steer miniature nano-bots through blood capillaries to deliver oxygen molecules to tired muscle cells.",
            primaryHex = 0xFF00695C,
            secondaryHex = 0xFF4DB6AC,
            surfaceHex = 0xFFE0F2F1,
            cardHex = 0xFFB2DFDB
        ),
        NeuroThemeData(
            id = "drone_telemetry_squadron",
            title = "Drone Squadron & Aerial Telemetry",
            category = NeuroThemeCategory.AI_ROBOTICS_TECH,
            emoji = "🚁",
            buddyName = "Wing-Commander Talon",
            buddyRole = "Autonomous Flight Lead",
            greeting = "Rotors spooled! Quadcopter GPS satellites locked! Cleared for take-off!",
            bestForDiagnoses = listOf("ADHD", "DYSCALCULIA", "EXECUTIVE_DYSFUNCTION"),
            bestForStrengths = listOf("Spatial & 3D Reasoning", "Technical & Digital Aptitude"),
            remediesStruggles = listOf("Multi-Step Instructions", "Focus & Staying On Task"),
            mathIntegration = "Calculate thrust-to-weight ratios, GPS latitude/longitude triangulation, and pitch/yaw/roll degrees.",
            readingIntegration = "Read FAA aviation safety protocols, pre-flight checklists, and aerial mapping project logs.",
            scienceIntegration = "Study Bernoulli aerodynamic lift, brushless motor electromagnetic coils, and LiPo discharge curves.",
            socialStudiesIntegration = "Explore drone applications in wildlife conservation, disaster relief rescue, and agriculture.",
            interactiveIdea = "Quadcopter flight stabilizer adjusting 4 independent rotor speeds to balance against crosswinds.",
            primaryHex = 0xFF2C3A47,
            secondaryHex = 0xFF182C61,
            surfaceHex = 0xFFF3F4F7,
            cardHex = 0xFFD8DDE6
        ),
        NeuroThemeData(
            id = "hologram_physics_lab",
            title = "Hologram Studio & Optical Photonics",
            category = NeuroThemeCategory.AI_ROBOTICS_TECH,
            emoji = "✨",
            buddyName = "Lumen AI",
            buddyRole = "Photonic Wave Guide",
            greeting = "Splitting the light beams! Watch 3D volumetric images shimmer into reality!",
            bestForDiagnoses = listOf("AUTISM_ASD", "SENSORY_PROCESSING", "GIFTED_2E"),
            bestForStrengths = listOf("Visual Pattern Recognition", "Creative Storytelling & Art"),
            remediesStruggles = listOf("Sensory Overload & Noise", "Task Initiation & Procrastination"),
            mathIntegration = "Calculate Snell's law of light refraction angles, laser wavelength frequencies (nanometers), and prism angles.",
            readingIntegration = "Analyze optics laboratory notebooks, visual interference diagrams, and future projection stories.",
            scienceIntegration = "Examine laser coherence, constructive/destructive wave interference, and diffraction grating physics.",
            socialStudiesIntegration = "Explore history of optics from Ibn al-Haytham to modern optical fiber telecommunications.",
            interactiveIdea = "Prism and beam-splitter playground directing laser rays to project a glowing 3D holographic crystal.",
            primaryHex = 0xFF6C5CE7,
            secondaryHex = 0xFFA29BFE,
            surfaceHex = 0xFFF8F7FF,
            cardHex = 0xFFE6E3FA
        ),
        NeuroThemeData(
            id = "space_station_automation",
            title = "Orbital Space Station & ISS Bio-Lab",
            category = NeuroThemeCategory.AI_ROBOTICS_TECH,
            emoji = "🛰️",
            buddyName = "Astro-Bot Echo",
            buddyRole = "Orbital Life Support Engineer",
            greeting = "Orbiting Earth every 90 minutes! Microgravity lab systems are operating smoothly!",
            bestForDiagnoses = listOf("ADHD", "AUTISM_ASD", "ANXIETY_STRESS"),
            bestForStrengths = listOf("Spatial & 3D Reasoning", "Logic, Puzzles & Systems"),
            remediesStruggles = listOf("Time Blindness & Transitions", "Emotional Frustration When Stuck"),
            mathIntegration = "Calculate orbital velocity (17,500 mph), solar array power consumption, and water recycling percentages.",
            readingIntegration = "Read astronaut mission diaries, spacewalk EVA contingency protocols, and international crew notes.",
            scienceIntegration = "Analyze plant hydroponic growth in zero-G, CO2 scrubber chemical reactions, and bone density biology.",
            socialStudiesIntegration = "Explore 15-nation peaceful space cooperation on the ISS and future Artemis lunar base agreements.",
            interactiveIdea = "Space station docking alignment hud matching roll, pitch, and yaw thrusters to lock into airlocks.",
            primaryHex = 0xFF2D3436,
            secondaryHex = 0xFF0984E3,
            surfaceHex = 0xFFF1F6FB,
            cardHex = 0xFFD4E6F8
        ),
        NeuroThemeData(
            id = "retro_arcade_gamedev",
            title = "8-Bit Retro Arcade & Video Game Devs",
            category = NeuroThemeCategory.AI_ROBOTICS_TECH,
            emoji = "🎮",
            buddyName = "Pixel Pilot",
            buddyRole = "Lead Indie Game Designer",
            greeting = "Insert Coin! Press Start! Let's code and play our way to high scores!",
            bestForDiagnoses = listOf("ADHD", "AUTISM_ASD", "DYSCALCULIA"),
            bestForStrengths = listOf("Technical & Digital Aptitude", "Visual Pattern Recognition"),
            remediesStruggles = listOf("Focus & Staying On Task", "Task Initiation & Procrastination"),
            mathIntegration = "Calculate 2D Cartesian coordinate sprite velocity (x, y vectors), collision bounding box hitboxes.",
            readingIntegration = "Draft engaging character dialogue trees, quest item inventory lore, and level walkthrough guides.",
            scienceIntegration = "Explore digital color palettes (RGB 8-bit channels), CRT refresh rates, and basic physics gravity loops.",
            socialStudiesIntegration = "Trace the evolution of digital gaming culture from 1970s arcade cabinets to modern esports.",
            interactiveIdea = "Interactive 2D sprite animator editing pixel grids and testing gravity jump arcs in real time.",
            primaryHex = 0xFF2C3A47,
            secondaryHex = 0xFFE71C23,
            surfaceHex = 0xFFFFF5F5,
            cardHex = 0xFFFFD6D8
        ),
        NeuroThemeData(
            id = "biomech_exoskeletons",
            title = "Bio-Mechanical Exoskeletons & Bionics",
            category = NeuroThemeCategory.AI_ROBOTICS_TECH,
            emoji = "🦾",
            buddyName = "Cyborg Coach Jax",
            buddyRole = "Bionic Mobility Specialist",
            greeting = "Harness the power of bionics! Engineering prosthetics to amplify human potential!",
            bestForDiagnoses = listOf("ADHD", "DYSLEXIA", "SENSORY_PROCESSING"),
            bestForStrengths = listOf("Spatial & 3D Reasoning", "Empathy & Emotional Insight"),
            remediesStruggles = listOf("Handwriting & Fine Motor", "Working Memory"),
            mathIntegration = "Calculate hydraulic mechanical advantage ratios, torque multipliers, and sensor response millisecond delays.",
            readingIntegration = "Read inspirational profiles of Paralympic bionic athletes and assistive bioengineering journals.",
            scienceIntegration = "Study myoelectric muscle EMG signals, titanium alloy tensile strength, and human kinesiology.",
            socialStudiesIntegration = "Explore accessibility laws (ADA), inclusive design in civic architecture, and universal mobility.",
            interactiveIdea = "Calibrate bionic finger grip sensors with soft haptic pressure dials to pick up delicate glass beakers.",
            primaryHex = 0xFF2F3640,
            secondaryHex = 0xFF718093,
            surfaceHex = 0xFFF5F6FA,
            cardHex = 0xFFDCDDE1
        ),

        // ==========================================
        // 3. MYTHOLOGICAL CREATURES & FOLKLORE (21-30)
        // ==========================================
        NeuroThemeData(
            id = "dragon_keepers",
            title = "Dragon Keepers & Elemental Wyrms",
            category = NeuroThemeCategory.MYTH_FANTASY,
            emoji = "🐉",
            buddyName = "Ignis the Drake",
            buddyRole = "Elder Dragon Lorekeeper",
            greeting = "Hail, dragon scholar! Ignite the fire of imagination and conquer every trial!",
            bestForDiagnoses = listOf("ADHD", "AUTISM_ASD", "DYSLEXIA", "GIFTED_2E"),
            bestForStrengths = listOf("Deep Passion & Hyperfocus", "Creative Storytelling & Art"),
            remediesStruggles = listOf("Reading Fluency & Phonics", "Task Initiation & Procrastination"),
            mathIntegration = "Calculate dragon flight wingspan ratios, gemstone hoard counting with multi-digit grouping, and thermal arcs.",
            readingIntegration = "Read epic fantasy narratives with dyslexic font assistance, mythical bestiaries, and rune poems.",
            scienceIntegration = "Compare mythological dragon anatomy to real reptiles, pterosaurs, and chemical bioluminescence.",
            socialStudiesIntegration = "Compare Eastern serpentine water dragons of Chinese folklore with Western European fire-breathers.",
            interactiveIdea = "Hatch and feed dragon eggs by solving math equations that balance temperature and gem mineral nutrients.",
            primaryHex = 0xFF581845,
            secondaryHex = 0xFFC70039,
            surfaceHex = 0xFFFFF5F8,
            cardHex = 0xFFFFE3EC
        ),
        NeuroThemeData(
            id = "phoenix_solar_wonders",
            title = "Phoenix Sanctuary & Solar Wonders",
            category = NeuroThemeCategory.MYTH_FANTASY,
            emoji = "🦅",
            buddyName = "Solara the Phoenix",
            buddyRole = "Guardian of Rebirth & Light",
            greeting = "From every mistake comes greater wisdom! Rise and shine with golden curiosity!",
            bestForDiagnoses = listOf("ANXIETY_STRESS", "ADHD", "SENSORY_PROCESSING"),
            bestForStrengths = listOf("Creative Storytelling & Art", "Empathy & Emotional Insight"),
            remediesStruggles = listOf("Emotional Frustration When Stuck", "Sensory Overload & Noise"),
            mathIntegration = "Calculate solar angle elevation, golden ratio spirals in phoenix feathers, and radiant light intensities.",
            readingIntegration = "Read growth-mindset folklore stories about resilience, renewal, and glowing poetry.",
            scienceIntegration = "Explore electromagnetic light spectrum, thermal convection currents, and bird feather aerodynamics.",
            socialStudiesIntegration = "Explore the phoenix in Greek, Egyptian (Bennu), and Chinese (Fenghuang) mythology traditions.",
            interactiveIdea = "Growth mindset affirmation flame visualizer that blooms golden sparkle embers when tasks are completed.",
            primaryHex = 0xFFD35400,
            secondaryHex = 0xFFF39C12,
            surfaceHex = 0xFFFFFBF0,
            cardHex = 0xFFFFECC7
        ),
        NeuroThemeData(
            id = "pegasus_sky_realms",
            title = "Pegasus & Cloud Castle Sky Realms",
            category = NeuroThemeCategory.MYTH_FANTASY,
            emoji = "🦄",
            buddyName = "Zephyr the Winged Steed",
            buddyRole = "Sky Realm Navigator",
            greeting = "Gallop across the sunlit clouds! Your thoughts have wings to soar anywhere!",
            bestForDiagnoses = listOf("ADHD", "DYSLEXIA", "ANXIETY_STRESS"),
            bestForStrengths = listOf("Creative Storytelling & Art", "Empathy & Emotional Insight"),
            remediesStruggles = listOf("Reading Fluency & Phonics", "Emotional Frustration When Stuck"),
            mathIntegration = "Calculate cloud altitude altitudes, rainbow wavelength refraction angles, and starry distance charts.",
            readingIntegration = "Read enchanting sky kingdom quests, character dialogue with text-to-speech, and lyrical verse.",
            scienceIntegration = "Examine water vapor condensation into cumulus clouds, atmospheric air pressure, and avian wing skeletons.",
            socialStudiesIntegration = "Study Greek constellations, celestial myth mapping across cultures, and ancient star navigation.",
            interactiveIdea = "Interactive cloud glider guiding Pegasus through glowing star gates by tapping matching phonics words.",
            primaryHex = 0xFF6A0572,
            secondaryHex = 0xFFAB83A1,
            surfaceHex = 0xFFFAF3F7,
            cardHex = 0xFFF3DDF2
        ),
        NeuroThemeData(
            id = "sea_monsters_merfolk",
            title = "Merfolk & Deep Oceanic Legends",
            category = NeuroThemeCategory.MYTH_FANTASY,
            emoji = "🧜‍♀️",
            buddyName = "Coralia the Pearl Guard",
            buddyRole = "Trench Kingdom Diplomat",
            greeting = "Dive into shimmering turquoise depths! Discover folklore treasures beneath the waves!",
            bestForDiagnoses = listOf("AUTISM_ASD", "SENSORY_PROCESSING", "DYSLEXIA"),
            bestForStrengths = listOf("Visual Pattern Recognition", "Empathy & Emotional Insight"),
            remediesStruggles = listOf("Sensory Overload & Noise", "Focus & Staying On Task"),
            mathIntegration = "Calculate ocean depth pressure (1 atm per 10m), shell spiral Fibonacci numbers, and pearl value grouping.",
            readingIntegration = "Read marine folklore tales of Selkies, Triton, and undersea kingdoms with calming word spacing.",
            scienceIntegration = "Explore deep-sea hydrothermal vents, abyssal bioluminescent anglerfish, and coral symbiosis.",
            socialStudiesIntegration = "Learn about Polynesian voyaging traditions, maritime folklore across continents, and ocean conservation.",
            interactiveIdea = "Calming underwater pearl organizer sorting glowing sea crystals with soothing bubble audio tones.",
            primaryHex = 0xFF0077B6,
            secondaryHex = 0xFF48CAE4,
            surfaceHex = 0xFFF0F9FD,
            cardHex = 0xFFCAF0F8
        ),
        NeuroThemeData(
            id = "kitsune_yokai_spirits",
            title = "Kitsune & Enchanted Forest Spirits",
            category = NeuroThemeCategory.MYTH_FANTASY,
            emoji = "🦊",
            buddyName = "Kiko the Nine-Tailed Fox",
            buddyRole = "Enchanted Spirit Guide",
            greeting = "Kon-kon! Walk quietly through the cedar grove! Wisdom hides in every shadow and leaf!",
            bestForDiagnoses = listOf("ADHD", "AUTISM_ASD", "GIFTED_2E"),
            bestForStrengths = listOf("Creative Storytelling & Art", "Logic, Puzzles & Systems"),
            remediesStruggles = listOf("Focus & Staying On Task", "Emotional Frustration When Stuck"),
            mathIntegration = "Calculate nine-tail permutation riddle combinations, lantern symmetry patterns, and shrine gate steps.",
            readingIntegration = "Read traditional Japanese folktales, animal fable morals, and poetic nature reflections.",
            scienceIntegration = "Study forest canopy temperate ecology, bioluminescent foxfire fungi, and seasonal deciduous cycles.",
            socialStudiesIntegration = "Explore Japanese cultural folklore, Shinto respect for nature spirits (Kami), and seasonal festivals.",
            interactiveIdea = "Match glowing will-o'-the-wisp spirit lanterns in symmetry sequence puzzles with soft wind-chime audio.",
            primaryHex = 0xFFE65100,
            secondaryHex = 0xFFFF9800,
            surfaceHex = 0xFFFFF8E1,
            cardHex = 0xFFFFE0B2
        ),
        NeuroThemeData(
            id = "nordic_frost_giants",
            title = "Nordic Frost Giants & Runestone Lore",
            category = NeuroThemeCategory.MYTH_FANTASY,
            emoji = "❄️",
            buddyName = "Jotun Frost-Weaver",
            buddyRole = "Glacial Citadel Elder",
            greeting = "Feel the crisp mountain gale! Stand strong like ancient ice against any problem!",
            bestForDiagnoses = listOf("ADHD", "DYSCALCULIA", "EXECUTIVE_DYSFUNCTION"),
            bestForStrengths = listOf("Spatial & 3D Reasoning", "Deep Passion & Hyperfocus"),
            remediesStruggles = listOf("Task Initiation & Procrastination", "Math Concepts & Working Memory"),
            mathIntegration = "Calculate ice crystal hexagonal symmetry geometry (60° angles), glacial weight loads, and runic values.",
            readingIntegration = "Read Norse myths of Thor, Loki, and the World Tree Yggdrasil with audio character dramatization.",
            scienceIntegration = "Analyze ice crystal lattice structure, permafrost thermal insulation, and Aurora Borealis solar wind.",
            socialStudiesIntegration = "Explore Scandinavian geography, fjord creation through glaciation, and medieval Scandinavian settlements.",
            interactiveIdea = "Hexagonal snowflake crystal growth simulator balancing temperature sliders to sculpt giant ice bridges.",
            primaryHex = 0xFF1E3799,
            secondaryHex = 0xFF4A69BD,
            surfaceHex = 0xFFF5F8FF,
            cardHex = 0xFFDEEAFF
        ),
        NeuroThemeData(
            id = "enchanted_forest_faeries",
            title = "Enchanted Forest & Faerie Botanists",
            category = NeuroThemeCategory.MYTH_FANTASY,
            emoji = "🧚‍♀️",
            buddyName = "Fauna the Fern Pixie",
            buddyRole = "Botanical Fairy Guardian",
            greeting = "Listen closely! The ancient moss whispers lessons of kindness and wonder!",
            bestForDiagnoses = listOf("SENSORY_PROCESSING", "DYSLEXIA", "ANXIETY_STRESS"),
            bestForStrengths = listOf("Creative Storytelling & Art", "Empathy & Emotional Insight"),
            remediesStruggles = listOf("Sensory Overload & Noise", "Reading Fluency & Phonics"),
            mathIntegration = "Count fairy ring mushroom concentric circles, petal Fibonacci spirals (1, 1, 2, 3, 5, 8, 13), and dew drops.",
            readingIntegration = "Read descriptive sensory forest tales, rhyming pixie spells, and plant identification journals.",
            scienceIntegration = "Examine plant photosynthesis chemistry, mycorrhizal fungal root networks, and pollen pollination.",
            socialStudiesIntegration = "Explore Celtic folklore traditions, herbal medicine history, and modern forest conservation biology.",
            interactiveIdea = "Fairy dust botanical garden watering interactive puzzle growing magic flowers on math solutions.",
            primaryHex = 0xFF2E7D32,
            secondaryHex = 0xFF81C784,
            surfaceHex = 0xFFF1F8F4,
            cardHex = 0xFFD8F3DC
        ),
        NeuroThemeData(
            id = "thunderbird_lore",
            title = "Thunderbird Guardians & Sky Legends",
            category = NeuroThemeCategory.MYTH_FANTASY,
            emoji = "⚡",
            buddyName = "Chief Storm-Wing",
            buddyRole = "Sky Clan Storyteller",
            greeting = "Thunder rolls across the plains! Harness the electric spark of curiosity!",
            bestForDiagnoses = listOf("ADHD", "AUTISM_ASD", "GIFTED_2E"),
            bestForStrengths = listOf("Visual Pattern Recognition", "Deep Passion & Hyperfocus"),
            remediesStruggles = listOf("Focus & Staying On Task", "Emotional Frustration When Stuck"),
            mathIntegration = "Calculate lightning strike distance using sound delay (5 seconds per mile), wind shear vectors.",
            readingIntegration = "Listen to authentic Indigenous oral storytelling traditions with deep respect and vocabulary guides.",
            scienceIntegration = "Study electrical static charge buildup in storm clouds, lightning plasma, and acoustic thunder rumbles.",
            socialStudiesIntegration = "Learn about North American Indigenous Nations, totem art symbolism, and sacred balance with nature.",
            interactiveIdea = "Storm cloud charge generator building up positive and negative charges to create safe lightning arcs.",
            primaryHex = 0xFF192A56,
            secondaryHex = 0xFFFBC531,
            surfaceHex = 0xFFF8F9FE,
            cardHex = 0xFFE1E5F8
        ),
        NeuroThemeData(
            id = "alchemical_wizardry",
            title = "Alchemical Wizardry & Potion Academy",
            category = NeuroThemeCategory.MYTH_FANTASY,
            emoji = "🧙‍♂️",
            buddyName = "Arch-Mage Merlin Jr.",
            buddyRole = "Grand Sorcery Instructor",
            greeting = "Ready your wand and stirring rod! Let us transmute curiosity into pure genius!",
            bestForDiagnoses = listOf("ADHD", "AUTISM_ASD", "DYSCALCULIA", "GIFTED_2E"),
            bestForStrengths = listOf("Logic, Puzzles & Systems", "Creative Storytelling & Art"),
            remediesStruggles = listOf("Multi-Step Instructions", "Math Concepts & Working Memory"),
            mathIntegration = "Measure potion liquid volume ratios (milliliters), spell ingredient proportions, and magical cauldron timers.",
            readingIntegration = "Read mystical grimoires, latin-root spell incantations, and alchemy recipe riddle steps.",
            scienceIntegration = "Explore acids and bases (pH scale with color-changing indicators), exothermic chemical reactions, and density.",
            socialStudiesIntegration = "Examine the historical transition from medieval alchemy to the modern scientific method and chemistry.",
            interactiveIdea = "Interactive potion brewing cauldron mixing colored liquids with fizzing particle bubbles and haptics.",
            primaryHex = 0xFF4B0082,
            secondaryHex = 0xFF9932CC,
            surfaceHex = 0xFFF7F0FC,
            cardHex = 0xFFE9D5F7
        ),
        NeuroThemeData(
            id = "chimera_genetics",
            title = "Chimera & Mythical Creature Genetics",
            category = NeuroThemeCategory.MYTH_FANTASY,
            emoji = "🦁",
            buddyName = "Biologist Chimera-X",
            buddyRole = "Mythic Zoology Researcher",
            greeting = "Combining lion courage, goat agility, and serpent wisdom! Let's explore traits!",
            bestForDiagnoses = listOf("AUTISM_ASD", "GIFTED_2E", "DYSLEXIA"),
            bestForStrengths = listOf("Visual Pattern Recognition", "Logic, Puzzles & Systems"),
            remediesStruggles = listOf("Reading Fluency & Phonics", "Working Memory"),
            mathIntegration = "Calculate Punnett square dominant/recessive probability ratios (3:1, 9:3:3:1) and trait matrices.",
            readingIntegration = "Read scientific creature classification field guides, genetic trait codes, and comparative anatomy.",
            scienceIntegration = "Study DNA chromosomes, allele inheritance, adaptation to ecosystems, and convergent evolution.",
            socialStudiesIntegration = "Trace how ancient travelers invented mythical creatures to explain unfamiliar exotic wildlife.",
            interactiveIdea = "Interactive Punnett square puzzle matching dragon scales and wings to discover offspring genetic traits.",
            primaryHex = 0xFF8E44AD,
            secondaryHex = 0xFF2ECC71,
            surfaceHex = 0xFFF8F5FB,
            cardHex = 0xFFE8DAEF
        ),

        // ==========================================
        // 4. CULINARY ADVENTURES & KITCHEN SCIENCE (31-40)
        // ==========================================
        NeuroThemeData(
            id = "culinary_adventures",
            title = "Culinary Adventures & Master Chef Bistro",
            category = NeuroThemeCategory.CULINARY_FOOD_SCIENCE,
            emoji = "🍳",
            buddyName = "Chef Pierre",
            buddyRole = "Executive Culinary Instructor",
            greeting = "Bon appétit! Put on your chef's hat! Knowledge is the tastiest recipe of all!",
            bestForDiagnoses = listOf("ADHD", "DYSCALCULIA", "EXECUTIVE_DYSFUNCTION"),
            bestForStrengths = listOf("Creative Storytelling & Art", "Spatial & 3D Reasoning"),
            remediesStruggles = listOf("Math Concepts & Working Memory", "Multi-Step Instructions"),
            mathIntegration = "Scale recipe ingredient fractions (1/2 cup to 3/4 cup), unit conversions (tablespoons to cups), and cooking timers.",
            readingIntegration = "Follow multi-step sequential recipe instructions, culinary glossary terms, and menu descriptions.",
            scienceIntegration = "Analyze Maillard browning reaction chemistry, emulsion stability in dressings, and yeast fermentation.",
            socialStudiesIntegration = "Explore regional culinary traditions, the Columbian Exchange of ingredients, and food geography.",
            interactiveIdea = "Interactive recipe scale measuring exact gram weights and mixing bowl animations with sizzle audio.",
            primaryHex = 0xFFD35400,
            secondaryHex = 0xFFE67E22,
            surfaceHex = 0xFFFFF7F0,
            cardHex = 0xFFFFE8D6
        ),
        NeuroThemeData(
            id = "molecular_gastronomy",
            title = "Molecular Gastronomy & Food Science Lab",
            category = NeuroThemeCategory.CULINARY_FOOD_SCIENCE,
            emoji = "🧪",
            buddyName = "Chef Spherica",
            buddyRole = "Molecular Food Chemist",
            greeting = "Transforming tastes with chemistry! Turn fruit juice into popping flavor pearls!",
            bestForDiagnoses = listOf("AUTISM_ASD", "GIFTED_2E", "ADHD"),
            bestForStrengths = listOf("Logic, Puzzles & Systems", "Technical & Digital Aptitude"),
            remediesStruggles = listOf("Focus & Staying On Task", "Working Memory"),
            mathIntegration = "Calculate sodium alginate concentration percentages (0.5% solutions), temperature Celsius to Fahrenheit.",
            readingIntegration = "Read food science laboratory protocol sheets, chemical safety guidelines, and molecular cooking guides.",
            scienceIntegration = "Study spherification cross-linking of polymers, liquid nitrogen rapid freezing, and foam hydrocolloids.",
            socialStudiesIntegration = "Explore modern food tech innovations, sustainable alternative proteins, and zero-waste dining.",
            interactiveIdea = "Pipette dropping calcium bath droplets to form gelatinous fruit caviar spheres with satisfying pop haptics.",
            primaryHex = 0xFF009688,
            secondaryHex = 0xFF80CBC4,
            surfaceHex = 0xFFE0F2F1,
            cardHex = 0xFFB2DFDB
        ),
        NeuroThemeData(
            id = "global_street_food",
            title = "Global Street Food & Spice Bazaar",
            category = NeuroThemeCategory.CULINARY_FOOD_SCIENCE,
            emoji = "🌮",
            buddyName = "Chef Carmen",
            buddyRole = "Street Food Cartographer",
            greeting = "Sizzling tacos, savory noodles, sweet churros! Let's tour the world's best food stalls!",
            bestForDiagnoses = listOf("ADHD", "DYSLEXIA", "SENSORY_PROCESSING"),
            bestForStrengths = listOf("Empathy & Emotional Insight", "Creative Storytelling & Art"),
            remediesStruggles = listOf("Reading Fluency & Phonics", "Emotional Frustration When Stuck"),
            mathIntegration = "Calculate currency conversion budgets for night market purchases, food cost percentages, and change calculation.",
            readingIntegration = "Read descriptive sensory travelogues, international phonetic food names, and cultural origins.",
            scienceIntegration = "Explore capsaicin heat receptors on the tongue (Scoville scale), spice antimicrobial properties, and preservation.",
            socialStudiesIntegration = "Investigate street food culture in Mexico, Thailand, India, and Morocco, and immigration food fusion.",
            interactiveIdea = "Street food cart assembly matching taco toppings or dim sum dumplings to fulfill customer orders.",
            primaryHex = 0xFFC0392B,
            secondaryHex = 0xFFF1C40F,
            surfaceHex = 0xFFFFFDF2,
            cardHex = 0xFFFFEAA7
        ),
        NeuroThemeData(
            id = "baking_chemistry",
            title = "Baking Chemistry & Sweet Patisserie",
            category = NeuroThemeCategory.CULINARY_FOOD_SCIENCE,
            emoji = "🧁",
            buddyName = "Pastry Chef Mia",
            buddyRole = "Master Patissier & Chemist",
            greeting = "Baking is precise science made sweet! Let's whisk up golden perfection!",
            bestForDiagnoses = listOf("AUTISM_ASD", "DYSCALCULIA", "EXECUTIVE_DYSFUNCTION"),
            bestForStrengths = listOf("Spatial & 3D Reasoning", "Logic, Puzzles & Systems"),
            remediesStruggles = listOf("Task Initiation & Procrastination", "Math Concepts & Working Memory"),
            mathIntegration = "Calculate baker's percentages (flour 100%), dough hydration ratios, and baking pan volume geometry.",
            readingIntegration = "Follow step-by-step baking timelines, recipe ingredient glossaries, and French pastry vocabulary.",
            scienceIntegration = "Study baking powder CO2 acid-base leavening, gluten protein network formation, and sugar caramelization.",
            socialStudiesIntegration = "Explore the history of bread across civilizations, Parisian patisseries, and festive holiday baking.",
            interactiveIdea = "Virtual oven temperature and timer dial monitoring bread dough rising with golden crust color shifts.",
            primaryHex = 0xFFE84393,
            secondaryHex = 0xFFFD79A8,
            surfaceHex = 0xFFFFF5F8,
            cardHex = 0xFFFFE3EC
        ),
        NeuroThemeData(
            id = "farm_to_table_organic",
            title = "Farm-to-Table Organic Harvest",
            category = NeuroThemeCategory.CULINARY_FOOD_SCIENCE,
            emoji = "🚜",
            buddyName = "Farmer Sam",
            buddyRole = "Sustainable Agronomist",
            greeting = "Plant the seeds, nurture the soil! Real food starts from the fertile earth!",
            bestForDiagnoses = listOf("ADHD", "SENSORY_PROCESSING", "ANXIETY_STRESS"),
            bestForStrengths = listOf("Visual Pattern Recognition", "Empathy & Emotional Insight"),
            remediesStruggles = listOf("Sensory Overload & Noise", "Focus & Staying On Task"),
            mathIntegration = "Calculate crop yield per acre, composting carbon-to-nitrogen ratios (30:1), and seasonal market pricing.",
            readingIntegration = "Read seed packet planting guides, agricultural weather forecasts, and organic soil health logs.",
            scienceIntegration = "Analyze soil nitrogen-fixing bacteria, companion planting pest repulsion, and rainwater harvesting.",
            socialStudiesIntegration = "Study community supported agriculture (CSA), global food supply chains, and regenerative farming.",
            interactiveIdea = "Grid garden planting simulator balancing sunlight, companion herbs, and drip irrigation channels.",
            primaryHex = 0xFF27AE60,
            secondaryHex = 0xFF2ECC71,
            surfaceHex = 0xFFF1FBF5,
            cardHex = 0xFFD5F5E3
        ),
        NeuroThemeData(
            id = "sweet_confectionery",
            title = "Candy Factory & Sugar Crystal Science",
            category = NeuroThemeCategory.CULINARY_FOOD_SCIENCE,
            emoji = "🍬",
            buddyName = "Chocolatier Willy",
            buddyRole = "Confectionery Engineer",
            greeting = "Welcome to the sweet candy laboratory! Where sugar crystals become works of art!",
            bestForDiagnoses = listOf("ADHD", "AUTISM_ASD", "DYSCALCULIA"),
            bestForStrengths = listOf("Creative Storytelling & Art", "Spatial & 3D Reasoning"),
            remediesStruggles = listOf("Focus & Staying On Task", "Emotional Frustration When Stuck"),
            mathIntegration = "Calculate candy thermometer boiling stages (soft ball 235°F to hard crack 300°F), chocolate tempering curves.",
            readingIntegration = "Read whimsical confectionery stories, factory safety instructions, and chocolate origin myths.",
            scienceIntegration = "Study sucrose crystallization kinetics, cocoa butter polymorphic crystal forms (Form V), and gelatin gelling.",
            socialStudiesIntegration = "Explore Mesoamerican cacao origins, fair trade cocoa farming, and global candy manufacturing.",
            interactiveIdea = "Sugar crystal growing temperature slider watching geometric crystals sparkle and form on candy sticks.",
            primaryHex = 0xFF8E44AD,
            secondaryHex = 0xFFE056FD,
            surfaceHex = 0xFFFAF3FC,
            cardHex = 0xFFF1DCF9
        ),
        NeuroThemeData(
            id = "campfire_trail_cooking",
            title = "Campfire Cooking & Wilderness Chef",
            category = NeuroThemeCategory.CULINARY_FOOD_SCIENCE,
            emoji = "⛺",
            buddyName = "Ranger Buck",
            buddyRole = "Trail Guide & Outdoor Cook",
            greeting = "Build up the glowing embers! Dutch oven stew tastes best under the starry night sky!",
            bestForDiagnoses = listOf("ADHD", "DYSLEXIA", "EXECUTIVE_DYSFUNCTION"),
            bestForStrengths = listOf("Spatial & 3D Reasoning", "Deep Passion & Hyperfocus"),
            remediesStruggles = listOf("Reading Fluency & Phonics", "Time Blindness & Transitions"),
            mathIntegration = "Calculate charcoal briquette temperature formula (Count above/below Dutch oven), trail calorie planning.",
            readingIntegration = "Read outdoor survival manuals, Leave No Trace principles, and campfire folklore ghost stories.",
            scienceIntegration = "Analyze wood combustion chemistry, cast iron thermal conductivity, and water purification boiling science.",
            socialStudiesIntegration = "Explore pioneer chuckwagon history, Native American food preservation (pemmican), and national parks.",
            interactiveIdea = "Cast iron Dutch oven heat balancer placing top and bottom glowing coals to bake trail biscuits.",
            primaryHex = 0xFF795548,
            secondaryHex = 0xFFFF5722,
            surfaceHex = 0xFFFFF8F0,
            cardHex = 0xFFFFE8D6
        ),
        NeuroThemeData(
            id = "space_nutrition",
            title = "Space Station Kitchen & Astronaut Meals",
            category = NeuroThemeCategory.CULINARY_FOOD_SCIENCE,
            emoji = "🪐",
            buddyName = "Astro-Chef Cosmo",
            buddyRole = "Microgravity Food Scientist",
            greeting = "Velcro your food pouch! No floating crumbs allowed in the orbital galley!",
            bestForDiagnoses = listOf("AUTISM_ASD", "GIFTED_2E", "ADHD"),
            bestForStrengths = listOf("Technical & Digital Aptitude", "Logic, Puzzles & Systems"),
            remediesStruggles = listOf("Multi-Step Instructions", "Sensory Overload & Noise"),
            mathIntegration = "Calculate rehydration water milliliters, daily astronaut caloric requirements in zero-g, and shelf-life months.",
            readingIntegration = "Read NASA space food development logs, pouch barcode scanning instructions, and mission nutrition logs.",
            scienceIntegration = "Study freeze-drying (lyophilization sublimation), radiation food sterilization, and zero-G taste bud shifts.",
            socialStudiesIntegration = "Learn how astronauts from different nations share cultural foods (tortillas, kimchi, espresso) on the ISS.",
            interactiveIdea = "Space food pouch rehydration needle injecting measured hot water without letting water bubbles escape.",
            primaryHex = 0xFF1B1464,
            secondaryHex = 0xFF006266,
            surfaceHex = 0xFFF0F6F6,
            cardHex = 0xFFD3ECEC
        ),
        NeuroThemeData(
            id = "fermentation_microbes",
            title = "Fermentation Lab & Microbe Culinary",
            category = NeuroThemeCategory.CULINARY_FOOD_SCIENCE,
            emoji = "🧫",
            buddyName = "Dr. Kimchi",
            buddyRole = "Microbial Fermentation Ecologist",
            greeting = "Tiny microscopic allies make big flavors! Let's cultivate friendly microbes!",
            bestForDiagnoses = listOf("AUTISM_ASD", "GIFTED_2E", "DYSCALCULIA"),
            bestForStrengths = listOf("Visual Pattern Recognition", "Logic, Puzzles & Systems"),
            remediesStruggles = listOf("Working Memory", "Task Initiation & Procrastination"),
            mathIntegration = "Calculate brine salinity percentages (2% to 5% salt by weight), fermentation day tracking, and pH drops.",
            readingIntegration = "Read sourdough starter feeding schedules, ancient fermentation scrolls, and microbiological labels.",
            scienceIntegration = "Analyze Lactobacillus anaerobic respiration, yeast converting sugars to CO2 and alcohol, and gut microbiome.",
            socialStudiesIntegration = "Study fermentation traditions across the world: kimchi in Korea, sauerkraut in Germany, miso in Japan.",
            interactiveIdea = "Microscope slider viewing active yeast and lactobacillus colonies bubbling to ferment sourdough bread.",
            primaryHex = 0xFF57606F,
            secondaryHex = 0xFF70A1FF,
            surfaceHex = 0xFFF1F5FC,
            cardHex = 0xFFD8E5FC
        ),
        NeuroThemeData(
            id = "oceanic_fishery",
            title = "Oceanic Kitchen & Sustainable Seafood",
            category = NeuroThemeCategory.CULINARY_FOOD_SCIENCE,
            emoji = "🐟",
            buddyName = "Chef Kai",
            buddyRole = "Sustainable Ocean Chef",
            greeting = "Respect the ocean bounty! Let's prepare nutritious seafood sustainably!",
            bestForDiagnoses = listOf("ADHD", "DYSLEXIA", "SENSORY_PROCESSING"),
            bestForStrengths = listOf("Spatial & 3D Reasoning", "Empathy & Emotional Insight"),
            remediesStruggles = listOf("Reading Fluency & Phonics", "Emotional Frustration When Stuck"),
            mathIntegration = "Calculate sustainable fish catch quotas, omega-3 fatty acid milligrams per serving, and sushi rice vinegar ratios.",
            readingIntegration = "Read ocean sustainable seafood watch guides, traditional Japanese sushi etiquette, and coastal stories.",
            scienceIntegration = "Study omega-3 nutrient biochemistry, seaweed kelp iodine benefits, and marine food chain trophic levels.",
            socialStudiesIntegration = "Explore coastal fishing village economies, aquaculture fish farming, and international maritime conservation.",
            interactiveIdea = "Sushi roll maker layering seaweed nori, rice, and avocado with satisfying rolling bamboo mat animation.",
            primaryHex = 0xFF006266,
            secondaryHex = 0xFF1289A7,
            surfaceHex = 0xFFF0F7F9,
            cardHex = 0xFFD2E8EE
        ),

        // ==========================================
        // 5. MUSICAL JOURNEYS & SOUND DESIGN (41-50)
        // ==========================================
        NeuroThemeData(
            id = "musical_journeys",
            title = "Musical Journeys & Symphony Hall",
            category = NeuroThemeCategory.MUSIC_AUDIO_ARTS,
            emoji = "🎻",
            buddyName = "Maestro Tempo",
            buddyRole = "Symphonic Conductor & Composer",
            greeting = "Tuning the strings! Every instrument plays an essential part in our harmony!",
            bestForDiagnoses = listOf("AUDITORY_PROCESSING", "ADHD", "AUTISM_ASD", "DYSLEXIA"),
            bestForStrengths = listOf("Musicality & Auditory Patterns", "Visual Pattern Recognition"),
            remediesStruggles = listOf("Focus & Staying On Task", "Emotional Frustration When Stuck"),
            mathIntegration = "Calculate musical note time signatures (4/4, 3/4, 6/8), fraction beat divisions (quarter, eighth, sixteenth notes).",
            readingIntegration = "Read musical score notation, composer biographies (Beethoven, Mozart, Florence Price), and tempo terms.",
            scienceIntegration = "Analyze acoustic sound wave frequency (Hertz), harmonics, resonance in hollow wooden instrument bodies.",
            socialStudiesIntegration = "Explore orchestral instrument families across world cultures and the evolution of classical music eras.",
            interactiveIdea = "Interactive conductor baton keeping tempo with visual rhythm ripples and orchestral harmony chords.",
            primaryHex = 0xFF4A148C,
            secondaryHex = 0xFF8E24AA,
            surfaceHex = 0xFFF3E5F5,
            cardHex = 0xFFE1BEE7
        ),
        NeuroThemeData(
            id = "synthwave_beat_lab",
            title = "Synthwave & Electronic Beat Lab",
            category = NeuroThemeCategory.MUSIC_AUDIO_ARTS,
            emoji = "🎹",
            buddyName = "DJ Neon-808",
            buddyRole = "Synthesizer Sound Designer",
            greeting = "Drop the beat! Modulating sine waves and bass drops to fuel your learning groove!",
            bestForDiagnoses = listOf("ADHD", "AUTISM_ASD", "DYSCALCULIA"),
            bestForStrengths = listOf("Musicality & Auditory Patterns", "Technical & Digital Aptitude"),
            remediesStruggles = listOf("Focus & Staying On Task", "Task Initiation & Procrastination"),
            mathIntegration = "Calculate tempo BPM (beats per minute), step sequencer 16-step grid math, and audio filter cutoff Hertz.",
            readingIntegration = "Read synthesizer patch manuals, MIDI communication protocols, and electronic music history.",
            scienceIntegration = "Study oscillator waveforms (sine, square, sawtooth, triangle), ADSR envelope filters, and Fourier transforms.",
            socialStudiesIntegration = "Explore the invention of the Moog synthesizer, Roland drum machines, and electronic dance music culture.",
            interactiveIdea = "16-step drum machine grid sequencer toggling kick, snare, hi-hat, and synth notes with glowing neon lights.",
            primaryHex = 0xFF2C003E,
            secondaryHex = 0xFFFE53BB,
            surfaceHex = 0xFFFAF0F8,
            cardHex = 0xFFF3CEEC
        ),
        NeuroThemeData(
            id = "jazz_improvisation",
            title = "Jazz Club & Improvisation Workshop",
            category = NeuroThemeCategory.MUSIC_AUDIO_ARTS,
            emoji = "🎷",
            buddyName = "Saxophone Duke",
            buddyRole = "Jazz Harmony Mentor",
            greeting = "No wrong notes in jazz—only opportunities to resolve with style! Let's swing!",
            bestForDiagnoses = listOf("ADHD", "ANXIETY_STRESS", "GIFTED_2E"),
            bestForStrengths = listOf("Creative Storytelling & Art", "Musicality & Auditory Patterns"),
            remediesStruggles = listOf("Emotional Frustration When Stuck", "Multi-Step Instructions"),
            mathIntegration = "Calculate chord intervals (major 3rds, flatted 5ths, 7ths), syncopated swing rhythms, and blues scale degrees.",
            readingIntegration = "Read rich Harlem Renaissance poetry (Langston Hughes), jazz history memoirs, and scat lyrics.",
            scienceIntegration = "Analyze brass and woodwind acoustic reed vibrations, brass bell acoustic projection, and room reverb.",
            socialStudiesIntegration = "Study the birth of jazz in New Orleans, the Great Migration, and civil rights cultural expressions.",
            interactiveIdea = "Interactive piano keyboard lighting up pentatonic blues scale notes for spontaneous easy improvisation.",
            primaryHex = 0xFFD35400,
            secondaryHex = 0xFFF1C40F,
            surfaceHex = 0xFFFFFDF5,
            cardHex = 0xFFFFEAA7
        ),
        NeuroThemeData(
            id = "world_percussion_safari",
            title = "World Percussion & Global Rhythm Safari",
            category = NeuroThemeCategory.MUSIC_AUDIO_ARTS,
            emoji = "🪘",
            buddyName = "Djembe Master Kofi",
            buddyRole = "Polyrhythm Cultural Master",
            greeting = "Feel the heartbeat of the drum! Polyrhythms connect communities across continents!",
            bestForDiagnoses = listOf("ADHD", "AUDITORY_PROCESSING", "SENSORY_PROCESSING"),
            bestForStrengths = listOf("Musicality & Auditory Patterns", "Empathy & Emotional Insight"),
            remediesStruggles = listOf("Focus & Staying On Task", "Sensory Overload & Noise"),
            mathIntegration = "Calculate polyrhythmic ratios (3 against 2, 4 against 3), drum diameter acoustic pitch relationships.",
            readingIntegration = "Read oral traditional call-and-response folklore lyrics, drum language communication, and poetry.",
            scienceIntegration = "Study membrane tension mechanics, fundamental resonant frequencies, and sound propagation through solids.",
            socialStudiesIntegration = "Learn about African djembe traditions, Latin congas, Middle Eastern darbuka, and Celtic bodhrán drums.",
            interactiveIdea = "Dual-touch polyrhythm pad tapping 3-against-2 rhythmic patterns with synchronized visual pulses.",
            primaryHex = 0xFF8B4513,
            secondaryHex = 0xFFCD853F,
            surfaceHex = 0xFFFFF8F0,
            cardHex = 0xFFFFE4C4
        ),
        NeuroThemeData(
            id = "electronic_dj_acoustics",
            title = "DJ Turntable & Stadium Acoustics",
            category = NeuroThemeCategory.MUSIC_AUDIO_ARTS,
            emoji = "🎧",
            buddyName = "DJ Crossfader",
            buddyRole = "Audio Mixing Engineer",
            greeting = "Cue the track! Match the beats! We are headlining the learning festival!",
            bestForDiagnoses = listOf("ADHD", "AUTISM_ASD", "DYSCALCULIA"),
            bestForStrengths = listOf("Technical & Digital Aptitude", "Musicality & Auditory Patterns"),
            remediesStruggles = listOf("Focus & Staying On Task", "Time Blindness & Transitions"),
            mathIntegration = "Calculate vinyl RPM pitch percentage shifts (+/- 8%), stereo decibel sound pressure levels (dB scale).",
            readingIntegration = "Read live festival sound check checklists, digital audio workstation (DAW) track labels, and reviews.",
            scienceIntegration = "Explore Doppler effect pitch shifts, speaker electromagnetic voice coils, and acoustic damping materials.",
            socialStudiesIntegration = "Trace the invention of hip-hop DJ scratching in the Bronx (DJ Kool Herc, Grandmaster Flash) to world festivals.",
            interactiveIdea = "Interactive turntable jog wheel with real vinyl scratch audio physics and crossfader slider.",
            primaryHex = 0xFF130F40,
            secondaryHex = 0xFF30336B,
            surfaceHex = 0xFFF3F4FB,
            cardHex = 0xFFD5D9F5
        ),
        NeuroThemeData(
            id = "hiphop_lyricism",
            title = "Hip-Hop Cypher & Lyric Rhyme Lab",
            category = NeuroThemeCategory.MUSIC_AUDIO_ARTS,
            emoji = "🎤",
            buddyName = "Lyricist Maya",
            buddyRole = "Rhyme Scheme & Poetics Coach",
            greeting = "Step into the cypher! Rhymes and wordplay turn knowledge into pure poetry!",
            bestForDiagnoses = listOf("DYSLEXIA", "ADHD", "GIFTED_2E"),
            bestForStrengths = listOf("Creative Storytelling & Art", "Musicality & Auditory Patterns"),
            remediesStruggles = listOf("Reading Fluency & Phonics", "Task Initiation & Procrastination"),
            mathIntegration = "Count 16-bar verse syllable meters, internal rhyme scheme grids (AABB, ABAB, multisyllabic rhyming).",
            readingIntegration = "Analyze complex metaphors, alliteration, assonance, double-entendres, and lyrical storytelling.",
            scienceIntegration = "Examine human vocal cord anatomy, resonance in the nasal and chest cavities, and microphone diaphragms.",
            socialStudiesIntegration = "Explore the five pillars of hip-hop culture (MCing, DJing, Breaking, Graffiti, Knowledge) and social justice.",
            interactiveIdea = "Rhyme dictionary puzzle matching multi-syllable phonics words into a 4-bar rhythmic rap verse.",
            primaryHex = 0xFF2C3A47,
            secondaryHex = 0xFFE71C23,
            surfaceHex = 0xFFFFF6F6,
            cardHex = 0xFFFFD7D9
        ),
        NeuroThemeData(
            id = "acoustic_nature_bioacoustics",
            title = "Bio-Acoustics & Nature Soundscapes",
            category = NeuroThemeCategory.MUSIC_AUDIO_ARTS,
            emoji = "🌿",
            buddyName = "Dr. Bio-Song",
            buddyRole = "Ecosystem Bio-Acoustician",
            greeting = "Shhh... listen closely to the forest chorus! Birds, frogs, and whales compose symphony!",
            bestForDiagnoses = listOf("SENSORY_PROCESSING", "AUTISM_ASD", "AUDITORY_PROCESSING"),
            bestForStrengths = listOf("Musicality & Auditory Patterns", "Visual Pattern Recognition"),
            remediesStruggles = listOf("Sensory Overload & Noise", "Emotional Frustration When Stuck"),
            mathIntegration = "Analyze audio spectrogram frequency waterfall charts, cricket chirp temperature formula (Dolbear's law).",
            readingIntegration = "Read naturalist audio field logs, acoustic ecology articles, and whale song sonograms.",
            scienceIntegration = "Study bat echolocation ultrasound (20kHz to 100kHz), elephant infrasound communications, and whale song.",
            socialStudiesIntegration = "Explore international ocean noise pollution regulations and acoustic biodiversity monitoring.",
            interactiveIdea = "Interactive sound spectrogram visualizer matching wildlife calls (whale, owl, wolf) to spectrogram shapes.",
            primaryHex = 0xFF1B4D3E,
            secondaryHex = 0xFF52B788,
            surfaceHex = 0xFFF1F8F4,
            cardHex = 0xFFD8F3DC
        ),
        NeuroThemeData(
            id = "film_score_composer",
            title = "Cinematic Film Score & Orchestral Studio",
            category = NeuroThemeCategory.MUSIC_AUDIO_ARTS,
            emoji = "🎬",
            buddyName = "Composer Maestro Leo",
            buddyRole = "Hollywood Film Scoring Mentor",
            greeting = "Lights, camera, music! Set the emotional mood of epic adventures through sound!",
            bestForDiagnoses = listOf("ADHD", "AUTISM_ASD", "GIFTED_2E"),
            bestForStrengths = listOf("Creative Storytelling & Art", "Musicality & Auditory Patterns"),
            remediesStruggles = listOf("Focus & Staying On Task", "Emotional Frustration When Stuck"),
            mathIntegration = "Calculate film frame rates (24 fps), audio sync timecode (SMPTE HH:MM:SS:FF), and tempo-to-hit points.",
            readingIntegration = "Read movie screenplay scripts, emotional character cue sheets, and storyboards.",
            scienceIntegration = "Analyze psychoacoustics (how minor chords evoke suspense, major brass chords evoke heroism), spatial stereo.",
            socialStudiesIntegration = "Explore the history of cinema music from silent film piano accompanists to modern blockbuster composers.",
            interactiveIdea = "Movie scene mood slider switching soundtrack orchestration from suspenseful strings to triumphant brass.",
            primaryHex = 0xFF2C3E50,
            secondaryHex = 0xFFE67E22,
            surfaceHex = 0xFFF8F9FA,
            cardHex = 0xFFE9ECEF
        ),
        NeuroThemeData(
            id = "chiptune_8bit_audio",
            title = "Chiptune & 8-Bit Game Audio Studio",
            category = NeuroThemeCategory.MUSIC_AUDIO_ARTS,
            emoji = "👾",
            buddyName = "Bit-Tracker Bob",
            buddyRole = "8-Bit Sound Synthesist",
            greeting = "4 audio channels, infinite possibilities! Let's craft iconic game melodies!",
            bestForDiagnoses = listOf("ADHD", "AUTISM_ASD", "DYSCALCULIA"),
            bestForStrengths = listOf("Technical & Digital Aptitude", "Musicality & Auditory Patterns"),
            remediesStruggles = listOf("Task Initiation & Procrastination", "Working Memory"),
            mathIntegration = "Calculate 4-channel audio allocation (Pulse 1, Pulse 2, Triangle Bass, Noise Drums), arpeggio pitch speed.",
            readingIntegration = "Read vintage computer hardware chip manuals, tracker instruction codes, and game audio design journals.",
            scienceIntegration = "Study digital sound quantization (8-bit 256 amplitude levels), square wave duty cycle pulse widths (12.5%, 25%, 50%).",
            socialStudiesIntegration = "Explore how computer memory limitations in the 1980s inspired creative musical masterpieces (Koji Kondo).",
            interactiveIdea = "4-channel chiptune mixer muting and soloing pulse, triangle, and noise tracks to create classic game jingles.",
            primaryHex = 0xFF0D0D0D,
            secondaryHex = 0xFF00FFCC,
            surfaceHex = 0xFFF0FDFB,
            cardHex = 0xFFD0FBF3
        ),
        NeuroThemeData(
            id = "opera_theatre_soundscape",
            title = "Opera House & Theatrical Acoustics",
            category = NeuroThemeCategory.MUSIC_AUDIO_ARTS,
            emoji = "🎭",
            buddyName = "Diva Aria",
            buddyRole = "Dramatic Vocal Coach",
            greeting = "Curtain rises! Fill the grand hall with expressive voice and storytelling!",
            bestForDiagnoses = listOf("DYSLEXIA", "SENSORY_PROCESSING", "ANXIETY_STRESS"),
            bestForStrengths = listOf("Creative Storytelling & Art", "Empathy & Emotional Insight"),
            remediesStruggles = listOf("Reading Fluency & Phonics", "Emotional Frustration When Stuck"),
            mathIntegration = "Calculate amphitheater seating parabolic sound reflections, vocal range frequencies (Soprano to Bass).",
            readingIntegration = "Read opera libretto dramatic scripts, Italian stage terminology (forte, piano, allegro), and character monologues.",
            scienceIntegration = "Analyze formant frequencies in singing, diaphragmatic breathing resonance, and stage lighting color physics.",
            socialStudiesIntegration = "Trace theater architecture from ancient Greek amphitheaters to modern Sydney Opera House.",
            interactiveIdea = "Auditorium acoustic mirror simulator angling reflective ceiling panels to send sound evenly to the back balcony.",
            primaryHex = 0xFF800020,
            secondaryHex = 0xFFD4AF37,
            surfaceHex = 0xFFFFFDF8,
            cardHex = 0xFFFFEFC7
        )
    ) + NEURO_THEMES_PART_2

    // Helper functions for retrieval and rotation
    fun getAllThemes(): List<NeuroThemeData> = ALL_100_THEMES

    fun findThemeById(id: String): NeuroThemeData {
        return ALL_100_THEMES.find { it.id.equals(id, ignoreCase = true) }
            ?: ALL_100_THEMES.first()
    }

    fun getRecommendedThemesForProfile(profile: ChildProfileEntity, limit: Int = 6): List<NeuroThemeData> {
        val userDiagnoses = profile.neurodivergentTypesCsv.split(",").map { it.trim().uppercase() }.filter { it.isNotBlank() }
        val userStrengths = profile.strengthsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val userStruggles = profile.strugglesCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val userFixations = profile.hyperFixationsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val ageTier = AgeGroupTier.values().find { it.id == profile.ageGroupTier } ?: AgeGroupTier.ELEMENTARY

        val scored = ALL_100_THEMES.map { theme ->
            var score = 0

            // Match age tier
            if (theme.targetAgeTiers.contains(ageTier)) {
                score += 5
            }

            // Match diagnoses
            theme.bestForDiagnoses.forEach { diag ->
                if (userDiagnoses.any { it.contains(diag, ignoreCase = true) || diag.contains(it, ignoreCase = true) }) {
                    score += 8
                }
            }

            // Match strengths
            theme.bestForStrengths.forEach { str ->
                if (userStrengths.any { it.contains(str, ignoreCase = true) || str.contains(it, ignoreCase = true) }) {
                    score += 6
                }
            }

            // Match struggles
            theme.remediesStruggles.forEach { stg ->
                if (userStruggles.any { it.contains(stg, ignoreCase = true) || stg.contains(it, ignoreCase = true) }) {
                    score += 5
                }
            }

            // Match hyperfixations
            userFixations.forEach { fix ->
                if (theme.title.contains(fix, ignoreCase = true) ||
                    theme.mathIntegration.contains(fix, ignoreCase = true) ||
                    theme.scienceIntegration.contains(fix, ignoreCase = true) ||
                    theme.category.title.contains(fix, ignoreCase = true)
                ) {
                    score += 15
                }
            }

            theme to score
        }

        return scored.sortedByDescending { it.second }.map { it.first }.take(limit)
    }

    fun getNextRotatedTheme(profile: ChildProfileEntity): NeuroThemeData {
        val recommended = getRecommendedThemesForProfile(profile, limit = 15)
        val currentIndex = recommended.indexOfFirst { it.id == profile.activeThemeId }
        val nextIndex = if (currentIndex >= 0 && currentIndex < recommended.size - 1) {
            currentIndex + 1
        } else {
            0
        }
        return recommended[nextIndex]
    }

    fun filterThemes(
        category: NeuroThemeCategory? = null,
        query: String? = null,
        ageTier: AgeGroupTier? = null,
        diagnosis: String? = null
    ): List<NeuroThemeData> {
        return ALL_100_THEMES.filter { theme ->
            val matchCat = category == null || theme.category == category
            val matchQuery = query.isNullOrBlank() ||
                    theme.title.contains(query, ignoreCase = true) ||
                    theme.buddyName.contains(query, ignoreCase = true) ||
                    theme.buddyRole.contains(query, ignoreCase = true) ||
                    theme.mathIntegration.contains(query, ignoreCase = true) ||
                    theme.scienceIntegration.contains(query, ignoreCase = true) ||
                    theme.readingIntegration.contains(query, ignoreCase = true)

            val matchAge = ageTier == null || theme.targetAgeTiers.contains(ageTier)
            val matchDiag = diagnosis.isNullOrBlank() || theme.bestForDiagnoses.any { it.contains(diagnosis, ignoreCase = true) }

            matchCat && matchQuery && matchAge && matchDiag
        }
    }
}
