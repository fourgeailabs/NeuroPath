package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

@Composable
fun TopSensoryBar(
    viewModel: NeuroPathViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.currentProfile.collectAsState()
    val activeSound by viewModel.soundManager.activeSound.collectAsState()
    val isSpeaking by viewModel.speechManager.isSpeaking.collectAsState()

    var showSoundDialog by remember { mutableStateOf(false) }
    var showContrastDialog by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 4.dp,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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

            // 3. Ambient Soundscapes Toggle/Picker
            SensoryPillButton(
                icon = if (activeSound != AmbientSoundType.OFF) activeSound.emoji else "🔇",
                label = if (activeSound != AmbientSoundType.OFF) activeSound.title else "Soundscape",
                isActive = activeSound != AmbientSoundType.OFF,
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
    }

    // Soundscape Dialog
    if (showSoundDialog) {
        AlertDialog(
            onDismissRequest = { showSoundDialog = false },
            title = { Text("🌿 Ambient Sensory Soundscapes", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Gentle synthesized noise helps ADHD learners sustain focus and blocks distracting background noises.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(soundType.emoji, fontSize = 22.sp)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        soundType.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        soundType.description,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
