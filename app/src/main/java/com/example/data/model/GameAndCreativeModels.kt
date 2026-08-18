package com.example.data.model

data class MarineVocabWord(
    val word: String,
    val definition: String,
    val emoji: String,
    val funFact: String
)

data class ClozeSentence(
    val id: Int,
    val textBeforeBlank: String,
    val correctWord: String,
    val textAfterBlank: String,
    val hint: String,
    val explanation: String,
    val visualEmoji: String
)

data class OceanReadingPassage(
    val id: String,
    val title: String,
    val subtitle: String,
    val scienceStandard: String,
    val passageText: String,
    val vocabulary: List<MarineVocabWord>,
    val clozeSentences: List<ClozeSentence>,
    val wordSearchWords: List<String>,
    val letterGrid: List<String>
)

val DEFAULT_OCEAN_PASSAGES = listOf(
    OceanReadingPassage(
        id = "coral_reef_ecosystem",
        title = "The Vibrant Coral Reef Ecosystem",
        subtitle = "Marine Biology & Biodiversity in Shallow Seas",
        scienceStandard = "NGSS.4-LS1-1 / CCSS.ELA-LITERACY.RI.3.4",
        passageText = "Beneath the sunlit waves lies a bustling marine community called a coral reef. Coral reefs are often called the underwater rainforests of the ocean because they provide shelter, food, and nursery grounds for millions of sea creatures. Corals might look like colorful rocks, but they are actually tiny living animals that build hard limestone skeletons. Within this delicate ecosystem, clownfish hide safely inside stinging sea anemone tentacles, sea turtles graze on nutritious seagrass, and schools of silver fish swim in tight coordination to confuse hungry predators. However, coral reefs are sensitive to water temperature. Protecting clean oceans ensures these glowing underwater kingdoms thrive for generations to come.",
        vocabulary = listOf(
            MarineVocabWord("CORAL", "Small marine animals that form hard limestone reef structures", "🪸", "Corals share a partnership with microscopic algae that give them bright colors!"),
            MarineVocabWord("ECOSYSTEM", "A biological community of interacting organisms and their physical environment", "🌊", "Reefs cover less than 1% of the ocean floor but support 25% of all sea life!"),
            MarineVocabWord("TENTACLES", "Flexible arm-like limbs used by sea creatures for grabbing food and defense", "🐙", "Sea anemone tentacles have special stingers that don't hurt clownfish due to their protective mucus!"),
            MarineVocabWord("PREDATOR", "An animal that naturally hunts and feeds on other animals", "🦈", "Sharks are apex predators that keep the ocean food web healthy and balanced."),
            MarineVocabWord("CAMOUFLAGE", "Special colors or patterns that disguise an animal in its environment", "🐠", "The mimic octopus can change its skin color and texture in milliseconds!"),
            MarineVocabWord("HABITAT", "The natural home or environment of a plant, animal, or organism", "🏝️", "Seagrass meadows provide a crucial nursery habitat for baby sea turtles.")
        ),
        clozeSentences = listOf(
            ClozeSentence(
                id = 1,
                textBeforeBlank = "A coral reef is a thriving marine ",
                correctWord = "ECOSYSTEM",
                textAfterBlank = " where thousands of sea creatures live and interact.",
                hint = "Think of a community where living things depend on one another.",
                explanation = "An ecosystem is an interconnected community of organisms living together in harmony.",
                visualEmoji = "🌊"
            ),
            ClozeSentence(
                id = 2,
                textBeforeBlank = "Tiny colonial animals called ",
                correctWord = "CORAL",
                textAfterBlank = " build hard limestone structures that form underwater reefs.",
                hint = "They look like stony plants, but they are living creatures.",
                explanation = "Corals are invertebrate animals that construct the foundation of reefs.",
                visualEmoji = "🪸"
            ),
            ClozeSentence(
                id = 3,
                textBeforeBlank = "Clownfish find safe shelter among the stinging ",
                correctWord = "TENTACLES",
                textAfterBlank = " of the sea anemone.",
                hint = "Flexible wavy arms that protect against bigger fish.",
                explanation = "Tentacles help anemones catch prey and protect clownfish friends.",
                visualEmoji = "🐙"
            ),
            ClozeSentence(
                id = 4,
                textBeforeBlank = "Sharks act as a top ocean ",
                correctWord = "PREDATOR",
                textAfterBlank = " by maintaining balance in the marine food chain.",
                hint = "An animal that hunts other animals to survive.",
                explanation = "Predators keep prey populations healthy and balanced in marine food webs.",
                visualEmoji = "🦈"
            ),
            ClozeSentence(
                id = 5,
                textBeforeBlank = "Many reef fish use color and pattern ",
                correctWord = "CAMOUFLAGE",
                textAfterBlank = " to blend smoothly into coral crevices.",
                hint = "The art of hiding in plain sight by matching surroundings.",
                explanation = "Camouflage helps creatures hide from danger or surprise food.",
                visualEmoji = "🐠"
            ),
            ClozeSentence(
                id = 6,
                textBeforeBlank = "Clean water and sunlight are necessary to protect each sea creature's natural ",
                correctWord = "HABITAT",
                textAfterBlank = " from pollution.",
                hint = "The scientific word for a creature's natural home.",
                explanation = "A habitat supplies food, water, shelter, and space for species to thrive.",
                visualEmoji = "🏝️"
            )
        ),
        wordSearchWords = listOf("CORAL", "REEF", "OCEAN", "SHARK", "TURTLE", "HABITAT"),
        letterGrid = listOf(
            "C O R A L Q R E",
            "H O C E A N E E",
            "A S H A R K E F",
            "B T U R T L E T",
            "I F I S H M I I",
            "T E N T A C L E",
            "A L G A E B O N",
            "T I D E P O O L"
        )
    ),
    OceanReadingPassage(
        id = "deep_sea_bioluminescence",
        title = "Midnight Abyss: Creatures of Bioluminescence",
        subtitle = "Deep Ocean Zones & Light Production",
        scienceStandard = "NGSS.5-PS4-2 / CCSS.ELA-LITERACY.RI.4.7",
        passageText = "Thousands of meters below the sunny ocean surface, the water is pitch black and icy cold. This mysterious layer is called the midnight zone. Because sunlight cannot reach these tremendous depths, deep-sea creatures have developed an extraordinary adaptation called bioluminescence. Bioluminescence is the ability of living organisms to produce their own glowing light through a chemical reaction. Anglerfish dangle a glowing lure like a lantern to attract curious prey in the dark. Comb jellies flash radiant rainbow pulses along their swimming cilia to startle hungry hunters. At the bottom of trenches, hydrothermal vents spew mineral-rich hot water, supporting bizarre tube worms and ghost crabs that thrive without ever seeing the sun.",
        vocabulary = listOf(
            MarineVocabWord("BIOLUMINESCENCE", "Light produced by a chemical reaction inside a living organism", "💡", "Over 75% of deep-sea creatures can produce their own living glow!"),
            MarineVocabWord("TRENCH", "A very deep, narrow steep-sided valley in the ocean floor", "⛰️", "The Mariana Trench is deeper than Mount Everest is tall!"),
            MarineVocabWord("PLANKTON", "Microscopic drifting organisms that form the base of ocean food webs", "🦐", "Phytoplankton produce more than 50% of the oxygen we breathe on Earth!"),
            MarineVocabWord("ADAPTATION", "A trait or body feature that helps an organism survive in its environment", "🧬", "Deep sea fish often have gigantic eyes or extra flexible stomachs to swallow rare meals.")
        ),
        clozeSentences = listOf(
            ClozeSentence(
                id = 1,
                textBeforeBlank = "The ability of creatures to create their own glow in the dark is called ",
                correctWord = "BIOLUMINESCENCE",
                textAfterBlank = ".",
                hint = "Bio (life) + luminescence (glowing light).",
                explanation = "Bioluminescence allows deep sea animals to see, signal mates, and lure food in dark waters.",
                visualEmoji = "💡"
            ),
            ClozeSentence(
                id = 2,
                textBeforeBlank = "The deepest parts of the seafloor are known as an ocean ",
                correctWord = "TRENCH",
                textAfterBlank = ", plunging miles into darkness.",
                hint = "A deep underwater canyon.",
                explanation = "Ocean trenches are formed where tectonic plates collide and dive beneath each other.",
                visualEmoji = "⛰️"
            ),
            ClozeSentence(
                id = 3,
                textBeforeBlank = "Tiny drifting organisms called ",
                correctWord = "PLANKTON",
                textAfterBlank = " float on ocean currents and nourish small fish.",
                hint = "Microscopic water drifters.",
                explanation = "Plankton is the foundation of marine nourishment across all ocean zones.",
                visualEmoji = "🦐"
            ),
            ClozeSentence(
                id = 4,
                textBeforeBlank = "Glowing lures and gigantic eyes are examples of a survival ",
                correctWord = "ADAPTATION",
                textAfterBlank = " for the deep ocean.",
                hint = "A special physical feature that helps animals survive difficult conditions.",
                explanation = "Adaptations evolve over generations to suit specific habitats and pressures.",
                visualEmoji = "🧬"
            )
        ),
        wordSearchWords = listOf("GLOW", "TRENCH", "ABYSS", "JELLY", "VENT", "PLANKTON"),
        letterGrid = listOf(
            "G L O W Q A B Y",
            "T R E N C H Y S",
            "J E L L Y V S S",
            "V E N T P E S Z",
            "P L A N K T O N",
            "B I O M E U M X",
            "F I S H R A Y C",
            "D E E P S E A S"
        )
    )
)

