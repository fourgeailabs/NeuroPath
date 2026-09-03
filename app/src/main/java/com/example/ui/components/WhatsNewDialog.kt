package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

data class ReleaseNote(
    val version: String,
    val date: String,
    val title: String,
    val highlights: List<String>,
    val isCurrentVersion: Boolean = false
)

val HISTORICAL_RELEASE_NOTES = listOf(
    ReleaseNote(
        version = "v1.14.00",
        date = "Current Update",
        title = "Clean Sizing, Chip & Pill Formatting Standardization and UI Alignment Polish",
        highlights = listOf(
            "📐 Fixed Theme Card Category Pill Squeezing: Redesigned the Active Theme Spotlight Card and Theme Catalog items with dedicated header rows, preventing vertical text squishing and text wrapping on longer theme titles.",
            "🎨 Standardized Top Header & Action Controls: Re-architected the NeuroBuddy chat header with clean, unclipped Gemini model pills (Flash Lite, 3.5 Flash, 3.1 Pro, Offline), balanced touch targets, and balanced buddy subtitle layouts.",
            "💬 Refined Chat Input Controls: Streamlined the bottom educational input bar with single-to-multi-line adaptive sizing, balanced 42dp action buttons, and concise placeholder prompts for optimal ergonomics.",
            "✨ Enhanced Follow-up & Suggestion Chips: Balanced font metrics, corner radii, and padding across all educational suggestion pills and cross-curricular subject badges.",
            "🛡️ High-Contrast Theme Card Contrast: Enhanced text contrast across all light and vibrant theme palette cards with dark, readable typography paired with theme-accented metadata badges."
        ),
        isCurrentVersion = true
    ),
    ReleaseNote(
        version = "v1.13.00",
        date = "Previous Update",
        title = "Fixed Gemini AI Architecture, camelCase REST Serialization & API Key Resolution",
        highlights = listOf(
            "🧠 Fixed Gemini REST API Serialization: Resolved JSON payload formatting by aligning all request/response models with Google Generative Language API camelCase specifications (generationConfig, systemInstruction, inlineData, etc.).",
            "⚡ Restored Multi-Tier AI Models: Seamlessly connected gemini-3.5-flash, gemini-3.1-flash-lite-preview, and gemini-3.1-pro-preview for lightning-fast educational explanations, adaptive hints, voice interactions, and deep reasoning.",
            "🔑 Enhanced Multi-Source API Key Resolution: Intelligently resolves API credentials across AI Studio runtime secrets, profile configurations, and custom keys with seamless offline Socratic fallback.",
            "🎙️ Restored Audio Transcription & Live Buddy Voice: Fixed endpoint model routing for voice transcription and interactive live voice study sessions.",
            "🛡️ Resilient Network Error Diagnostics: Added enhanced HTTP status handling and diagnostic feedback across all learning modules and parental controls."
        ),
        isCurrentVersion = false
    ),
    ReleaseNote(
        version = "v1.12.00",
        date = "Previous Update",
        title = "Interactive Theme Preview Modal, Palette Inspector & Background Atmosphere Simulation",
        highlights = listOf(
            "🎨 Interactive Theme Preview Modal: Inspect and visualize any selected theme world's vibrant color palette, companion buddy, and ambient background atmosphere before applying globally.",
            "📱 Live Screen Simulation View: Real-time interactive simulation showing how the child will experience the theme's ambient canvas gradient mesh, sensory header bar, buddy greeting card, and themed quest missions.",
            "🔬 Color Palette & WCAG Contrast Inspector: In-depth token breakdown of Primary, Secondary, Surface, and Card container hex colors with AAA accessibility contrast ratings and sample live UI buttons.",
            "📚 Companion Buddy & Subject Integration: Preview the companion character's role, dialogue, and tailored Mathematics, Reading, Science, and Social Studies cross-disciplinary connections.",
            "🔄 Quick Carousel & 100-Theme Switcher: Effortlessly cycle through all 100 theme worlds or search by topic within the preview modal, with one-tap global application and rotation scheduling."
        ),
        isCurrentVersion = false
    ),
    ReleaseNote(
        version = "v1.11.00",
        date = "Previous Update",
        title = "100 Adaptive Neuro-Themes, Rotation Schedule Engine & OER Video/Audio Modules",
        highlights = listOf(
            "🎨 100 Adaptive Neuro-Themes: Comprehensive library of 100 immersive theme worlds covering Pre-K through 12th Grade (Ancient Civilizations, Robotics & AI, Mythological Creatures, Culinary Adventures, Musical Journeys, Sports Superstars, Environmental Explorers, Artistic Expression, Transportation Tycoons, Spy Academy, Deep Space, Medical Science, Architecture, Gaming, etc.).",
            "🔄 Profile-Based Theme Rotation Engine: Configure permanent theme worlds or automatic periodic rotation (Daily, Every 3 Days, Weekly, Bi-Weekly, Monthly) tailored to child diagnoses, strengths, struggles, and hyper-fixations.",
            "📚 Full 100-Theme Catalog Browser: In-depth browser with category filters, keyword search, companion buddy profiles, and cross-disciplinary subject mapping (Math, Reading, Science, Social Studies).",
            "🎬 OER Commons Video & Audio Playback: Full-featured multimedia playback modules with interactive checkpoints, live synchronized transcripts, playback speed control (0.75x - 1.5x), and full accessibility accommodations.",
            "🛠️ Seamless Profile Integration: Theme worlds grow dynamically with the child's age tier and curriculum milestones."
        ),
        isCurrentVersion = false
    ),
    ReleaseNote(
        version = "v1.08.00",
        date = "Previous Update",
        title = "Rebuilt Gemini Chat Interface, Educational Personalization & Room Message History",
        highlights = listOf(
            "🤖 Free Model Gemini Integration: Fully leverages Gemini 3.5 Flash & Gemini 3.1 Flash Lite free-tier models with zero-cost educational tutoring and automatic offline Socratic fallback.",
            "🎓 Personalized Explanation Modes: One-tap switching between Step-by-Step (Socratic), Simpler Analogy (ELI5), Visual Breakdown, Deep Concept, and Direct Solution.",
            "🗄️ Persistent Room Message History: Full local database storage for multi-topic conversations, past session browsing, and topic management.",
            "🔍 Keyword Search & Study Bookmarks: Instant search across past explanations and one-tap bookmarking for study notes.",
            "💡 Message Transformation Actions: Instant 'Explain Simpler' and 'Step-by-Step' transformers, TTS speech playback, copy clipboard, and dynamic follow-up chips."
        ),
        isCurrentVersion = false
    ),
    ReleaseNote(
        version = "v1.07.00",
        date = "Previous Update",
        title = "4-7-8 Breathing Exercise Visualizer & Dynamic Rhythmic Feedback",
        highlights = listOf(
            "🧘 Animated 4-7-8 Breathing Visualizer: Built a dedicated multi-phase visualizer component featuring expanding lotus blossom petals, concentric fluid wave ripples, and cosmic orbital spheres on Jetpack Compose Canvas.",
            "⏱️ Guided Rhythmic Feedback: Real-time countdown HUD with color transitions (Teal Inhale 4s, Golden Amber Hold 7s, Violet Exhale 8s) and smooth circular progress arc.",
            "📊 Segmented Phase Timeline: Dynamic horizontal rhythm timeline tracking exact elapsed progress per breath phase.",
            "🎶 Ambient Soundscapes & Vagus Regulation: Built-in background soundscapes (Rain, Ocean Swells, Forest Breeze) with live Vagus Nerve Calm Index tracking and sensory affirmations.",
            "⚙️ Interactive Session Controls: Customize target cycles (3, 4, 8), switch visual rendering modes, pause/resume, and reset with celebratory completion rewards."
        ),
        isCurrentVersion = false
    ),

    ReleaseNote(
        version = "v1.06.00",
        date = "Previous Update",
        title = "Pre-Installed OER Commons K-12 Collection & AI Tutor Retrieval",
        highlights = listOf(
            "📚 Pre-Installed OER Commons K-12 Collection: Integrated curated open educational resources pre-installed and cached locally in Room Database covering Kindergarten through Grade 12 (Math, ELA, Sciences, Social Studies & Civics).",
            "🔍 Online & Offline OER Service: Real-time synchronization and offline fallback parser for oercommons.org curated collections.",
            "🧠 Curriculum-Aware AI Tutor: Gemini AI tutor and Voice Assist automatically query OER Commons materials to tailor explanations and practice to exact grade benchmarks in all 21 supported languages."
        ),
        isCurrentVersion = false
    ),
    ReleaseNote(
        version = "v1.05.00",
        date = "Previous Update",
        title = "21-Language Global Localization & App Auto-Update",
        highlights = listOf(
            "🌐 21-Language Global Localization: Complete end-to-end multi-language dictionary across English (US/UK), Spanish, French, German, Mandarin, Japanese, Korean, Portuguese, Italian, Dutch, Swedish, Russian, Turkish, Polish, Greek, Vietnamese, Thai, Indonesian, Hindi, and Arabic.",
            "⚡ Instant Reactive Switching: Switching languages in Setup, Parent Settings, or Dashboard immediately adapts every UI screen, dialog, button, and educational instruction.",
            "🔄 GitHub Actions Auto-Updater: Real-time update checking with skip and remind-later capabilities."
        ),
        isCurrentVersion = false
    ),
    ReleaseNote(
        version = "v1.02.00",
        date = "Previous Update",
        title = "Speech-to-Text & Granular Global School District Alignment",
        highlights = listOf(
            "🎙️ Speech-to-Text (STT) Integration: Speak naturally into your microphone using free Gemini API online speech processing.",
            "🏛️ Granular Educational Requirements: Select Country, State/Province, City, and School District to align K-12 standards precisely with local district curricula.",
            "🌐 Baked-In Global Language Selector: Full native language support across 10 global languages (English, Spanish, French, German, Mandarin, Japanese, Portuguese, Hindi, Arabic, Italian).",
            "🔊 Dual TTS & STT Voice Assist: Seamless voice interactions for speech synthesis and voice input across all learning modules."
        ),
        isCurrentVersion = false
    ),
    ReleaseNote(
        version = "v1.01.00",
        date = "Previous Update",
        title = "Self-Healing Build Pipeline & Sensory Accommodation Enhancements",
        highlights = listOf(
            "⚡ Automatic Keystore Decoder: Added self-healing Gradle base64 keystore decoding for Android CI/CD pipelines.",
            "🧘 Enhanced 4-7-8 Breathing Guide & Sensory Tools: Added visual pacing rings and haptic pop-it fidget feedback.",
            "📖 OpenDyslexic Typography: Improved letter-spacing and base weighting for dyslexia accessibility."
        )
    ),
    ReleaseNote(
        version = "v1.00.00",
        date = "Initial Launch",
        title = "Initial Release of NeuroPath Learning Assistant",
        highlights = listOf(
            "🧠 Neurodiversity-First K-12 Curriculum: Adaptive Math, Reading, Science, and SEL modules.",
            "🦖 Special Interest Themes: Dinosaur, Outer Space, Ocean, Fantasy, and Robot themes.",
            "🛡️ 100% Offline Resilience & COPPA Privacy Guarantee."
        )
    )
)

@Composable
fun WhatsNewDialog(
    onDismiss: () -> Unit
) {
    // Only one drop down open at a time, starts closed (-1)
    var expandedIndex by remember { mutableStateOf<Int?>(-1) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.NewReleases, contentDescription = "What's New", tint = MaterialTheme.colorScheme.primary)
                        Text(
                            "What's New in NeuroPath",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    "Explore the latest update highlights and historical version notes below. Click any release to expand or collapse details.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(HISTORICAL_RELEASE_NOTES) { index, note ->
                        val isExpanded = expandedIndex == index

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (note.isCurrentVersion) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .clickable {
                                        expandedIndex = if (isExpanded) -1 else index
                                    }
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                note.version,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            if (note.isCurrentVersion) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color(0xFFD4EDDA)
                                                ) {
                                                    Text(
                                                        "CURRENT",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF155724),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(note.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle dropdown"
                                    )
                                }

                                AnimatedVisibility(visible = isExpanded) {
                                    Column(
                                        modifier = Modifier.padding(top = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            "Released: ${note.date}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        note.highlights.forEach { highlight ->
                                            Text(
                                                "• $highlight",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
