package com.example.data.model

data class ConceptStep(
    val stepNumber: Int,
    val title: String,
    val text: String,
    val visualEmoji: String,
    val tipOrFunFact: String,
    val interactivePrompt: String? = null,
    val interactiveAnswers: List<String>? = null,
    val interactiveCorrectIndex: Int? = null
)

data class QuestionItem(
    val id: Int,
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int,
    val hint: String,
    val growthMindsetExplanation: String,
    val themeVariantText: String? = null,
    val visualAidEmoji: String = "✨"
)

data class FullLesson(
    val id: String,
    val subject: EducationalSubject,
    val gradeLevel: GradeLevel,
    val stateStandardCode: String,
    val standardDescription: String,
    val title: String,
    val summary: String,
    val themeWorldId: String,
    val teachSteps: List<ConceptStep>,
    val questions: List<QuestionItem>
)

enum class AvatarCategory(val id: String, val title: String) {
    AVATAR("AVATAR", "Avatars"),
    HAT("HAT", "Hats & Gear"),
    PET("PET", "Companions"),
    BADGE("BADGE", "Badges")
}

data class AvatarItem(
    val id: String,
    val name: String,
    val emoji: String,
    val category: AvatarCategory,
    val starCost: Int,
    val gemCost: Int,
    val description: String,
    val isUnlockedDefault: Boolean = false
)

val DEFAULT_AVATAR_SHOP_ITEMS = listOf(
    AvatarItem("av_robot", "Robo Explorer", "🤖", AvatarCategory.AVATAR, 0, 0, "A friendly learning bot", true),
    AvatarItem("av_dino", "Spike Dino", "🦖", AvatarCategory.AVATAR, 5, 1, "A curious baby dinosaur"),
    AvatarItem("av_astronaut", "Astro Hero", "👨‍🚀", AvatarCategory.AVATAR, 10, 2, "Reaches for the stars"),
    AvatarItem("av_wizard", "Arch-Mage", "🧙‍♂️", AvatarCategory.AVATAR, 15, 3, "Master of wisdom spells"),
    AvatarItem("av_ocean", "Ocean Diver", "🤿", AvatarCategory.AVATAR, 20, 4, "Explores ocean depths"),
    AvatarItem("av_unicorn", "Star Unicorn", "🦄", AvatarCategory.AVATAR, 25, 5, "Brings magical joy"),
    AvatarItem("av_superhero", "Captain Mind", "🦸‍♀️", AvatarCategory.AVATAR, 30, 6, "Defends knowledge"),
    AvatarItem("av_phoenix", "Solar Phoenix", "🦅", AvatarCategory.AVATAR, 28, 5, "Legendary bird of rebirth and light"),
    AvatarItem("av_dragon_knight", "Drake Knight", "🛡️", AvatarCategory.AVATAR, 35, 7, "Protector of ancient folklore lore"),
    AvatarItem("av_fairy_queen", "Faerie Queen", "🧚‍♀️", AvatarCategory.AVATAR, 22, 4, "Guardian of the enchanted forest"),
    
    // Accessories
    AvatarItem("hat_crown", "Royal Gold Crown", "👑", AvatarCategory.HAT, 8, 1, "Wearable achievement crown"),
    AvatarItem("hat_space_helmet", "Cosmic Helmet", "🪖", AvatarCategory.HAT, 12, 2, "Space exploration gear"),
    AvatarItem("hat_wizard_hat", "Star Wizard Hat", "🎩", AvatarCategory.HAT, 10, 1, "Infused with starry glow"),
    AvatarItem("hat_sunglasses", "Cool Shades", "🕶️", AvatarCategory.HAT, 6, 1, "Sensory-friendly eye shades"),
    AvatarItem("hat_dragon_horns", "Mystic Dragon Horns", "✨", AvatarCategory.HAT, 14, 2, "Glows with ancient fire magic"),
    AvatarItem("hat_phoenix_crown", "Phoenix Feather Crown", "🪶", AvatarCategory.HAT, 16, 3, "Woven from radiant plumage"),
    
    // Pets
    AvatarItem("pet_dragon", "Baby Dragon", "🐉", AvatarCategory.PET, 15, 3, "Loyal study companion"),
    AvatarItem("pet_cat", "Cosmic Kitten", "🐱", AvatarCategory.PET, 10, 2, "Purrs during quiet focus"),
    AvatarItem("pet_dog", "Galaxy Puppy", "🐶", AvatarCategory.PET, 10, 2, "Wags tail on every win"),
    AvatarItem("pet_owl", "Professor Owl", "🦉", AvatarCategory.PET, 18, 4, "Wise nocturnal mentor"),
    AvatarItem("pet_kitsune", "Nine-Tailed Fox", "🦊", AvatarCategory.PET, 20, 4, "Clever mythical folklore spirit"),
    AvatarItem("pet_pegasus", "Mini Pegasus", "🦄", AvatarCategory.PET, 22, 4, "Soars across cloud realms"),
    
    // Badges
    AvatarItem("badge_streak", "Focus Champion", "🏆", AvatarCategory.BADGE, 10, 2, "Awarded for consistent learning"),
    AvatarItem("badge_mindful", "Zen Master", "🧘", AvatarCategory.BADGE, 8, 1, "Master of calm breathing"),
    AvatarItem("badge_dino_expert", "Dino Fossil Medal", "🦴", AvatarCategory.BADGE, 12, 2, "Master of prehistory"),
    AvatarItem("badge_lorekeeper", "Folklore Lorekeeper", "📜", AvatarCategory.BADGE, 15, 3, "Master of ancient legends"),
    AvatarItem("badge_ocean_master", "Deep Sea Explorer", "🪸", AvatarCategory.BADGE, 12, 2, "Master of ocean science")
)