data class ThemeStoryPrompt(
    val id: String,
    val themeId: String,
    val title: String,
    val promptStarter: String,
    val emoji: String,
    val suggestedStickers: List<String>
)

val DEFAULT_STORY_PROMPTS = listOf(
    ThemeStoryPrompt(
        id = "myth_dragon_mountain",
        themeId = "mythical",
        title = "The Friendly Fire-Drake of Sun Peak",
        promptStarter = "High atop Mount Solara, where the clouds sparkled with golden mist, a young dragon named Ignis discovered a lost ancient crystal that whispered secrets of...",
        emoji = "🐉",
        suggestedStickers = listOf("🐉", "🦅", "🦄", "🏰", "✨", "🔥", "📜", "👑")
    ),
    ThemeStoryPrompt(
        id = "myth_griffin_flight",
        themeId = "mythical",
        title = "Flight of the Golden Griffin",
        promptStarter = "With feathers of pure amber and the courage of a lion, Zephyr the Griffin spread wide wings to protect the hidden forest village from...",
        emoji = "🦅",
        suggestedStickers = listOf("🦅", "🧚‍♀️", "🌲", "✨", "🏹", "🛡️", "🌟", "🦊")
    ),
    ThemeStoryPrompt(
        id = "ocean_bioluminescent_dive",
        themeId = "ocean",
        title = "Journey to the Glowing Coral Cave",
        promptStarter = "Deep beneath the sparkling turquoise waves, Marina the curious sea turtle found a secret underwater tunnel that led to a magical reef where...",
        emoji = "🐬",
        suggestedStickers = listOf("🐬", "🪸", "🐠", "🐙", "🦈", "🫧", "💎", "🏝️")
    ),
    ThemeStoryPrompt(
        id = "space_cosmic_discovery",
        themeId = "space",
        title = "Mission: Planet Stardust",
        promptStarter = "Commander Leo piloted the star-cruiser past rings of purple ice. The radar beeped excitedly as an uncharted glowing planetary base appeared...",
        emoji = "🚀",
        suggestedStickers = listOf("🚀", "👨‍🚀", "🪐", "⭐", "🛸", "👾", "✨", "🛰️")
    ),
    ThemeStoryPrompt(
        id = "dino_secret_valley",
        themeId = "dino",
        title = "The Secret of Dinosaur Canyon",
        promptStarter = "Walking through giant ferns twice as tall as houses, Spike the Triceratops spotted footprint tracks that led toward a sparkling crystal waterfall where...",
        emoji = "🦖",
        suggestedStickers = listOf("🦖", "🦕", "🌴", "🦴", "🌋", "🥚", "🌿", "⭐")
    )
)

data class PlacedSticker(
    val id: String,
    val emoji: String,
    val xRatio: Float,
    val yRatio: Float
)
