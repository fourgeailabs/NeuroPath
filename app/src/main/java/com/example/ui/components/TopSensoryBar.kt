package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AmbientSoundType
import com.example.ui.AppScreen
import com.example.ui.NeuroPathViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopSensoryBar(
    viewModel: NeuroPathViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.currentProfile.collectAsState()
    val activeSound by viewModel.soundManager.activeSound.collectAsState()
    val isSpeaking by viewModel.speechManager.isSpeaking.collectAsState()
    val isGeneratingLyria by viewModel.isGeneratingLyriaMusic.collectAsState()
    val lyriaStatus by viewModel.lyriaGeneratedStatus.collectAsState()
    val isLyriaPlaying by viewModel.lyriaMusicPlayer.isPlaying.collectAsState()

    var showSoundDialog by remember { mutableStateOf(false) }
    var showContrastDialog by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 4.dp,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sensory Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌿", fontSize = 14.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Sensory Suite",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // 1. Pop-It Fidget Quick Launch
                SensoryPillButton(
                    icon = "🫧",
                    label = "Pop-It",
                    onClick = {
                        viewModel.recordSensoryBreakTaken()
                        viewModel.navigateTo(AppScreen.FIDGET_POPIT)
                    },
                    testTag = "sensory_popit_pill"
                )

                // 2. 4-7-8 Breathing Quick Launch
                SensoryPillButton(
                    icon = "🧘",
                    label = "4-7-8 Calm",
                    onClick = {
                        viewModel.recordSensoryBreakTaken()
                        viewModel.navigateTo(AppScreen.BREATHING_GUIDE)
                    },
                    testTag = "sensory_breathing_pill"
                )

                // 3. Ambient Soundscapes & Lyria Music Toggle/Picker
                val isMusicActive = (activeSound != AmbientSoundType.OFF) || isLyriaPlaying || (lyriaStatus != null)
                SensoryPillButton(
                    icon = if (isLyriaPlaying) "🎵" else if (activeSound != AmbientSoundType.OFF) activeSound.emoji else "🔇",
                    label = if (isLyriaPlaying) "Lyria AI Music" else if (activeSound != AmbientSoundType.OFF) activeSound.title else "Soundscapes",
                    isActive = isMusicActive,
                    onClick = { showSoundDialog = true },
                    testTag = "sensory_soundscape_pill"
                )

                // 4. Dyslexia Font Toggle
                SensoryPillButton(
                    icon = "📖",
                    label = if (profile.dyslexiaFontEnabled) "Dyslexic: ON" else "Dyslexic: OFF",
                    isActive = profile.dyslexiaFontEnabled,
                    onClick = {
                        viewModel.updateProfileSettings(
                            name = profile.name,
                            gradeLevel = profile.gradeLevel,
                            stateStandard = profile.stateStandard,
                            themeId = profile.activeThemeId,
                            neuroTypes = profile.neurodivergentTypesCsv,
                            dyslexiaFont = !profile.dyslexiaFontEnabled,
                            contrastMode = profile.highContrastMode,
                            ttsSpeed = profile.ttsSpeed,
                            readAloud = profile.readAnswersAloud,
                            dailyMinutes = profile.dailyGoalMinutes
                        )
                    },
                    testTag = "sensory_dyslexia_toggle"
                )

                // 5. Contrast Palette Selector
                SensoryPillButton(
                    icon = "🎨",
                    label = "Theme Mode",
                    onClick = { showContrastDialog = true },
                    testTag = "sensory_contrast_picker"
                )

                // 6. Stop TTS if currently speaking
                if (isSpeaking) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFEF5350),
                        modifier = Modifier
                            .clickable { viewModel.speechManager.stop() }
                            .padding(horizontal = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeMute,
                                contentDescription = "Stop Speech",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Stop Audio", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Lyria Active Track Bar (if generated music is active)
            if (isLyriaPlaying || isGeneratingLyria || lyriaStatus != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isGeneratingLyria) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "Lyria Playing",
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(
                                lyriaStatus ?: "Synthesizing Lyria Soundscape...",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                maxLines = 1
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.toggleLyriaPlayPause() },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = if (isLyriaPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Toggle Lyria",
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.stopLyriaMusic() },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop Lyria",
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Soundscape & Lyria Music Generation Dialog
    if (showSoundDialog) {
        var customMusicPrompt by remember { mutableStateOf("") }
        var isShortClipMode by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showSoundDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🌿 Ambient Soundscapes & AI Music", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Generate calming audio using Google Lyria AI models (lyria-3-clip-preview & lyria-3-pro-preview) or procedural noise frequencies tailored for ADHD and ASD sensory regulation.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Lyria AI Generator Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Lyria AI",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Lyria AI Music Generator",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(Modifier.height(6.dp))

                            // Model Length Selector Pill (Clip vs Pro)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isShortClipMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.clickable { isShortClipMode = true }
                                ) {
                                    Text(
                                        "⚡ 30s Focus Clip (lyria-3-clip-preview)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isShortClipMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (!isShortClipMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.clickable { isShortClipMode = false }
                                ) {
                                    Text(
                                        "🎵 Full Track (lyria-3-pro-preview)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (!isShortClipMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(6.dp))

                            OutlinedTextField(
                                value = customMusicPrompt,
                                onValueChange = { customMusicPrompt = it },
                                placeholder = { Text("e.g. 432Hz lo-fi piano with gentle woodland stream", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                maxLines = 2
                            )

                            Spacer(Modifier.height(6.dp))

                            ElevatedButton(
                                onClick = {
                                    viewModel.generateLyriaSoundscape(
                                        soundType = AmbientSoundType.OCEAN,
                                        isShortClip = isShortClipMode,
                                        customPrompt = customMusicPrompt.ifBlank { null }
                                    )
                                    showSoundDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Generate AI Soundscape", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text("Standard Ambient Presets:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    AmbientSoundType.values().forEach { soundType ->
                        val isCurrent = activeSound == soundType
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.toggleAmbientSound(soundType)
                                    showSoundDialog = false
                                }
                                .padding(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(soundType.emoji, fontSize = 20.sp)
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        soundType.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        soundType.description,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (soundType != AmbientSoundType.OFF) {
                                    IconButton(
                                        onClick = {
                                            viewModel.generateLyriaSoundscape(
                                                soundType = soundType,
                                                isShortClip = isShortClipMode
                                            )
                                            showSoundDialog = false
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Generate Lyria Track",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSoundDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Contrast Palette Dialog
    if (showContrastDialog) {
        AlertDialog(
            onDismissRequest = { showContrastDialog = false },
            title = { Text("🎨 Sensory Color & Contrast Palettes", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val modes = listOf(
                        Triple("PASTEL", "Soft Pastel (Low Stimulation)", "🌱 Calm low-saturation pastel theme"),
                        Triple("BUTTERCREAM", "Warm Buttercream (Low Strain)", "🧈 Soothing yellow/warm background"),
                        Triple("TWILIGHT_DARK", "Twilight Soft Dark", "🌙 Reduced blue light night mode"),
                        Triple("MINT", "Gentle Mint Calm", "🍃 Restful botanical green palette"),
                        Triple("HIGH_CONTRAST", "Accessible High Contrast", "⚡ Maximum legibility without harsh flashing")
                    )

                    modes.forEach { (modeCode, title, desc) ->
                        val isSelected = profile.highContrastMode == modeCode
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateProfileSettings(
                                        name = profile.name,
                                        gradeLevel = profile.gradeLevel,
                                        stateStandard = profile.stateStandard,
                                        themeId = profile.activeThemeId,
                                        neuroTypes = profile.neurodivergentTypesCsv,
                                        dyslexiaFont = profile.dyslexiaFontEnabled,
                                        contrastMode = modeCode,
                                        ttsSpeed = profile.ttsSpeed,
                                        readAloud = profile.readAnswersAloud,
                                        dailyMinutes = profile.dailyGoalMinutes
                                    )
                                    showContrastDialog = false
                                }
                                .padding(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showContrastDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun SensoryPillButton(
    icon: String,
    label: String,
    onClick: () -> Unit,
    isActive: Boolean = false,
    testTag: String = ""
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = if (isActive) 2.dp else 0.dp,
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
