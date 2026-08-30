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
        version = "v1.02.00",
        date = "Current Update",
        title = "Speech-to-Text & Granular Global School District Alignment",
        highlights = listOf(
            "🎙️ Speech-to-Text (STT) Integration: Speak naturally into your microphone using free Gemini API online speech processing.",
            "🏛️ Granular Educational Requirements: Select Country, State/Province, City, and School District to align K-12 standards precisely with local district curricula.",
            "🌐 Baked-In Global Language Selector: Full native language support across 10 global languages (English, Spanish, French, German, Mandarin, Japanese, Portuguese, Hindi, Arabic, Italian).",
            "🔊 Dual TTS & STT Voice Assist: Seamless voice interactions for speech synthesis and voice input across all learning modules."
        ),
        isCurrentVersion = true
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
